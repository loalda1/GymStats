package com.example.gymstats.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymstats.R
import com.example.gymstats.model.Routine
import com.example.gymstats.ui.routine.RoutineAdapter
import com.example.gymstats.viewmodel.RoutineViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymstats.worker.WorkoutReminderWorker
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var routineAdapter: RoutineAdapter
    private val routines = mutableListOf<Routine>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewUserEmail = view.findViewById<TextView>(R.id.textViewUserEmail)
        val buttonAddRoutine = view.findViewById<Button>(R.id.buttonAddRoutine)
        val buttonLogout = view.findViewById<Button>(R.id.buttonLogout)
        val recyclerViewRoutines = view.findViewById<RecyclerView>(R.id.recyclerViewRoutines)
        val buttonReminder = view.findViewById<Button>(R.id.buttonReminder)
        val textViewRoutineStats = view.findViewById<TextView>(R.id.textViewRoutineStats)

        buttonReminder.setOnClickListener {
            scheduleWorkoutReminder()
        }

        textViewUserEmail.text =
            routineViewModel.getCurrentUserEmail() ?: "Usuario no identificado"

        routineAdapter = RoutineAdapter(
            routines,
            onEditClick = { routine ->
                val bundle = Bundle().apply {
                    putString("routineId", routine.id)
                }

                findNavController().navigate(
                    R.id.action_homeFragment_to_addRoutineFragment,
                    bundle
                )
            },
            onDeleteClick = { routine ->
                routineViewModel.deleteRoutine(routine.id)
            }
        )

        recyclerViewRoutines.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRoutines.adapter = routineAdapter

        routineViewModel.routines.observe(viewLifecycleOwner) { routineList ->
            routines.clear()
            routines.addAll(routineList)
            routineAdapter.notifyDataSetChanged()

            textViewRoutineStats.text = "Rutinas creadas: ${routineList.size}"
        }

        routineViewModel.message.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        routineViewModel.loadRoutines()

        buttonAddRoutine.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addRoutineFragment)
        }

        buttonLogout.setOnClickListener {
            routineViewModel.logout()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun scheduleWorkoutReminder() {
        val workRequest = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(requireContext())
            .enqueue(workRequest)

        Toast.makeText(
            requireContext(),
            "Recordatorio activado. Recibirás una notificación en 10 segundos.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onResume() {
        super.onResume()
        routineViewModel.loadRoutines()
    }
}