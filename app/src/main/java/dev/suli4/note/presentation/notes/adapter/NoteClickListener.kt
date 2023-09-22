package dev.suli4.note.presentation.notes.adapter

import dev.suli4.note.model.NoteModel

interface NoteClickListener {

    fun onNoteClick(note: NoteModel, position: Int)

}