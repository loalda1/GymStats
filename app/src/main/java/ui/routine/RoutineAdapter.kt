package com.example.gymstats.ui.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymstats.R
import com.example.gymstats.model.Routine

class RoutineAdapter(
    private val routines: List<Routine>,
    private val onEditClick: (Routine) -> Unit,
    private val onDeleteClick: (Routine) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewRoutineName: TextView = itemView.findViewById(R.id.textViewRoutineName)
        val textViewRoutineDay: TextView = itemView.findViewById(R.id.textViewRoutineDay)
        val textViewRoutineDescription: TextView = itemView.findViewById(R.id.textViewRoutineDescription)
        val buttonEditRoutine: Button = itemView.findViewById(R.id.buttonEditRoutine)
        val buttonDeleteRoutine: Button = itemView.findViewById(R.id.buttonDeleteRoutine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine, parent, false)

        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]

        holder.textViewRoutineName.text = routine.name
        holder.textViewRoutineDay.text = routine.dayOfWeek
        holder.textViewRoutineDescription.text = routine.description

        holder.buttonEditRoutine.setOnClickListener {
            onEditClick(routine)
        }

        holder.buttonDeleteRoutine.setOnClickListener {
            onDeleteClick(routine)
        }
    }

    override fun getItemCount(): Int {
        return routines.size
    }
}