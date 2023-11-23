package dev.suli4.note.ext

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.suli4.note.presentation.MainActivity
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

fun Fragment.setTitle(resId: Int) {
    (requireActivity() as MainActivity).supportActionBar?.title = getString(resId)
}

fun Fragment.setSubTitle(resId: Int) {
    (requireActivity() as MainActivity).supportActionBar?.subtitle = getString(resId)
}

fun Fragment.setTitle(title: String) {
    (requireActivity() as MainActivity).supportActionBar?.title = title
}

fun Fragment.setSubTitle(subTitle: String) {
    (requireActivity() as MainActivity).supportActionBar?.subtitle = subTitle
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

fun ViewModel.launch(callback: suspend () -> Unit) {
    viewModelScope.launch {
        callback()
    }
}
