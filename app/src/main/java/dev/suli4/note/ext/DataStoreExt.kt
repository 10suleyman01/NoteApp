package dev.suli4.note.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.suli4.note.ext.serializer.SortingSerializer

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.protoDataStore by dataStore("typed_settings.json", SortingSerializer)

object PreferencesKeys {
    val ViewTypeSettings = booleanPreferencesKey("view_type_grid_or_linear")
}

