package dev.suli4.note.presentation.notes.viewholder

import android.graphics.Color
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import dev.suli4.note.databinding.NoteItemLinearBinding
import dev.suli4.note.ext.getTimeFormatted
import dev.suli4.note.model.NoteModel


class NoteViewHolder(
    private val itemNoteItemBinding: NoteItemLinearBinding,
    private val tracker: SelectionTracker<Long>?
) : RecyclerView.ViewHolder(itemNoteItemBinding.root) {

    fun bind(note: NoteModel) {
        itemNoteItemBinding.apply {
            if (note.color == NoteModel.Color.White) {
                tvTitle.setTextColor(Color.BLACK)
                tvText.setTextColor(Color.BLACK)
            } else {
                tvTitle.setTextColor(Color.WHITE)
                tvText.setTextColor(Color.WHITE)
            }
            tvTitle.text = note.title
            tvText.text = note.text
            tvCreatedAt.text = getTimeFormatted(note.createdAt)
            val color = note.color.value
            setCardBackgroundColor(color)

            tracker?.let {
                checked.isVisible = it.isSelected(adapterPosition.toLong())
            }
        }
    }


    private fun setCardBackgroundColor(color: String) {
        itemNoteItemBinding.root.setCardBackgroundColor(Color.parseColor(color))
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long = adapterPosition.toLong()
        }


}