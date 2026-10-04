package org.gymstats.android.ui
import android.view.*
import androidx.recyclerview.widget.*
import org.gymstats.android.R
import org.gymstats.android.domain.Routine
import org.gymstats.android.databinding.ItemRoutineBinding
class RoutineAdapter(private val open: (Routine) -> Unit) : ListAdapter<Routine, RoutineAdapter.Holder>(object : DiffUtil.ItemCallback<Routine>() {
    override fun areItemsTheSame(old: Routine, new: Routine) = old.id == new.id
    override fun areContentsTheSame(old: Routine, new: Routine) = old == new
}) {
    class Holder(val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, type: Int) = Holder(ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val r = getItem(position); val b = holder.binding
        b.name.text = r.name; b.summary.text = b.root.context.getString(R.string.routine_summary, r.exercises.size, r.exercises.sumOf { it.sets }); b.schedule.text = r.dayOfWeek
        b.root.setOnClickListener { open(r) }
    }
}
