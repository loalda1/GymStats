package org.gymstats.android.ui
import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.LocaleListCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.timepicker.*
import org.gymstats.android.R
import org.gymstats.android.databinding.FragmentSettingsBinding
import org.gymstats.android.worker.ReminderScheduler
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
class SettingsFragment : BindingFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    private var hour = 18
    private var minute = 0
    private val boxes = mutableListOf<MaterialCheckBox>()
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { allowed -> if (view != null) { if (allowed) saveReminder() else message(R.string.notification_denied) } }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val appearance = requireContext().getSharedPreferences("appearance", Context.MODE_PRIVATE)
        binding.account.text = if (gymHost.demo) getString(R.string.demo_account) else gymHost.auth?.currentUser?.email
        fun selector(spinner: Spinner, labels: List<String>, selected: Int, onChange: (Int) -> Unit) {
            spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels); spinner.setSelection(selected)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { onChange(position) }
            }
        }
        val languages = listOf("", "en", "es")
        selector(binding.language, listOf(getString(R.string.language_system), "English", "Español"), languages.indexOf(appearance.getString("language", "")).coerceAtLeast(0)) { index ->
            val code = languages[index]; if (code != appearance.getString("language", "")) { appearance.edit().putString("language", code).apply(); AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code)) }
        }
        val themes = listOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES)
        selector(binding.theme, listOf(getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)), themes.indexOf(appearance.getInt("theme", -1)).coerceAtLeast(0)) { index ->
            if (themes[index] != appearance.getInt("theme", -1)) { appearance.edit().putInt("theme", themes[index]).apply(); AppCompatDelegate.setDefaultNightMode(themes[index]) }
        }
        val uid = gymHost.auth?.currentUser?.uid; val p = uid?.let { ReminderScheduler.preferences(requireContext(), it) }
        hour = savedInstanceState?.getInt("hour") ?: p?.getInt("hour", 18) ?: 18; minute = savedInstanceState?.getInt("minute") ?: p?.getInt("minute", 0) ?: 0
        binding.enabled.isChecked = savedInstanceState?.getBoolean("enabled") ?: p?.getBoolean("enabled", false) ?: false
        val selected = savedInstanceState?.getStringArrayList("days")?.toSet() ?: p?.getStringSet("days", emptySet()) ?: emptySet()
        for (day in 1..7) { val box = MaterialCheckBox(requireContext()).apply { text = DayOfWeek.of(day).getDisplayName(TextStyle.FULL, resources.configuration.locales[0]); isChecked = day.toString() in selected }; boxes.add(box); binding.days.addView(box) }
        showTime()
        binding.time.setOnClickListener {
            val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(hour).setMinute(minute).setTitleText(R.string.choose_time).build()
            picker.addOnPositiveButtonClickListener { if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) { hour = picker.hour; minute = picker.minute; showTime() } }; picker.show(childFragmentManager, "time")
        }
        binding.save.setOnClickListener {
            if (gymHost.demo || uid == null) { message(R.string.demo_reminder); return@setOnClickListener }
            if (binding.enabled.isChecked && !NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS) else message(R.string.notification_denied)
            } else saveReminder()
        }
        binding.logout.setOnClickListener { gymHost.logout() }
    }
    private fun showTime() { binding.time.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute) }
    private fun saveReminder() {
        val uid = gymHost.auth?.currentUser?.uid ?: return
        val days = boxes.mapIndexedNotNull { i, box -> if (box.isChecked) (i + 1).toString() else null }.toSet()
        if (binding.enabled.isChecked && days.isEmpty()) { message(R.string.days_required); return }
        ReminderScheduler.preferences(requireContext(), uid).edit().putBoolean("enabled", binding.enabled.isChecked).putStringSet("days", days).putInt("hour", hour).putInt("minute", minute).apply()
        ReminderScheduler.restore(requireContext(), uid, replace = true); message(R.string.saved)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        if (view != null) { outState.putInt("hour", hour); outState.putInt("minute", minute); outState.putBoolean("enabled", binding.enabled.isChecked); outState.putStringArrayList("days", ArrayList(boxes.mapIndexedNotNull { i, box -> if (box.isChecked) (i + 1).toString() else null })) }
        super.onSaveInstanceState(outState)
    }
    override fun onDestroyView() { boxes.clear(); super.onDestroyView() }
}
