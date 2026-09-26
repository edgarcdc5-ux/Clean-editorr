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
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.delay

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
  item{CardGlass(Modifier.fillMaxWidth()){Text("SALDO TOTAL",color=Muted,fontSize=12.sp);Text(money(current),color=Color.White,fontSize=39.sp,fontWeight=FontWeight.ExtraBold);Text((if(delta>=0)"↗ +" else "↘ -")+money(kotlin.math.abs(delta)),color=if(delta>=0)Neon else Loss);Chart(s.filter{it.status=="FINISHED"}.sortedBy{it.startedAt}.takeLast(7).map{it.resultCents})}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("LUCRO",money(s.filter{it.status=="FINISHED"}.sumOf{it.resultCents}),Modifier.weight(1f));Metric("SESSÕES",s.size.toString(),Modifier.weight(1f));Metric("MELHOR",money(s.filter{it.status=="FINISHED"}.maxOfOrNull{it.resultCents}?:0),Modifier.weight(1f))}}
  item{Button(go,Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text(if(active)"CONTINUAR SESSÃO" else "NOVA SESSÃO",fontWeight=FontWeight.Bold)}}
 }
}
@Composable private fun Metric(t:String,v:String,m:Modifier)=CardGlass(m){Text(t,color=Muted,fontSize=10.sp);Text(v,color=Color.White,fontSize=16.sp,fontWeight=FontWeight.Bold)}
@Composable private fun Chart(values:List<Long> = emptyList()){Canvas(Modifier.fillMaxWidth().height(75.dp)){if(values.isEmpty()){drawLine(Muted,Offset(0f,size.height/2),Offset(size.width,size.height/2),2f)}else{val min=values.minOrNull()?:0L;val max=values.maxOrNull()?:1L;val range=(max-min).coerceAtLeast(1L);val p=Path();values.forEachIndexed{i,v->{val x=if(values.size==1)size.width/2 else size.width*i/(values.size-1).toFloat();val y=size.height-(size.height*((v-min).toFloat()/range));if(i==0)p.moveTo(x,y)else p.lineTo(x,y);drawCircle(if(v>=0)Neon else Loss,4f,Offset(x,y))}};drawPath(p,Neon,style=Stroke(4f))}}}

@Composable private fun NewSession(slots:List<SlotEntity>,ctx:android.content.Context,vm:CasinoProsViewModel,back:()->Unit,started:()->Unit){
 val apps=remember{ctx.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),PackageManager.MATCH_ALL).distinctBy{it.activityInfo.packageName}.sortedBy{it.loadLabel(ctx.packageManager).toString()}}
 var app by remember{mutableStateOf(apps.firstOrNull())}
 var slot by remember{mutableStateOf(slots.firstOrNull())}
 var appMenu by remember{mutableStateOf(false)}
 var slotMenu by remember{mutableStateOf(false)}
 var stop by remember{mutableLongStateOf(5000)}
 var target by remember{mutableLongStateOf(10000)}
 LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Header("Nova Sessão","Selecione cassino, slot e limites",back)}
  item{
   CardGlass{
    Text("SELECIONAR CASSINO",color=Lime,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Box{
     OutlinedButton({appMenu=true},Modifier.fillMaxWidth()){
      Text(app?.loadLabel(ctx.packageManager)?.toString()?:"Nenhum app encontrado",color=Color.White,modifier=Modifier.weight(1f))
      Icon(Icons.Default.ArrowDropDown,null,tint=Lime)
     }
     DropdownMenu(expanded=appMenu,onDismissRequest={appMenu=false}){
      apps.take(30).forEach{x->DropdownMenuItem(text={Text(x.loadLabel(ctx.packageManager).toString())},onClick={app=x;appMenu=false})}
     }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(app?.activityInfo?.packageName?:"",color=Muted,fontSize=10.sp);if(app!=null)TextButton({ctx.startActivity(ctx.packageManager.getLaunchIntentForPackage(app!!.activityInfo.packageName))}){Text("ABRIR",color=Neon)}}
   }
  }
  item{
   CardGlass{
    Text("SELECIONAR SLOT",color=Lime,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Box{
     OutlinedButton({slotMenu=true},Modifier.fillMaxWidth()){
      Text(slot?.name?:"Nenhum slot cadastrado",color=Color.White,modifier=Modifier.weight(1f))
      Icon(Icons.Default.ArrowDropDown,null,tint=Lime)
     }
     DropdownMenu(expanded=slotMenu,onDismissRequest={slotMenu=false}){
      slots.forEach{x->DropdownMenuItem(text={Text(x.name)},onClick={slot=x;slotMenu=false})}
     }
    }
    Text(slot?.provider?.ifBlank{"Adicione seus slots na aba Slots"}?:"Adicione seus slots na aba Slots",color=Muted)
   }
  }
  item{Text("Limites da sessão",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("STOP LOSS",money(stop),Modifier.weight(1f));Metric("META",money(target),Modifier.weight(1f))}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({stop=(stop-1000).coerceAtLeast(1000)},Modifier.weight(1f)){Text("- Stop")};OutlinedButton({target+=1000},Modifier.weight(1f)){Text("+ Meta")}}}
  item{Button({vm.startSession(app?.loadLabel(ctx.packageManager)?.toString()?:"",app?.activityInfo?.packageName?:"",slot?.name?:"",stop,target);started()},Modifier.fillMaxWidth().height(58.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy),enabled=app!=null){
   Text("INICIAR SESSÃO",fontWeight=FontWeight.Bold)
  }}
 }
}
@Composable private fun ActiveSession(s:SessionEntity,current:Long,vm:CasinoProsViewModel,result:()->Unit,back:()->Unit){
 var now by remember{mutableLongStateOf(System.currentTimeMillis())}
 LaunchedEffect(s.id){while(true){now=System.currentTimeMillis();delay(1000)}}
 val profit=current-s.startingCents
 val elapsed=((now-s.startedAt).coerceAtLeast(0))/1000
 val elapsedText=String.format(Locale.US,"%02d:%02d:%02d",elapsed/3600,(elapsed%3600)/60,elapsed%60)
 LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Header("Sessão Ativa",s.casinoName+" • "+s.slotName,back)}
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("● SESSÃO ATIVA",color=Neon,fontWeight=FontWeight.Bold);Text(elapsedText,color=Color.White,fontWeight=FontWeight.Bold)}}
  item{CardGlass(Modifier.fillMaxWidth()){Text("LUCRO ATUAL",color=Muted);Text((if(profit>=0)"+" else "-")+money(kotlin.math.abs(profit)),color=if(profit>=0)Neon else Loss,fontSize=42.sp,fontWeight=FontWeight.ExtraBold);Chart()}}
  item{CardGlass{Text("LIMITES",color=Muted);Text("Stop Loss: "+money(s.stopLossCents),color=Loss);Text("Meta: "+money(s.targetCents),color=Neon);Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={((kotlin.math.abs(profit)).toFloat()/s.targetCents.coerceAtLeast(1)).coerceIn(0f,1f)},Modifier.fillMaxWidth(),color=if(profit>=0)Neon else Loss,trackColor=Glass2)}}
  item{Text("Registrar resultado",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick("+ R$ 10",Neon){vm.registerResult(1000)};Quick("+ R$ 20",Neon){vm.registerResult(2000)};Quick("+ R$ 50",Neon){vm.registerResult(5000)}}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick("- R$ 10",Loss){vm.registerResult(-1000)};Quick("- R$ 20",Loss){vm.registerResult(-2000)};Quick("- R$ 50",Loss){vm.registerResult(-5000)}}}
  item{Text("Stop Loss: "+money(s.stopLossCents)+" • Meta: "+money(s.targetCents),color=Muted,fontSize=12.sp)}
  item{Button(result,Modifier.fillMaxWidth().height(54.dp)){Text("REGISTRAR RESULTADO")}}
  item{Button({vm.finishLatest();back()},Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text("ENCERRAR SESSÃO",fontWeight=FontWeight.Bold)}}
 }
}
@Composable private fun Quick(t:String,c:Color,onClick:()->Unit)=Button(onClick,shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f).height(50.dp),colors=ButtonDefaults.buttonColors(containerColor=Glass2)){Text(t,color=c,fontWeight=FontWeight.Bold)}
@Composable private fun Result(s:SessionEntity,vm:CasinoProsViewModel,back:()->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Header("Registrar Resultado","Adição rápida ao histórico",back)}
  item{CardGlass{Text("RESULTADO DA SESSÃO",color=Lime,fontWeight=FontWeight.Bold);Text("Um toque atualiza a banca e o histórico.",color=Muted,fontSize=18.sp)}}
  listOf(1000L,2000L,5000L,10000L,-1000L,-2000L,-5000L,-10000L).chunked(2).forEach{row->
   item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach{v->
    Button({vm.registerResult(v);back()},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=Glass2)){
     Text((if(v>0)"+" else "-")+money(kotlin.math.abs(v)),color=if(v>0)Neon else Loss)
    }
   }}}
  }
 }
}
@Composable private fun Bankroll(m:Modifier,current:Long,reserve:Long,vm:CasinoProsViewModel){
 val total=current+reserve;val op=if(total==0L)0 else (current*100/total).toInt()
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Minha Banca",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Operacional e reserva",color=Muted)};item{CardGlass{Text("BANCA TOTAL",color=Muted);Text(money(total),color=Color.White,fontSize=40.sp,fontWeight=FontWeight.ExtraBold);Text("Atualizado agora",color=Neon)}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("OPERACIONAL",money(current),Modifier.weight(1f));Metric("RESERVA",money(reserve),Modifier.weight(1f))}};item{LinearProgressIndicator(progress={op/100f},Modifier.fillMaxWidth(),color=Neon,trackColor=Glass2)};item{Text("Distribuição: "+op+"% operacional • "+(100-op)+"% reserva",color=Muted)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({vm.setReservePercent(10)},Modifier.weight(1f)){Text("10%")};OutlinedButton({vm.setReservePercent(30)},Modifier.weight(1f)){Text("30%")};OutlinedButton({vm.setReservePercent(50)},Modifier.weight(1f)){Text("50%")}}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({vm.quickAdjust(5000)},Modifier.weight(1f)){Text("+ R$ 50")};OutlinedButton({vm.quickAdjust(-5000)},Modifier.weight(1f)){Text("- R$ 50")}}}}
}
@Composable private fun Slots(m:Modifier,slots:List<SlotEntity>,vm:CasinoProsViewModel){
 var n by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var c by remember{mutableStateOf("")};var stake by remember{mutableStateOf("1,00")};var editing by remember{mutableStateOf<SlotEntity?>(null)}
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Text("Meus Slots",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Biblioteca pessoal",color=Muted)}
  item{CardGlass{Text("NOVO SLOT",color=Lime,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));OutlinedTextField(n,{n=it},Modifier.fillMaxWidth(),label={Text("Nome do slot")});Spacer(Modifier.height(6.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(p,{p=it},Modifier.weight(1f),label={Text("Provider")});OutlinedTextField(c,{c=it},Modifier.weight(1f),label={Text("Cassino")})};Spacer(Modifier.height(6.dp));OutlinedTextField(stake,{stake=it},Modifier.fillMaxWidth(),label={Text("Stake padrão (R$)")});Spacer(Modifier.height(8.dp));Button({vm.addSlot(n,p,c,((stake.replace(",","." ).toDoubleOrNull()?:1.0)*100).toLong());n="";p="";c="";stake="1,00"},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Navy)){Text("+ ADICIONAR SLOT")}}}
 items(slots){x->CardGlass(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(x.name,color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold);Text(x.provider.ifBlank{"Provider não informado"}+" • "+x.casinoName.ifBlank{"Cassino não informado"},color=Muted);Text("Stake padrão: "+money(x.defaultStakeCents),color=Neon,fontSize=12.sp)};Column{TextButton({editing=x}){Text("EDITAR",color=Neon)};TextButton({vm.deleteSlot(x)}){Text("EXCLUIR",color=Loss)}}}}}
 }
 if(editing!=null){var e by remember(editing){mutableStateOf(editing!!)};AlertDialog(onDismissRequest={editing=null},confirmButton={TextButton({vm.updateSlot(e);editing=null}){Text("SALVAR",color=Neon)}},dismissButton={TextButton({editing=null}){Text("CANCELAR")}},title={Text("Editar slot")},text={Column{OutlinedTextField(e.name,{e=e.copy(name=it)},label={Text("Nome")});OutlinedTextField(e.provider,{e=e.copy(provider=it)},label={Text("Provider")});OutlinedTextField(e.casinoName,{e=e.copy(casinoName=it)},label={Text("Cassino")});OutlinedTextField((e.defaultStakeCents/100.0).toString(),{v->e=e.copy(defaultStakeCents=((v.replace(",","." ).toDoubleOrNull()?:0.0)*100).toLong())},label={Text("Stake padrão")})}})}
}@Composable private fun History(m:Modifier,s:List<SessionEntity>){
 var filter by remember{mutableStateOf("Todas")};var period by remember{mutableStateOf("Tudo")}
 val now=System.currentTimeMillis();val start=when(period){"Hoje"->now-86400000L;"7 dias"->now-7*86400000L;"30 dias"->now-30*86400000L;else->0L}
 val list=s.filter{it.startedAt>=start}.filter{filter=="Todas"||(filter=="Lucro"&&it.resultCents>0)||(filter=="Prejuízo"&&it.resultCents<0)}
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  item{Text("Histórico",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Resultados por período",color=Muted)}
  item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Tudo","Hoje","7 dias","30 dias").forEach{x->FilterChip(period==x,{period=x},label={Text(x)})}}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Todas","Lucro","Prejuízo").forEach{x->FilterChip(filter==x,{filter=x},label={Text(x)})}}}
  item{CardGlass{Text("RESULTADO DO PERÍODO",color=Muted,fontSize=11.sp);Text(money(list.sumOf{it.resultCents}),color=if(list.sumOf{it.resultCents}>=0)Neon else Loss,fontSize=28.sp,fontWeight=FontWeight.Bold);Text(list.size.toString()+" sessões",color=Muted)}}
  items(list){x->CardGlass(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(x.casinoName.ifBlank{"Cassino"}+" • "+x.slotName.ifBlank{"Slot"},color=Color.White,fontWeight=FontWeight.Bold);Text((if(x.status=="ACTIVE")"Ativa" else "Encerrada")+" • "+SimpleDateFormat("dd/MM HH:mm",Locale("pt","BR")).format(Date(x.startedAt)),color=Muted,fontSize=11.sp)};Text((if(x.resultCents>=0)"+" else "-")+money(kotlin.math.abs(x.resultCents)),color=if(x.resultCents>=0)Neon else Loss,fontWeight=FontWeight.Bold)}}}
 }
}
@Composable private fun Stats(m:Modifier,s:List<SessionEntity>){
 val f=s.filter{it.status=="FINISHED"}.sortedBy{it.startedAt};val p=f.sumOf{it.resultCents};val w=f.count{it.resultCents>0};val wr=if(f.isEmpty())0 else w*100/f.size;val avg=if(f.isEmpty())0 else p/f.size
 val slotGroups=f.filter{it.slotName.isNotBlank()}.groupBy{it.slotName}.mapValues{it.value.sumOf{v->v.resultCents}}.toList().sortedByDescending{it.second}.take(5)
 val casinoGroups=f.filter{it.casinoName.isNotBlank()}.groupBy{it.casinoName}.mapValues{it.value.sumOf{v->v.resultCents}}.toList().sortedByDescending{it.second}.take(5)
 var streak=0;var best=0;f.forEach{x->{if(x.resultCents>0){streak++;best=maxOf(best,streak)}else if(x.resultCents<0)streak=0}}
 LazyColumn(m.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Text("Estatísticas",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Performance da banca",color=Muted)}
  item{CardGlass{Text("EVOLUÇÃO",color=Muted);Chart(f.takeLast(7).map{it.resultCents})}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("LUCRO TOTAL",money(p),Modifier.weight(1f));Metric("WINRATE",wr.toString()+"%",Modifier.weight(1f))}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("MÉDIA/SESSÃO",money(avg),Modifier.weight(1f));Metric("MELHOR STREAK",best.toString(),Modifier.weight(1f))}}
  item{CardGlass{Text("POR SLOT",color=Lime,fontWeight=FontWeight.Bold);if(slotGroups.isEmpty())Text("Ainda sem sessões por slot",color=Muted) else slotGroups.forEach{(name,value)->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,color=Color.White);Text((if(value>=0)"+" else "-")+money(kotlin.math.abs(value)),color=if(value>=0)Neon else Loss,fontWeight=FontWeight.Bold)}}}}
  item{CardGlass{Text("POR CASSINO",color=Lime,fontWeight=FontWeight.Bold);if(casinoGroups.isEmpty())Text("Ainda sem sessões por cassino",color=Muted) else casinoGroups.forEach{(name,value)->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,color=Color.White);Text((if(value>=0)"+" else "-")+money(kotlin.math.abs(value)),color=if(value>=0)Neon else Loss,fontWeight=FontWeight.Bold)}}}}
  item{Metric("SESSÕES ENCERRADAS",f.size.toString(),Modifier.fillMaxWidth())}
 }
}
