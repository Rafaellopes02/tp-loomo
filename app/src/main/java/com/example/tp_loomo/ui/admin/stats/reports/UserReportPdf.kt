package com.example.tp_loomo.ui.admin.stats.reports

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.tp_loomo.ui.admin.stats.*
import java.text.SimpleDateFormat
import java.util.*

fun exportUserHtmlToPdf(context: Context, htmlContent: String, userName: String) {
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Relatorio_User_${userName.replace(" ", "_")}"
            val printAdapter = view.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}

fun buildUserReportHtml(
    user: StatUser,
    email: String?,
    avatarUrl: String?,
    memberProjects: List<StatProject>,
    managedProjects: List<StatProject>,
    assignedTasks: List<StatTask>,
    records: List<StatTaskRecord2>,
    allTasks: List<StatTask>
): String {
    val currentDate = SimpleDateFormat("dd MMMM yyyy · HH:mm", Locale("pt", "PT")).format(Date())

    val roleLabel = when (user.role) {
        "admin" -> "Administrador"
        "project_manager" -> "Gestor de Projeto"
        else -> "Membro da Equipa"
    }
    val roleColor = when (user.role) {
        "admin" -> "#6c3483"
        "project_manager" -> "#1a3a5c"
        else -> "#1a7a6e"
    }
    val roleBg = when (user.role) {
        "admin" -> "#f3e8fd"
        "project_manager" -> "#dbeafe"
        else -> "#d1fae5"
    }

    val name = user.full_name ?: "Sem Nome"
    val initials = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()

    // KPIs
    val totalTasks = assignedTasks.size
    val completedTasks = assignedTasks.count { it.status?.lowercase() in listOf("completed", "concluído") }
    val inProgressTasks = assignedTasks.count { it.status?.lowercase() == "in_progress" }
    val pendingTasks = assignedTasks.count { it.status?.lowercase() == "pending" }
    val completionPct = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
    val totalHours = records.mapNotNull { it.time_spent?.replace("h", "")?.trim()?.toIntOrNull() }.sum()
    val totalRecords = records.size

    // Projetos HTML
    val allProjects = (memberProjects + managedProjects).distinctBy { it.id }
    val projectsHtml = if (allProjects.isEmpty()) {
        "<p style='color:#8a96a8; font-size:13px;'>Sem projetos associados.</p>"
    } else {
        allProjects.joinToString("\n") { proj ->
            val isManager = managedProjects.any { it.id == proj.id }
            val projTasks = assignedTasks.count { it.project_id == proj.id }
            val tagLabel = if (isManager) "Gestor" else "Membro"
            val tagColor = if (isManager) "#1a3a5c" else "#1a7a6e"
            val tagBg = if (isManager) "#dbeafe" else "#d1fae5"
            """
            <div class="proj-row">
              <div>
                <div class="proj-name">${proj.name}</div>
                <div class="proj-sub">$projTasks tarefas atribuídas</div>
              </div>
              <span class="mini-badge" style="background:$tagBg; color:$tagColor;">$tagLabel</span>
            </div>
            """
        }
    }

    // Tarefas HTML
    val tasksHtml = if (assignedTasks.isEmpty()) {
        "<tr><td colspan='4' style='text-align:center; color:#8a96a8; padding:20px;'>Sem tarefas atribuídas.</td></tr>"
    } else {
        assignedTasks.joinToString("\n") { task ->
            val projName = allTasks.firstOrNull()?.let { allProjects.find { p -> p.id == task.project_id }?.name } ?: "—"
            val statusLabel = when (task.status?.lowercase()) {
                "completed" -> "Concluída"
                "in_progress" -> "Em Progresso"
                else -> "Pendente"
            }
            val tagClass = when (task.status?.lowercase()) {
                "completed" -> "tag-concluido"
                "in_progress" -> "tag-progresso"
                else -> "tag-pendente"
            }
            val prazo = task.due_date ?: "—"
            val taskRecords = records.filter { it.task_id == task.id }
            val lastProgress = taskRecords.maxByOrNull { it.id ?: 0 }?.progress ?: task.completion_rate ?: 0
            """
            <tr>
              <td class="task-name">${task.title}</td>
              <td>$projName</td>
              <td>$prazo</td>
              <td>$lastProgress%</td>
              <td><span class="$tagClass">$statusLabel</span></td>
            </tr>
            """
        }
    }

    // Registos HTML
    val recordsHtml = if (records.isEmpty()) {
        "<tr><td colspan='5' style='text-align:center; color:#8a96a8; padding:20px;'>Sem registos de trabalho.</td></tr>"
    } else {
        records.sortedByDescending { it.date }.joinToString("\n") { r ->
            val taskTitle = allTasks.find { it.id == r.task_id }?.title ?: "—"
            val date = r.date ?: "—"
            val time = r.time_spent ?: "—"
            val prog = r.progress?.let { "$it%" } ?: "—"
            val obs = r.observations ?: "—"
            """
            <tr>
              <td>$date</td>
              <td style="font-weight:500; color:#1a2332;">$taskTitle</td>
              <td>$prog</td>
              <td>$time</td>
              <td>$obs</td>
            </tr>
            """
        }
    }

    val avatarSection = if (avatarUrl != null) {
        """<img src="$avatarUrl" style="width:64px; height:64px; border-radius:50%; object-fit:cover; border:3px solid rgba(255,255,255,0.3);" />"""
    } else {
        """<div style="width:64px; height:64px; border-radius:50%; background:rgba(255,255,255,0.2); display:flex; align-items:center; justify-content:center; font-size:22px; font-weight:700; color:#fff;">$initials</div>"""
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
          .header-top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 2rem; }
          .report-badge { background: rgba(255,255,255,0.12); border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; padding: 6px 14px; font-size: 12px; font-weight: 500; letter-spacing: 0.08em; text-transform: uppercase; }
          .user-hero { display: flex; align-items: center; gap: 18px; }
          .user-name { font-family: 'DM Serif Display', serif; font-size: 1.9rem; font-weight: 400; line-height: 1.15; }
          .user-email { font-size: 13px; opacity: 0.65; margin-top: 4px; }
          .header-meta { display: flex; gap: 2rem; margin-top: 1.8rem; padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,0.12); flex-wrap: wrap; }
          .meta-item { display: flex; flex-direction: column; gap: 3px; }
          .meta-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.08em; opacity: 0.55; font-weight: 500; }
          .meta-value { font-size: 14px; font-weight: 500; opacity: 0.95; }

          .kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 1rem; }
          .kpi-box { background: #fff; border-radius: 12px; border: 0.5px solid #dde3ec; padding: 16px 14px; text-align: center; }
          .kpi-number { font-size: 1.9rem; font-weight: 600; color: #1a2332; line-height: 1; margin-bottom: 4px; }
          .kpi-label { font-size: 11px; color: #8a96a8; text-transform: uppercase; letter-spacing: 0.06em; }

          .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
          .full-col { margin-bottom: 1rem; }
          .section-card { background: #fff; border-radius: 14px; border: 0.5px solid #dde3ec; padding: 1.5rem; }
          .section-title { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.1em; color: #8a96a8; margin-bottom: 1.1rem; }

          .mini-badge { display: inline-flex; border-radius: 20px; padding: 3px 10px; font-size: 11px; font-weight: 600; }
          .role-badge { display: inline-flex; border-radius: 20px; padding: 5px 14px; font-size: 12px; font-weight: 600; margin-top: 6px; }

          .proj-row { display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; background: #f4f6f9; border-radius: 8px; margin-bottom: 8px; }
          .proj-name { font-size: 14px; font-weight: 600; color: #1a2332; }
          .proj-sub { font-size: 12px; color: #8a96a8; margin-top: 2px; }

          .progress-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; }
          .progress-label { font-size: 13px; color: #4a5568; font-weight: 500; }
          .progress-pct { font-size: 1.4rem; font-weight: 600; color: #1a2332; }
          .progress-track { height: 10px; background: #e8edf4; border-radius: 20px; overflow: hidden; margin-bottom: 1rem; }
          .progress-fill { height: 100%; background: linear-gradient(90deg, #1a2332, #2567aa); border-radius: 20px; }
          .info-row { display: flex; justify-content: space-between; padding: 9px 0; border-bottom: 0.5px solid #f0f2f5; font-size: 13px; }
          .info-row:last-child { border-bottom: none; }
          .info-key { color: #8a96a8; font-weight: 500; }
          .info-val { color: #1a2332; font-weight: 500; }

          .tasks-table, .records-table { width: 100%; border-collapse: collapse; font-size: 13px; }
          .tasks-table th, .records-table th { text-align: left; padding: 8px 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; color: #8a96a8; border-bottom: 1px solid #dde3ec; background: #f8f9fb; }
          .tasks-table td, .records-table td { padding: 11px 12px; border-bottom: 0.5px solid #edf0f5; color: #3a4556; vertical-align: top; }
          .task-name { font-weight: 500; color: #1a2332; }
          .tag-pendente { display: inline-flex; background: #fef3cd; color: #7a4b00; border-radius: 20px; padding: 3px 10px; font-size: 11px; font-weight: 600; }
          .tag-concluido { display: inline-flex; background: #d1fae5; color: #065f46; border-radius: 20px; padding: 3px 10px; font-size: 11px; font-weight: 600; }
          .tag-progresso { display: inline-flex; background: #dbeafe; color: #1e40af; border-radius: 20px; padding: 3px 10px; font-size: 11px; font-weight: 600; }

          .report-footer { margin-top: 1.5rem; display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.2rem; background: #fff; border-radius: 12px; border: 0.5px solid #dde3ec; }
          .footer-note { font-size: 12px; color: #8a96a8; }
        </style>
        </head>
        <body>
        <div class="report-wrap">

          <div class="report-header">
            <div class="header-top">
              <svg width="120" height="34" viewBox="0 0 120 34" fill="none" xmlns="http://www.w3.org/2000/svg">
                <text x="0" y="26" font-family="DM Serif Display, serif" font-size="28" fill="white" letter-spacing="-0.5">Loomo</text>
              </svg>
              <span class="report-badge">Relatório de Utilizador</span>
            </div>
            <div class="user-hero">
              $avatarSection
              <div>
                <div class="user-name">$name</div>
                <div class="user-email">${email ?: user.username ?: "—"}</div>
                <span class="role-badge" style="background:$roleBg; color:$roleColor;">$roleLabel</span>
              </div>
            </div>
            <div class="header-meta">
              <div class="meta-item">
                <span class="meta-label">Data do Relatório</span>
                <span class="meta-value">$currentDate</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Projetos</span>
                <span class="meta-value">${allProjects.size}</span>
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
              <div class="kpi-number">$totalTasks</div>
              <div class="kpi-label">Tarefas</div>
            </div>
            <div class="kpi-box">
              <div class="kpi-number" style="color:#065f46;">$completedTasks</div>
              <div class="kpi-label">Concluídas</div>
            </div>
            <div class="kpi-box">
              <div class="kpi-number" style="color:#b07800;">$pendingTasks</div>
              <div class="kpi-label">Pendentes</div>
            </div>
            <div class="kpi-box">
              <div class="kpi-number">${if (totalHours > 0) "${totalHours}h" else "—"}</div>
              <div class="kpi-label">Horas</div>
            </div>
          </div>

          <!-- DESEMPENHO + PROJETOS -->
          <div class="two-col">
            <div class="section-card">
              <div class="section-title">Desempenho</div>
              <div class="progress-header">
                <span class="progress-label">Taxa de conclusão</span>
                <span class="progress-pct">$completionPct%</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" style="width:$completionPct%;"></div>
              </div>
              <div class="info-row"><span class="info-key">Em progresso</span><span class="info-val">$inProgressTasks</span></div>
              <div class="info-row"><span class="info-key">Registos de trabalho</span><span class="info-val">$totalRecords</span></div>
              <div class="info-row"><span class="info-key">Total de horas</span><span class="info-val">${if (totalHours > 0) "${totalHours}h" else "—"}</span></div>
            </div>
            <div class="section-card">
              <div class="section-title">Projetos Associados</div>
              $projectsHtml
            </div>
          </div>

          <!-- TAREFAS -->
          <div class="section-card full-col">
            <div class="section-title">Tarefas Atribuídas</div>
            <div style="overflow-x:auto;">
              <table class="tasks-table">
                <thead>
                  <tr>
                    <th>Tarefa</th>
                    <th>Projeto</th>
                    <th>Prazo</th>
                    <th>Progresso</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>$tasksHtml</tbody>
              </table>
            </div>
          </div>

          <!-- REGISTOS -->
          <div class="section-card full-col">
            <div class="section-title">Historial de Registos de Trabalho</div>
            <div style="overflow-x:auto;">
              <table class="records-table">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Tarefa</th>
                    <th>Progresso</th>
                    <th>Tempo</th>
                    <th>Observações</th>
                  </tr>
                </thead>
                <tbody>$recordsHtml</tbody>
              </table>
            </div>
          </div>
        </div>
        </body>
        </html>
    """.trimIndent()
}