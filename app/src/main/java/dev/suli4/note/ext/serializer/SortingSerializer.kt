package dev.suli4.note.ext.serializer

import androidx.datastore.core.Serializer
import dev.suli4.note.model.NoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class SortingModel(
    val isAsc: Boolean,
    val field: NoteModel.Fields
)

object SortingSerializer : Serializer<SortingModel> {
    override val defaultValue: SortingModel
        get() = SortingModel(isAsc = true, field = NoteModel.Fields.Title)

    override suspend fun readFrom(input: InputStream): SortingModel {
        return try {
            Json.decodeFromString(
                deserializer = SortingModel.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: SortingModel, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = SortingModel.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}