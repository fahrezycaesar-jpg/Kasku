package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.KasWeekEntity
import com.example.data.model.StudentDebtReport
import com.example.data.model.StudentEntity
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusHutangBg
import com.example.ui.theme.StatusHutangRed
import com.example.ui.theme.StatusLunasBg
import com.example.ui.theme.StatusLunasGreen
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.KasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: KasViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.adminTab.collectAsState()
    val cashSummary by viewModel.cashSummary.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AdminHeader(
                cashBalance = cashSummary.currentBalance,
                debtorsCount = cashSummary.debtorCount,
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            AdminBottomNavigation(
                activeTab = activeTab,
                onTabSelected = { viewModel.selectAdminTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                AdminTab.CHECKLIST -> AdminChecklistTab(viewModel = viewModel)
                AdminTab.DEBTORS -> AdminDebtorsTab(viewModel = viewModel)
                AdminTab.EXPENSES -> AdminExpensesTab(viewModel = viewModel)
                AdminTab.NOTIFICATIONS -> AdminNotificationTab(viewModel = viewModel)
                AdminTab.MEMBERS_SETTINGS -> AdminMembersAndSettingsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AdminHeader(
    cashBalance: Long,
    debtorsCount: Int,
    onLogout: () -> Unit
) {
    Surface(
        color = EmeraldDark,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Panel Bendahara",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jadwal Kas: Setiap Rabu & Kamis",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Keluar", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick financial metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Total Saldo Kas",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = formatRupiah(cashBalance),
                            color = AmberGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Penunggak",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$debtorsCount Siswa",
                            color = if (debtorsCount > 0) Color(0xFFFF8A80) else StatusLunasGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBottomNavigation(
    activeTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = activeTab == AdminTab.CHECKLIST,
            onClick = { onTabSelected(AdminTab.CHECKLIST) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Checklist") },
            label = { Text("Checklist", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("admin_nav_checklist")
        )
        NavigationBarItem(
            selected = activeTab == AdminTab.DEBTORS,
            onClick = { onTabSelected(AdminTab.DEBTORS) },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Penunggak") },
            label = { Text("Penunggak", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("admin_nav_debtors")
        )
        NavigationBarItem(
            selected = activeTab == AdminTab.EXPENSES,
            onClick = { onTabSelected(AdminTab.EXPENSES) },
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Belanja") },
            label = { Text("Belanja", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("admin_nav_expenses")
        )
        NavigationBarItem(
            selected = activeTab == AdminTab.NOTIFICATIONS,
            onClick = { onTabSelected(AdminTab.NOTIFICATIONS) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notif") },
            label = { Text("Kirim Notif", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("admin_nav_notif")
        )
        NavigationBarItem(
            selected = activeTab == AdminTab.MEMBERS_SETTINGS,
            onClick = { onTabSelected(AdminTab.MEMBERS_SETTINGS) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Siswa") },
            label = { Text("Siswa/Atur", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("admin_nav_settings")
        )
    }
}

// ================= TAB 1: CHECKLIST KAS =================

@Composable
private fun AdminChecklistTab(viewModel: KasViewModel) {
    val weeks by viewModel.allWeeks.collectAsState()
    val selectedWeekId by viewModel.selectedWeekId.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val payments by viewModel.currentWeekPayments.collectAsState()
    val nominalPerSesi by viewModel.nominalPerSesi.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val currentWeek = weeks.find { it.weekId == selectedWeekId } ?: weeks.firstOrNull()

    val filteredStudents = remember(searchQuery, students) {
        if (searchQuery.isBlank()) students
        else students.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.absen.toString().contains(searchQuery.trim())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Week Chips Selector
            Text(
                text = "Pilih Minggu Penarikan Kas:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weeks.forEach { week ->
                    val isSelected = week.weekId == currentWeek?.weekId
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectWeek(week.weekId) },
                        label = { Text("${week.label} (${week.monthYear})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("week_chip_${week.weekNumber}")
                    )
                }
                // Add Week Button
                Button(
                    onClick = { viewModel.addNewWeek() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_week_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Minggu Baru", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Week Info Card
            if (currentWeek != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tarif Kas: ${formatRupiah(currentWeek.nominalPerSesi)} / Sesi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Sesi 1: Rabu (${currentWeek.rabuDate}) • Sesi 2: Kamis (${currentWeek.kamisDate})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions & Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { currentWeek?.let { viewModel.markAllInCurrentWeek(true) } },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_paid_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Semua Lunas", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { currentWeek?.let { viewModel.markAllInCurrentWeek(false) } },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_unpaid_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Semua Belum", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama / nomor absen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_checklist_search")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No & Nama Siswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1.8f)
                )
                Text(
                    text = "Rabu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = "Kamis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AmberGold,
                    modifier = Modifier.weight(0.8f)
                )
            }
        }

        // Student Checklist Items
        items(filteredStudents, key = { it.id }) { student ->
            val payment = payments.find { it.studentId == student.id }
            val rabuPaid = payment?.rabuPaid == true
            val kamisPaid = payment?.kamisPaid == true
            val paymentId = payment?.id ?: 0L
            val weekId = currentWeek?.weekId ?: 1

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_row_${student.absen}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Absen & Name
                    Row(
                        modifier = Modifier.weight(1.8f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (rabuPaid && kamisPaid) StatusLunasBg else EmeraldPrimary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${student.absen}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rabuPaid && kamisPaid) StatusLunasGreen else EmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (rabuPaid && kamisPaid) "Lunas" else if (!rabuPaid && !kamisPaid) "Belum Bayar" else "Sebagian",
                                fontSize = 10.sp,
                                color = if (rabuPaid && kamisPaid) StatusLunasGreen else StatusHutangRed
                            )
                        }
                    }

                    // Rabu Checkbox
                    Row(
                        modifier = Modifier.weight(0.8f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rabuPaid,
                            onCheckedChange = {
                                viewModel.togglePaymentRabu(paymentId, weekId, student.id, rabuPaid)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary),
                            modifier = Modifier.testTag("cb_rabu_${student.absen}")
                        )
                    }

                    // Kamis Checkbox
                    Row(
                        modifier = Modifier.weight(0.8f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = kamisPaid,
                            onCheckedChange = {
                                viewModel.togglePaymentKamis(paymentId, weekId, student.id, kamisPaid)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = AmberGold),
                            modifier = Modifier.testTag("cb_kamis_${student.absen}")
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ================= TAB 2: REKAP PENUNGGAK =================

@Composable
private fun AdminDebtorsTab(viewModel: KasViewModel) {
    val debtReports by viewModel.debtReports.collectAsState()
    var filterStatus by remember { mutableStateOf("UNPAID_ONLY") } // ALL or UNPAID_ONLY
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(debtReports, filterStatus, searchQuery) {
        debtReports.filter { item ->
            val matchFilter = if (filterStatus == "UNPAID_ONLY") !item.isLunas else true
            val matchSearch = if (searchQuery.isBlank()) true else {
                item.student.name.contains(searchQuery.trim(), ignoreCase = true) ||
                        item.student.absen.toString().contains(searchQuery.trim())
            }
            matchFilter && matchSearch
        }
    }

    val totalUncollectedDebt = remember(debtReports) {
        debtReports.sumOf { it.totalDebtAmount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Debt Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StatusHutangBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Kekurangan Kas Belum Terbayar",
                        fontSize = 12.sp,
                        color = StatusHutangRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatRupiah(totalUncollectedDebt),
                        fontSize = 24.sp,
                        color = StatusHutangRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${debtReports.count { !it.isLunas }} dari ${debtReports.size} siswa belum melunasi kas.",
                        fontSize = 12.sp,
                        color = StatusHutangRed.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "UNPAID_ONLY",
                    onClick = { filterStatus = "UNPAID_ONLY" },
                    label = { Text("Hanya Menunggak (${debtReports.count { !it.isLunas }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusHutangRed,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_unpaid_only")
                )
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("Semua Siswa (${debtReports.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_all_students")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari siswa menunggak...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debtors_search_input")
            )
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusLunasGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Luar Biasa! Semua Siswa Telah Lunas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = StatusLunasGreen
                        )
                        Text(
                            text = "Tidak ada tunggakan kas kelas saat ini.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.student.id }) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debtor_item_${report.student.absen}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (report.isLunas) StatusLunasBg else StatusHutangBg
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${report.student.absen}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (report.isLunas) StatusLunasGreen else StatusHutangRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = report.student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Absen #${report.student.absen}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            StatusBadge(isLunas = report.isLunas)
                        }

                        if (!report.isLunas) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Sesi Belum Dibayar:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = report.unpaidDescriptions.joinToString(", "),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StatusHutangRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Kekurangan: ${formatRupiah(report.totalDebtAmount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = StatusHutangRed
                                        )

                                        // Quick notify / tagih button
                                        Button(
                                            onClick = {
                                                viewModel.sendNotification(
                                                    targetStudentId = report.student.id,
                                                    targetName = report.student.name,
                                                    title = "Pengingat Kas Kelas",
                                                    message = "Hai ${report.student.name}, mohon lunasi kas kelas untuk ${report.unpaidDescriptions.take(3).joinToString(", ")} total ${formatRupiah(report.totalDebtAmount)} ya. Terima kasih!"
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusHutangRed),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("tagih_btn_${report.student.absen}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ingatkan di HP", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ================= TAB 3: PENGELUARAN =================

@Composable
private fun AdminExpensesTab(viewModel: KasViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    val cashSummary by viewModel.cashSummary.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Expense Metric Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Pengeluaran Kas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRupiah(cashSummary.totalExpense),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusHutangRed
                        )
                        Text(
                            text = "Saldo Bersih: ${formatRupiah(cashSummary.currentBalance)}",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_expense_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catat Belanja", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Riwayat Catatan Belanja (${expenses.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Belum ada catatan pengeluaran kas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_card_${expense.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = expense.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = expense.date,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = expense.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (expense.note.isNotBlank()) {
                                Text(
                                    text = expense.note,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "- ${formatRupiah(expense.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = StatusHutangRed
                            )
                            IconButton(
                                onClick = { viewModel.deleteExpense(expense) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, date, category, note ->
                viewModel.addExpense(title, amount, date, category, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Kebersihan") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Kebersihan", "ATK", "Acara", "Lainnya")
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pengeluaran Kas", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Keterangan Belanja") },
                    placeholder = { Text("Misal: Spidol whiteboard") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Nominal Pengeluaran (Rp)") },
                    placeholder = { Text("Misal: 25000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull()
                    if (title.isBlank() || amount == null || amount <= 0) {
                        error = "Harap masukkan keterangan dan nominal yang valid"
                        return@Button
                    }
                    onConfirm(title, amount, currentDateStr, category, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ================= TAB 4: KIRIM NOTIFIKASI =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminNotificationTab(viewModel: KasViewModel) {
    val students by viewModel.allStudents.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()

    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) } // null = Semua Siswa
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var notificationTitle by remember { mutableStateOf("Pengingat Kas Kelas") }
    var notificationMessage by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kirim Notifikasi Realtime ke Bilah HP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Pesan akan disimpan di database dan langsung memicu notifikasi di bilah status sistem HP siswa.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Selector
                    Text("Target Penerima:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.let { "#${it.absen} - ${it.name}" } ?: "📢 Semua Siswa (Broadcast)",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("target_student_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📢 Semua Siswa (Broadcast Seluruh Kelas)") },
                                onClick = {
                                    selectedStudent = null
                                    isDropdownExpanded = false
                                }
                            )
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text("#${student.absen} - ${student.name}") },
                                    onClick = {
                                        selectedStudent = student
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notificationTitle,
                        onValueChange = { notificationTitle = it },
                        label = { Text("Judul Notifikasi") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notif_title_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notificationMessage,
                        onValueChange = {
                            notificationMessage = it
                            errorText = null
                        },
                        label = { Text("Isi Pesan Notifikasi") },
                        placeholder = { Text("Contoh: Pengingat kas minggu ini Rp2.000 mohon segera dilunasi ya!") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notif_message_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorText ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (notificationMessage.isBlank()) {
                                errorText = "Harap ketik isi pesan notifikasi"
                                return@Button
                            }
                            viewModel.sendNotification(
                                targetStudentId = selectedStudent?.id,
                                targetName = selectedStudent?.name ?: "Semua Siswa",
                                title = notificationTitle,
                                message = notificationMessage
                            )
                            notificationMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_notif_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Kirim Notifikasi ke Bilah HP",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Riwayat Pesan Terkirim (${notifications.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(notifications, key = { it.id }) { notif ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (notif.targetStudentId == null) AmberGold.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Ke: ${notif.targetStudentName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (notif.targetStudentId == null) AmberGold else EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(notif.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notif.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = notif.message,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ================= TAB 5: MANAJEMEN SISWA & PENGATURAN =================

@Composable
private fun AdminMembersAndSettingsTab(viewModel: KasViewModel) {
    val students by viewModel.allStudents.collectAsState()
    val nominalPerSesi by viewModel.nominalPerSesi.collectAsState()

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showNominalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Settings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pengaturan Sistem Kas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Nominal Kas per Sesi", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = "${formatRupiah(nominalPerSesi)} / Sesi (Rabu & Kamis)",
                                fontSize = 12.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { showNominalDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_nominal_button")
                        ) {
                            Text("Ubah", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PIN Keamanan Bendahara", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Amankan akses menu admin", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_pin_button")
                        ) {
                            Text("Ganti PIN", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Member Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Siswa Kelas (${students.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = { showAddStudentDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_new_student_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Siswa", fontSize = 11.sp)
                }
            }
        }

        items(students, key = { it.id }) { student ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${student.absen}",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Absen #${student.absen}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { studentToEdit = student },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { studentToDelete = student },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        var newName by remember { mutableStateOf("") }
        var newAbsen by remember { mutableStateOf("${(students.maxOfOrNull { it.absen } ?: 0) + 1}") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Tambah Siswa Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nama Lengkap Siswa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAbsen,
                        onValueChange = { newAbsen = it },
                        label = { Text("Nomor Absen") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dialogError != null) {
                        Text(dialogError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val absenInt = newAbsen.toIntOrNull()
                        if (newName.isBlank() || absenInt == null) {
                            dialogError = "Nama dan nomor absen tidak boleh kosong"
                            return@Button
                        }
                        viewModel.addStudent(newName, absenInt)
                        showAddStudentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("Batal") }
            }
        )
    }

    // Edit Student Dialog
    studentToEdit?.let { student ->
        var editName by remember { mutableStateOf(student.name) }
        var editAbsen by remember { mutableStateOf(student.absen.toString()) }

        AlertDialog(
            onDismissRequest = { studentToEdit = null },
            title = { Text("Edit Data Siswa", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAbsen,
                        onValueChange = { editAbsen = it },
                        label = { Text("Nomor Absen") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val absenInt = editAbsen.toIntOrNull() ?: student.absen
                        viewModel.updateStudent(student.copy(name = editName, absen = absenInt))
                        studentToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToEdit = null }) { Text("Batal") }
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Hapus Siswa?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus ${student.name} (Absen #${student.absen}) dari database kas kelas?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) { Text("Batal") }
            }
        )
    }

    // Edit Nominal Dialog
    if (showNominalDialog) {
        var inputNominal by remember { mutableStateOf(nominalPerSesi.toString()) }
        AlertDialog(
            onDismissRequest = { showNominalDialog = false },
            title = { Text("Ubah Nominal Kas per Sesi", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputNominal,
                    onValueChange = { inputNominal = it },
                    label = { Text("Nominal per Sesi (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val n = inputNominal.toLongOrNull() ?: 2000L
                        viewModel.updateNominalPerSesi(n)
                        showNominalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNominalDialog = false }) { Text("Batal") }
            }
        )
    }

    // Edit PIN Dialog
    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Ganti PIN Bendahara", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("PIN Baru (angka atau teks)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.isNotBlank()) {
                            viewModel.updateAdminPin(newPin)
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan PIN", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Batal") }
            }
        )
    }
}
