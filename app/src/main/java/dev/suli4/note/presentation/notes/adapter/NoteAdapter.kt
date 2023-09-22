package dev.suli4.note.presentation.notes.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.suli4.note.databinding.NoteItemLinearBinding
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.notes.viewholder.NoteViewHolder

class NoteAdapter : RecyclerView.Adapter<NoteViewHolder>() {

    init {
        setHasStableIds(true)
    }

    val notes: MutableList<NoteModel> = mutableListOf()
    private var listener: NoteClickListener? = null

    private var tracker: SelectionTracker<Long>? = null
    fun setTracker(tracker: SelectionTracker<Long>?) {
        this.tracker = tracker
    }

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
        return NoteViewHolder(NoteItemLinearBinding.inflate(layoutInflater, parent, false), tracker)
    }

    override fun getItemCount() = notes.size

    override fun getItemId(position: Int) = position.toLong()

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
        listener?.let { noteListener ->
            holder.itemView.setOnClickListener {
                noteListener.onNoteClick(note, position)
            }
        }
    }

    class ItemLookup(private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = rv.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (rv.getChildViewHolder(view) as NoteViewHolder)
                    .getItemDetails()
            }
            return null
        }
    }

}