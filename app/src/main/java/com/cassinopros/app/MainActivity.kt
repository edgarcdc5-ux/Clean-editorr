package com.cassinopros.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{CassinoPros()}}
}
private val Bg=Color(0xFF0B0D10); private val Card=Color(0xFF15181D); private val Accent=Color(0xFF25F4EE)
@Composable fun CassinoPros(){
 var bankroll by remember{mutableDoubleStateOf(1000.0)}
 var sessions by remember{mutableIntStateOf(0)}
 var active by remember{mutableStateOf(false)}
 MaterialTheme(colorScheme=darkColorScheme(background=Bg,surface=Card,primary=Accent)){
  Scaffold(containerColor=Bg,bottomBar={NavigationBar(containerColor=Card){listOf(Icons.Default.Home to "Início",Icons.Default.AccountBalanceWallet to "Banca",Icons.Default.Casino to "Slots",Icons.Default.History to "Histórico",Icons.Default.BarChart to "Estatísticas").forEach{NavigationBarItem(selected=false,onClick={},icon={Icon(it.first,null)},label={Text(it.second)})}}}){
   LazyColumn(Modifier.fillMaxSize().padding(it).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
    item{Text("CASSINO PROS",color=Accent,fontSize=26.sp,fontWeight=FontWeight.ExtraBold);Text("Gerencie sua banca de slots",color=Color.LightGray)}
    item{Card(colors=CardDefaults.cardColors(Card)){Column(Modifier.padding(22.dp)){Text("BANCA ATUAL",color=Color.Gray,fontSize=12.sp);Text("R$ %.2f".format(bankroll),fontSize=36.sp,fontWeight=FontWeight.Bold);Text(if(bankroll>=1000)"+ R$ %.2f hoje".format(bankroll-1000) else "- R$ %.2f hoje".format(1000-bankroll),color=Accent)}}}
    item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat("Inicial","R$ 1.000,00",Modifier.weight(1f));Stat("Sessões",sessions.toString(),Modifier.weight(1f))}}
    item{Button({active=!active;if(active)sessions++},Modifier.fillMaxWidth().height(56.dp)){Icon(if(active)Icons.Default.Stop else Icons.Default.PlayArrow,null);Spacer(Modifier.width(8.dp));Text(if(active)"ENCERRAR SESSÃO" else "INICIAR SESSÃO")}}
    item{Text("Resultado rápido",fontSize=18.sp,fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton({bankroll=(bankroll-10).coerceAtLeast(0.0)},Modifier.weight(1f)){Text("- R$ 10")};Button({bankroll+=10},Modifier.weight(1f)){Text("+ R$ 10")}}}
    item{Text("Próximos módulos",fontSize=18.sp,fontWeight=FontWeight.Bold);Text("Banca operacional e reserva • Slots • Cassinos instalados • Limites • Metas • Segurança • Backup",color=Color.Gray)}
   }
  }
 }
}
@Composable private fun Stat(t:String,v:String,m:Modifier){Card(m,colors=CardDefaults.cardColors(Card)){Column(Modifier.padding(16.dp)){Text(t,color=Color.Gray,fontSize=12.sp);Text(v,fontWeight=FontWeight.Bold,fontSize=19.sp)}}}