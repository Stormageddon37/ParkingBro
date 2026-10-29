package com.bresler.parkingbro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bresler.parkingbro.domain.AutoSaveSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
	@Inject lateinit var settings: AutoSaveSettings

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
		val pending = goAsync()
		CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
			try {
				if (settings.enabled.first()) AutoSaveService.start(context)
			} finally {
				pending.finish()
			}
		}
	}
}
