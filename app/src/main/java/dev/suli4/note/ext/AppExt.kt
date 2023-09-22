package dev.suli4.note.ext

import android.graphics.drawable.Drawable
import android.text.style.TtsSpan.TimeBuilder
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.suli4.note.ext.Formatter.dateFormatDate
import dev.suli4.note.ext.Formatter.dateFormatTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatter {
    val dateFormatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatDate = SimpleDateFormat("mm.dd", Locale.getDefault())
}

fun Fragment.getDrawable(id: Int): Drawable? {
    return ContextCompat.getDrawable(requireContext(), id)
}

fun getTimeFormatted(time: Long): String {

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time

    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val calCurrent = Calendar.getInstance()
    calCurrent.timeInMillis = System.currentTimeMillis()
    val currentDay = calCurrent.get(Calendar.DAY_OF_MONTH)

    if (day == currentDay) {
        return dateFormatTime.format(time).toString()
    } else if (day < currentDay) {
        return "Вчера"
    }

    return dateFormatDate.format(time)
}

fun EditText.text(): String {
    return text.toString()
}