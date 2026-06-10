package com.example.tp_loomo.ui.admin.stats.reports

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.tp_loomo.ui.admin.stats.*
import java.text.SimpleDateFormat
import java.util.*

fun exportHtmlToPdf(context: Context, htmlContent: String, projectName: String) {
    val webView = WebView(context)

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Relatorio_${projectName.replace(" ", "_")}"
            val printAdapter = view.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }

    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}

fun buildReportHtml(
    project: StatProject,
    tasks: List<StatTask>,
    manager: StatUser?,
    team: List<StatUser>,
    taskAssignments: List<StatTaskAssignment>,
    allUsers: List<StatUser>,
): String {
    val description = project.description ?: "Sem descrição disponível."
    val totalTasks = tasks.size
    val completedTasks = tasks.count {
        it.status?.lowercase() in listOf("completed", "concluído")
    }
    val pendingTasks = totalTasks - completedTasks
    val completionPct = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
    val currentDate = SimpleDateFormat("dd MMMM yyyy · HH:mm", Locale("pt", "PT")).format(Date())

    val managerName = manager?.full_name ?: "Sem Gestor"
    val managerInitials = managerName.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
    val managerEmail = manager?.username?.let { "$it@loomo.pt" } ?: "indisponivel@loomo.pt"

    val tasksHtml = tasks.joinToString("\n") { task ->
        val assignment = taskAssignments.filter { it.task_id == task.id }.firstOrNull()
        val user = allUsers.find { it.id == assignment?.user_id }
        val resp = user?.full_name ?: "A Definir"
        val prazo = task.due_date ?: "Sem prazo"
        val status = task.status ?: "Pendente"
        val tagClass = if (status.lowercase() in listOf("completed", "concluído")) "tag-concluido" else "tag-pendente"
        val statusLabel = if (status.lowercase() == "completed") "Concluído" else status

        """
        <tr>
          <td class="task-name">${task.title}</td>
          <td>$resp</td>
          <td>$prazo</td>
          <td><span class="$tagClass">$statusLabel</span></td>
        </tr>
        """
    }

    val colors = listOf("blue", "teal", "coral", "violet", "green", "amber")
    val teamHtml = team.mapIndexed { index, user ->
        val color = colors[index % colors.size]
        val name = user.full_name ?: "Membro"
        val initials = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
        """
        <div class="member-row">
          <div class="avatar $color">$initials</div>
          <div class="member-info">
            <div class="person-name">$name</div>
            <div class="person-role">Membro da Equipa</div>
          </div>
        </div>
        """
    }.joinToString("\n")

    // (HTML igual ao original, omitido por brevidade — copia o return completo do ficheiro original)
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
          .report-header::before { content: ''; position: absolute; top: -60px; right: -60px; width: 220px; height: 220px; border-radius: 50%; background: rgba(255,255,255,0.06); }
          .report-header::after { content: ''; position: absolute; bottom: -40px; left: 40%; width: 300px; height: 160px; border-radius: 50%; background: rgba(255,255,255,0.04); }
          .header-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; margin-bottom: 2rem; }
          .report-badge { background: rgba(255,255,255,0.18); border: 1px solid rgba(255,255,255,0.25); border-radius: 8px; padding: 6px 14px; font-size: 12px; font-weight: 500; letter-spacing: 0.08em; text-transform: uppercase; }
          .project-name { font-family: 'DM Serif Display', serif; font-size: 2.2rem; font-weight: 400; line-height: 1.15; margin-bottom: 0.6rem; position: relative; }
          .header-meta { display: flex; gap: 2rem; margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,0.18); flex-wrap: wrap; }
          .meta-item { display: flex; flex-direction: column; gap: 3px; }
          .meta-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.08em; opacity: 0.6; font-weight: 500; }
          .meta-value { font-size: 14px; font-weight: 500; opacity: 0.95; }
          .status-pill { display: inline-flex; align-items: center; gap: 6px; border-radius: 20px; padding: 5px 12px; font-size: 12px; font-weight: 500; }
          .status-pill.andamento { background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.35); color: #fff; }
          .status-dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
          .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
          .full-col { margin-bottom: 1rem; }
          .section-card { background: #ffffff; border-radius: 14px; border: 0.5px solid #dde3ec; padding: 1.5rem; }
          .section-title { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.1em; color: #8a96a8; margin-bottom: 1.1rem; }
          .project-description { font-size: 14px; line-height: 1.75; color: #3a4556; }
          .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 1.2rem; }
          .stat-box { background: #f4f6f9; border-radius: 10px; padding: 14px 12px; text-align: center; }
          .stat-number { font-size: 2rem; font-weight: 600; color: #1a2332; line-height: 1; margin-bottom: 4px; }
          .stat-number.pending { color: #b07800; }
          .stat-label { font-size: 11px; color: #8a96a8; text-transform: uppercase; }
          .progress-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; }
          .progress-label { font-size: 13px; color: #4a5568; font-weight: 500; }
          .progress-pct { font-size: 1.4rem; font-weight: 600; color: #2567aa; }
          .progress-track { height: 10px; background: #e8edf4; border-radius: 20px; overflow: hidden; }
          .progress-fill { height: 100%; background: linear-gradient(90deg, #2567aa, #4a9fd4); border-radius: 20px; }
          .manager-row { display: flex; align-items: center; gap: 12px; padding: 12px; background: #eef4fb; border-radius: 10px; margin-bottom: 1rem; }
          .avatar { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; }
          .avatar.blue { background: #2567aa; color: #fff; }
          .avatar.teal { background: #1a7a6e; color: #fff; }
          .avatar.coral { background: #b03a2e; color: #fff; }
          .avatar.violet { background: #6c3483; color: #fff; }
          .avatar.green { background: #1e8449; color: #fff; }
          .avatar.amber { background: #9a6500; color: #fff; }
          .person-name { font-size: 14px; font-weight: 600; color: #1a2332; line-height: 1.2; }
          .person-role { font-size: 12px; color: #8a96a8; }
          .person-email { font-size: 12px; color: #2567aa; margin-top: 2px; }
          .members-list { display: flex; flex-direction: column; gap: 8px; }
          .member-row { display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 8px; background: #f4f6f9; }
          .member-info { flex: 1; }
          .tasks-table { width: 100%; border-collapse: collapse; font-size: 13px; }
          .tasks-table th { text-align: left; padding: 8px 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; color: #8a96a8; border-bottom: 1px solid #dde3ec; background: #f8f9fb; }
          .tasks-table td { padding: 11px 12px; border-bottom: 0.5px solid #edf0f5; color: #3a4556; }
          .task-name { font-weight: 500; color: #1a2332; }
          .tag-pendente { display: inline-flex; align-items: center; gap: 5px; background: #fef3cd; color: #7a4b00; border-radius: 20px; padding: 3px 10px; font-size: 11px; font-weight: 600; }
          .tag-concluido { display: inline-flex; align-items: center; gap: 5px; background: #d1fae5; color: #065f46; border-radius: 20px; padding: 3px 10px; font-size: 11px; font-weight: 600; }
          .report-footer { margin-top: 1.5rem; display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.2rem; background: #fff; border-radius: 12px; border: 0.5px solid #dde3ec; }
          .footer-note { font-size: 12px; color: #8a96a8; }
          .footer-brand { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #8a96a8; font-weight: 500; }
        </style>
        </head>
        <body>
        <div class="report-wrap">
          <div class="report-header">
            <div class="header-top">
              <div class="logo-area">
                <svg width="120" height="34" viewBox="0 0 120 34" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <text x="0" y="26" font-family="DM Serif Display, serif" font-size="28" fill="white" letter-spacing="-0.5">Loomo</text>
                </svg>
              </div>
              <span class="report-badge">Relatório de Projeto</span>
            </div>
            <div class="project-name">${project.name}</div>
            <div style="margin-top:10px; display:flex; gap:10px; align-items:center;">
              <span class="status-pill andamento"><span class="status-dot"></span>Em Andamento</span>
            </div>
            <div class="header-meta">
              <div class="meta-item">
                <span class="meta-label">Data do Relatório</span>
                <span class="meta-value">$currentDate</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Gerado por</span>
                <span class="meta-value">Administrador</span>
              </div>
            </div>
          </div>

          <div class="two-col">
            <div class="section-card">
              <div class="section-title">Detalhes do projeto</div>
              <p class="project-description">$description</p>
            </div>
            <div class="section-card">
              <div class="section-title">Progresso</div>
              <div class="stats-grid">
                <div class="stat-box">
                  <div class="stat-number">$totalTasks</div>
                  <div class="stat-label">Total</div>
                </div>
                <div class="stat-box">
                  <div class="stat-number" style="color:#1a7a6e;">$completedTasks</div>
                  <div class="stat-label">Concluídas</div>
                </div>
                <div class="stat-box">
                  <div class="stat-number pending">$pendingTasks</div>
                  <div class="stat-label">Pendentes</div>
                </div>
              </div>
              <div class="progress-section">
                <div class="progress-header">
                  <span class="progress-label">Taxa de conclusão</span>
                  <span class="progress-pct">$completionPct%</span>
                </div>
                <div class="progress-track">
                  <div class="progress-fill" style="width: $completionPct%;"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="section-card full-col">
            <div class="section-title">Recursos humanos</div>
            <div style="font-size:11px; text-transform:uppercase; color:#8a96a8; font-weight:600; margin-bottom:8px;">Gestor do projeto</div>
            <div class="manager-row">
              <div class="avatar blue">$managerInitials</div>
              <div>
                <div class="person-name">$managerName</div>
                <div class="person-email">$managerEmail</div>
              </div>
            </div>
            <div style="font-size:11px; text-transform:uppercase; color:#8a96a8; font-weight:600; margin-bottom:8px;">Membros da equipa</div>
            <div class="members-list">
              $teamHtml
            </div>
          </div>

          <div class="section-card full-col">
            <div class="section-title">Lista de tarefas</div>
            <div style="overflow-x:auto;">
              <table class="tasks-table">
                <thead>
                  <tr>
                    <th style="width:45%;">Tarefa</th>
                    <th>Responsável</th>
                    <th>Prazo</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  $tasksHtml
                </tbody>
              </table>
            </div>
          </div>
        </div>
        </body>
        </html>
    """.trimIndent()
}