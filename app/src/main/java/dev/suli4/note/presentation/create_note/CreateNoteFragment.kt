package dev.suli4.note.presentation.create_note

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.R
import dev.suli4.note.databinding.ChooseColorViewBinding
import dev.suli4.note.databinding.FragmentCreateNoteBinding
import dev.suli4.note.ext.formatTime
import dev.suli4.note.ext.getColoredIcon
import dev.suli4.note.ext.getDrawableCompat
import dev.suli4.note.ext.setTitle
import dev.suli4.note.ext.text
import dev.suli4.note.ext.toast
import dev.suli4.note.model.NoteModel
import dev.suli4.note.utils.saveImageToInternalStorage
import dev.suli4.note.viewmodel.NoteViewModel


@AndroidEntryPoint
class CreateNoteFragment : Fragment() {

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!

    private var _bindingChooseColor: ChooseColorViewBinding? = null
    private val bindingChooseColor get() = _bindingChooseColor!!

    private val args: CreateNoteFragmentArgs? by navArgs()
    private var note: NoteModel? = null

    private val noteViewModel: NoteViewModel by activityViewModels()

    private val createViewModel: CreateNoteViewModel by viewModels()

    private var alertDialog: AlertDialog? = null
    private var menuItemSelectionColor: MenuItem? = null
    private var menuItemFavorite: MenuItem? = null

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val saved = requireContext().saveImageToInternalStorage(uri)
                saved?.let {
                    createViewModel.setImagePath(saved)
                    loadImage(saved)
                }
            }
        }

    private fun loadImage(path: String) {

        if (path.isEmpty()) return

        Glide.with(binding.imageView.context)
            .load(Uri.parse("file://${path}"))
            .into(binding.imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = args?.note
        setTitle(R.string.create_new_note)

        note?.apply {
            noteViewModel.setCurrentColor(color)
            createViewModel.setState(isFavorite)
            setTitle(R.string.editing)
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {

            bottomMenu.menu.apply {

                val attachImage = findItem(R.id.attach_image)

                attachImage.setOnMenuItemClickListener { item ->
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    true
                }

            }

            note?.let { model ->
                etNoteTitle.setText(model.title)
                etNoteText.setText(model.text)

                if (model.imagePath!!.isNotEmpty()) {
                    toast(model.imagePath!!)
                    loadImage(model.imagePath ?: "")
                }

                if (model.lastEdited > 0) {
                    val editedTimeText =
                        "${getString(R.string.changed)}:${formatTime(model.lastEdited)}"
                    tvLastEdited.text = editedTimeText
                } else {
                    tvLastEdited.isVisible = false
                }
            }

            fabSaveNote.setOnClickListener {

                val title = etNoteTitle.text().trim()
                val text = etNoteText.text().trim()

                if (note != null) {
                    note?.let {
                        noteViewModel.updateNote(
                            it.copy(
                                title = title,
                                text = text,
                                isFavorite = createViewModel.isFavorite(),
                                color = noteViewModel.currentColorState.value,
                                lastEdited = System.currentTimeMillis(),
                                imagePath = createViewModel.imagePathState.value
                            )
                        )
                    }
                } else {
                    noteViewModel.insertNote(
                        NoteModel(
                            title = title,
                            text = text,
                            createdAt = System.currentTimeMillis(),
                            lastEdited = System.currentTimeMillis(),
                            isFavorite = createViewModel.isFavorite(),
                            color = noteViewModel.currentColorState.value,
                            imagePath = createViewModel.imagePathState.value
                        )
                    )
                }
                findNavController().popBackStack()
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_create_note, menu)

                menuItemSelectionColor = menu.findItem(R.id.chooseColor)
                menuItemSelectionColor?.icon = getColoredIcon(
                    R.drawable.baseline_color_lens_24,
                    noteViewModel.getCurrentColor()
                )

                menuItemFavorite = menu.findItem(R.id.favorite)

                note?.also {
                    menuItemFavorite?.icon = getDrawable(
                        if (it.isFavorite) R.drawable.round_bookmark_24
                        else R.drawable.baseline_bookmark_border_24
                    )
                }

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

                    R.id.favorite -> {
                        createViewModel.toggleFavorite()
                        menuItemFavorite?.icon = getDrawable(
                            if (createViewModel.isFavorite()) R.drawable.round_bookmark_24
                            else R.drawable.baseline_bookmark_border_24
                        )

                    }

                    R.id.delete -> {
                        note?.also {
                            noteViewModel.deleteNotes(it)
                        }
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

    private fun getChooseColorDialog(): AlertDialog {
        val showChooseColorDialog = AlertDialog.Builder(requireContext())

        _bindingChooseColor = ChooseColorViewBinding.inflate(requireActivity().layoutInflater)
        showChooseColorDialog.setView(bindingChooseColor.root)

        selectedColor(noteViewModel.currentColorState.value, bindingChooseColor)

        bindingChooseColor.apply {
            val colorButtons = mapOf(
                red to NoteModel.Color.Red,
                green to NoteModel.Color.Green,
                orange to NoteModel.Color.Orange,
                yellow to NoteModel.Color.Yellow,
                cyan to NoteModel.Color.Cyan,
                purple to NoteModel.Color.Purple,
                pink to NoteModel.Color.Pink,
            )

            colorButtons.forEach { (button, color) ->
                button.setOnClickListener {
                    selectedColor(color, this)
                }
            }

            saveColor.setOnClickListener {
                alertDialog?.dismiss()
                _bindingChooseColor = null
            }
        }

        return showChooseColorDialog.create()
    }

    private fun selectedColor(
        color: NoteModel.Color,
        binding: ChooseColorViewBinding
    ) {
        unselectOther(binding)

        val selectedColorButton = when (color) {
            NoteModel.Color.Red -> binding.red
            NoteModel.Color.Green -> binding.green
            NoteModel.Color.Orange -> binding.orange
            NoteModel.Color.Yellow -> binding.yellow
            NoteModel.Color.Cyan -> binding.cyan
            NoteModel.Color.Purple -> binding.purple
            NoteModel.Color.Pink -> binding.pink
            else -> binding.red
        }

        menuItemSelectionColor?.icon =
            getColoredIcon(R.drawable.baseline_color_lens_24, color.value)
        noteViewModel.setCurrentColor(color)

        note?.let {
            noteViewModel.updateNote(
                it.copy(
                    color = color
                )
            )
        }

        selectedColorButton.background = getDrawable(getSelectedColorDrawableId(color))
    }

    private fun getSelectedColorDrawableId(color: NoteModel.Color): Int {
        return when (color) {
            NoteModel.Color.Red -> R.drawable.color_shape_red_selected
            NoteModel.Color.Green -> R.drawable.color_shape_green_selected
            NoteModel.Color.Orange -> R.drawable.color_shape_orange_selected
            NoteModel.Color.Yellow -> R.drawable.color_shape_yellow_selected
            NoteModel.Color.Cyan -> R.drawable.color_shape_cyan_selected
            NoteModel.Color.Purple -> R.drawable.color_shape_purple_selected
            NoteModel.Color.Pink -> R.drawable.color_shape_pink_selected
            else -> R.drawable.color_shape_red_selected
        }
    }


    private fun unselectOther(binding: ChooseColorViewBinding) {
        binding.apply {
            red.background = getDrawable(R.drawable.color_shape_red)
            green.background = getDrawable(R.drawable.color_shape_green)
            orange.background = getDrawable(R.drawable.color_shape_orange)
            yellow.background = getDrawable(R.drawable.color_shape_yellow)
            cyan.background = getDrawable(R.drawable.color_shape_cyan)
            purple.background = getDrawable(R.drawable.color_shape_purple)
            pink.background = getDrawable(R.drawable.color_shape_pink)
        }
    }

    private fun getDrawable(resId: Int): Drawable? {
        return requireContext().getDrawableCompat(resId)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _bindingChooseColor = null
        createViewModel.reset()
    }


}