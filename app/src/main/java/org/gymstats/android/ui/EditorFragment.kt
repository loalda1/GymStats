package org.gymstats.android.ui
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import org.gymstats.android.R
import org.gymstats.android.databinding.FragmentEditorBinding
import org.gymstats.android.domain.*
import org.json.*
import java.util.UUID
class DraftViewModel(private val saved: SavedStateHandle) : ViewModel() {
    var draft: String? get() = saved["draft"]; set(value) { saved["draft"] = value }
}
class EditorFragment : BindingFragment<FragmentEditorBinding>(FragmentEditorBinding::inflate) {
    private val draft: DraftViewModel by viewModels()
    private data class Row(val id: String, val name: TextInputEditText, val sets: TextInputEditText, val reps: TextInputEditText, val weight: TextInputEditText)
    private val rows = mutableListOf<Row>()
    private var createdAt = 0L
    private var routineId = ""
    private var ready = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        routineId = requireArguments().getString("routineId", "")
        val stored = draft.draft?.let(::JSONObject)
        if (stored != null) {
            routineId = stored.getString("id"); createdAt = stored.getLong("createdAt")
            binding.name.setText(stored.getString("name")); binding.description.setText(stored.getString("description")); binding.schedule.setText(stored.getString("schedule"))
            val list = stored.getJSONArray("rows")
            for (i in 0 until list.length()) { val r = list.getJSONObject(i); addRow(r.getString("id"), r.getString("name"), r.getString("sets"), r.getString("reps"), r.getString("weight")) }
            ready = true
        } else if (routineId.isEmpty()) {
            routineId = UUID.randomUUID().toString(); createdAt = System.currentTimeMillis(); addRow(); ready = true
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (vm.state.value.busy) message(R.string.save_pending) else { isEnabled = false; requireActivity().onBackPressedDispatcher.onBackPressed() } }
        })
        binding.addExercise.setOnClickListener { if (rows.size < Training.MAX_EXERCISES) addRow() else message(R.string.invalid_routine) }
        binding.save.setOnClickListener {
            val r = Routine(routineId, binding.name.text.toString().trim(), binding.description.text.toString().trim(), binding.schedule.text.toString().trim(), createdAt, rows.map { Exercise(it.id, it.name.text.toString().trim(), it.sets.text.toString().toIntOrNull() ?: 0, it.reps.text.toString().toIntOrNull() ?: 0, it.weight.text.toString().replace(',', '.').toDoubleOrNull() ?: Double.NaN) })
            if (!Training.validRoutine(r)) { message(R.string.invalid_routine); return@setOnClickListener }; snapshot(); vm.save(r)
        }
        observeState { state ->
            if (!ready && !state.loading) {
                val r = state.routines.find { it.id == routineId }
                if (r != null) {
                    binding.name.setText(r.name); binding.description.setText(r.description); binding.schedule.setText(r.dayOfWeek); createdAt = r.createdAt
                    r.exercises.forEach { addRow(it.id, it.name, it.sets.toString(), it.reps.toString(), it.weight.toString()) }; if (rows.isEmpty()) addRow(); ready = true
                } else binding.status.setText(R.string.not_found)
            }
            binding.save.isEnabled = ready && !state.busy; binding.addExercise.isEnabled = ready && !state.busy; binding.save.setText(if (state.busy) R.string.save_pending else R.string.save)
        }
        observeEvents { if (it == TrainingEvent.RoutineSaved) { draft.draft = null; ready = false; message(R.string.saved); findNavController().popBackStack() } else if (it == TrainingEvent.Failed) message(R.string.error) }
    }
    private fun addRow(id: String = UUID.randomUUID().toString(), name: String = "", sets: String = "3", reps: String = "10", weight: String = "0") {
        val box = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 16, 0, 24) }; binding.exercises.addView(box)
        val row = Row(id, box.input(R.string.exercise_name, name, InputType.TYPE_CLASS_TEXT), box.input(R.string.sets, sets, InputType.TYPE_CLASS_NUMBER, 2), box.input(R.string.reps, reps, InputType.TYPE_CLASS_NUMBER, 3), box.input(R.string.weight, weight, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL, 8))
        rows.add(row); box.action(R.string.remove) { rows.remove(row); binding.exercises.removeView(box) }
    }
    private fun snapshot() {
        if (!ready || view == null) return
        val list = JSONArray(); rows.forEach { list.put(JSONObject().put("id", it.id).put("name", it.name.text.toString()).put("sets", it.sets.text.toString()).put("reps", it.reps.text.toString()).put("weight", it.weight.text.toString())) }
        draft.draft = JSONObject().put("id", routineId).put("createdAt", createdAt).put("name", binding.name.text.toString()).put("description", binding.description.text.toString()).put("schedule", binding.schedule.text.toString()).put("rows", list).toString()
    }
    override fun onPause() { snapshot(); super.onPause() }
    override fun onSaveInstanceState(outState: Bundle) { snapshot(); super.onSaveInstanceState(outState) }
    override fun onDestroyView() { rows.clear(); ready = false; super.onDestroyView() }
}
