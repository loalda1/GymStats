package com.example.gymstats.ui.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gymstats.R
import com.example.gymstats.viewmodel.RoutineViewModel

class AddRoutineFragment : Fragment() {

    private val routineViewModel: RoutineViewModel by viewModels()

    private var routineId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routineId = arguments?.getString("routineId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewTitle = view.findViewById<TextView>(R.id.textViewAddRoutineTitle)
        val editTextRoutineName = view.findViewById<EditText>(R.id.editTextRoutineName)
        val editTextRoutineDescription = view.findViewById<EditText>(R.id.editTextRoutineDescription)
        val editTextRoutineDay = view.findViewById<EditText>(R.id.editTextRoutineDay)
        val buttonSaveRoutine = view.findViewById<Button>(R.id.buttonSaveRoutine)

        val isEditMode = routineId.isNotEmpty()

        if (isEditMode) {
            textViewTitle.text = "Editar rutina"
            buttonSaveRoutine.text = "Guardar cambios"
            routineViewModel.loadRoutineById(routineId)
        } else {
            textViewTitle.text = "Añadir rutina"
            buttonSaveRoutine.text = "Guardar rutina"
        }

        routineViewModel.selectedRoutine.observe(viewLifecycleOwner) { routine ->
            if (routine != null) {
                editTextRoutineName.setText(routine.name)
                editTextRoutineDescription.setText(routine.description)
                editTextRoutineDay.setText(routine.dayOfWeek)
            }
        }

        routineViewModel.message.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            if (
                message == "Rutina guardada" ||
                message == "Rutina actualizada"
            ) {
                findNavController().navigateUp()
            }
        }

        buttonSaveRoutine.setOnClickListener {
            val name = editTextRoutineName.text.toString().trim()
            val description = editTextRoutineDescription.text.toString().trim()
            val dayOfWeek = editTextRoutineDay.text.toString().trim()

            if (name.isEmpty() || dayOfWeek.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Rellena al menos el nombre y el día",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                routineViewModel.updateRoutine(
                    routineId = routineId,
                    name = name,
                    description = description,
                    dayOfWeek = dayOfWeek
                )
            } else {
                routineViewModel.createRoutine(
                    name = name,
                    description = description,
                    dayOfWeek = dayOfWeek
                )
            }
        }
    }
}