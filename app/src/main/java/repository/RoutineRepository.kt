package com.example.gymstats.repository

import com.example.gymstats.model.Routine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RoutineRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun logout() {
        auth.signOut()
    }

    fun getRoutines(
        onSuccess: (List<Routine>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("Usuario no autenticado"))
            return
        }

        db.collection("users")
            .document(userId)
            .collection("routines")
            .get()
            .addOnSuccessListener { result ->
                val routines = result.map { document ->
                    document.toObject(Routine::class.java)
                }
                onSuccess(routines)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getRoutineById(
        routineId: String,
        onSuccess: (Routine?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("Usuario no autenticado"))
            return
        }

        db.collection("users")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .get()
            .addOnSuccessListener { document ->
                val routine = document.toObject(Routine::class.java)
                onSuccess(routine)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun createRoutine(
        name: String,
        description: String,
        dayOfWeek: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("Usuario no autenticado"))
            return
        }

        val routineId = db.collection("users")
            .document(userId)
            .collection("routines")
            .document()
            .id

        val routine = Routine(
            id = routineId,
            name = name,
            description = description,
            dayOfWeek = dayOfWeek
        )

        db.collection("users")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .set(routine)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateRoutine(
        routineId: String,
        name: String,
        description: String,
        dayOfWeek: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("Usuario no autenticado"))
            return
        }

        db.collection("users")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .update(
                mapOf(
                    "name" to name,
                    "description" to description,
                    "dayOfWeek" to dayOfWeek
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteRoutine(
        routineId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("Usuario no autenticado"))
            return
        }

        db.collection("users")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}