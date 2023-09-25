package dev.suli4.note.presentation.create_note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.R
import dev.suli4.note.databinding.ChooseColorViewBinding
import dev.suli4.note.databinding.FragmentCreateNoteBinding
import dev.suli4.note.ext.getDrawable
import dev.suli4.note.ext.getModel
import dev.suli4.note.ext.text
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.notes.NotesFragment.Companion.NOTE_KEY
import dev.suli4.note.presentation.notes.NotesFragment.Companion.NOTE_POSITION
import dev.suli4.note.presentation.notes.NotesFragment.Companion.REQUEST_KEY_DELETE_NOTE
import dev.suli4.note.presentation.notes.NotesFragment.Companion.REQUEST_KEY_EDIT_NOTE
import dev.suli4.note.presentation.notes.NotesFragment.Companion.REQUEST_KEY_NEW_NOTE
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class CreateNoteFragment : Fragment() {

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!

    private var _bindingChooseColor: ChooseColorViewBinding? = null
    private val bindingChooseColor get() = _bindingChooseColor!!

    private val args: CreateNoteFragmentArgs? by navArgs()
    private var note: NoteModel? = null

    private val colorState: MutableStateFlow<NoteModel.Color?> =
        MutableStateFlow(null)

    private var alertDialog: AlertDialog? = null

    companion object {
        const val COLOR_STATE = "color_state"
        const val SHOW_COLOR_VIEW = "show_color_view"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        note = args?.note

        if (savedInstanceState != null) {
            colorState.value = savedInstanceState.getModel(COLOR_STATE, NoteModel.Color::class.java)
                ?: colorState.value

            if (savedInstanceState.getBoolean(SHOW_COLOR_VIEW)) {
                getChooseColorDialog().show()
                setCurrentColor()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNoteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(COLOR_STATE, colorState.value)
        outState.putBoolean(SHOW_COLOR_VIEW, alertDialog?.isShowing ?: false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(SHOW_COLOR_VIEW)) {
                getChooseColorDialog().show()
                setCurrentColor()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            if (note != null) {
                etNoteTitle.setText(note?.text)
                etNoteText.setText(note?.text)
            }

            fabSaveNote.setOnClickListener {

                val title = etNoteTitle.text().trim()
                val text = etNoteText.text().trim()

                if (text.isNotEmpty()) {
                    if (note != null) {
                        note?.title = title
                        note?.text = text
                        colorState.value?.let { state ->
                            note?.color = state
                        }
                        val noteBundle = bundleOf(NOTE_KEY to note, NOTE_POSITION to args?.position)
                        setFragmentResult(REQUEST_KEY_EDIT_NOTE, noteBundle)
                    } else {
                        val noteModel = NoteModel(
                            title = title,
                            text = text,
                            createdAt = System.currentTimeMillis(),
                            color = colorState.value ?: NoteModel.Color.Red,
                        )
                        val noteBundle = bundleOf(NOTE_KEY to noteModel)
                        setFragmentResult(REQUEST_KEY_NEW_NOTE, noteBundle)
                    }
                    findNavController().popBackStack()
                } else {
                    etNoteText.error = getString(R.string.field_cannot_be_empty)
                }
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_create_note, menu)

                val menuItemDelete = menu.findItem(R.id.delete)
                if (note != null) {
                    menuItemDelete.isVisible = true
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                when (menuItem.itemId) {

                    R.id.chooseColor -> {
                        alertDialog = getChooseColorDialog()
                        alertDialog?.show()
                    }

                    R.id.delete -> {
                        setFragmentResult(REQUEST_KEY_DELETE_NOTE, bundleOf(NOTE_KEY to note))
                        findNavController().popBackStack()
                    }

                    android.R.id.home -> {
                        findNavController().popBackStack()
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setCurrentColor() {
        if (colorState.value != null) {
            colorState.value?.let { state ->
                onColorSelected(state, bindingChooseColor)
            }
        } else {
            note?.let {
                onColorSelected(it.color, bindingChooseColor)
            }
        }
    }

    private fun onColorSelected(color: NoteModel.Color, binding: ChooseColorViewBinding) {
        unselectOther(binding)
        colorState.value = color
        when (color) {
            NoteModel.Color.Red -> {
                binding.red.background = getDrawable(R.drawable.color_shape_red_selected)
            }

            NoteModel.Color.Orange -> {
                binding.orange.background = getDrawable(R.drawable.color_shape_orange_selected)
            }

            NoteModel.Color.Yellow -> {
                binding.yellow.background = getDrawable(R.drawable.color_shape_yellow_selected)
            }

            NoteModel.Color.Cyan -> {
                binding.cyan.background = getDrawable(R.drawable.color_shape_cyan_selected)
            }

            NoteModel.Color.Pink -> {
                binding.pink.background = getDrawable(R.drawable.color_shape_pink_selected)
            }

            else -> {}
        }
    }

    private fun getChooseColorDialog(): AlertDialog {
        val showChooseColorDialog = AlertDialog.Builder(requireContext())

        _bindingChooseColor = ChooseColorViewBinding.inflate(requireActivity().layoutInflater)
        showChooseColorDialog.setView(bindingChooseColor.root)

        setCurrentColor()

        bindingChooseColor.apply {
            red.setOnClickListener {
                onColorSelected(NoteModel.Color.Red, this)
            }
            orange.setOnClickListener {
                onColorSelected(NoteModel.Color.Orange, this)
            }
            yellow.setOnClickListener {
                onColorSelected(NoteModel.Color.Yellow, this)
            }
            cyan.setOnClickListener {
                onColorSelected(NoteModel.Color.Cyan, this)
            }
            pink.setOnClickListener {
                onColorSelected(NoteModel.Color.Pink, this)
            }

            saveColor.setOnClickListener {
                alertDialog?.dismiss()
            }
        }

        return showChooseColorDialog.create()
    }

    private fun unselectOther(binding: ChooseColorViewBinding) {
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