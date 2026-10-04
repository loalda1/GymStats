package org.gymstats.android
import org.junit.Assert.*
import org.junit.Test
import org.gymstats.android.domain.*
import java.time.*
class TrainingTest {
    private val zone=ZoneId.of("Europe/Madrid")
    private val now=ZonedDateTime.of(2026,9,30,12,0,0,0,zone)
    private fun workout(days:Long=0,weight:Double=50.0):Workout {
        val time=now.minusDays(days).toInstant().toEpochMilli()
        return Workout("w$days","r","Upper",time-60000,time,listOf(WorkoutSet("e","Bench",8,weight)))
    }
    private fun routine()=Routine("r","Upper","","Monday",1,listOf(Exercise("e","Bench",3,8,50.0)))
    @Test fun volumeUsesActualRepsAndLoad(){assertEquals(400.0,Training.volume(workout()),0.0)}
    @Test fun bodyweightVolumeIsZero(){assertEquals(0.0,Training.volume(workout(weight=0.0)),0.0)}
    @Test fun routineRequiresExercise(){assertFalse(Training.validRoutine(routine().copy(exercises=emptyList())))}
    @Test fun invalidNumbersRejected(){for(w in listOf(-1.0,Double.NaN,Double.POSITIVE_INFINITY,1001.0))assertFalse(Training.validRoutine(routine().copy(exercises=listOf(Exercise("e","Bench",3,8,w)))))}
    @Test fun duplicateExerciseIdsRejected(){val e=routine().exercises.first();assertFalse(Training.validRoutine(routine().copy(exercises=listOf(e,e))))}
    @Test fun plannedSetLimitEnforced(){assertFalse(Training.validRoutine(routine().copy(exercises=(1..3).map{Exercise("e$it","Bench",10,8,50.0)})))}
    @Test fun invalidRepsRejected(){assertFalse(Training.validWorkout(workout().copy(sets=listOf(WorkoutSet("e","Bench",0,50.0)))))}
    @Test fun reversedTimesRejected(){val w=workout();assertFalse(Training.validWorkout(w.copy(completedAt=w.startedAt-1)))}
    @Test fun countUsesCalendarWeekAndMonth(){val s=Training.stats(listOf(workout(0),workout(1),workout(3),workout(30)),now.toInstant(),zone);assertEquals(2,s.thisWeek);assertEquals(3,s.thisMonth);assertEquals(800.0,s.volume,0.0)}
    @Test fun multipleSessionsSameDayCountAsOneStreakDay(){assertEquals(3,Training.stats(listOf(workout(),workout(),workout(1),workout(2)),now.toInstant(),zone).streak)}
    @Test fun yesterdayStreakRemainsActive(){assertEquals(2,Training.stats(listOf(workout(1),workout(2)),now.toInstant(),zone).streak)}
    @Test fun missingYesterdayBreaksStreak(){assertEquals(0,Training.stats(listOf(workout(2)),now.toInstant(),zone).streak)}
    @Test fun futureWorkoutsExcluded(){assertEquals(0,Training.stats(listOf(workout(-1)),now.toInstant(),zone).thisMonth)}
    @Test fun weeklyChartHasSevenBuckets(){assertEquals(7,Training.stats(emptyList(),now.toInstant(),zone).weekly.size)}
    @Test fun progressUsesMaximumPerSession(){val w=workout().copy(sets=listOf(WorkoutSet("e","Bench",8,40.0),WorkoutSet("e","Bench",6,60.0)));assertEquals(60.0,Training.progress(listOf(w),"bench").single().second,0.0)}
    @Test fun reminderSelectsNextDay(){assertEquals(DayOfWeek.FRIDAY,ReminderTime.next(now,setOf(5),18,0).dayOfWeek)}
    @Test fun reminderAfterTimeMovesToNextWeek(){assertEquals(now.toLocalDate().plusWeeks(1),ReminderTime.next(now,setOf(3),10,0).toLocalDate())}
    @Test fun reminderHandlesDstGap(){val before=ZonedDateTime.of(2026,3,28,12,0,0,0,zone);assertEquals(3,ReminderTime.next(before,setOf(7),2,30).hour)}
}
