package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConfigEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.KasPaymentEntity
import com.example.data.model.KasWeekEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KasDao {

    // === STUDENTS ===
    @Query("SELECT * FROM students ORDER BY absen ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY absen ASC")
    suspend fun getAllStudentsList(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Int): StudentEntity?

    @Query("SELECT * FROM students WHERE absen = :absen LIMIT 1")
    suspend fun getStudentByAbsen(absen: Int): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    // === WEEKS ===
    @Query("SELECT * FROM kas_weeks ORDER BY weekNumber ASC")
    fun getAllWeeks(): Flow<List<KasWeekEntity>>

    @Query("SELECT * FROM kas_weeks ORDER BY weekNumber ASC")
    suspend fun getAllWeeksList(): List<KasWeekEntity>

    @Query("SELECT * FROM kas_weeks WHERE weekId = :weekId LIMIT 1")
    suspend fun getWeekById(weekId: Int): KasWeekEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeek(week: KasWeekEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeks(weeks: List<KasWeekEntity>)

    @Update
    suspend fun updateWeek(week: KasWeekEntity)

    @Delete
    suspend fun deleteWeek(week: KasWeekEntity)

    // === PAYMENTS ===
    @Query("SELECT * FROM kas_payments WHERE weekId = :weekId")
    fun getPaymentsByWeek(weekId: Int): Flow<List<KasPaymentEntity>>

    @Query("SELECT * FROM kas_payments WHERE weekId = :weekId")
    suspend fun getPaymentsByWeekList(weekId: Int): List<KasPaymentEntity>

    @Query("SELECT * FROM kas_payments")
    fun getAllPayments(): Flow<List<KasPaymentEntity>>

    @Query("SELECT * FROM kas_payments")
    suspend fun getAllPaymentsList(): List<KasPaymentEntity>

    @Query("SELECT * FROM kas_payments WHERE studentId = :studentId")
    fun getPaymentsForStudent(studentId: Int): Flow<List<KasPaymentEntity>>

    @Query("SELECT * FROM kas_payments WHERE weekId = :weekId AND studentId = :studentId LIMIT 1")
    suspend fun getPayment(weekId: Int, studentId: Int): KasPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(payment: KasPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<KasPaymentEntity>)

    @Update
    suspend fun updatePayment(payment: KasPaymentEntity)

    // === EXPENSES ===
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // === NOTIFICATIONS ===
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE targetStudentId IS NULL OR targetStudentId = :studentId ORDER BY timestamp DESC")
    fun getNotificationsForStudent(studentId: Int): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    // === CONFIG ===
    @Query("SELECT value FROM app_config WHERE `key` = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: ConfigEntity)
}
