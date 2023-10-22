package dev.suli4.note.ext

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Context.getDrawableCompat(id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}


fun formatTime(timeMillis: Long): String {

    return when {
        DateUtils.isToday(timeMillis) -> "сегодня"
        DateUtils.isToday(timeMillis + DateUtils.DAY_IN_MILLIS) -> "вчера"
        else -> {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timeMillis))
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
