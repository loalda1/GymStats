package com.example.gymstats.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gymstats.model.Routine
import com.example.gymstats.repository.RoutineRepository

class RoutineViewModel : ViewModel() {

    private val repository = RoutineRepository()

    private val _routines = MutableLiveData<List<Routine>>()
    val routines: LiveData<List<Routine>> = _routines

    private val _selectedRoutine = MutableLiveData<Routine?>()
    val selectedRoutine: LiveData<Routine?> = _selectedRoutine

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getCurrentUserEmail(): String? {
        return repository.getCurrentUserEmail()
    }

    fun logout() {
        repository.logout()
    }

    fun loadRoutines() {
        repository.getRoutines(
            onSuccess = { routineList ->
                _routines.value = routineList
            },
            onFailure = { exception ->
                _message.value = "Error al cargar rutinas: ${exception.message}"
            }
        )
    }

    fun loadRoutineById(routineId: String) {
        repository.getRoutineById(
            routineId = routineId,
            onSuccess = { routine ->
                _selectedRoutine.value = routine
            },
            onFailure = { exception ->
                _message.value = "Error al cargar rutina: ${exception.message}"
            }
        )
    }

    fun createRoutine(name: String, description: String, dayOfWeek: String) {
        repository.createRoutine(
            name = name,
            description = description,
            dayOfWeek = dayOfWeek,
            onSuccess = {
                _message.value = "Rutina guardada"
            },
            onFailure = { exception ->
                _message.value = "Error: ${exception.message}"
            }
        )
    }

    fun updateRoutine(routineId: String, name: String, description: String, dayOfWeek: String) {
        repository.updateRoutine(
            routineId = routineId,
            name = name,
            description = description,
            dayOfWeek = dayOfWeek,
            onSuccess = {
                _message.value = "Rutina actualizada"
            },
            onFailure = { exception ->
                _message.value = "Error al actualizar: ${exception.message}"
            }
        )
    }

    fun deleteRoutine(routineId: String) {
        repository.deleteRoutine(
            routineId = routineId,
            onSuccess = {
                _message.value = "Rutina eliminada"
                loadRoutines()
            },
            onFailure = { exception ->
                _message.value = "Error al eliminar: ${exception.message}"
            }
        )
    }
}