package dev.suli4.note.presentation.notes.adapter

import androidx.recyclerview.selection.SelectionTracker.SelectionPredicate
import dev.suli4.note.model.NoteModel

class NoteSelectionPredicate(
    val noteAdapter: NoteAdapter
) : SelectionPredicate<NoteModel>() {
    override fun canSetStateForKey(key: NoteModel, nextState: Boolean): Boolean {
        return true
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        return true
    }

    override fun canSelectMultiple() = true

}