package org.gymstats.android.ui
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
abstract class BindingFragment<B : ViewBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> B) : Fragment() {
    private var current: B? = null
    protected val binding: B get() = checkNotNull(current)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View = inflate(inflater, container, false).also { current = it }.root
    override fun onDestroyView() { current = null; super.onDestroyView() }
}
