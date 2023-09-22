package dev.suli4.note.presentation.notes.viewholder

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import dev.suli4.note.databinding.NoteItemLinearBinding
import dev.suli4.note.ext.getTimeFormatted
import dev.suli4.note.model.NoteModel


class NoteViewHolderLinear(
    private val itemNoteItemBinding: NoteItemLinearBinding
): RecyclerView.ViewHolder(itemNoteItemBinding.root) {

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
            val color = note.color.color
            root.setCardBackgroundColor(Color.parseColor(color))
        }
    }

}