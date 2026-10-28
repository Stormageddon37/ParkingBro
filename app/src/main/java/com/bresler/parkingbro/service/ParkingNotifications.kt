package com.bresler.parkingbro.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bresler.parkingbro.R
import com.bresler.parkingbro.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ParkingNotifications @Inject constructor(
	@param:ApplicationContext private val context: Context,
) {
	init {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val manager = context.getSystemService(NotificationManager::class.java)
			manager.createNotificationChannel(
				NotificationChannel(ONGOING_CHANNEL_ID, "Watching for your car", NotificationManager.IMPORTANCE_MIN),
			)
			manager.createNotificationChannel(
				NotificationChannel(ALERT_CHANNEL_ID, "Parking alerts", NotificationManager.IMPORTANCE_DEFAULT),
			)
		}
	}

	fun ongoing(): Notification =
		NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_baseline_location_on_24)
			.setContentTitle("ParkingBro")
			.setContentText("Watching for your car")
			.setPriority(NotificationCompat.PRIORITY_MIN)
			.setOngoing(true)
			.build()

	fun showLocationUnavailable() {
		// POST_NOTIFICATIONS is runtime-granted on API 33+; silently skip if denied.
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
			!= PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 33
		) return
		val open = PendingIntent.getActivity(
			context,
			0,
			Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
		)
		val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_baseline_location_on_24)
			.setContentTitle("Couldn't save your parking spot")
			.setContentText("Location was off. Tap to save manually.")
			.setContentIntent(open)
			.setAutoCancel(true)
			.build()
		NotificationManagerCompat.from(context).notify(ALERT_NOTIFICATION_ID, notification)
	}

	companion object {
		const val ONGOING_CHANNEL_ID = "parking_ongoing"
		const val ALERT_CHANNEL_ID = "parking_alerts"
		const val FOREGROUND_NOTIFICATION_ID = 1001
		const val ALERT_NOTIFICATION_ID = 1002
	}
}
