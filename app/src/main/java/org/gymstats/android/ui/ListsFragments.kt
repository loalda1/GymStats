package org.gymstats.android.ui
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.gymstats.android.R
import org.gymstats.android.domain.*
import org.gymstats.android.databinding.*
class DashboardFragment : BindingFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.create.setOnClickListener { findNavController().navigate(R.id.editorFragment) }; binding.retry.setOnClickListener { vm.retry() }
        observeState { state ->
            binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE; binding.retry.visibility = if (state.error) View.VISIBLE else View.GONE
            binding.metrics.visibility = if (state.loading || state.error) View.GONE else View.VISIBLE
            binding.heading.setText(if (state.error) R.string.error else R.string.dashboard)
            binding.metrics.removeAllViews(); val s = Training.stats(state.workouts)
            listOf(R.string.this_week to s.thisWeek.toString(), R.string.this_month to s.thisMonth.toString(), R.string.weekly_volume to number(s.volume), R.string.streak to s.streak.toString()).chunked(2).forEach { pair ->
                val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }; binding.metrics.addView(row)
                pair.forEach { (title, value) -> row.card(value, getString(title)).layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply { setMargins(0, 0, 8, 12) } }
            }
            binding.routines.removeAllViews()
            if (!state.loading && !state.error && state.routines.isEmpty()) binding.routines.label(getString(R.string.empty_routines))
            state.routines.take(2).forEach { r -> binding.routines.card(r.name, getString(R.string.routine_summary, r.exercises.size, r.exercises.sumOf { it.sets })) { findNavController().navigate(R.id.detailFragment, bundleOf("routineId" to r.id)) } }
            binding.recent.removeAllViews()
            if (!state.loading && !state.error && state.workouts.isEmpty()) binding.recent.label(getString(R.string.empty_history))
            state.workouts.take(3).forEach { w -> binding.recent.card(w.routineName, date(w.completedAt) + " · " + number(Training.volume(w)) + " kg") }
        }
    }
}
class RoutinesFragment : BindingFragment<FragmentRoutinesBinding>(FragmentRoutinesBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = RoutineAdapter { findNavController().navigate(R.id.detailFragment, bundleOf("routineId" to it.id)) }
        binding.list.layoutManager = LinearLayoutManager(requireContext()); binding.list.adapter = adapter
        binding.add.setOnClickListener { findNavController().navigate(R.id.editorFragment) }; binding.retry.setOnClickListener { vm.retry() }
        observeState { state ->
            adapter.submitList(state.routines)
            binding.status.visibility = if (state.loading || state.error || state.routines.isEmpty()) View.VISIBLE else View.GONE
            binding.status.setText(when { state.loading -> R.string.loading; state.error -> R.string.error; else -> R.string.empty_routines })
            binding.retry.visibility = if (state.error) View.VISIBLE else View.GONE
        }
    }
    override fun onDestroyView() { binding.list.adapter = null; super.onDestroyView() }
}
class DetailFragment : BindingFragment<FragmentDetailBinding>(FragmentDetailBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = requireArguments().getString("routineId")!!
        binding.edit.setOnClickListener { findNavController().navigate(R.id.editorFragment, bundleOf("routineId" to id)) }
        binding.start.setOnClickListener { findNavController().navigate(R.id.workoutFragment, bundleOf("routineId" to id)) }
        binding.delete.setOnClickListener { confirm { vm.deleteRoutine(id) } }
        observeState { state ->
            val r = state.routines.find { it.id == id }
            binding.start.isEnabled = r?.let(Training::validRoutine) == true && !state.busy; binding.edit.isEnabled = r != null && !state.busy; binding.delete.isEnabled = r != null && !state.busy
            binding.name.text = r?.name ?: getString(R.string.not_found); binding.description.text = listOfNotNull(r?.description, r?.dayOfWeek).filter { it.isNotBlank() }.joinToString("\n")
            binding.exercises.removeAllViews(); r?.exercises?.forEach { binding.exercises.card(it.name, getString(R.string.exercise_summary, it.sets, it.reps, number(it.weight))) }
        }
        observeEvents { when (it) { TrainingEvent.Deleted -> findNavController().popBackStack(); TrainingEvent.Failed -> message(R.string.error); else -> Unit } }
    }
}
class HistoryFragment : BindingFragment<FragmentHistoryBinding>(FragmentHistoryBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.retry.setOnClickListener { vm.retry() }
        observeState { state ->
            binding.entries.removeAllViews(); binding.retry.visibility = if (state.error) View.VISIBLE else View.GONE
            if (state.loading || state.error || state.workouts.isEmpty()) binding.entries.label(getString(when { state.loading -> R.string.loading; state.error -> R.string.error; else -> R.string.empty_history }))
            state.workouts.forEach { w ->
                val card = binding.entries.card(w.routineName, date(w.completedAt) + "\n" + getString(R.string.workout_summary, w.sets.size, number(Training.volume(w)), ((w.completedAt - w.startedAt) / 60000).toInt()))
                val content = card.getChildAt(0) as LinearLayout
                w.sets.groupBy { it.exerciseName }.forEach { (name, sets) -> content.label(name + " · " + sets.joinToString(" / ") { "${it.reps} × ${number(it.weight)} kg" }) }
                content.action(R.string.delete) { confirm { vm.deleteWorkout(w.id) } }.isEnabled = !state.busy
            }
        }
        observeEvents { if (it == TrainingEvent.Failed) message(R.string.error) else if (it == TrainingEvent.Deleted) message(R.string.deleted) }
    }
}
