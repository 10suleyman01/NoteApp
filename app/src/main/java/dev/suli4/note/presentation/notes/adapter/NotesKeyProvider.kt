package dev.suli4.note.presentation.notes.adapter

import androidx.recyclerview.selection.ItemKeyProvider
import dev.suli4.note.model.NoteModel

class NotesKeyProvider(private val adapter: NoteAdapter) :
    ItemKeyProvider<NoteModel>(SCOPE_CACHED) {
    override fun getKey(position: Int): NoteModel =
        adapter.getNote(position)

    override fun getPosition(key: NoteModel): Int {
        return adapter.getPosition(key.id)
    }
}