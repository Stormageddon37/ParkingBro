package com.bresler.parkingbro.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.SharedPreferencesMigration
import com.bresler.parkingbro.domain.ParkingStorage
import com.bresler.parkingbro.domain.SavedParking
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Name of the legacy Activity-private SharedPreferences file (Activity.getPreferences). */
private const val LEGACY_PREFS = "MainActivity"

private val Context.dataStore by preferencesDataStore(
    name = "parking",
    // Carry over any location/timestamp/flags an existing install already saved.
    produceMigrations = { ctx -> listOf(SharedPreferencesMigration(ctx, LEGACY_PREFS)) },
)

/** DataStore-backed [ParkingStorage]. Keys match the legacy SharedPreferences keys. */
@Singleton
class DataStoreParkingStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ParkingStorage {

    private object Keys {
        val LOCATION = stringPreferencesKey("location")
        val TIMESTAMP = stringPreferencesKey("timestamp")
        val DIALOG = booleanPreferencesKey("dialog")
        val FLUSH = booleanPreferencesKey("flush")
        val IMAGES = stringPreferencesKey("images")
    }

    private val data: Flow<Preferences> = context.dataStore.data

    override val saved: Flow<SavedParking> = data.map {
        SavedParking(location = it[Keys.LOCATION], timestamp = it[Keys.TIMESTAMP])
    }

    override suspend fun save(location: String, timestamp: String) {
        context.dataStore.edit {
            it[Keys.LOCATION] = location
            it[Keys.TIMESTAMP] = timestamp
        }
    }

    override val showDeleteDialog: Flow<Boolean> = data.map { it[Keys.DIALOG] ?: true }

    override suspend fun setShowDeleteDialog(show: Boolean) {
        context.dataStore.edit { it[Keys.DIALOG] = show }
    }

    override val flushAfterSave: Flow<Boolean> = data.map { it[Keys.FLUSH] ?: true }

    override suspend fun setFlushAfterSave(flush: Boolean) {
        context.dataStore.edit { it[Keys.FLUSH] = flush }
    }

    override val images: Flow<List<String>> = data.map {
        it[Keys.IMAGES]?.split("\n")?.filter(String::isNotEmpty) ?: emptyList()
    }

    override suspend fun setImages(uris: List<String>) {
        context.dataStore.edit { it[Keys.IMAGES] = uris.joinToString("\n") }
    }
}
