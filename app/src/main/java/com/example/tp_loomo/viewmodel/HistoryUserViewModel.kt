package com.example.tp_loomo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.data.repository.ProjectRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Estrutura para segurar todos os dados necessários para um item do histórico
data class HistoryTaskUiModel(
    val task: Task,
    val projectName: String,
    val timeSpent: String,
    val completionDateRaw: String?, // yyyy-MM-dd, usado para ordenação
    val completionDateFormatted: String, // dd/MM/yyyy
    val monthGroup: String, // ex: "Abril De 2026"
    val monthSortKey: String // ex: "2026-04", usado para ordenação dos grupos
)

class HistoryUserViewModel : ViewModel() {
    private val repository = ProjectRepository()

    var historyList by mutableStateOf<List<HistoryTaskUiModel>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val uiDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val groupSortFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    fun loadHistory() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                // 1. Vai buscar as tarefas do user e filtra apenas as concluídas
                val completedTasks = repository.getUserTasks(userId)
                    .filter { it.status == "completed" }

                val combinedList = mutableListOf<HistoryTaskUiModel>()

                for (task in completedTasks) {
                    val project = task.project_id.let { repository.getProjectById(it) }

                    // 2. Vai buscar o registo mais recente desta tarefa para obter tempo e data de conclusão
                    val records = task.id?.let { repository.getTaskRecords(it) } ?: emptyList()
                    val latestRecord = records.maxByOrNull { it.date }

                    val completionDateRaw = latestRecord?.date ?: task.updated_at?.take(10)
                    val timeSpent = latestRecord?.time_spent ?: "-"

                    val completionDateFormatted = formatDate(completionDateRaw, uiDateFormat)
                    val monthGroup = formatMonthGroup(completionDateRaw)
                    val monthSortKey = formatDate(completionDateRaw, groupSortFormat)

                    combinedList.add(
                        HistoryTaskUiModel(
                            task = task,
                            projectName = project?.name ?: "Sem Projeto",
                            timeSpent = timeSpent,
                            completionDateRaw = completionDateRaw,
                            completionDateFormatted = completionDateFormatted,
                            monthGroup = monthGroup,
                            monthSortKey = monthSortKey
                        )
                    )
                }

                // 3. Ordena por mês e depois por data de conclusão
                historyList = combinedList.sortedWith(
                    compareBy({ it.monthSortKey }, { it.completionDateRaw ?: "" })
                )

            } catch (e: Exception) {
                android.util.Log.e("HistoryUserVM", "Erro ao carregar histórico: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun formatDate(rawDate: String?, format: SimpleDateFormat): String {
        if (rawDate.isNullOrBlank()) return ""
        return try {
            val date = dbDateFormat.parse(rawDate) ?: return ""
            format.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatMonthGroup(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return "Sem Data"
        return try {
            val date = dbDateFormat.parse(rawDate) ?: return "Sem Data"
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            val month = monthNames[calendar.get(java.util.Calendar.MONTH)]
            val year = calendar.get(java.util.Calendar.YEAR)
            "$month De $year"
        } catch (e: Exception) {
            "Sem Data"
        }
    }
}
