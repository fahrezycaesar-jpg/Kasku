package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val absen: Int,
    val name: String
)

@Entity(tableName = "kas_weeks")
data class KasWeekEntity(
    @PrimaryKey(autoGenerate = true)
    val weekId: Int = 0,
    val weekNumber: Int,
    val label: String, // e.g. "Minggu 1"
    val monthYear: String, // e.g. "Oktober 2026"
    val rabuDate: String, // e.g. "15 Okt"
    val kamisDate: String, // e.g. "16 Okt"
    val nominalPerSesi: Long = 2000L
)

@Entity(
    tableName = "kas_payments",
    indices = [
        Index(value = ["weekId", "studentId"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["weekId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = KasWeekEntity::class,
            parentColumns = ["weekId"],
            childColumns = ["weekId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class KasPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekId: Int,
    val studentId: Int,
    val rabuPaid: Boolean = false,
    val kamisPaid: Boolean = false,
    val rabuPaidAt: Long? = null,
    val kamisPaidAt: Long? = null
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val date: String,
    val category: String = "Lainnya",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetStudentId: Int? = null, // null means broadcast / semua siswa
    val targetStudentName: String = "Semua Siswa",
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "app_config")
data class ConfigEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

// UI Helper Models
data class StudentPaymentRow(
    val student: StudentEntity,
    val paymentId: Long,
    val weekId: Int,
    val rabuPaid: Boolean,
    val kamisPaid: Boolean
)

data class StudentDebtReport(
    val student: StudentEntity,
    val unpaidDescriptions: List<String>, // e.g. ["Rabu Mgg 1", "Kamis Mgg 2"]
    val totalDebtAmount: Long,
    val totalPaidAmount: Long,
    val isLunas: Boolean
)

data class CashSummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val currentBalance: Long,
    val totalStudents: Int,
    val debtorCount: Int
)

data class StudentPersonalOverview(
    val student: StudentEntity,
    val isLunas: Boolean,
    val totalDebt: Long,
    val totalPaid: Long,
    val unpaidList: List<String>,
    val weekRecords: List<StudentWeekStatus>
)

data class StudentWeekStatus(
    val week: KasWeekEntity,
    val rabuPaid: Boolean,
    val kamisPaid: Boolean
)
