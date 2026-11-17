package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ConfigEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.KasPaymentEntity
import com.example.data.model.KasWeekEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        KasWeekEntity::class,
        KasPaymentEntity::class,
        ExpenseEntity::class,
        NotificationEntity::class,
        ConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KasDatabase : RoomDatabase() {
    abstract fun kasDao(): KasDao

    companion object {
        @Volatile
        private var INSTANCE: KasDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasDatabase::class.java,
                    "kas_kelas_db"
                )
                    .addCallback(KasDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class KasDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.kasDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: KasDao) {
                val initialStudents = listOf(
                    "AFFLAH INTAN NOOR IZZAH",
                    "AHMAD RENO KHOIDOR MAULANA",
                    "AMELIA AYU SAFIRA",
                    "ANGGUN KHAIRUNNISA AZZAHRA",
                    "ARIFFI KHOIRUN NIZAR",
                    "ATHALIA WIDYA PRAMESWARI",
                    "AYU NOVITA DIUSTIRA",
                    "BISMA NARAENDRA AZKA LAKSANA",
                    "CALLISTA MICHEILS NATHANIA",
                    "CYNTHA LIALATUL QUR'AN",
                    "DEVARA RAKHA ADWIJAYA",
                    "DHIA AULIA NUR MAKAILA",
                    "DINI ANGGRAINI",
                    "FAIRIS ZALFA HERDIANI",
                    "FIRMAN MAULANA PUTRA",
                    "GHISTA FELIYANA FAIRINI",
                    "IBNU SAWQISYAH",
                    "JULIA PRICILIA TUNGGAL DEWI",
                    "M. RAFY HANIF ASLAM",
                    "MOCH ARIEF RAHMAN HAKIM",
                    "MUHAMMAD CAESAR FAHREZY",
                    "MUHAMMAD NAUFAL PUTRA RADITYA",
                    "MUHAMMAD RIZAL TRISANTOSO",
                    "NAFITA DWI LESTARI",
                    "NINDIYA CALLISTA PRIMALIA SARI",
                    "PRADANA RAHADIAN NUGROHO",
                    "QANIAH SHAKIRAH PULUNGAN",
                    "SATRIO ANDI BRAMANTIO",
                    "SAZKIA AULIA PUTRI",
                    "YANUARTA AINUN NADZAR",
                    "YULIANA NUR CAHYA"
                ).mapIndexed { index, name ->
                    StudentEntity(absen = index + 1, name = name)
                }

                dao.insertStudents(initialStudents)
                val insertedStudents = dao.getAllStudentsList()

                // Initial 4 weeks
                val weeks = listOf(
                    KasWeekEntity(
                        weekNumber = 1,
                        label = "Minggu 1",
                        monthYear = "Oktober 2026",
                        rabuDate = "07 Okt",
                        kamisDate = "08 Okt",
                        nominalPerSesi = 2000L
                    ),
                    KasWeekEntity(
                        weekNumber = 2,
                        label = "Minggu 2",
                        monthYear = "Oktober 2026",
                        rabuDate = "14 Okt",
                        kamisDate = "15 Okt",
                        nominalPerSesi = 2000L
                    ),
                    KasWeekEntity(
                        weekNumber = 3,
                        label = "Minggu 3",
                        monthYear = "Oktober 2026",
                        rabuDate = "21 Okt",
                        kamisDate = "22 Okt",
                        nominalPerSesi = 2000L
                    ),
                    KasWeekEntity(
                        weekNumber = 4,
                        label = "Minggu 4",
                        monthYear = "Oktober 2026",
                        rabuDate = "28 Okt",
                        kamisDate = "29 Okt",
                        nominalPerSesi = 2000L
                    )
                )
                dao.insertWeeks(weeks)
                val insertedWeeks = dao.getAllWeeksList()

                // Seed initial payment records
                val payments = mutableListOf<KasPaymentEntity>()
                val now = System.currentTimeMillis()

                insertedWeeks.forEach { week ->
                    insertedStudents.forEach { student ->
                        val rabuPaid: Boolean
                        val kamisPaid: Boolean
                        when (week.weekNumber) {
                            1 -> {
                                // Week 1: almost everyone paid, a couple missed
                                rabuPaid = student.absen != 17 && student.absen != 23
                                kamisPaid = student.absen != 17 && student.absen != 28
                            }
                            2 -> {
                                // Week 2: majority paid
                                rabuPaid = student.absen % 5 != 0
                                kamisPaid = student.absen % 6 != 0
                            }
                            3 -> {
                                // Week 3 (current active week): partial
                                rabuPaid = student.absen % 2 == 1
                                kamisPaid = student.absen % 3 == 0
                            }
                            else -> {
                                // Week 4: future week, all unpaid
                                rabuPaid = false
                                kamisPaid = false
                            }
                        }
                        payments.add(
                            KasPaymentEntity(
                                weekId = week.weekId,
                                studentId = student.id,
                                rabuPaid = rabuPaid,
                                kamisPaid = kamisPaid,
                                rabuPaidAt = if (rabuPaid) now - 86400000 else null,
                                kamisPaidAt = if (kamisPaid) now - 43200000 else null
                            )
                        )
                    }
                }
                dao.insertPayments(payments)

                // Sample Expenses
                val expenses = listOf(
                    ExpenseEntity(
                        title = "Sapu Lantai & Pel Microfiber Kelas",
                        amount = 35000L,
                        date = "09 Okt 2026",
                        category = "Kebersihan",
                        note = "Beli di Toko Plastik Berkah untuk piket harian"
                    ),
                    ExpenseEntity(
                        title = "Spidol Snowman Whiteboard & Refill",
                        amount = 22000L,
                        date = "16 Okt 2026",
                        category = "ATK",
                        note = "2 spidol hitam + 1 botol tinta isi ulang"
                    ),
                    ExpenseEntity(
                        title = "Pengharum Ruangan & Kantong Sampah",
                        amount = 18000L,
                        date = "20 Okt 2026",
                        category = "Kebersihan",
                        note = "Agar ruang kelas tetap wangi dan nyaman"
                    )
                )
                expenses.forEach { dao.insertExpense(it) }

                // Initial notifications
                dao.insertNotification(
                    NotificationEntity(
                        targetStudentId = null,
                        targetStudentName = "Semua Siswa",
                        title = "Jadwal Kas Rabu & Kamis",
                        message = "Halo teman-teman! Kas kelas ditarik setiap Rabu & Kamis Rp2.000 per sesi. Mohon disiapkan ya!",
                        timestamp = now - 172800000
                    )
                )
                dao.insertNotification(
                    NotificationEntity(
                        targetStudentId = null,
                        targetStudentName = "Semua Siswa",
                        title = "Laporan Belanja Kas Kelas",
                        message = "Telah dibeli alat kebersihan baru dan spidol whiteboard untuk kenyamanan belajar kita.",
                        timestamp = now - 86400000
                    )
                )

                // Initial config
                dao.setConfig(ConfigEntity("nominal_per_sesi", "2000"))
                dao.setConfig(ConfigEntity("admin_pin", "1234"))
            }
        }
    }
}
