package org.gymstats.android.domain

import java.time.*
import java.util.UUID

data class Exercise(val id: String = UUID.randomUUID().toString(), val name: String = "", val sets: Int = 3, val reps: Int = 10, val weight: Double = 0.0)
data class Routine(val id: String = "", val name: String = "", val description: String = "", val dayOfWeek: String = "", val createdAt: Long = 0, val exercises: List<Exercise> = emptyList())
data class WorkoutSet(val exerciseId: String = "", val exerciseName: String = "", val reps: Int = 0, val weight: Double = 0.0)
data class Workout(val id: String = "", val routineId: String = "", val routineName: String = "", val startedAt: Long = 0, val completedAt: Long = 0, val sets: List<WorkoutSet> = emptyList())
data class WeeklyVolume(val date: LocalDate, val volume: Double)
data class Stats(val thisWeek: Int, val thisMonth: Int, val volume: Double, val streak: Int, val weekly: List<WeeklyVolume>)

object Training {
    const val MAX_EXERCISES = 6
    const val MAX_SETS = 20
    fun validExercise(e: Exercise) = e.id.length in 1..80 && e.name.trim().length in 1..80 && e.sets in 1..10 && e.reps in 1..100 && e.weight.isFinite() && e.weight in 0.0..1000.0
    fun validRoutine(r: Routine) = r.id.length in 1..80 && r.name.trim().length in 1..80 && r.description.length <= 500 && r.dayOfWeek.length <= 40 && r.createdAt > 0 && r.exercises.size in 1..MAX_EXERCISES && r.exercises.all(::validExercise) && r.exercises.sumOf { it.sets } <= MAX_SETS && r.exercises.map { it.id }.distinct().size == r.exercises.size
    fun validWorkout(w: Workout) = w.id.length in 1..80 && w.routineId.length in 1..80 && w.routineName.length in 1..80 && w.startedAt > 0 && w.completedAt >= w.startedAt && w.sets.size in 1..MAX_SETS && w.sets.all { it.exerciseId.length in 1..80 && it.exerciseName.length in 1..80 && it.reps in 1..100 && it.weight.isFinite() && it.weight in 0.0..1000.0 }
    fun volume(w: Workout) = w.sets.sumOf { it.reps * it.weight }
    fun stats(workouts: List<Workout>, now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): Stats {
        val today = now.atZone(zone).toLocalDate()
        val valid = workouts.filter { it.completedAt > 0 && it.completedAt <= now.toEpochMilli() }
        fun date(w: Workout) = Instant.ofEpochMilli(w.completedAt).atZone(zone).toLocalDate()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val weekly = (6 downTo 0).map { offset ->
            val start = monday.minusWeeks(offset.toLong())
            WeeklyVolume(start, valid.filter { date(it) >= start && date(it) < start.plusWeeks(1) }.sumOf(::volume))
        }
        val dates = valid.map(::date).toSet()
        var cursor = if (today in dates) today else today.minusDays(1)
        var streak = 0
        while (cursor in dates) { streak++; cursor = cursor.minusDays(1) }
        return Stats(valid.count { date(it) >= monday }, valid.count { YearMonth.from(date(it)) == YearMonth.from(today) }, weekly.last().volume, streak, weekly)
    }
    fun progress(workouts: List<Workout>, name: String): List<Pair<Long, Double>> = workouts.sortedBy { it.completedAt }.mapNotNull { w ->
        w.sets.filter { it.exerciseName.equals(name, true) }.maxOfOrNull { it.weight }?.let { w.completedAt to it }
    }
}
object ReminderTime {
    fun next(now: ZonedDateTime, days: Set<Int>, hour: Int, minute: Int): ZonedDateTime {
        require(days.isNotEmpty() && days.all { it in 1..7 } && hour in 0..23 && minute in 0..59)
        return (0L..7L).map { now.toLocalDate().plusDays(it).atTime(hour, minute).atZone(now.zone) }.first { it.dayOfWeek.value in days && it.isAfter(now) }
    }
}
