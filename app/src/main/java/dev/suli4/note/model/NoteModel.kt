package dev.suli4.note.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.IdRes
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
    var color: Color,
): Parcelable {

    @Parcelize
    enum class Color(val value: String): Parcelable {
        White("#ffffff"),
        Red("#E57373"),
        Orange("#f5c27d"),
        Yellow("#ffe666"),
        Cyan("#bfe7f6"),
        Pink("#f6cebf")
    }

}

