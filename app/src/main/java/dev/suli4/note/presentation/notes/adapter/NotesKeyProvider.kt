package dev.suli4.note.presentation.notes.adapter

import androidx.recyclerview.selection.ItemKeyProvider

class NotesKeyProvider(private val adapter: NoteAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long =
        adapter.notes[position].id

    override fun getPosition(key: Long): Int =
        adapter.notes.indexOfFirst { it.id == key }
}