package dev.suli4.note.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class NoteModel(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String,
    var text: String,
    val createdAt: Long,
    var color: Color
): Parcelable {

    enum class Color(val value: String) {
        White("#ffffff"),
        Red("#E57373"),
        Orange("#f5c27d"),
        Yellow("#ffe666"),
        Cyan("#bfe7f6"),
        Pink("#f6cebf")
    }

}

