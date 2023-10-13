package dev.suli4.note.presentation.create_note

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.notes.NotesFragment
import dev.suli4.note.presentation.notes.NotesFragment.Companion.NOTE_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateNoteViewModel : ViewModel() {

    fun editNote(position: Int?, note: NoteModel?, callback: (bundle: Bundle) -> Unit) {
        callback.invoke(bundleOf(NOTE_KEY to note, NotesFragment.NOTE_POSITION to position))
    }

    fun createNote(
        title: String,
        text: String,
        createdAt: Long,
        color: NoteModel.Color,
        callback: (bundle: Bundle) -> Unit
    ) {
        val note = NoteModel(
            title = title,
            text = text,
            createdAt = createdAt,
            color = color,
        )
        callback.invoke(bundleOf(NOTE_KEY to note))
    }

    fun showChooseColors() {

    }

}