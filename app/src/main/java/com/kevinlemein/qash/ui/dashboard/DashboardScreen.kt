package com.kevinlemein.qash.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme // <--- IMPORT THIS
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

val CATEGORIES = listOf(
    "Bills & Utilities", "Food & Groceries", "Dining", "Transport",
    "Entertainment", "Airtime & Data", "Family & Friends",
    "Rent", "Salary", "Government", "Insurance", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))

    // --- DARK MODE LOGIC ---
    val isDarkMode = isSystemInDarkTheme()

    // Define colors based on the mode
    val screenBackgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val cardBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val topCardColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.Black // Lighter black for dark mode
    // -----------------------

    var isBalanceHidden by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) viewModel.syncSms()
//    }

    // Launcher for SMS AND Notification permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.READ_SMS] ?: false
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (smsGranted) {
            viewModel.syncSms()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Qash",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = contentColor // Adaptive Color
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBackgroundColor, // Adaptive Background
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor
                ),
                actions = {
                    IconButton(onClick = {
                        // Prepare permissions to ask
                        val permissionsToRequest = mutableListOf<String>()

                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.READ_SMS)
                        }
                        // Only needed for Android 13+ (Tiramisu)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }

                        if (permissionsToRequest.isEmpty()) {
                            // All granted! Sync now.
                            viewModel.syncSms()
                            Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show()
                        } else {
                            // Ask for missing permissions
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                }
            )
        },
        containerColor = screenBackgroundColor // Adaptive Background
    ) { paddingValues ->

        if (showDialog && selectedTransaction != null) {
            CategorySelectionDialog(
                isDarkMode = isDarkMode, // Pass theme info to dialog
                onDismiss = { showDialog = false },
                onCategorySelected = { category ->
                    viewModel.updateCategory(selectedTransaction!!, category)
                    showDialog = false
                    Toast.makeText(context, "Updated to $category", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)
        ) {
            // --- BALANCE CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = topCardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Balance", color = Color.Gray, fontSize = 12.sp)
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Privacy",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp).clickable { isBalanceHidden = !isBalanceHidden }
                        )
                    }

                    val displayBalance = if (isBalanceHidden) "Ksh ****" else currencyFormat.format(uiState.currentBalance)
                    Text(
                        text = displayBalance, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.DarkGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("TODAY", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("In:  ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.incomeToday)}", color = Color(0xFF00C853), fontSize = 12.sp)
                            Text("Out: ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.expenseToday)}", color = Color(0xFFD50000), fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("THIS WEEK", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("In:  ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.incomeWeek)}", color = Color(0xFF00C853), fontSize = 12.sp)
                            Text("Out: ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.expenseWeek)}", color = Color(0xFFD50000), fontSize = 12.sp)
                        }
                    }
                }
            }

            Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        backgroundColor = cardBackgroundColor,
                        textColor = contentColor,
                        onClick = {
                            selectedTransaction = transaction
                            showDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, backgroundColor: Color, textColor: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, color = textColor)
                Text(
                    text = "${SimpleDateFormat("MMM dd", Locale.ENGLISH).format(transaction.date)} • ${transaction.category}",
                    fontSize = 12.sp,
                    color = if (transaction.category == "Uncategorized") Color(0xFFD50000) else Color.Gray
                )
            }
            Text(
                text = "Ksh ${transaction.amount}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.RECEIVED) Color(0xFF00C853) else textColor
            )
        }
    }
}

@Composable
fun CategorySelectionDialog(isDarkMode: Boolean, onDismiss: () -> Unit, onCategorySelected: (String) -> Unit) {
    val backgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(CATEGORIES) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category, fontSize = 16.sp, color = textColor)
                        }
                        Divider(color = if(isDarkMode) Color.DarkGray else Color(0xFFEEEEEE))
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text("Cancel", color = Color.Red)
                }
            }
        }
    }
}