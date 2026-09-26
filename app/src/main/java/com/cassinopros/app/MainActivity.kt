package com.cassinopros.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cassinopros.app.data.SessionEntity
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CassinoPros() }
    }
}

private val Bg = Color(0xFF0B0D10)
private val Card = Color(0xFF15181D)
private val Accent = Color(0xFF25F4EE)

@Composable
fun CassinoPros(vm: CasinoProsViewModel = viewModel()) {
    val bankroll by vm.bankroll.collectAsState()
    val sessions by vm.sessions.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    val active = sessions.firstOrNull { it.status == "ACTIVE" }

    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Card, primary = Accent)) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Card) {
                    val items = listOf(
                        Icons.Default.Home to "Início",
                        Icons.Default.AccountBalanceWallet to "Banca",
                        Icons.Default.Casino to "Slots",
                        Icons.Default.History to "Histórico",
                        Icons.Default.BarChart to "Estatísticas"
                    )
                    items.forEachIndexed { i, item ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(item.first, null) },
                            label = { Text(item.second) }
                        )
                    }
                }
            }
        ) { pad ->
            when (tab) {
                0 -> Dashboard(Modifier.padding(pad), bankroll?.currentCents ?: 0, bankroll?.initialCents ?: 0, sessions, active != null, vm)
                1 -> BankrollScreen(Modifier.padding(pad), bankroll?.currentCents ?: 0, bankroll?.reserveCents ?: 0, vm)
                2 -> PlaceholderScreen(Modifier.padding(pad), "Slots", "A biblioteca de slots será conectada à próxima camada.")
                3 -> HistoryScreen(Modifier.padding(pad), sessions)
                else -> StatisticsScreen(Modifier.padding(pad), sessions)
            }
        }
    }
}

@Composable private fun Dashboard(m: Modifier, current: Long, initial: Long, sessions: List<SessionEntity>, active: Boolean, vm: CasinoProsViewModel) {
    val delta = current - initial
    LazyColumn(m.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("CASSINO PROS", color = Accent, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text("Gerencie sua banca de slots", color = Color.LightGray)
        }
        item {
            Card(colors = CardDefaults.cardColors(Card)) {
                Column(Modifier.padding(22.dp)) {
                    Text("BANCA ATUAL", color = Color.Gray, fontSize = 12.sp)
                    Text(money(current), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text((if (delta >= 0) "+ " else "- ") + money(kotlin.math.abs(delta)), color = if (delta >= 0) Accent else MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("Inicial", money(initial), Modifier.weight(1f))
                Stat("Sessões", sessions.size.toString(), Modifier.weight(1f))
            }
        }
        item {
            Button({ if (active) vm.finishLatest() else vm.startSession() }, Modifier.fillMaxWidth().height(56.dp)) {
                Icon(if (active) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (active) "ENCERRAR SESSÃO" else "INICIAR SESSÃO")
            }
        }
        item { Text("Resultado rápido", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton({ vm.quickAdjust(-1000) }, Modifier.weight(1f)) { Text("- R$ 10") }
                Button({ vm.quickAdjust(1000) }, Modifier.weight(1f)) { Text("+ R$ 10") }
            }
        }
    }
}

@Composable private fun BankrollScreen(m: Modifier, current: Long, reserve: Long, vm: CasinoProsViewModel) {
    LazyColumn(m.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Minha Banca", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Operacional e reserva", color = Color.Gray) }
        item { Stat("Operacional", money(current), Modifier.fillMaxWidth()) }
        item { Stat("Reserva", money(reserve), Modifier.fillMaxWidth()) }
        item { Text("Ajuste rápido", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton({ vm.quickAdjust(-5000) }, Modifier.weight(1f)) { Text("- R$ 50") }
                Button({ vm.quickAdjust(5000) }, Modifier.weight(1f)) { Text("+ R$ 50") }
            }
        }
    }
}

@Composable private fun HistoryScreen(m: Modifier, sessions: List<SessionEntity>) {
    LazyColumn(m.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Histórico", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        if (sessions.isEmpty()) item { Text("Nenhuma sessão registrada ainda.", color = Color.Gray) }
        items(sessions) { s ->
            Card(colors = CardDefaults.cardColors(Card)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (s.status == "ACTIVE") "Sessão ativa" else "Sessão encerrada", fontWeight = FontWeight.Bold)
                        Text(s.slotName.ifBlank { "Slot não definido" }, color = Color.Gray)
                    }
                    Text(money(s.resultCents), color = if (s.resultCents >= 0) Accent else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable private fun StatisticsScreen(m: Modifier, sessions: List<SessionEntity>) {
    val finished = sessions.filter { it.status == "FINISHED" }
    val profit = finished.sumOf { it.resultCents }
    val best = finished.maxOfOrNull { it.resultCents } ?: 0
    val worst = finished.minOfOrNull { it.resultCents } ?: 0
    LazyColumn(m.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Estatísticas", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        item { Stat("Resultado acumulado", money(profit), Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("Maior lucro", money(best), Modifier.weight(1f))
                Stat("Maior perda", money(worst), Modifier.weight(1f))
            }
        }
        item { Stat("Sessões encerradas", finished.size.toString(), Modifier.fillMaxWidth()) }
    }
}

@Composable private fun PlaceholderScreen(m: Modifier, title: String, text: String) {
    Column(m.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text, color = Color.Gray)
    }
}

@Composable private fun Stat(t: String, v: String, m: Modifier) {
    Card(m, colors = CardDefaults.cardColors(Card)) {
        Column(Modifier.padding(16.dp)) {
            Text(t, color = Color.Gray, fontSize = 12.sp)
            Text(v, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
    }
}

private fun money(cents: Long): String = String.format(Locale("pt", "BR"), "R$ %,.2f", cents / 100.0)
