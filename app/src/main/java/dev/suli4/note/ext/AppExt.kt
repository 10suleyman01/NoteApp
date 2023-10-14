package dev.suli4.note.ext

import android.graphics.Color
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

fun formatTime(timeMillis: Long): String {
    val currentTimeMillis = System.currentTimeMillis()
    val differenceMillis = currentTimeMillis - timeMillis

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeMillis

    return when {
        differenceMillis < 24 * 60 * 60 * 1000 -> "сегодня"
        differenceMillis < 2 * 24 * 60 * 60 * 1000 -> "вчера"
        else -> {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            sdf.format(calendar.time)
        }
    }
}

fun Fragment.getColoredIcon(icon: Int, hexColor: String): Drawable? {
    val drawable = ContextCompat.getDrawable(
        requireContext(),
        icon
    )

    drawable?.setTint(Color.parseColor(hexColor))

    return drawable
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
