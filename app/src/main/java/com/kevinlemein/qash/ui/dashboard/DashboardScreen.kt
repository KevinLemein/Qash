package com.kevinlemein.qash.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.core.content.ContextCompat
import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))

    // UI State for Blurring Balance (Default is TRUE = Hidden)
    var isBalanceHidden by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.syncSms()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Qash",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5),
                    titleContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.syncSms()
                            Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show()
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_SMS)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.Black)
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- MAIN CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
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

                        // 2. THE EYE ICON
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Privacy",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { isBalanceHidden = !isBalanceHidden }
                        )
                    }

                    // 3. BLUR LOGIC
                    val displayBalance = if (isBalanceHidden) "Ksh ****" else currencyFormat.format(uiState.currentBalance)

                    Text(
                        text = displayBalance,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.DarkGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TODAY", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "In:  ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.incomeToday)}",
                                color = Color(0xFF00C853), fontSize = 12.sp
                            )
                            Text(
                                "Out: ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.expenseToday)}",
                                color = Color(0xFFD50000), fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("THIS WEEK", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "In:  ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.incomeWeek)}",
                                color = Color(0xFF00C853), fontSize = 12.sp
                            )
                            Text(
                                "Out: ${if(isBalanceHidden) "***" else currencyFormat.format(uiState.expenseWeek)}",
                                color = Color(0xFFD50000), fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
} // <--- THIS CLOSING BRACE WAS IN THE WRONG SPOT! IT MUST BE HERE.

// NOW THIS FUNCTION IS OUTSIDE DASHBOARDSCREEN
@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = SimpleDateFormat("MMM dd, h:mm a", Locale.ENGLISH).format(transaction.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "Ksh ${transaction.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (transaction.type == TransactionType.RECEIVED) Color(0xFF00C853) else Color.Black
            )
        }
    }
}