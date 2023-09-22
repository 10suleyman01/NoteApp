package dev.suli4.note.presentation.notes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.suli4.note.databinding.NoteItemLinearBinding
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.notes.viewholder.NoteViewHolderLinear

class NoteAdapter(
    val viewState: Boolean
) : RecyclerView.Adapter<NoteViewHolderLinear>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolderLinear {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NoteViewHolderLinear(NoteItemLinearBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount() = notes.size

    override fun getItemId(position: Int): Long {
        return notes[position].createdAt
    }

    override fun onBindViewHolder(holder: NoteViewHolderLinear, position: Int) {
        val note = notes[position]
        holder.bind(note)
        listener?.let { noteListener ->
            holder.itemView.setOnClickListener {
                noteListener.onNoteClick(note, position)
            }
        }
    }

}