package ma.bam.inventaire.ui.inventorylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.SessionCounters

data class SessionListItem(
    val session: InventorySessionEntity,
    val counters: SessionCounters
)

@HiltViewModel
class InventoryListViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    /** Sessions (ordre du dépôt : dateCreation DESC) et leurs compteurs, mis à jour à chaque modification d'article. */
    val sessions: StateFlow<List<SessionListItem>> =
        combine(repository.observeSessions(), repository.observeCountersBySession()) { sessions, rows ->
            val counters = rows.associate { it.sessionId to it.counters }
            sessions.map { SessionListItem(it, counters[it.id] ?: SessionCounters.ZERO) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun closeSession(sessionId: String) {
        viewModelScope.launch {
            repository.closeSession(sessionId)
        }
    }

    fun reopenSession(sessionId: String) {
        viewModelScope.launch {
            repository.reopenSession(sessionId)
        }
    }
}
