package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KasDatabase
import com.example.data.model.CashSummary
import com.example.data.model.ExpenseEntity
import com.example.data.model.KasPaymentEntity
import com.example.data.model.KasWeekEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.StudentDebtReport
import com.example.data.model.StudentEntity
import com.example.data.model.StudentPersonalOverview
import com.example.data.repository.KasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UserSession {
    object LoggedOut : UserSession()
    object Admin : UserSession()
    data class Student(val studentId: Int) : UserSession()
}

enum class AdminTab {
    CHECKLIST,
    DEBTORS,
    EXPENSES,
    NOTIFICATIONS,
    MEMBERS_SETTINGS
}

enum class StudentTab {
    MY_STATUS,
    TRANSPARENCY,
    EXPENSES,
    INBOX
}

class KasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KasRepository

    init {
        val db = KasDatabase.getDatabase(application, viewModelScope)
        repository = KasRepository(db.kasDao(), application)
    }

    // Auth & Navigation State
    private val _userSession = MutableStateFlow<UserSession>(UserSession.LoggedOut)
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.CHECKLIST)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _studentTab = MutableStateFlow(StudentTab.MY_STATUS)
    val studentTab: StateFlow<StudentTab> = _studentTab.asStateFlow()

    // Data from Repository
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allWeeks: StateFlow<List<KasWeekEntity>> = repository.allWeeks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val cashSummary: StateFlow<CashSummary> = repository.cashSummary.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CashSummary(0, 0, 0, 0, 0)
    )

    val debtReports: StateFlow<List<StudentDebtReport>> = repository.debtReports.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Current selected week for Admin Checklist
    private val _selectedWeekId = MutableStateFlow<Int?>(null)
    val selectedWeekId: StateFlow<Int?> = _selectedWeekId.asStateFlow()

    // Payments for selected week
    val currentWeekPayments: StateFlow<List<KasPaymentEntity>> = _selectedWeekId.flatMapLatest { weekId ->
        if (weekId == null) {
            allWeeks.flatMapLatest { weeks ->
                val fallbackWeekId = weeks.firstOrNull()?.weekId
                if (fallbackWeekId != null) {
                    _selectedWeekId.value = fallbackWeekId
                    repository.getPaymentsByWeek(fallbackWeekId)
                } else {
                    flowOf(emptyList())
                }
            }
        } else {
            repository.getPaymentsByWeek(weekId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filter in checklist / debtor list
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Student Logged-in Personal Data
    val studentPersonalOverview: StateFlow<StudentPersonalOverview?> = _userSession.flatMapLatest { session ->
        if (session is UserSession.Student) {
            repository.getStudentPersonalOverview(session.studentId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val studentNotifications: StateFlow<List<NotificationEntity>> = _userSession.flatMapLatest { session ->
        if (session is UserSession.Student) {
            repository.getNotificationsForStudent(session.studentId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notification toast / message feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    // Config cache
    private val _nominalPerSesi = MutableStateFlow(2000L)
    val nominalPerSesi: StateFlow<Long> = _nominalPerSesi.asStateFlow()

    init {
        viewModelScope.launch {
            _nominalPerSesi.value = repository.getNominalPerSesi()
        }
    }

    // ================= ACTIONS =================

    fun selectAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun selectStudentTab(tab: StudentTab) {
        _studentTab.value = tab
    }

    fun selectWeek(weekId: Int) {
        _selectedWeekId.value = weekId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun verifyAndLoginAdmin(pin: String): Boolean {
        val currentPin = repository.getAdminPin()
        return if (pin.trim() == currentPin) {
            _userSession.value = UserSession.Admin
            _adminTab.value = AdminTab.CHECKLIST
            true
        } else {
            false
        }
    }

    fun loginStudent(student: StudentEntity) {
        _userSession.value = UserSession.Student(student.id)
        _studentTab.value = StudentTab.MY_STATUS
    }

    fun logout() {
        _userSession.value = UserSession.LoggedOut
        _searchQuery.value = ""
    }

    fun togglePaymentRabu(paymentId: Long, weekId: Int, studentId: Int, currentlyPaid: Boolean) {
        viewModelScope.launch {
            repository.updateRabuStatus(paymentId, weekId, studentId, !currentlyPaid)
        }
    }

    fun togglePaymentKamis(paymentId: Long, weekId: Int, studentId: Int, currentlyPaid: Boolean) {
        viewModelScope.launch {
            repository.updateKamisStatus(paymentId, weekId, studentId, !currentlyPaid)
        }
    }

    fun markAllInCurrentWeek(markPaid: Boolean) {
        val weekId = _selectedWeekId.value ?: allWeeks.value.firstOrNull()?.weekId ?: return
        viewModelScope.launch {
            repository.markAllForWeek(weekId, markPaid)
            _toastMessage.value = if (markPaid) "Semua siswa ditandai LUNAS minggu ini" else "Semua siswa ditandai BELUM BAYAR"
        }
    }

    fun addExpense(title: String, amount: Long, date: String, category: String, note: String) {
        viewModelScope.launch {
            repository.addExpense(title, amount, date, category, note)
            _toastMessage.value = "Pengeluaran Rp $amount berhasil dicatat!"
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _toastMessage.value = "Catatan pengeluaran dihapus"
        }
    }

    fun addStudent(name: String, absen: Int) {
        viewModelScope.launch {
            repository.addStudent(name, absen)
            _toastMessage.value = "Siswa $name berhasil ditambahkan!"
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _toastMessage.value = "Data siswa diperbarui"
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _toastMessage.value = "Siswa ${student.name} telah dihapus"
        }
    }

    fun addNewWeek() {
        viewModelScope.launch {
            val existingWeeks = allWeeks.value
            val nextNumber = (existingWeeks.maxOfOrNull { it.weekNumber } ?: 0) + 1
            repository.addNewWeek(
                weekNumber = nextNumber,
                label = "Minggu $nextNumber",
                monthYear = "November 2026",
                rabuDate = "Rabu W$nextNumber",
                kamisDate = "Kamis W$nextNumber",
                nominalPerSesi = _nominalPerSesi.value
            )
            _toastMessage.value = "Minggu $nextNumber berhasil ditambahkan ke jadwal kas"
        }
    }

    fun sendNotification(targetStudentId: Int?, targetName: String, title: String, message: String) {
        viewModelScope.launch {
            repository.sendNotification(
                targetStudentId = targetStudentId,
                targetStudentName = targetName,
                title = title.ifBlank { "Pengumuman Kas Kelas" },
                message = message.trim()
            )
            _toastMessage.value = "Notifikasi terkirim ke bilah status HP!"
        }
    }

    fun updateNominalPerSesi(nominal: Long) {
        viewModelScope.launch {
            repository.setNominalPerSesi(nominal)
            _nominalPerSesi.value = nominal
            _toastMessage.value = "Nominal kas diperbarui: Rp $nominal / sesi"
        }
    }

    fun updateAdminPin(newPin: String) {
        viewModelScope.launch {
            repository.setAdminPin(newPin)
            _toastMessage.value = "PIN Bendahara berhasil diubah!"
        }
    }
}
