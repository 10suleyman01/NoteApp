package dev.suli4.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.suli4.note.model.NoteModel
import dev.suli4.note.usecases.AllUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val allNotesUseCase: AllUseCases
) : ViewModel() {

    private var _state: MutableStateFlow<NotesState> = MutableStateFlow(NotesState.Loading(false))
    val state = _state.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() = viewModelScope.launch {
        allNotesUseCase.getAllNotesUseCase.invoke().collectLatest {notes ->
            _state.value = NotesState.GetAllNotes(notes.sortedByDescending { it.createdAt })
        }
    }

    fun insertNote(note: NoteModel) = viewModelScope.launch {
        allNotesUseCase.insertNoteUseCase.invoke(note)
    }

    fun updateNote(note: NoteModel) = viewModelScope.launch {
        allNotesUseCase.editNoteUseCase.invoke(note)
    }

    sealed class NotesState {
        data class GetAllNotes(val notes: List<NoteModel>) : NotesState()
        data class Loading(val isLoading: Boolean) : NotesState()
    }

}