package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.StudentDebtReport
import com.example.data.model.StudentPersonalOverview
import com.example.ui.components.NotificationPermissionCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusHutangBg
import com.example.ui.theme.StatusHutangRed
import com.example.ui.theme.StatusLunasBg
import com.example.ui.theme.StatusLunasGreen
import com.example.ui.viewmodel.KasViewModel
import com.example.ui.viewmodel.StudentTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    viewModel: KasViewModel,
    modifier: Modifier = Modifier
) {
    val studentOverview by viewModel.studentPersonalOverview.collectAsState()
    val activeTab by viewModel.studentTab.collectAsState()
    val cashSummary by viewModel.cashSummary.collectAsState()

    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StudentHeader(
                overview = studentOverview,
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            StudentBottomNavigation(
                activeTab = activeTab,
                onTabSelected = { viewModel.selectStudentTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                StudentTab.MY_STATUS -> StudentMyStatusTab(
                    overview = studentOverview,
                    hasPermission = hasNotificationPermission,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                StudentTab.TRANSPARENCY -> StudentTransparencyTab(viewModel = viewModel)
                StudentTab.EXPENSES -> StudentExpensesTab(viewModel = viewModel, totalBalance = cashSummary.currentBalance)
                StudentTab.INBOX -> StudentInboxTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun StudentHeader(
    overview: StudentPersonalOverview?,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${overview?.student?.absen ?: "-"}",
                            color = AmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = overview?.student?.name ?: "Siswa",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Absen #${overview?.student?.absen ?: "-"} • Kas Kelas Rutin",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("student_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Keluar", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun StudentBottomNavigation(
    activeTab: StudentTab,
    onTabSelected: (StudentTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = activeTab == StudentTab.MY_STATUS,
            onClick = { onTabSelected(StudentTab.MY_STATUS) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Status Saya") },
            label = { Text("Status Saya", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("student_nav_my_status")
        )
        NavigationBarItem(
            selected = activeTab == StudentTab.TRANSPARENCY,
            onClick = { onTabSelected(StudentTab.TRANSPARENCY) },
            icon = { Icon(Icons.Default.Group, contentDescription = "Transparansi") },
            label = { Text("Rekap Kelas", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("student_nav_transparency")
        )
        NavigationBarItem(
            selected = activeTab == StudentTab.EXPENSES,
            onClick = { onTabSelected(StudentTab.EXPENSES) },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Pengeluaran") },
            label = { Text("Pengeluaran", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("student_nav_expenses")
        )
        NavigationBarItem(
            selected = activeTab == StudentTab.INBOX,
            onClick = { onTabSelected(StudentTab.INBOX) },
            icon = { Icon(Icons.Default.Inbox, contentDescription = "Notifikasi") },
            label = { Text("Kotak Masuk", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = EmeraldDark),
            modifier = Modifier.testTag("student_nav_inbox")
        )
    }
}

// ================= TAB 1: STATUS PRIBADI SISWA =================

@Composable
private fun StudentMyStatusTab(
    overview: StudentPersonalOverview?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    if (overview == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Memuat data status kas...")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Notification permission banner (Requests browser / system status bar alert permission)
            NotificationPermissionCard(
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission
            )

            // Primary Personal Kas Status Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_status_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (overview.isLunas) StatusLunasBg else StatusHutangBg
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Status Pembayaran Kas Anda:",
                                fontSize = 12.sp,
                                color = if (overview.isLunas) StatusLunasGreen else StatusHutangRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (overview.isLunas) "LUNAS SEMUA SESI" else "MENUNGGAK / KURANG BAYAR",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (overview.isLunas) StatusLunasGreen else StatusHutangRed
                            )
                        }

                        Icon(
                            imageVector = if (overview.isLunas) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (overview.isLunas) StatusLunasGreen else StatusHutangRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!overview.isLunas) {
                        Surface(
                            color = Color.White.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Total Nominal yang Harus Dilunasi:",
                                    fontSize = 11.sp,
                                    color = StatusHutangRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatRupiah(overview.totalDebt),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusHutangRed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Detail Sesi Belum Lunas:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = overview.unpaidList.joinToString(" • "),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StatusHutangRed
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Terima kasih! Semua sesi kas (Rabu & Kamis) telah lunas. Total kontribusi: ${formatRupiah(overview.totalPaid)}.",
                            fontSize = 13.sp,
                            color = StatusLunasGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rincian per Minggu (Jadwal Rabu & Kamis):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Breakdown per week
        items(overview.weekRecords, key = { it.week.weekId }) { record ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${record.week.label} (${record.week.monthYear})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tarif: ${formatRupiah(record.week.nominalPerSesi)} / Sesi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val weekLunas = record.rabuPaid && record.kamisPaid
                        StatusBadge(isLunas = weekLunas)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rabu Sesi
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = if (record.rabuPaid) StatusLunasBg else StatusHutangBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Sesi 1: Rabu (${record.week.rabuDate})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (record.rabuPaid) "✓ Sudah Lunas" else "✕ Belum Bayar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.rabuPaid) StatusLunasGreen else StatusHutangRed
                                )
                            }
                        }

                        // Kamis Sesi
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = if (record.kamisPaid) StatusLunasBg else StatusHutangBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Sesi 2: Kamis (${record.week.kamisDate})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (record.kamisPaid) "✓ Sudah Lunas" else "✕ Belum Bayar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.kamisPaid) StatusLunasGreen else StatusHutangRed
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
}

// ================= TAB 2: TRANSPARANSI KAS KELAS =================

@Composable
private fun StudentTransparencyTab(viewModel: KasViewModel) {
    val debtReports by viewModel.debtReports.collectAsState()
    val cashSummary by viewModel.cashSummary.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, LUNAS, MENUNGGAK

    val filteredList = remember(debtReports, searchQuery, filterMode) {
        debtReports.filter { item ->
            val matchFilter = when (filterMode) {
                "LUNAS" -> item.isLunas
                "MENUNGGAK" -> !item.isLunas
                else -> true
            }
            val matchSearch = if (searchQuery.isBlank()) true else {
                item.student.name.contains(searchQuery.trim(), ignoreCase = true) ||
                        item.student.absen.toString().contains(searchQuery.trim())
            }
            matchFilter && matchSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Class Transparency Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transparansi Kas Seluruh Kelas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Semua siswa dapat melihat rekap pembayaran kelas agar adil dan terbuka.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = StatusLunasBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Sudah Lunas", fontSize = 11.sp, color = StatusLunasGreen)
                                Text(
                                    text = "${debtReports.count { it.isLunas }} Siswa",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusLunasGreen
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = StatusHutangBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Menunggak", fontSize = 11.sp, color = StatusHutangRed)
                                Text(
                                    text = "${debtReports.count { !it.isLunas }} Siswa",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusHutangRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterMode == "ALL",
                    onClick = { filterMode = "ALL" },
                    label = { Text("Semua (${debtReports.size})") }
                )
                FilterChip(
                    selected = filterMode == "LUNAS",
                    onClick = { filterMode = "LUNAS" },
                    label = { Text("Lunas (${debtReports.count { it.isLunas }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusLunasGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = filterMode == "MENUNGGAK",
                    onClick = { filterMode = "MENUNGGAK" },
                    label = { Text("Menunggak (${debtReports.count { !it.isLunas }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusHutangRed,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama siswa atau no. absen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredList, key = { it.student.id }) { report ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
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
                            .background(
                                if (report.isLunas) StatusLunasBg else StatusHutangBg
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${report.student.absen}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (report.isLunas) StatusLunasGreen else StatusHutangRed
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.student.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        if (!report.isLunas) {
                            Text(
                                text = "Kurang ${formatRupiah(report.totalDebtAmount)} (${report.unpaidDescriptions.size} sesi)",
                                fontSize = 11.sp,
                                color = StatusHutangRed,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Lunas Seluruh Sesi",
                                fontSize = 11.sp,
                                color = StatusLunasGreen
                            )
                        }
                    }

                    StatusBadge(isLunas = report.isLunas)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ================= TAB 3: PENGELUARAN KELAS =================

@Composable
private fun StudentExpensesTab(viewModel: KasViewModel, totalBalance: Long) {
    val expenses by viewModel.allExpenses.collectAsState()
    val cashSummary by viewModel.cashSummary.collectAsState()

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
                        text = "Laporan Keuangan & Pengeluaran Kelas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Rincian bagaimana uang kas kelas dipergunakan untuk keperluan bersama.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Saldo Kas Aktif", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    text = formatRupiah(cashSummary.currentBalance),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Belanja", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text(
                                    text = formatRupiah(cashSummary.totalExpense),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusHutangRed
                                )
                            }
                        }
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
                        .padding(vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Belum ada pengeluaran kas yang dicatat.")
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
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

                        Text(
                            text = "- ${formatRupiah(expense.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = StatusHutangRed
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

// ================= TAB 4: KOTAK MASUK NOTIFIKASI =================

@Composable
private fun StudentInboxTab(viewModel: KasViewModel) {
    val notifications by viewModel.studentNotifications.collectAsState()
    val overview by viewModel.studentPersonalOverview.collectAsState()

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
                        text = "Kotak Masuk Notifikasi & Pengumuman",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Setiap pesan yang dikirim oleh bendahara akan muncul di sini dan langsung ke bilah status sistem HP.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test local status bar trigger button
                    OutlinedButton(
                        onClick = {
                            overview?.student?.let { s ->
                                viewModel.sendNotification(
                                    targetStudentId = s.id,
                                    targetName = s.name,
                                    title = "Pengingat Kas Mandiri",
                                    message = "Hai ${s.name}, jangan lupa jadwal pembayaran kas setiap hari Rabu & Kamis ya!"
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_self_notif_button")
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tes Notifikasi Bilah Status HP", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pesan Diterima (${notifications.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (notifications.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Pesan",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Notifikasi dari bendahara kelas akan ditampilkan di sini.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(notifications, key = { it.id }) { notif ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (notif.targetStudentId == null) AmberGold.copy(alpha = 0.15f) else EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (notif.targetStudentId == null) "📢 Pengumuman Kelas" else "👤 Pesan Pribadi",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (notif.targetStudentId == null) AmberGold else EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(notif.timestamp)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notif.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notif.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
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
