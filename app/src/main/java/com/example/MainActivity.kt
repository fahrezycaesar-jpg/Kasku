package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.KasNotificationHelper
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KasViewModel
import com.example.ui.viewmodel.UserSession

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create status bar notification channel
        KasNotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                val viewModel: KasViewModel = viewModel()
                val session by viewModel.userSession.collectAsState()

                // Check if opened from Android Notification click
                LaunchedEffect(intent) {
                    handleNotificationIntent(intent, viewModel)
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val currentSession = session) {
                        is UserSession.LoggedOut -> {
                            LoginScreen(viewModel = viewModel)
                        }
                        is UserSession.Admin -> {
                            BackHandler {
                                viewModel.logout()
                            }
                            AdminDashboardScreen(viewModel = viewModel)
                        }
                        is UserSession.Student -> {
                            BackHandler {
                                viewModel.logout()
                            }
                            StudentDashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?, viewModel: KasViewModel) {
        if (intent == null) return
        val target = intent.getStringExtra("OPEN_TARGET")
        val studentId = intent.getIntExtra("STUDENT_ID", -1)

        if (target == "STUDENT_DASHBOARD" && studentId > 0) {
            val student = viewModel.allStudents.value.find { it.id == studentId }
            if (student != null) {
                viewModel.loginStudent(student)
            }
        }
    }
}
