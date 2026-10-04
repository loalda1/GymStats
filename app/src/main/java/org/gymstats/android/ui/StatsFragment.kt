package org.gymstats.android.ui
import android.os.Bundle
import android.view.View
import android.widget.*
import org.gymstats.android.R
import org.gymstats.android.databinding.FragmentStatsBinding
import org.gymstats.android.domain.*
import java.time.*
import java.time.format.DateTimeFormatter
class StatsFragment : BindingFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {
    private var selected = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selected = savedInstanceState?.getString("selected", "") ?: selected; binding.progressChart.bars = false
        observeState { state ->
            val stats = Training.stats(state.workouts); val format = DateTimeFormatter.ofPattern("dd/MM")
            binding.volumeChart.points = stats.weekly.map { it.date.format(format) to it.volume }
            val names = state.workouts.flatMap { it.sets }.map { it.exerciseName.lowercase(java.util.Locale.ROOT) }.distinct().sorted()
            if (selected !in names) selected = names.firstOrNull().orEmpty()
            binding.exercise.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
            if (selected in names) binding.exercise.setSelection(names.indexOf(selected))
            fun chart() { binding.progressChart.points = Training.progress(state.workouts, selected).takeLast(30).map { Instant.ofEpochMilli(it.first).atZone(ZoneId.systemDefault()).format(format) to it.second } }
            chart(); binding.exercise.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { if (position in names.indices) { selected = names[position]; chart() } }
            }
            binding.summary.removeAllViews()
            if (state.error || state.loading || state.workouts.isEmpty()) binding.summary.label(getString(when { state.error -> R.string.error; state.loading -> R.string.loading; else -> R.string.empty_history }))
            if (state.error) binding.summary.action(R.string.retry) { vm.retry() }
            binding.summary.card(stats.thisMonth.toString(), getString(R.string.this_month))
        }
    }
    override fun onSaveInstanceState(outState: Bundle) { outState.putString("selected", selected); super.onSaveInstanceState(outState) }
}
