package dev.suli4.note.presentation.notes.adapter

import androidx.recyclerview.widget.DiffUtil
import dev.suli4.note.model.NoteModel

class NoteAdapterDiffUtil(
    private val oldList: List<NoteModel>,
    private val newList: List<NoteModel>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}