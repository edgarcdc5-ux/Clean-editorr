package com.cassinopros.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cassinopros.app.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CasinoProsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    val bankroll = db.bankrollDao().observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val sessions = db.sessionDao().observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (bankroll.value == null) {
                db.bankrollDao().save(BankrollEntity())
            }
        }
    }

    fun quickAdjust(cents: Long) = viewModelScope.launch {
        val current = bankroll.value ?: BankrollEntity()
        db.bankrollDao().save(current.copy(currentCents=(current.currentCents+cents).coerceAtLeast(0)))
    }

    fun startSession() = viewModelScope.launch {
        val current = bankroll.value ?: BankrollEntity()
        db.sessionDao().insert(SessionEntity(startingCents=current.currentCents))
    }

    fun finishLatest() = viewModelScope.launch {
        val active = sessions.value.firstOrNull { it.status == "ACTIVE" } ?: return@launch
        val current = bankroll.value ?: BankrollEntity()
        val result = current.currentCents - active.startingCents
        db.sessionDao().finish(active.id, current.currentCents, result, System.currentTimeMillis())
    }
}
