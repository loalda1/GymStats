package org.gymstats.android.data

import org.gymstats.android.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.*

interface TrainingRepository {
    fun routines(): Flow<List<Routine>>
    fun workouts(): Flow<List<Workout>>
    suspend fun saveRoutine(routine: Routine)
    suspend fun deleteRoutine(id: String)
    suspend fun saveWorkout(workout: Workout)
    suspend fun deleteWorkout(id: String)
}
@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreTrainingRepository(private val db: FirebaseFirestore, private val uid: String) : TrainingRepository {
    private fun collection(name: String) = db.collection("users").document(uid).collection(name)
    override fun routines(): Flow<List<Routine>> = callbackFlow {
        val listener = collection("routines").addSnapshotListener { snapshot, error ->
            if (error != null) close(error)
            else try { trySend(snapshot!!.documents.map { it.toObject(Routine::class.java)!!.copy(id = it.id) }.sortedByDescending { it.createdAt }) } catch (e: Exception) { close(e) }
        }
        awaitClose { listener.remove() }
    }
    override fun workouts(): Flow<List<Workout>> = callbackFlow<List<DocumentSnapshot>> {
        val listener = collection("workouts").addSnapshotListener { snapshot, error ->
            if (error != null) close(error) else trySend(snapshot!!.documents)
        }
        awaitClose { listener.remove() }
    }.mapLatest { documents ->
        coroutineScope { documents.map { d -> async {
            val chunks = d.reference.collection("chunks").get().await().documents.sortedBy { it.id.toInt() }
            val sets = chunks.flatMap { c ->
                @Suppress("UNCHECKED_CAST")
                val rows = c.get("sets") as List<Map<String, Any>>
                rows.map { WorkoutSet(it["exerciseId"] as String, it["exerciseName"] as String, (it["reps"] as Number).toInt(), (it["weight"] as Number).toDouble()) }
            }
            check(sets.size == d.getLong("setCount")!!.toInt() && chunks.size == d.getLong("chunkCount")!!.toInt())
            Workout(d.id, d.getString("routineId")!!, d.getString("routineName")!!, d.getLong("startedAt")!!, d.getLong("completedAt")!!, sets)
        } }.awaitAll().sortedByDescending { it.completedAt } }
    }
    override suspend fun saveRoutine(routine: Routine) {
        require(Training.validRoutine(routine))
        collection("routines").document(routine.id).set(routine).await()
    }
    override suspend fun deleteRoutine(id: String) { collection("routines").document(id).delete().await() }
    override suspend fun saveWorkout(workout: Workout) {
        require(Training.validWorkout(workout))
        val ref = collection("workouts").document(workout.id)
        val chunks = workout.sets.chunked(5)
        val batch = db.batch()
        batch.set(ref, mapOf("id" to workout.id, "routineId" to workout.routineId, "routineName" to workout.routineName, "startedAt" to workout.startedAt, "completedAt" to workout.completedAt, "setCount" to workout.sets.size, "chunkCount" to chunks.size))
        chunks.forEachIndexed { i, sets -> batch.set(ref.collection("chunks").document(i.toString()), mapOf("sets" to sets)) }
        batch.commit().await()
    }
    override suspend fun deleteWorkout(id: String) {
        val ref = collection("workouts").document(id)
        val chunks = ref.collection("chunks").get().await()
        val batch = db.batch()
        chunks.forEach { batch.delete(it.reference) }; batch.delete(ref); batch.commit().await()
    }
}
/** Explicit sample data, isolated from Firebase; never used as a fallback on cloud errors. */
class DemoTrainingRepository(seed: Boolean = true) : TrainingRepository {
    private val rs = MutableStateFlow<List<Routine>>(emptyList())
    private val ws = MutableStateFlow<List<Workout>>(emptyList())
    init { if (seed) {
        val exercises = listOf(Exercise("bench", "Bench press", 3, 8, 50.0), Exercise("row", "Barbell row", 3, 10, 40.0))
        val r = Routine("demo-upper", "Upper body", "Controlled repetitions. Consistent progress.", "Monday / Thursday", System.currentTimeMillis(), exercises)
        rs.value = listOf(r)
        val now = java.time.ZonedDateTime.now()
        ws.value = (0..7).map { i ->
            val time = now.minusDays((i * 3 + 1).toLong()).toInstant().toEpochMilli()
            Workout("demo-$i", r.id, r.name, time - 2400000, time, exercises.flatMap { e -> List(e.sets) { WorkoutSet(e.id, e.name, e.reps, e.weight - i) } })
        }
    } }
    override fun routines() = rs.asStateFlow()
    override fun workouts() = ws.asStateFlow()
    override suspend fun saveRoutine(routine: Routine) { require(Training.validRoutine(routine)); rs.value = listOf(routine) + rs.value.filterNot { it.id == routine.id } }
    override suspend fun deleteRoutine(id: String) { rs.value = rs.value.filterNot { it.id == id } }
    override suspend fun saveWorkout(workout: Workout) { require(Training.validWorkout(workout)); ws.value = listOf(workout) + ws.value.filterNot { it.id == workout.id } }
    override suspend fun deleteWorkout(id: String) { ws.value = ws.value.filterNot { it.id == id } }
}
