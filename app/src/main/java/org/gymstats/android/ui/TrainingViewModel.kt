package org.gymstats.android.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.gymstats.android.data.TrainingRepository
import org.gymstats.android.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
data class TrainingState(val loading: Boolean = true, val busy: Boolean = false, val routines: List<Routine> = emptyList(), val workouts: List<Workout> = emptyList(), val error: Boolean = false)
sealed interface TrainingEvent {
    data object RoutineSaved : TrainingEvent
    data object WorkoutSaved : TrainingEvent
    data object Deleted : TrainingEvent
    data object Failed : TrainingEvent
}
class TrainingViewModel : ViewModel() {
    private var repo: TrainingRepository? = null
    private var subscription: Job? = null
    private var mutation: Job? = null
    private var generation = 0
    private val mutableState = MutableStateFlow(TrainingState())
    val state = mutableState.asStateFlow()
    private val pending = MutableStateFlow<TrainingEvent?>(null)
    val event = pending.asStateFlow()
    fun consumeEvent() { pending.value = null }
    fun connect(repository: TrainingRepository) {
        generation++; subscription?.cancel(); mutation?.cancel(); repo = repository; consumeEvent()
        mutableState.value = TrainingState()
        subscription = viewModelScope.launch {
            combine(repository.routines(), repository.workouts()) { r, w -> r to w }
                .catch { e -> if (e is CancellationException) throw e; mutableState.update { it.copy(loading = false, error = true) } }
                .collect { (r, w) -> mutableState.update { it.copy(loading = false, error = false, routines = r, workouts = w) } }
        }
    }
    fun retry() { if (!state.value.busy) repo?.let(::connect) }
    fun disconnect() { generation++; subscription?.cancel(); mutation?.cancel(); repo = null; mutableState.value = TrainingState(); consumeEvent() }
    private fun change(result: TrainingEvent, action: suspend (TrainingRepository) -> Unit) {
        if (state.value.busy) return
        val repository = repo ?: return
        val operationGeneration = generation
        mutableState.update { it.copy(busy = true) }
        mutation = viewModelScope.launch {
            try { action(repository); if (operationGeneration == generation) pending.value = result }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { if (operationGeneration == generation) pending.value = TrainingEvent.Failed }
            finally { if (operationGeneration == generation) mutableState.update { it.copy(busy = false) } }
        }
    }
    fun save(routine: Routine) = change(TrainingEvent.RoutineSaved) { it.saveRoutine(routine) }
    fun deleteRoutine(id: String) = change(TrainingEvent.Deleted) { it.deleteRoutine(id) }
    fun save(workout: Workout) = change(TrainingEvent.WorkoutSaved) { it.saveWorkout(workout) }
    fun deleteWorkout(id: String) = change(TrainingEvent.Deleted) { it.deleteWorkout(id) }
}
