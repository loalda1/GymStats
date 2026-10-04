package org.gymstats.android.worker
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.gymstats.android.MainActivity
import org.gymstats.android.R
import org.gymstats.android.domain.ReminderTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
object ReminderScheduler {
    fun preferences(context: Context, uid: String) = context.getSharedPreferences("reminder_$uid", Context.MODE_PRIVATE)
    fun cancel(context: Context, uid: String) { WorkManager.getInstance(context).cancelUniqueWork("reminder_$uid") }
    fun restore(context: Context, uid: String, replace: Boolean = false) {
        val p = preferences(context, uid)
        if (!p.getBoolean("enabled", false)) { cancel(context, uid); return }
        val days = p.getStringSet("days", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()
        if (days.isEmpty()) { cancel(context, uid); return }
        val now = ZonedDateTime.now()
        val next = ReminderTime.next(now, days, p.getInt("hour", 18), p.getInt("minute", 0))
        val delay = java.time.Duration.between(now, next).toMillis().coerceAtLeast(0)
        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(15, TimeUnit.MINUTES).setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(workDataOf("uid" to uid)).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("reminder_$uid", if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
class WorkoutReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid") ?: return Result.failure()
        if (FirebaseApp.getApps(applicationContext).isEmpty()) FirebaseApp.initializeApp(applicationContext)
        if (FirebaseApp.getApps(applicationContext).isEmpty() || FirebaseAuth.getInstance().currentUser?.uid != uid) return Result.success()
        val p = ReminderScheduler.preferences(applicationContext, uid)
        if (!p.getBoolean("enabled", false)) return Result.success()
        val now = ZonedDateTime.now(); val today = now.toLocalDate().toString()
        val days = p.getStringSet("days", emptySet())!!
        if (now.dayOfWeek.value.toString() !in days || p.getString("last", "") == today || now.hour * 60 + now.minute < p.getInt("hour", 18) * 60 + p.getInt("minute", 0)) return Result.success()
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val manager = NotificationManagerCompat.from(applicationContext); val system = applicationContext.getSystemService(NotificationManager::class.java)
        system.createNotificationChannel(NotificationChannel("training", applicationContext.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT))
        if (!manager.areNotificationsEnabled() || system.getNotificationChannel("training").importance == NotificationManager.IMPORTANCE_NONE) return Result.success()
        val intent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(applicationContext, "training").setSmallIcon(R.drawable.ic_notification).setContentTitle(applicationContext.getString(R.string.reminder_title)).setContentText(applicationContext.getString(R.string.reminder_body)).setContentIntent(intent).setAutoCancel(true).build()
        try { manager.notify(7, notification); p.edit().putString("last", today).apply() } catch (_: SecurityException) { return Result.success() }
        return Result.success()
    }
}
