package dev.suli4.note.presentation.notes.adapter

import android.graphics.Color
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
//                if (note.color == NoteModel.Color.White ||
//                    note.color == NoteModel.Color.Yellow
//                ) {
//                    tvTitle.setTextColor(Color.BLACK)
//                    tvText.setTextColor(Color.BLACK)
//                    tvCreatedAt.setTextColor(Color.BLACK)
//                } else {
//                    tvTitle.setTextColor(Color.WHITE)
//                    tvText.setTextColor(Color.WHITE)
//                    tvCreatedAt.setTextColor(Color.WHITE)
//                }

                if (note.title.isEmpty()) {
                    tvTitle.isVisible = false
                } else {
                    tvTitle.isVisible = true
                    tvTitle.text = note.title
                }

                tvText.text = note.text
                tvCreatedAt.text = formatTime(note.createdAt)
                val color = note.color.value
                setCardBackgroundColor(color)

                checked.isVisible = viewModel.trackerState.value?.isSelected(note) ?: false

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