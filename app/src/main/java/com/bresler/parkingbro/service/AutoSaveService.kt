package com.bresler.parkingbro.service

import android.app.Service
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.bresler.parkingbro.domain.AutoSaveDecider
import com.bresler.parkingbro.domain.AutoSaveResult
import com.bresler.parkingbro.domain.AutoSaveSettings
import com.bresler.parkingbro.domain.AutoSaveUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutoSaveService : Service() {
	@Inject lateinit var decider: AutoSaveDecider
	@Inject lateinit var useCase: AutoSaveUseCase
	@Inject lateinit var settings: AutoSaveSettings
	@Inject lateinit var notifications: ParkingNotifications

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

	@Volatile private var cars: Set<String> = emptySet()
	private var receiver: BroadcastReceiver? = null

	override fun onCreate() {
		super.onCreate()
		try {
			startForeground(ParkingNotifications.FOREGROUND_NOTIFICATION_ID, notifications.ongoing())
		} catch (e: Exception) {
			// e.g. API 34+ location FGS without location permission
			stopSelf()
			return
		}
		scope.launch { settings.carAddresses.collect { cars = it } }

		val r = object : BroadcastReceiver() {
			override fun onReceive(context: Context, intent: Intent) {
				val device = IntentCompat.getParcelableExtra(
					intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java,
				)
				val address = device?.address ?: return
				if (intent.action != BluetoothDevice.ACTION_ACL_DISCONNECTED &&
					intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1) != BluetoothProfile.STATE_DISCONNECTED
				) return
				if (decider.shouldSave(address, cars, SystemClock.elapsedRealtime())) {
					scope.launch(Dispatchers.IO) {
						if (useCase.run() is AutoSaveResult.NoLocation) notifications.showLocationUnavailable()
					}
				}
			}
		}
		receiver = r
		val filter = IntentFilter().apply {
			addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
			addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
			addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
		}
		ContextCompat.registerReceiver(this, r, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onDestroy() {
		receiver?.let { unregisterReceiver(it) }
		scope.cancel()
		super.onDestroy()
	}

	companion object {
		fun start(context: Context) {
			ContextCompat.startForegroundService(context, Intent(context, AutoSaveService::class.java))
		}

		fun stop(context: Context) {
			context.stopService(Intent(context, AutoSaveService::class.java))
		}
	}
}
