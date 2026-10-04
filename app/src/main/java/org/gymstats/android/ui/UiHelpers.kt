package org.gymstats.android.ui
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.gymstats.android.R
import org.gymstats.android.MainActivity
import java.text.NumberFormat
import java.time.*
import java.time.format.DateTimeFormatter
val Fragment.gymHost get() = requireActivity() as MainActivity
val Fragment.vm get() = gymHost.training
fun Fragment.observeState(block: (TrainingState) -> Unit) { viewLifecycleOwner.lifecycleScope.launch { viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { vm.state.collect(block) } } }
fun Fragment.observeEvents(block: (TrainingEvent) -> Unit) { viewLifecycleOwner.lifecycleScope.launch { viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) { vm.event.filterNotNull().collect { vm.consumeEvent(); block(it) } } } }
fun Fragment.message(id: Int) { view?.let { Snackbar.make(it, id, Snackbar.LENGTH_LONG).show() } }
fun Fragment.confirm(action: () -> Unit) { MaterialAlertDialogBuilder(requireContext()).setMessage(R.string.delete_prompt).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.delete) { _, _ -> action() }.show() }
fun number(value: Double) = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(value)
fun date(value: Long): String = DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(value))
fun LinearLayout.label(value: String, large: Boolean = false): TextView = TextView(context).also { it.text = value; it.textSize = if (large) 22f else 16f; it.setPadding(0, 12, 0, 12); addView(it) }
fun LinearLayout.action(title: Int, click: () -> Unit) = MaterialButton(context).also { it.setText(title); addView(it); it.setOnClickListener { click() } }
fun LinearLayout.card(title: String, subtitle: String, click: (() -> Unit)? = null): MaterialCardView {
    val d = resources.displayMetrics.density
    val card = MaterialCardView(context).apply { radius = 20 * d; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = (12 * d).toInt() } }
    val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; val pad = (20 * d).toInt(); setPadding(pad, pad, pad, pad) }
    content.label(title, true); content.label(subtitle); card.addView(content); addView(card)
    click?.let { callback -> card.isClickable = true; card.isFocusable = true; card.setOnClickListener { callback() } }
    return card
}
fun LinearLayout.input(hint: Int, value: String, type: Int, limit: Int = 80): TextInputEditText {
    val wrapper = TextInputLayout(context).apply { setHint(hint); layoutParams = LinearLayout.LayoutParams(-1, -2) }
    val field = TextInputEditText(context).apply { inputType = type; filters = arrayOf(android.text.InputFilter.LengthFilter(limit)); setText(value); layoutParams = LinearLayout.LayoutParams(-1, -2) }
    wrapper.addView(field); addView(wrapper); return field
}
