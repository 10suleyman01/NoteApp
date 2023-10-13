package dev.suli4.note.ext

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun Fragment.getDrawable(id: Int): Drawable? {
    return ContextCompat.getDrawable(requireContext(), id)
}

fun getTimeFormatted(time: Long): String {

    val dateFormatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatDate = SimpleDateFormat("mm.dd", Locale.getDefault())

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

fun <T : Parcelable> Bundle.getParcelableModel(key: String, clazz: Class<T>): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelable(key, clazz)
    }
    return getParcelable(key)
}

fun ViewModel.launch(callback: suspend () -> Unit) {
    viewModelScope.launch {
        callback()
    }
}
