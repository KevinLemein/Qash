package com.kevinlemein.qash.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevinlemein.qash.domain.model.Transaction
import com.kevinlemein.qash.domain.model.TransactionType
import com.kevinlemein.qash.domain.repository.SmsRepository
import com.kevinlemein.qash.domain.usecase.ParseSmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class DashboardUiState(
    val transactions: List<Transaction> = emptyList(),
    val currentBalance: Double = 0.0, // Real balance from SMS
    val incomeToday: Double = 0.0,
    val expenseToday: Double = 0.0,
    val incomeWeek: Double = 0.0,
    val expenseWeek: Double = 0.0,
    val isBalanceHidden: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SmsRepository,
    private val parseSmsUseCase: ParseSmsUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getAllTransactions()
        .map { list ->
            // 1. Sort list: Newest first
            val sortedList = list.sortedByDescending { it.date }

            // 2. Get Real Balance from the LATEST transaction
            val latestBalance = sortedList.firstOrNull()?.newBalance ?: 0.0

            // 3. Filter for Today & Week
            val (todayIncome, todayExpense) = calculateTotals(sortedList, isToday = true)
            val (weekIncome, weekExpense) = calculateTotals(sortedList, isToday = false)

            DashboardUiState(
                transactions = sortedList,
                currentBalance = latestBalance,
                incomeToday = todayIncome,
                expenseToday = todayExpense,
                incomeWeek = weekIncome,
                expenseWeek = weekExpense
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    // Helper to calculate totals based on date filter
    private fun calculateTotals(list: List<Transaction>, isToday: Boolean): Pair<Double, Double> {
        val now = Calendar.getInstance()
        val filtered = list.filter {
            val txDate = Calendar.getInstance().apply { time = it.date }
            if (isToday) {
                // Check if Same Day and Same Year
                txDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                        txDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            } else {
                // Check if Same Week and Same Year
                txDate.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) &&
                        txDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            }
        }

        val income = filtered.filter { it.type == TransactionType.RECEIVED }.sumOf { it.amount }
        val expense = filtered.filter { it.type == TransactionType.SENT }.sumOf { it.amount }

        return Pair(income, expense)
    }

    fun syncSms() {
        viewModelScope.launch(Dispatchers.IO) { repository.syncMessages() }
    }

    fun simulateSms(smsText: String) {
        viewModelScope.launch {
            val transaction = parseSmsUseCase(smsText)
            if (transaction != null) repository.saveTransaction(transaction)
        }
    }

    fun updateCategory(transaction: Transaction, newCategory: String) {
        viewModelScope.launch {
            repository.updateCategory(transaction.mpesaCode, newCategory)
        }
    }

    fun toggleBalanceVisibility() {
        val currentState = uiState.value
        // We can't modify StateFlow directly, we rely on the flow logic.
        // But since our flow is driven by the DB, we need a local state override.
        // A better approach for simple UI toggles is using a MutableStateFlow for the UI state directly
        // OR simply handle this specific toggle in the UI (Composable) since it doesn't affect data logic.
    }
}