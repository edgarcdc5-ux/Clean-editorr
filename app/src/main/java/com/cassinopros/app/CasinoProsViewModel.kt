package com.cassinopros.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cassinopros.app.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CasinoProsViewModel(app:Application):AndroidViewModel(app){
    private val db=AppDatabase.get(app)
    val bankroll=db.bankrollDao().observe().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),null)
    val sessions=db.sessionDao().observeAll().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val slots=db.slotDao().observeAll().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())

    init{viewModelScope.launch{if(bankroll.value==null)db.bankrollDao().save(BankrollEntity())}}
    fun quickAdjust(cents:Long)=viewModelScope.launch{
        val b=bankroll.value?:BankrollEntity()
        db.bankrollDao().save(b.copy(currentCents=(b.currentCents+cents).coerceAtLeast(0),updatedAt=System.currentTimeMillis()))
    }
    fun startSession(casino:String="",pkg:String="",slot:String="",stopLoss:Long=5000,target:Long=10000,stake:Long=100)=viewModelScope.launch{
        val b=bankroll.value?:BankrollEntity()
        if(sessions.value.none{it.status=="ACTIVE"})db.sessionDao().insert(SessionEntity(casinoName=casino,casinoPackage=pkg,slotName=slot,startingCents=b.currentCents,stopLossCents=stopLoss,targetCents=target,stakeCents=stake))
    }
    fun registerResult(cents:Long)=viewModelScope.launch{
        val b=bankroll.value?:return@launch
        val active=sessions.value.firstOrNull{it.status=="ACTIVE"}?:return@launch
        val next=(b.currentCents+cents).coerceAtLeast(0)
        db.bankrollDao().save(b.copy(currentCents=next,updatedAt=System.currentTimeMillis()))
        val result=next-active.startingCents
        db.sessionDao().register(active.id,if(cents<0)-cents else cents,if(cents>0)cents else 0,result)
        if(result >= active.targetCents || result <= -active.stopLossCents){
            db.sessionDao().finish(active.id,next,result,System.currentTimeMillis())
        }
    }
    fun finishLatest()=viewModelScope.launch{
        val active=sessions.value.firstOrNull{it.status=="ACTIVE"}?:return@launch
        val b=bankroll.value?:return@launch
        db.sessionDao().finish(active.id,b.currentCents,b.currentCents-active.startingCents,System.currentTimeMillis())
    }
    fun setReservePercent(percent:Int)=viewModelScope.launch{
        val b=bankroll.value?:return@launch
        val total=(b.currentCents+b.reserveCents).coerceAtLeast(0)
        val reserve=(total*percent.coerceIn(0,100))/100
        db.bankrollDao().save(b.copy(currentCents=total-reserve,reserveCents=reserve,updatedAt=System.currentTimeMillis()))
    }
    fun deleteSlot(slot:SlotEntity)=viewModelScope.launch{db.slotDao().delete(slot)}
    fun updateSlot(slot:SlotEntity)=viewModelScope.launch{db.slotDao().update(slot)}
    fun addSlot(name:String,provider:String,casino:String,stakeCents:Long=100)=viewModelScope.launch{
        if(name.isNotBlank())db.slotDao().insert(SlotEntity(name=name.trim(),provider=provider.trim(),casinoName=casino.trim(),defaultStakeCents=stakeCents.coerceAtLeast(0)))
    }
}