package dev.suli4.note.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "notes")
data class NoteModel(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String,
    var text: String,
    val createdAt: Long,
    val lastEdited: Long,
    val isFavorite: Boolean,
    var color: Color,
    var imagePath: String? = ""
) : Parcelable {

    fun contentIsEmpty() = title.isEmpty() && text.isEmpty()

    @Parcelize
    enum class Color(val value: String) : Parcelable {
        White("#ffffff"),
        Red("#E57373"),
        Orange("#f5c27d"),
        Yellow("#ffe666"),
        Green("#AED581"),
        Cyan("#bfe7f6"),
        Purple("#B39DDB"),
        Pink("#f6cebf")
    }

    enum class Fields(val value: String) {
        Title("title"),
        CreatedAt("createdAt"),
    }
}

