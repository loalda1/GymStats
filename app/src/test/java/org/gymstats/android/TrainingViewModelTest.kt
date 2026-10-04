package org.gymstats.android
import org.junit.*
import org.junit.Assert.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.gymstats.android.data.*
import org.gymstats.android.domain.*
import org.gymstats.android.ui.*
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    @Before fun setup(){Dispatchers.setMain(dispatcher)}
    @After fun cleanup(){Dispatchers.resetMain()}
    private val r=Routine("r","Upper","","",1,listOf(Exercise("e","Bench",3,8,50.0)))
    @Test fun successfulSaveProducesTypedEventAndData()=runTest(dispatcher){val vm=TrainingViewModel();vm.connect(DemoTrainingRepository(false));runCurrent();vm.save(r);runCurrent();assertEquals(TrainingEvent.RoutineSaved,vm.event.value);assertEquals(listOf(r),vm.state.value.routines);assertFalse(vm.state.value.busy);vm.disconnect()}
    @Test fun failureDoesNotPretendToSucceed()=runTest(dispatcher){val vm=TrainingViewModel();vm.connect(DemoTrainingRepository(false));runCurrent();vm.save(r.copy(name=""));runCurrent();assertEquals(TrainingEvent.Failed,vm.event.value);assertTrue(vm.state.value.routines.isEmpty());assertFalse(vm.state.value.busy);vm.disconnect()}
    @Test fun disconnectClearsDataAndEvents()=runTest(dispatcher){val vm=TrainingViewModel();vm.connect(DemoTrainingRepository());runCurrent();vm.disconnect();runCurrent();assertTrue(vm.state.value.routines.isEmpty());assertNull(vm.event.value)}
    @Test fun deletingRoutinePreservesSessionSnapshot()=runTest(dispatcher){val repo=DemoTrainingRepository();val w=repo.workouts().first().first();repo.deleteRoutine(w.routineId);assertTrue(repo.routines().first().isEmpty());assertTrue(repo.workouts().first().contains(w))}
    @Test fun listenerFailureAndRetry()=runTest(dispatcher){
        val repo=object:TrainingRepository {
            var fail=true
            override fun routines():Flow<List<Routine>> = flow{if(fail)error("denied")else emit(listOf(r))}
            override fun workouts()=flowOf(emptyList<Workout>())
            override suspend fun saveRoutine(routine:Routine){};override suspend fun deleteRoutine(id:String){}
            override suspend fun saveWorkout(workout:Workout){};override suspend fun deleteWorkout(id:String){}
        }
        val vm=TrainingViewModel();vm.connect(repo);runCurrent();assertTrue(vm.state.value.error);repo.fail=false;vm.retry();runCurrent();assertFalse(vm.state.value.error);assertEquals(listOf(r),vm.state.value.routines);vm.disconnect()
    }
}
