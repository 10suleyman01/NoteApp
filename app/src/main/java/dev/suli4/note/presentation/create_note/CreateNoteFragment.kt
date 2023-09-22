package dev.suli4.note.presentation.create_note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.R
import dev.suli4.note.databinding.FragmentCreateNoteBinding
import dev.suli4.note.ext.getDrawable
import dev.suli4.note.ext.text
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.notes.NotesFragment.Companion.NOTE_KEY
import dev.suli4.note.presentation.notes.NotesFragment.Companion.NOTE_POSITION
import dev.suli4.note.presentation.notes.NotesFragment.Companion.REQUEST_KEY_EDIT_NOTE
import dev.suli4.note.presentation.notes.NotesFragment.Companion.REQUEST_KEY_NEW_NOTE
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class CreateNoteFragment : Fragment() {

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!

    private val args: CreateNoteFragmentArgs? by navArgs()

    private val colorState: MutableStateFlow<NoteModel.Color> =
        MutableStateFlow(NoteModel.Color.Red)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNoteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onColorSelected(colorState.value, binding.red)

        val note = args?.note

        binding.apply {

            red.setOnClickListener {
                onColorSelected(NoteModel.Color.Red, it)
            }
            orange.setOnClickListener {
                onColorSelected(NoteModel.Color.Orange, it)
            }
            yellow.setOnClickListener {
                onColorSelected(NoteModel.Color.Yellow, it)
            }
            cyan.setOnClickListener {
                onColorSelected(NoteModel.Color.Cyan, it)
            }
            pink.setOnClickListener {
                onColorSelected(NoteModel.Color.Pink, it)
            }

            if (note != null) {
                etNoteTitle.setText(note.title)
                etNoteText.setText(note.text)
                val view = when (note.color) {
                    NoteModel.Color.Red -> binding.red
                    NoteModel.Color.Orange -> binding.orange
                    NoteModel.Color.Yellow -> binding.yellow
                    NoteModel.Color.Cyan -> binding.cyan
                    NoteModel.Color.Pink -> binding.pink
                    else -> binding.red
                }
                onColorSelected(note.color, view)
            }

            fabSaveNote.setOnClickListener {

                val title = etNoteTitle.text().trim()
                val text = etNoteText.text().trim()

                if (title.isNotEmpty() && text.isNotEmpty()) {
                    if (note != null) {
                        note.title = title
                        note.text = text
                        note.color = colorState.value
                        val noteBundle = bundleOf(NOTE_KEY to note, NOTE_POSITION to args?.position)
                        setFragmentResult(REQUEST_KEY_EDIT_NOTE, noteBundle)
                    } else {
                        val noteModel = NoteModel(
                            title = title,
                            text = text,
                            createdAt = System.currentTimeMillis(),
                            color = colorState.value,
                        )
                        val noteBundle = bundleOf(NOTE_KEY to noteModel)
                        setFragmentResult(REQUEST_KEY_NEW_NOTE, noteBundle)
                    }
                    findNavController().popBackStack()
                } else {
                    if (etNoteTitle.text.isEmpty()) {
                        etNoteTitle.error = "Поле не может быть пустое!"
                    } else {
                        etNoteText.error = "Поле не может быть пустое!"
                    }
                }
            }
        }
    }

    private fun onColorSelected(color: NoteModel.Color, view: View) {
        unselectOther(binding)
        colorState.value = color
        when (color) {
            NoteModel.Color.Red -> {
                view.background = getDrawable(R.drawable.color_shape_red_selected)
            }

            NoteModel.Color.Orange -> {
                view.background = getDrawable(R.drawable.color_shape_orange_selected)
            }

            NoteModel.Color.Yellow -> {
                view.background = getDrawable(R.drawable.color_shape_yellow_selected)
            }

            NoteModel.Color.Cyan -> {
                view.background = getDrawable(R.drawable.color_shape_cyan_selected)
            }

            NoteModel.Color.Pink -> {
                view.background = getDrawable(R.drawable.color_shape_pink_selected)
            }
            else -> {}
        }
    }

    private fun unselectOther(binding: FragmentCreateNoteBinding) {
        binding.apply {
            red.background = getDrawable(R.drawable.color_shape_red)
            orange.background = getDrawable(R.drawable.color_shape_orange)
            yellow.background = getDrawable(R.drawable.color_shape_yellow)
            cyan.background = getDrawable(R.drawable.color_shape_cyan)
            pink.background = getDrawable(R.drawable.color_shape_pink)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }



}