package dev.suli4.note.presentation.create_note

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateNoteViewModel : ViewModel() {

    private var _favoriteState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var favoriteState = _favoriteState.asStateFlow()

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
}