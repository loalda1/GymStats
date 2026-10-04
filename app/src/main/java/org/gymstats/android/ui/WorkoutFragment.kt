package org.gymstats.android.ui
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import org.gymstats.android.R
import org.gymstats.android.databinding.FragmentWorkoutBinding
import org.gymstats.android.domain.*
import org.json.*
import java.util.UUID
class WorkoutFragment : BindingFragment<FragmentWorkoutBinding>(FragmentWorkoutBinding::inflate) {
    private val draft: DraftViewModel by viewModels()
    private data class SetRow(val exerciseId: String, val name: String, val reps: TextInputEditText, val weight: TextInputEditText)
    private val rows = mutableListOf<SetRow>()
    private var workoutId = ""
    private var routineId = ""
    private var routineName = ""
    private var started = 0L
    private var restDeadline = 0L
    private var ready = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        routineId = requireArguments().getString("routineId")!!
        draft.draft?.let { value ->
            val obj = JSONObject(value); workoutId = obj.getString("id"); routineName = obj.getString("name"); started = obj.getLong("started"); restDeadline = obj.optLong("rest", 0)
            val sets = obj.getJSONArray("sets"); for (i in 0 until sets.length()) { val s = sets.getJSONObject(i); addSet(s.getString("exerciseId"), s.getString("name"), s.getString("reps"), s.getString("weight"), i + 1) }; ready = true
        }
        binding.rest.setOnClickListener { restDeadline = System.currentTimeMillis() + 60000; snapshot() }
        viewLifecycleOwner.lifecycleScope.launch { while (true) { val seconds = ((restDeadline - System.currentTimeMillis()) / 1000).coerceAtLeast(0); binding.rest.text = if (seconds > 0) getString(R.string.rest_remaining, seconds.toInt()) else getString(R.string.rest); delay(500) } }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vm.state.value.busy) { message(R.string.save_pending); return }
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.leave_title).setMessage(R.string.leave_message).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.discard) { _, _ -> ready = false; draft.draft = null; findNavController().popBackStack() }.show()
            }
        })
        binding.finish.setOnClickListener {
            val w = Workout(workoutId, routineId, routineName, started, System.currentTimeMillis(), rows.map { WorkoutSet(it.exerciseId, it.name, it.reps.text.toString().toIntOrNull() ?: 0, it.weight.text.toString().replace(',', '.').toDoubleOrNull() ?: Double.NaN) })
            if (!Training.validWorkout(w)) { message(R.string.invalid_workout); return@setOnClickListener }; snapshot(); vm.save(w)
        }
        observeState { state ->
            if (!ready && !state.loading) {
                val r = state.routines.find { it.id == routineId }
                if (r?.let(Training::validRoutine) == true) {
                    routineName = r.name; workoutId = UUID.randomUUID().toString(); started = System.currentTimeMillis()
                    r.exercises.forEach { e -> repeat(e.sets) { addSet(e.id, e.name, e.reps.toString(), e.weight.toString(), it + 1) } }; ready = true
                } else binding.heading.setText(R.string.not_found)
            }
            if (ready) binding.heading.text = routineName
            binding.finish.isEnabled = ready && !state.busy; binding.finish.setText(if (state.busy) R.string.save_pending else R.string.finish)
        }
        observeEvents { if (it == TrainingEvent.WorkoutSaved) { ready = false; draft.draft = null; message(R.string.saved); findNavController().popBackStack() } else if (it == TrainingEvent.Failed) message(R.string.error) }
    }
    private fun addSet(id: String, name: String, reps: String, weight: String, index: Int) {
        val box = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 12, 0, 16) }; binding.sets.addView(box)
        box.label(name + " · " + getString(R.string.set_number, index)); rows.add(SetRow(id, name, box.input(R.string.reps, reps, InputType.TYPE_CLASS_NUMBER, 3), box.input(R.string.weight, weight, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL, 8)))
    }
    private fun snapshot() {
        if (!ready || view == null) return
        val list = JSONArray(); rows.forEach { list.put(JSONObject().put("exerciseId", it.exerciseId).put("name", it.name).put("reps", it.reps.text.toString()).put("weight", it.weight.text.toString())) }
        draft.draft = JSONObject().put("id", workoutId).put("name", routineName).put("started", started).put("rest", restDeadline).put("sets", list).toString()
    }
    override fun onPause() { snapshot(); super.onPause() }
    override fun onSaveInstanceState(outState: Bundle) { snapshot(); super.onSaveInstanceState(outState) }
    override fun onDestroyView() { rows.clear(); ready = false; super.onDestroyView() }
}
