package com.example.tp_loomo.ui.admin.stats.reports

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.tp_loomo.ui.admin.stats.*
import java.text.SimpleDateFormat
import java.util.*

// --- DATA CLASS PARA TASK RECORDS ---
@kotlinx.serialization.Serializable
data class StatTaskRecord2(
    val id: Int? = null,
    val task_id: Int,
    val user_id: String,
    val progress: Int? = null,
    val location: String? = null,
    val date: String? = null,
    val time_spent: String? = null,
    val observations: String? = null,
    val photo_url: String? = null
)

fun exportTaskHtmlToPdf(context: Context, htmlContent: String, taskTitle: String) {
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Relatorio_Tarefa_${taskTitle.replace(" ", "_")}"
            val printAdapter = view.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}

fun buildTaskReportHtml(
    task: StatTask,
    projectName: String,
    assignedUsers: List<StatUser>,
    records: List<StatTaskRecord2>
): String {
    val currentDate = SimpleDateFormat("dd MMMM yyyy · HH:mm", Locale("pt", "PT")).format(Date())

    val statusLabel = when (task.status?.lowercase()) {
        "completed" -> "Concluída"
        "in_progress" -> "Em Progresso"
        "pending" -> "Pendente"
        else -> task.status ?: "Pendente"
    }
    val statusColor = when (task.status?.lowercase()) {
        "completed" -> "#065f46"
        "in_progress" -> "#1e40af"
        else -> "#7a4b00"
    }
    val statusBg = when (task.status?.lowercase()) {
        "completed" -> "#d1fae5"
        "in_progress" -> "#dbeafe"
        else -> "#fef3cd"
    }

    val totalRecords = records.size
    val totalTimeSpent = records.mapNotNull { r ->
        r.time_spent?.replace("h", "")?.trim()?.toIntOrNull()
    }.sum()
    val lastProgress = records.maxByOrNull { it.id ?: 0 }?.progress ?: task.completion_rate ?: 0
    val prazo = task.due_date ?: "Sem prazo definido"

    // HTML dos utilizadores atribuídos
    val colors = listOf("blue", "teal", "coral", "violet", "green", "amber")
    val assigneesHtml = if (assignedUsers.isEmpty()) {
        "<p style='color:#8a96a8; font-size:13px;'>Nenhum utilizador atribuído.</p>"
    } else {
        assignedUsers.mapIndexed { index, user ->
            val color = colors[index % colors.size]
            val name = user.full_name ?: "Membro"
            val initials = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
            val roleLabel = when (user.role) {
                "admin" -> "Administrador"
                "project_manager" -> "Gestor de Projeto"
                else -> "Membro da Equipa"
            }
            """
            <div class="member-row">
              <div class="avatar $color">$initials</div>
              <div class="member-info">
                <div class="person-name">$name</div>
                <div class="person-role">$roleLabel</div>
              </div>
            </div>
            """
        }.joinToString("\n")
    }

    // HTML dos registos de trabalho
    val recordsHtml = if (records.isEmpty()) {
        "<tr><td colspan='5' style='text-align:center; color:#8a96a8; padding:20px;'>Sem registos de trabalho.</td></tr>"
    } else {
        records.sortedByDescending { it.date }.joinToString("\n") { r ->
            val prog = r.progress?.let { "$it%" } ?: "—"
            val loc = r.location ?: "—"
            val date = r.date ?: "—"
            val time = r.time_spent ?: "—"
            val obs = r.observations ?: "—"
            val hasPhoto = r.photo_url != null
            """
            <tr>
              <td>$date</td>
              <td style="font-weight:500; color:#1a2332;">$prog</td>
              <td>$loc</td>
              <td>$time</td>
              <td>$obs</td>
            </tr>
            ${if (hasPhoto) """
            <tr>
              <td colspan="5" style="padding: 6px 12px 12px; background:#fafbfc;">
                <div style="font-size:11px; color:#8a96a8; margin-bottom:4px; text-transform:uppercase; font-weight:600;">Fotografia</div>
                <img src="${r.photo_url}" style="max-width:280px; border-radius:8px; border:1px solid #dde3ec;" />
              </td>
            </tr>
            """ else ""}
            """
        }
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
          @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=DM+Serif+Display&display=swap');
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { font-family: 'DM Sans', sans-serif; background: #f4f6f9; color: #1a2332; -webkit-print-color-adjust: exact; }
          .report-wrap { max-width: 860px; margin: 0 auto; padding: 2rem 1rem 3rem; }

          .report-header { background: #2567aa; border-radius: 16px; padding: 2.5rem 2.5rem 2rem; color: #fff; margin-bottom: 1.5rem; position: relative; overflow: hidden; }
          .report-header::before { content: ''; position: absolute; top: -60px; right: -60px; width: 220px; height: 220px; border-radius: 50%; background: rgba(255,255,255,0.05); }
          .report-header::after { content: ''; position: absolute; bottom: -40px; left: 40%; width: 300px; height: 160px; border-radius: 50%; background: rgba(255,255,255,0.04); }
          .header-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; margin-bottom: 1.5rem; }
          .report-badge { background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.25); border-radius: 8px; padding: 6px 14px; font-size: 12px; font-weight: 500; letter-spacing: 0.08em; text-transform: uppercase; }
          .task-title { font-family: 'DM Serif Display', serif; font-size: 2rem; font-weight: 400; line-height: 1.2; margin-bottom: 0.5rem; }
          .project-ref { font-size: 13px; opacity: 0.7; margin-bottom: 1rem; }
          .header-meta { display: flex; gap: 2rem; margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,0.15); flex-wrap: wrap; }
          .meta-item { display: flex; flex-direction: column; gap: 3px; }
          .meta-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.08em; opacity: 0.6; font-weight: 500; }
          .meta-value { font-size: 14px; font-weight: 500; opacity: 0.95; }

          .kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 1rem; }
          .kpi-box { background: #fff; border-radius: 12px; border: 0.5px solid #dde3ec; padding: 16px 14px; text-align: center; }
          .kpi-number { font-size: 1.8rem; font-weight: 600; color: #1a3a5c; line-height: 1; margin-bottom: 4px; }
          .kpi-label { font-size: 11px; color: #8a96a8; text-transform: uppercase; letter-spacing: 0.06em; }

          .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
          .full-col { margin-bottom: 1rem; }
          .section-card { background: #ffffff; border-radius: 14px; border: 0.5px solid #dde3ec; padding: 1.5rem; }
          .section-title { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.1em; color: #8a96a8; margin-bottom: 1.1rem; }

          .status-badge { display: inline-flex; align-items: center; gap: 6px; border-radius: 20px; padding: 5px 14px; font-size: 12px; font-weight: 600; }
          .info-row { display: flex; justify-content: space-between; padding: 9px 0; border-bottom: 0.5px solid #f0f2f5; font-size: 13px; }
          .info-row:last-child { border-bottom: none; }
          .info-key { color: #8a96a8; font-weight: 500; }
          .info-val { color: #1a2332; font-weight: 500; text-align: right; }

          .progress-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; }
          .progress-label { font-size: 13px; color: #4a5568; font-weight: 500; }
          .progress-pct { font-size: 1.4rem; font-weight: 600; color: #1a3a5c; }
          .progress-track { height: 10px; background: #e8edf4; border-radius: 20px; overflow: hidden; }
          .progress-fill { height: 100%; background: linear-gradient(90deg, #1a3a5c, #2567aa); border-radius: 20px; }

          .avatar { width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
          .avatar.blue { background: #2567aa; color: #fff; }
          .avatar.teal { background: #1a7a6e; color: #fff; }
          .avatar.coral { background: #b03a2e; color: #fff; }
          .avatar.violet { background: #6c3483; color: #fff; }
          .avatar.green { background: #1e8449; color: #fff; }
          .avatar.amber { background: #9a6500; color: #fff; }
          .member-row { display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 8px; background: #f4f6f9; margin-bottom: 8px; }
          .member-info { flex: 1; }
          .person-name { font-size: 14px; font-weight: 600; color: #1a2332; }
          .person-role { font-size: 12px; color: #8a96a8; }

          .records-table { width: 100%; border-collapse: collapse; font-size: 13px; }
          .records-table th { text-align: left; padding: 8px 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; color: #8a96a8; border-bottom: 1px solid #dde3ec; background: #f8f9fb; }
          .records-table td { padding: 11px 12px; border-bottom: 0.5px solid #edf0f5; color: #3a4556; vertical-align: top; }

          .report-footer { margin-top: 1.5rem; display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.2rem; background: #fff; border-radius: 12px; border: 0.5px solid #dde3ec; }
          .footer-note { font-size: 12px; color: #8a96a8; }
        </style>
        </head>
        <body>
        <div class="report-wrap">

          <!-- HEADER -->
          <div class="report-header">
            <div class="header-top">
              <svg width="120" height="34" viewBox="0 0 120 34" fill="none" xmlns="http://www.w3.org/2000/svg">
                <text x="0" y="26" font-family="DM Serif Display, serif" font-size="28" fill="white" letter-spacing="-0.5">Loomo</text>
              </svg>
              <span class="report-badge">Relatório de Tarefa</span>
            </div>
            <div class="task-title">${task.title}</div>
            <div class="project-ref">Projeto: $projectName</div>
            <div style="margin-top:4px;">
              <span class="status-badge" style="background:$statusBg; color:$statusColor;">$statusLabel</span>
            </div>
            <div class="header-meta">
              <div class="meta-item">
                <span class="meta-label">Data do Relatório</span>
                <span class="meta-value">$currentDate</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Prazo</span>
                <span class="meta-value">$prazo</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Gerado por</span>
                <span class="meta-value">Administrador</span>
              </div>
            </div>
          </div>

          <!-- KPIs -->
          <div class="kpi-row">
            <div class="kpi-box">
              <div class="kpi-number">$lastProgress%</div>
              <div class="kpi-label">Progresso</div>
            </div>
            <div class="kpi-box">
              <div class="kpi-number">${assignedUsers.size}</div>
              <div class="kpi-label">Atribuídos</div>
            </div>
            <div class="kpi-box">
              <div class="kpi-number">$totalRecords</div>
              <div class="kpi-label">Registos</div>
            </div>
            <div class="kpi-box">
              <div class="kpi-number">${if (totalTimeSpent > 0) "${totalTimeSpent}h" else "—"}</div>
              <div class="kpi-label">Tempo Total</div>
            </div>
          </div>

          <!-- DETALHES + PROGRESSO -->
          <div class="two-col">
            <div class="section-card">
              <div class="section-title">Detalhes</div>
              <div class="info-row"><span class="info-key">Descrição</span><span class="info-val">${task.description ?: "Sem descrição"}</span></div>
              <div class="info-row"><span class="info-key">Localização</span><span class="info-val">${task.location ?: "—"}</span></div>
              <div class="info-row"><span class="meta-label">Tempo estimado</span><span class="info-val">${task.estimated_time?.let { "${it}h" } ?: "—"}</span></div>
              <div class="info-row"><span class="info-key">Tempo real</span><span class="info-val">${task.actual_time?.let { "${it}h" } ?: "—"}</span></div>
              <div class="info-row"><span class="info-key">Notas</span><span class="info-val">${task.notes ?: "—"}</span></div>
            </div>
            <div class="section-card">
              <div class="section-title">Progresso da Tarefa</div>
              <div class="progress-header">
                <span class="progress-label">Conclusão atual</span>
                <span class="progress-pct">$lastProgress%</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" style="width: $lastProgress%;"></div>
              </div>
              <div style="margin-top: 1.2rem;">
                <div class="info-row"><span class="info-key">Estado</span><span class="info-val">$statusLabel</span></div>
                <div class="info-row"><span class="info-key">Prazo</span><span class="info-val">$prazo</span></div>
                <div class="info-row"><span class="info-key">Nº de registos</span><span class="info-val">$totalRecords</span></div>
              </div>
            </div>
          </div>

          <!-- EQUIPA ATRIBUÍDA -->
          <div class="section-card full-col">
            <div class="section-title">Utilizadores Atribuídos</div>
            $assigneesHtml
          </div>

          <!-- HISTORIAL DE REGISTOS -->
          <div class="section-card full-col">
            <div class="section-title">Historial de Registos de Trabalho</div>
            <div style="overflow-x:auto;">
              <table class="records-table">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Progresso</th>
                    <th>Localização</th>
                    <th>Tempo</th>
                    <th>Observações</th>
                  </tr>
                </thead>
                <tbody>
                  $recordsHtml
                </tbody>
              </table>
            </div>
          </div>
        </div>
        </body>
        </html>
    """.trimIndent()
}