package dev.suli4.note.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.selection.SelectionTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.suli4.note.ext.launch
import dev.suli4.note.db.serializer.SortingModel
import dev.suli4.note.db.serializer.SortingSerializer
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

    companion object {
        const val GRID_VIEW = false
    }

    private var _trackerState: MutableStateFlow<SelectionTracker<NoteModel>?> =
        MutableStateFlow(null)
    val trackerState = _trackerState.asStateFlow()

    var currentColorState: MutableStateFlow<NoteModel.Color> =
        MutableStateFlow(NoteModel.Color.Red)

    fun getCurrentColor(): String {
        return currentColorState.value.value
    }

    fun setCurrentColor(color: NoteModel.Color) {
        currentColorState.value = color
    }

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

    private var _sortTypeState: MutableStateFlow<SortingModel> =
        MutableStateFlow(SortingSerializer.defaultValue)
    private val sortTypeState = _sortTypeState.asStateFlow()

    fun setSortType(sortingModel: SortingModel) {
        _sortTypeState.value = sortingModel
    }

    fun loadNotes(sortingModel: SortingModel = SortingSerializer.defaultValue) = launch {
        allNotesUseCase.sortNotesUseCase.invoke(
            sortingModel
        ).collectLatest { sorted ->
            _state.value = NotesState.GetAllNotes(sorted)
        }
        _state.value = NotesState.Loading(false)
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

    fun insertNote(note: NoteModel) = launch {
        if (!note.contentIsEmpty()) {
            allNotesUseCase.insertNoteUseCase.invoke(note)
        }
    }

    fun updateNote(note: NoteModel) = launch {
        if (!note.contentIsEmpty()) {
            allNotesUseCase.editNoteUseCase.invoke(note)
        }
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
        } else {
            loadNotes(sortTypeState.value)
        }
        _state.value = NotesState.Loading(false)
    }

    sealed class NotesState {
        data class GetAllNotes(val notes: List<NoteModel>) : NotesState()

        data class Loading(val isLoading: Boolean) : NotesState()
    }

}