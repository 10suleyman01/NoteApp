package dev.suli4.note.viewmodel

import androidx.lifecycle.ViewModel
import androidx.recyclerview.selection.SelectionTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.suli4.note.ext.launch
import dev.suli4.note.model.NoteModel
import dev.suli4.note.usecases.AllUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val allNotesUseCase: AllUseCases
) : ViewModel() {

    init {
        loadNotes()
    }

    companion object {
        const val GRID_VIEW = false
    }

    private var _trackerState: MutableStateFlow<SelectionTracker<NoteModel>?> =
        MutableStateFlow(null)
    val trackerState = _trackerState.asStateFlow()


    fun setTracker(selectionTracker: SelectionTracker<NoteModel>) {

        _trackerState.value = selectionTracker
    }

    fun hasSelection(): Boolean = _trackerState.value?.hasSelection() ?: false

    fun clearSelection() = _trackerState.value?.clearSelection()

    private var _state: MutableStateFlow<NotesState> = MutableStateFlow(NotesState.Loading(false))
    val state = _state.asStateFlow()

    private var _shouldToDeleteItems: MutableStateFlow<MutableList<NoteModel>> =
        MutableStateFlow(mutableListOf())
    val shouldToDeleteItems = _shouldToDeleteItems.asStateFlow()

    fun addItem(item: NoteModel) {
        _shouldToDeleteItems.value.add(item)
    }

    fun removeItem(item: NoteModel?) {
        _shouldToDeleteItems.value.remove(item)
    }

    private var _viewTypeState: MutableStateFlow<Boolean> = MutableStateFlow(GRID_VIEW)
    val viewTypeState = _viewTypeState.asStateFlow()

    fun setViewTypeState(type: Boolean = GRID_VIEW) {
        _viewTypeState.value = type
    }

    private var _searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    val searchQueryState = _searchQueryState.asStateFlow()

    fun setQueryState(state: String) {
        _searchQueryState.value = state
    }

    private var _selectedItemsState: MutableStateFlow<String> = MutableStateFlow("")
    val selectedItemsState = _selectedItemsState.asStateFlow()

    fun setItemsSelected(state: String = "") {
        _selectedItemsState.value = state
    }

    private var _deleteActionIsVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val deleteActionIsVisible = _deleteActionIsVisible.asStateFlow()

    fun setShowDeleteAction(state: Boolean) {
        _deleteActionIsVisible.value = state
    }

    fun loadNotes() = launch {
        allNotesUseCase.getAllNotesUseCase.invoke().collectLatest { notes ->
            _state.value = NotesState.GetAllNotes(notes.sortedByDescending { it.createdAt })
        }
        _state.value = NotesState.Loading(false)
    }

    fun insertNote(note: NoteModel) = launch {
        allNotesUseCase.insertNoteUseCase.invoke(note)
    }

    fun updateNote(note: NoteModel) = launch {
        allNotesUseCase.editNoteUseCase.invoke(note)
    }

    fun deleteNotes(vararg note: NoteModel) = launch {
        note.forEach { item ->
            allNotesUseCase.deleteNoteUseCase.invoke(item)
        }
    }

    fun searchItems(query: String?) = launch {
        if (!query.isNullOrEmpty()) {
            allNotesUseCase.searchNotesUseCase.invoke("%${query.lowercase()}%")
                .collectLatest { notes ->
                    _state.value = NotesState.GetAllNotes(notes)
                }
            _state.value = NotesState.Loading(false)
        } else {
            loadNotes()
        }
    }

    sealed class NotesState {
        data class GetAllNotes(val notes: List<NoteModel>) : NotesState()
        data class Loading(val isLoading: Boolean) : NotesState()
    }

}