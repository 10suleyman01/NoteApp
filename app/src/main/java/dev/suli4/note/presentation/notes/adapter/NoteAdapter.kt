package dev.suli4.note.presentation.notes.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.suli4.note.databinding.NoteItemLinearBinding
import dev.suli4.note.ext.formatTime
import dev.suli4.note.model.NoteModel
import dev.suli4.note.viewmodel.NoteViewModel


class NoteAdapter(
    val viewModel: NoteViewModel
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    init {
        setHasStableIds(true)
    }

    val notes: MutableList<NoteModel> = mutableListOf()
    private var listener: NoteClickListener? = null

    fun setNotes(newNotes: List<NoteModel>) {
        val diffCallback = NoteAdapterDiffUtil(notes, newNotes)
        val diffCourses = DiffUtil.calculateDiff(diffCallback)
        notes.clear()
        notes.addAll(newNotes)
        diffCourses.dispatchUpdatesTo(this)
    }

    fun setNoteListener(listener: NoteClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NoteViewHolder(NoteItemLinearBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount() = notes.size

    override fun getItemId(position: Int) = notes[position].id

    fun getNote(position: Int) = notes[position]

    fun getPosition(noteId: Long) = notes.indexOfFirst { it.id == noteId }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
        listener?.let { noteListener ->
            holder.itemView.setOnClickListener {
                noteListener.onNoteClick(note, position)
            }
        }
    }

    class ItemLookup(private val rv: RecyclerView) : ItemDetailsLookup<NoteModel>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<NoteModel>? {
            val view = rv.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (rv.getChildViewHolder(view) as NoteViewHolder)
                    .getItemDetails()
            }
            return null
        }
    }

    inner class NoteViewHolder(
        private val itemNoteItemBinding: NoteItemLinearBinding
    ) : RecyclerView.ViewHolder(itemNoteItemBinding.root) {

        fun bind(note: NoteModel) {
            itemNoteItemBinding.apply {

                if (note.title.isEmpty()) {
                    tvTitle.isVisible = false
                } else {
                    tvTitle.isVisible = true
                    tvTitle.text = note.title
                }

                if (note.text.isEmpty()) {
                    tvText.isVisible = false
                } else {
                    tvText.isVisible = true
                    tvText.text = note.text
                }

                tvText.text = note.text
                tvCreatedAt.text = formatTime(note.createdAt)
                val color = note.color.value
                setCardBackgroundColor(color)

                val isSelected = viewModel.trackerState.value?.isSelected(note) ?: false

                favorite.isVisible = !isSelected && note.isFavorite
                favorite.background.colorFilter =
                    PorterDuffColorFilter(
                        Color.parseColor(note.color.value),
                        PorterDuff.Mode.SRC_ATOP
                    )
                checked.isVisible = isSelected

            }
        }

        private fun setCardBackgroundColor(color: String) {
            itemNoteItemBinding.colorIndicator.setBackgroundColor(Color.parseColor(color))
        }

        fun getItemDetails(): ItemDetails<NoteModel> {
            return object : ItemDetails<NoteModel>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): NoteModel = notes[position]
            }
        }
    }

}