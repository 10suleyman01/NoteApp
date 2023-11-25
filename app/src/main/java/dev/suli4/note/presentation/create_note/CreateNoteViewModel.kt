package dev.suli4.note.presentation.create_note

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateNoteViewModel : ViewModel() {

    private var _favoriteState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val favoriteState = _favoriteState.asStateFlow()

    fun toggleFavorite() {
        _favoriteState.value = !_favoriteState.value
    }

    fun setState(isFavorite: Boolean) {
        _favoriteState.value = isFavorite
    }


    fun isFavorite(): Boolean {
        return favoriteState.value
    }

    fun reset() {
        _favoriteState.value = false
    }

    private var _imagePathState: MutableStateFlow<String> = MutableStateFlow("")
    val imagePathState = _imagePathState.asStateFlow()

    fun setImagePath(path: String) {
        _imagePathState.value = path
    }
}