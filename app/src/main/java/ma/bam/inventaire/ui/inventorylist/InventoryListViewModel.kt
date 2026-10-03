package ma.bam.inventaire.ui.inventorylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.SessionCounters
import javax.inject.Inject

data class SessionListItem(
    val session: InventorySessionEntity,
    val counters: SessionCounters
)

@HiltViewModel
class InventoryListViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    val sessions: StateFlow<List<SessionListItem>> = repository.observeSessions()
        .map { sessions -> sessions.map { SessionListItem(it, repository.getSessionCounters(it.id)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }
}
