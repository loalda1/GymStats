package org.gymstats.android
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import kotlinx.coroutines.launch
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.gymstats.android.data.*
import org.gymstats.android.databinding.ActivityMainBinding
import org.gymstats.android.ui.TrainingViewModel
import org.gymstats.android.worker.ReminderScheduler
class SessionViewModel : ViewModel() {
    var repository: TrainingRepository? = null
    var demo = false
    var uid: String? = null
}
class MainActivity : AppCompatActivity() {
    val training: TrainingViewModel by viewModels()
    private val session: SessionViewModel by viewModels()
    var auth: AuthRepository? = null
        private set
    val demo get() = session.demo
    private lateinit var binding: ActivityMainBinding
    private val nav get() = (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
    private val authListener = FirebaseAuth.AuthStateListener { firebase ->
        if (!demo && firebase.currentUser != null && session.uid != firebase.currentUser?.uid) authenticated()
        else if (!demo && firebase.currentUser == null && session.uid != null) logout()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val p = getSharedPreferences("appearance", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(p.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(p.getString("language", "")!!))
        binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom); insets
        }
        setSupportActionBar(binding.toolbar)
        if (FirebaseApp.getApps(this).isNotEmpty() || FirebaseApp.initializeApp(this) != null) auth = AuthRepository(FirebaseAuth.getInstance())
        val top = setOf(R.id.dashboardFragment, R.id.routinesFragment, R.id.historyFragment, R.id.statsFragment, R.id.settingsFragment, R.id.loginFragment)
        binding.toolbar.setupWithNavController(nav, AppBarConfiguration(top)); binding.bottomNav.setupWithNavController(nav)
        nav.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = if (destination.id in top && destination.id != R.id.loginFragment) View.VISIBLE else View.GONE
            binding.demoBanner.visibility = if (demo && destination.id != R.id.loginFragment) View.VISIBLE else View.GONE
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        if (session.repository == null && auth?.currentUser != null) connectCloud()
        if (session.repository != null && nav.currentDestination?.id == R.id.loginFragment) enterHome()
        if (session.repository == null && nav.currentDestination?.id != R.id.loginFragment) nav.navigate(R.id.loginFragment, null, NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
        if (auth?.currentUser != null) ReminderScheduler.restore(this, auth!!.currentUser!!.uid)
    }
    override fun onStart() { super.onStart(); if (auth != null) FirebaseAuth.getInstance().addAuthStateListener(authListener) }
    override fun onStop() { if (auth != null) FirebaseAuth.getInstance().removeAuthStateListener(authListener); super.onStop() }
    private fun connectCloud() {
        val uid = checkNotNull(auth?.currentUser?.uid)
        session.demo = false; session.uid = uid; session.repository = FirestoreTrainingRepository(FirebaseFirestore.getInstance(), uid)
        training.connect(session.repository!!)
    }
    fun authenticated() { if (session.uid == auth?.currentUser?.uid && session.repository != null && !demo) return; connectCloud(); ReminderScheduler.restore(this, session.uid!!); enterHome() }
    fun startDemo() { session.demo = true; session.repository = DemoTrainingRepository(); training.connect(session.repository!!); enterHome() }
    private fun enterHome() { nav.navigate(R.id.dashboardFragment, null, NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()) }
    fun logout() {
        session.uid?.let { ReminderScheduler.cancel(this, it) }
        session.uid = null; auth?.logout(); training.disconnect(); session.repository = null; session.demo = false
        nav.navigate(R.id.loginFragment, null, NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
        lifecycleScope.launch { try { CredentialManager.create(this@MainActivity).clearCredentialState(ClearCredentialStateRequest()) } catch (_: Exception) { /* Firebase sign-out has already completed. */ } }
    }
}
