package com.example.data.repository

import android.content.Context
import com.example.data.local.KasDao
import com.example.data.model.CashSummary
import com.example.data.model.ConfigEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.KasPaymentEntity
import com.example.data.model.KasWeekEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.StudentDebtReport
import com.example.data.model.StudentEntity
import com.example.data.model.StudentPersonalOverview
import com.example.data.model.StudentWeekStatus
import com.example.notification.KasNotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class KasRepository(
    private val dao: KasDao,
    private val context: Context
) {
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allWeeks: Flow<List<KasWeekEntity>> = dao.getAllWeeks()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    fun getPaymentsByWeek(weekId: Int): Flow<List<KasPaymentEntity>> {
        return dao.getPaymentsByWeek(weekId)
    }

    fun getNotificationsForStudent(studentId: Int): Flow<List<NotificationEntity>> {
        return dao.getNotificationsForStudent(studentId)
    }

    // Comprehensive Cash Summary (Income, Expense, Balance, Debtors)
    val cashSummary: Flow<CashSummary> = combine(
        dao.getAllPayments(),
        dao.getAllWeeks(),
        dao.getAllExpenses(),
        dao.getAllStudents()
    ) { payments, weeks, expenses, students ->
        val weekNominalMap = weeks.associate { it.weekId to it.nominalPerSesi }
        var totalIncome = 0L
        val studentUnpaidMap = mutableMapOf<Int, Int>() // studentId -> unpaid count

        students.forEach { studentUnpaidMap[it.id] = 0 }

        payments.forEach { p ->
            val nominal = weekNominalMap[p.weekId] ?: 2000L
            if (p.rabuPaid) totalIncome += nominal else {
                studentUnpaidMap[p.studentId] = (studentUnpaidMap[p.studentId] ?: 0) + 1
            }
            if (p.kamisPaid) totalIncome += nominal else {
                studentUnpaidMap[p.studentId] = (studentUnpaidMap[p.studentId] ?: 0) + 1
            }
        }

        val totalExpense = expenses.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val debtorCount = studentUnpaidMap.count { it.value > 0 }

        CashSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            currentBalance = balance,
            totalStudents = students.size,
            debtorCount = debtorCount
        )
    }

    // Auto-calculated Debtor Reports
    val debtReports: Flow<List<StudentDebtReport>> = combine(
        dao.getAllStudents(),
        dao.getAllWeeks(),
        dao.getAllPayments()
    ) { students, weeks, payments ->
        val weekMap = weeks.associateBy { it.weekId }
        val paymentGroup = payments.groupBy { it.studentId }

        students.map { student ->
            val studentPayments = paymentGroup[student.id] ?: emptyList()
            val unpaidList = mutableListOf<String>()
            var totalDebt = 0L
            var totalPaid = 0L

            weeks.forEach { week ->
                val p = studentPayments.find { it.weekId == week.weekId }
                val rate = week.nominalPerSesi

                if (p == null || !p.rabuPaid) {
                    unpaidList.add("Rabu ${week.label}")
                    totalDebt += rate
                } else {
                    totalPaid += rate
                }

                if (p == null || !p.kamisPaid) {
                    unpaidList.add("Kamis ${week.label}")
                    totalDebt += rate
                } else {
                    totalPaid += rate
                }
            }

            StudentDebtReport(
                student = student,
                unpaidDescriptions = unpaidList,
                totalDebtAmount = totalDebt,
                totalPaidAmount = totalPaid,
                isLunas = unpaidList.isEmpty()
            )
        }
    }

    // Student personal overview
    fun getStudentPersonalOverview(studentId: Int): Flow<StudentPersonalOverview?> {
        return combine(
            dao.getAllStudents(),
            dao.getAllWeeks(),
            dao.getPaymentsForStudent(studentId)
        ) { students, weeks, payments ->
            val student = students.find { it.id == studentId } ?: return@combine null
            val unpaidList = mutableListOf<String>()
            var totalDebt = 0L
            var totalPaid = 0L

            val weekRecords = weeks.map { week ->
                val payment = payments.find { it.weekId == week.weekId }
                val rabu = payment?.rabuPaid == true
                val kamis = payment?.kamisPaid == true
                val rate = week.nominalPerSesi

                if (!rabu) {
                    unpaidList.add("Rabu ${week.label}")
                    totalDebt += rate
                } else {
                    totalPaid += rate
                }

                if (!kamis) {
                    unpaidList.add("Kamis ${week.label}")
                    totalDebt += rate
                } else {
                    totalPaid += rate
                }

                StudentWeekStatus(
                    week = week,
                    rabuPaid = rabu,
                    kamisPaid = kamis
                )
            }

            StudentPersonalOverview(
                student = student,
                isLunas = unpaidList.isEmpty(),
                totalDebt = totalDebt,
                totalPaid = totalPaid,
                unpaidList = unpaidList,
                weekRecords = weekRecords
            )
        }
    }

    // Payment Operations
    suspend fun updateRabuStatus(paymentId: Long, weekId: Int, studentId: Int, paid: Boolean) {
        val existing = dao.getPayment(weekId, studentId)
        if (existing != null) {
            dao.updatePayment(
                existing.copy(
                    rabuPaid = paid,
                    rabuPaidAt = if (paid) System.currentTimeMillis() else null
                )
            )
        } else {
            dao.upsertPayment(
                KasPaymentEntity(
                    weekId = weekId,
                    studentId = studentId,
                    rabuPaid = paid,
                    kamisPaid = false,
                    rabuPaidAt = if (paid) System.currentTimeMillis() else null
                )
            )
        }
    }

    suspend fun updateKamisStatus(paymentId: Long, weekId: Int, studentId: Int, paid: Boolean) {
        val existing = dao.getPayment(weekId, studentId)
        if (existing != null) {
            dao.updatePayment(
                existing.copy(
                    kamisPaid = paid,
                    kamisPaidAt = if (paid) System.currentTimeMillis() else null
                )
            )
        } else {
            dao.upsertPayment(
                KasPaymentEntity(
                    weekId = weekId,
                    studentId = studentId,
                    rabuPaid = false,
                    kamisPaid = paid,
                    kamisPaidAt = if (paid) System.currentTimeMillis() else null
                )
            )
        }
    }

    suspend fun markAllForWeek(weekId: Int, markAsPaid: Boolean) {
        val students = dao.getAllStudentsList()
        val now = System.currentTimeMillis()
        students.forEach { student ->
            val existing = dao.getPayment(weekId, student.id)
            if (existing != null) {
                dao.updatePayment(
                    existing.copy(
                        rabuPaid = markAsPaid,
                        kamisPaid = markAsPaid,
                        rabuPaidAt = if (markAsPaid) now else null,
                        kamisPaidAt = if (markAsPaid) now else null
                    )
                )
            } else {
                dao.upsertPayment(
                    KasPaymentEntity(
                        weekId = weekId,
                        studentId = student.id,
                        rabuPaid = markAsPaid,
                        kamisPaid = markAsPaid,
                        rabuPaidAt = if (markAsPaid) now else null,
                        kamisPaidAt = if (markAsPaid) now else null
                    )
                )
            }
        }
    }

    // Expense Operations
    suspend fun addExpense(title: String, amount: Long, date: String, category: String, note: String) {
        dao.insertExpense(
            ExpenseEntity(
                title = title.trim(),
                amount = amount,
                date = date,
                category = category,
                note = note.trim()
            )
        )
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    // Member Management
    suspend fun addStudent(name: String, absen: Int) {
        val newId = dao.insertStudent(StudentEntity(absen = absen, name = name.trim().uppercase())).toInt()
        val weeks = dao.getAllWeeksList()
        weeks.forEach { week ->
            dao.upsertPayment(
                KasPaymentEntity(
                    weekId = week.weekId,
                    studentId = newId,
                    rabuPaid = false,
                    kamisPaid = false
                )
            )
        }
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student.copy(name = student.name.trim().uppercase()))
    }

    suspend fun deleteStudent(student: StudentEntity) {
        dao.deleteStudent(student)
    }

    // Week Management
    suspend fun addNewWeek(
        weekNumber: Int,
        label: String,
        monthYear: String,
        rabuDate: String,
        kamisDate: String,
        nominalPerSesi: Long
    ) {
        val newWeekId = dao.insertWeek(
            KasWeekEntity(
                weekNumber = weekNumber,
                label = label,
                monthYear = monthYear,
                rabuDate = rabuDate,
                kamisDate = kamisDate,
                nominalPerSesi = nominalPerSesi
            )
        ).toInt()

        val students = dao.getAllStudentsList()
        val initialPayments = students.map {
            KasPaymentEntity(
                weekId = newWeekId,
                studentId = it.id,
                rabuPaid = false,
                kamisPaid = false
            )
        }
        dao.insertPayments(initialPayments)
    }

    // Notifications (Realtime in DB + Native Status Bar)
    suspend fun sendNotification(
        targetStudentId: Int?,
        targetStudentName: String,
        title: String,
        message: String
    ) {
        dao.insertNotification(
            NotificationEntity(
                targetStudentId = targetStudentId,
                targetStudentName = targetStudentName,
                title = title,
                message = message,
                timestamp = System.currentTimeMillis()
            )
        )

        // Trigger real Android Status Bar Notification
        KasNotificationHelper.showKasNotification(
            context = context,
            recipientName = targetStudentName,
            message = message,
            studentId = targetStudentId
        )
    }

    suspend fun markNotificationRead(id: Long) {
        dao.markNotificationAsRead(id)
    }

    // Config
    suspend fun getNominalPerSesi(): Long {
        return dao.getConfigValue("nominal_per_sesi")?.toLongOrNull() ?: 2000L
    }

    suspend fun setNominalPerSesi(nominal: Long) {
        dao.setConfig(ConfigEntity("nominal_per_sesi", nominal.toString()))
    }

    suspend fun getAdminPin(): String {
        return dao.getConfigValue("admin_pin") ?: "1234"
    }

    suspend fun setAdminPin(pin: String) {
        dao.setConfig(ConfigEntity("admin_pin", pin))
    }
}
