package com.cassinopros.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cassinopros.app.data.SessionEntity
import com.cassinopros.app.data.SlotEntity
import java.util.Locale

class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{CasinoPros()}}
}
private val Navy=Color(0xFF0A1228);private val Glass=Color(0x12FFFFFF);private val Glass2=Color(0x20FFFFFF)
private val Neon=Color(0xFF00FF88);private val Lime=Color(0xFFB4FF39);private val Loss=Color(0xFFFF5A5A);private val Muted=Color(0xFF9BA5BD)
private fun money(c:Long)=String.format(Locale("pt","BR"),"R$ %,.2f",c/100.0)

@Composable fun CasinoPros(vm:CasinoProsViewModel=viewModel()){
 val b by vm.bankroll.collectAsState();val sessions by vm.sessions.collectAsState();val slots by vm.slots.collectAsState()
 var tab by remember{mutableIntStateOf(0)};var page by remember{mutableStateOf("home")};val active=sessions.firstOrNull{it.status=="ACTIVE"};val ctx=LocalContext.current
 MaterialTheme(colorScheme=darkColorScheme(background=Navy,surface=Navy,primary=Neon,onPrimary=Navy)){
  Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy,Color(0xFF111A38))))){
   when(page){
    "new"->NewSession(slots,ctx,vm,{page="home"},{page="active"})
    "active"->if(active!=null)ActiveSession(active,b?.currentCents?:0,vm,{page="result"},{page="home"})else{page="home"}
    "result"->if(active!=null)Result(active,vm,{page="active"})else{page="home"}
    else->Scaffold(containerColor=Color.Transparent,bottomBar={
     NavigationBar(containerColor=Color(0xDD0A1228)){navItems(tab){tab=it}}
    }){p->when(tab){
     0->Dashboard(Modifier.padding(p),b?.currentCents?:0,b?.initialCents?:0,sessions,active!=null){page=if(active!=null)"active" else "new"}
     1->Bankroll(Modifier.padding(p),b?.currentCents?:0,b?.reserveCents?:0,vm)
     2->Slots(Modifier.padding(p),slots,vm)
     3->History(Modifier.padding(p),sessions)
     else->Stats(Modifier.padding(p),sessions)
    }}
   }
  }
 }
}
@Composable private fun navItems(tab:Int,set:(Int)->Unit){
 listOf(Icons.Default.Home to "Início",Icons.Default.AccountBalanceWallet to "Banca",Icons.Default.Casino to "Slots",Icons.Default.History to "Histórico",Icons.Default.BarChart to "Estatísticas").forEachIndexed{i,x->
  NavigationBarItem(tab==i,{set(i)},icon={Icon(x.first,null)},label={Text(x.second)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Neon,selectedTextColor=Neon,indicatorColor=Glass2,unselectedIconColor=Muted,unselectedTextColor=Muted))
 }
}
@Composable private fun CardGlass(m:Modifier=Modifier,body:@Composable ColumnScope.()->Unit)=Card(m,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Glass)){Column(Modifier.padding(16.dp),body)}
@Composable private fun Header(t:String,s:String,back:()->Unit)=Row(Modifier.fillMaxWidth()){IconButton(back){Icon(Icons.Default.ArrowBack,null,tint=Color.White)};Column{Text(t,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Text(s,color=Muted,fontSize=13.sp)}}
@Composable private fun Dashboard(m:Modifier,current:Long,initial:Long,s:List<SessionEntity>,active:Boolean,go:()->Unit){
 val delta=current-initial
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Text("Olá,",color=Muted);Text("Cassino Pros",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)}
  item{CardGlass(Modifier.fillMaxWidth()){Text("SALDO TOTAL",color=Muted,fontSize=12.sp);Text(money(current),color=Color.White,fontSize=39.sp,fontWeight=FontWeight.ExtraBold);Text((if(delta>=0)"↗ +" else "↘ -")+money(kotlin.math.abs(delta)),color=Neon);Chart()}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("LUCRO",money(s.filter{it.status=="FINISHED"}.sumOf{it.resultCents}),Modifier.weight(1f));Metric("SESSÕES",s.size.toString(),Modifier.weight(1f));Metric("MELHOR",money(s.filter{it.status=="FINISHED"}.maxOfOrNull{it.resultCents}?:0),Modifier.weight(1f))}}
  item{Button(go,Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text(if(active)"CONTINUAR SESSÃO" else "NOVA SESSÃO",fontWeight=FontWeight.Bold)}}
 }
}
@Composable private fun Metric(t:String,v:String,m:Modifier)=CardGlass(m){Text(t,color=Muted,fontSize=10.sp);Text(v,color=Color.White,fontSize=16.sp,fontWeight=FontWeight.Bold)}
@Composable private fun Chart(){Canvas(Modifier.fillMaxWidth().height(75.dp)){val p=Path();p.moveTo(0f,size.height*.8f);p.lineTo(size.width*.2f,size.height*.62f);p.lineTo(size.width*.4f,size.height*.7f);p.lineTo(size.width*.58f,size.height*.38f);p.lineTo(size.width*.76f,size.height*.48f);p.lineTo(size.width,size.height*.12f);drawPath(p,Neon,style=Stroke(4f));drawCircle(Lime,5f,Offset(size.width,size.height*.12f))}}

@Composable private fun NewSession(slots:List<SlotEntity>,ctx:android.content.Context,vm:CasinoProsViewModel,back:()->Unit,started:()->Unit){
 val apps=ctx.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),PackageManager.MATCH_ALL).distinctBy{it.activityInfo.packageName}.sortedBy{it.loadLabel(ctx.packageManager).toString()}
 var app by remember{mutableStateOf(apps.firstOrNull())};var slot by remember{mutableStateOf(slots.firstOrNull())};var stop by remember{mutableLongStateOf(5000)};var target by remember{mutableLongStateOf(10000)}
 LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Header("Nova Sessão","Selecione cassino, slot e limites",back)}
  item{CardGlass{Text("SELECIONAR CASSINO",color=Lime,fontWeight=FontWeight.Bold);Text(app?.loadLabel(ctx.packageManager)?.toString()?:"Nenhum app encontrado",color=Color.White,fontSize=18.sp);Text(app?.activityInfo?.packageName?:"",color=Muted,fontSize=10.sp)}}
  item{CardGlass{Text("SELECIONAR SLOT",color=Lime,fontWeight=FontWeight.Bold);Text(slot?.name?:"Nenhum slot cadastrado",color=Color.White,fontSize=18.sp);Text(slot?.provider?:"Cadastre na aba Slots",color=Muted)}}
  item{Text("Limites da sessão",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("STOP LOSS",money(stop),Modifier.weight(1f));Metric("META",money(target),Modifier.weight(1f))}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({stop=(stop-1000).coerceAtLeast(1000)},Modifier.weight(1f)){Text("- Stop")};OutlinedButton({target+=1000},Modifier.weight(1f)){Text("+ Meta")}}}
  item{Button({vm.startSession(app?.loadLabel(ctx.packageManager)?.toString()?:"",app?.activityInfo?.packageName?:"",slot?.name?:"",stop,target);started()},Modifier.fillMaxWidth().height(58.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text("INICIAR SESSÃO",fontWeight=FontWeight.Bold)}}
 }
}
@Composable private fun ActiveSession(s:SessionEntity,current:Long,vm:CasinoProsViewModel,result:()->Unit,back:()->Unit){
 val profit=current-s.startingCents
 LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Header("Sessão Ativa",s.casinoName+" • "+s.slotName,back)}
  item{Text("● SESSÃO ATIVA     AO VIVO",color=Neon,fontWeight=FontWeight.Bold)}
  item{CardGlass(Modifier.fillMaxWidth()){Text("LUCRO ATUAL",color=Muted);Text((if(profit>=0)"+" else "-")+money(kotlin.math.abs(profit)),color=if(profit>=0)Neon else Loss,fontSize=42.sp,fontWeight=FontWeight.ExtraBold);Chart()}}
  item{CardGlass{Text("LIMITES",color=Muted);Text("Stop Loss: "+money(s.stopLossCents),color=Loss);Text("Meta: "+money(s.targetCents),color=Neon);Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={((kotlin.math.abs(profit)).toFloat()/s.targetCents.coerceAtLeast(1)).coerceIn(0f,1f)},Modifier.fillMaxWidth(),color=if(profit>=0)Neon else Loss,trackColor=Glass2)}}
  item{Text("Registrar resultado",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick("+ R$ 10",Neon);Quick("+ R$ 20",Neon);Quick("+ R$ 50",Neon)}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick("- R$ 10",Loss);Quick("- R$ 20",Loss);Quick("- R$ 50",Loss)}}
  item{Button(result,Modifier.fillMaxWidth().height(54.dp)){Text("REGISTRAR RESULTADO")}}
  item{Button({vm.finishLatest();back()},Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text("ENCERRAR SESSÃO",fontWeight=FontWeight.Bold)}}
 }
}
@Composable private fun Quick(t:String,c:Color)=Surface(color=Glass2,shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f).height(50.dp)){Box(Modifier.fillMaxSize(),contentAlignment=androidx.compose.ui.Alignment.Center){Text(t,color=c,fontWeight=FontWeight.Bold)}}
@Composable private fun Result(s:SessionEntity,vm:CasinoProsViewModel,back:()->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Header("Registrar Resultado","Adição rápida ao histórico",back)};item{CardGlass{Text("RESULTADO DA SESSÃO",color=Lime,fontWeight=FontWeight.Bold);Text("Um toque atualiza a banca e o histórico.",color=Muted,fontSize=18.sp)}};item{listOf(1000L,2000L,5000L,10000L,-1000L,-2000L,-5000L,-10000L).chunked(2).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach{v->Button({vm.registerResult(v);back()},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Glass2)){Text((if(v>0)"+" else "-")+money(kotlin.math.abs(v)),color=if(v>0)Neon else Loss)}};Spacer(Modifier.height(8.dp))}}}}
 }
}
@Composable private fun Bankroll(m:Modifier,current:Long,reserve:Long,vm:CasinoProsViewModel){
 val total=current+reserve;val op=if(total==0L)0 else (current*100/total).toInt()
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Minha Banca",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Operacional e reserva",color=Muted)};item{CardGlass{Text("BANCA TOTAL",color=Muted);Text(money(total),color=Color.White,fontSize=40.sp,fontWeight=FontWeight.ExtraBold);Text("Atualizado agora",color=Neon)}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("OPERACIONAL",money(current),Modifier.weight(1f));Metric("RESERVA",money(reserve),Modifier.weight(1f))}};item{LinearProgressIndicator(progress={op/100f},Modifier.fillMaxWidth(),color=Neon,trackColor=Glass2)};item{Text("Distribuição: "+op+"% operacional • "+(100-op)+"% reserva",color=Muted)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({vm.quickAdjust(5000)},Modifier.weight(1f)){Text("+ R$ 50")};OutlinedButton({vm.quickAdjust(-5000)},Modifier.weight(1f)){Text("- R$ 50")}}}}
}
@Composable private fun Slots(m:Modifier,slots:List<SlotEntity>,vm:CasinoProsViewModel){
 var n by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var c by remember{mutableStateOf("")};val ctx=LocalContext.current
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Meus Slots",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Biblioteca pessoal",color=Muted)};item{OutlinedTextField(n,{n=it},Modifier.fillMaxWidth(),label={Text("Nome do slot")})};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(p,{p=it},Modifier.weight(1f),label={Text("Provider")});OutlinedTextField(c,{c=it},Modifier.weight(1f),label={Text("Cassino")})}};item{Button({vm.addSlot(n,p,c);n="";p="";c=""},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text("+ ADICIONAR SLOT")}};items(slots){x->CardGlass(Modifier.fillMaxWidth()){Text(x.name,color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold);Text(x.provider+" • "+x.casinoName,color=Muted);Text("+R$ 0,00 • ÚLTIMA SESSÃO",color=Neon,fontSize=12.sp)}};item{Text("Aplicativos instalados",color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Bold)};val apps=ctx.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),PackageManager.MATCH_ALL).distinctBy{it.activityInfo.packageName}.sortedBy{it.loadLabel(ctx.packageManager).toString().lowercase()};items(apps.take(20)){x->CardGlass(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(x.loadLabel(ctx.packageManager).toString(),color=Color.White,fontWeight=FontWeight.Bold);Text(x.activityInfo.packageName,color=Muted,fontSize=10.sp)};Button({ctx.startActivity(ctx.packageManager.getLaunchIntentForPackage(x.activityInfo.packageName))}){Text("ABRIR")}}}}}
}
@Composable private fun History(m:Modifier,s:List<SessionEntity>){var f by remember{mutableStateOf("Todas")};val list=s.filter{f=="Todas"||(f=="Lucro"&&it.resultCents>0)||(f=="Prejuízo"&&it.resultCents<0)};LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Histórico",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Row{listOf("Todas","Lucro","Prejuízo").forEach{x->FilterChip(f==x,{f=x},label={Text(x)})}}};items(list){x->CardGlass(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(x.casinoName+" • "+x.slotName,color=Color.White,fontWeight=FontWeight.Bold);Text(if(x.status=="ACTIVE")"Ativa" else "Encerrada",color=Muted)};Text((if(x.resultCents>=0)"+" else "-")+money(kotlin.math.abs(x.resultCents)),color=if(x.resultCents>=0)Neon else Loss,fontWeight=FontWeight.Bold)}}}}}
@Composable private fun Stats(m:Modifier,s:List<SessionEntity>){val f=s.filter{it.status=="FINISHED"};val p=f.sumOf{it.resultCents};val w=f.count{it.resultCents>0};val wr=if(f.isEmpty())0 else w*100/f.size;LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Estatísticas",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Performance da banca",color=Muted)};item{CardGlass{Text("EVOLUÇÃO DA BANCA",color=Muted);Chart()}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("LUCRO TOTAL",money(p),Modifier.weight(1f));Metric("WINRATE",wr.toString()+"%",Modifier.weight(1f))}};item{Metric("SESSÕES ENCERRADAS",f.size.toString(),Modifier.fillMaxWidth())}}}
