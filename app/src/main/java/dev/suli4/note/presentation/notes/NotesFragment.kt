package dev.suli4.note.presentation.notes

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.R
import dev.suli4.note.databinding.FragmentNotesBinding
import dev.suli4.note.ext.getDrawable
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.notes.adapter.NoteAdapter
import dev.suli4.note.presentation.notes.adapter.NoteClickListener
import dev.suli4.note.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_KEY_NEW_NOTE = "new_note"
        const val REQUEST_KEY_EDIT_NOTE = "edit_note"
        const val NOTE_KEY = "note"
        const val NOTE_POSITION = "position"
    }

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    private val viewTypeState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = NoteAdapter(viewTypeState.value)
        adapter.setNoteListener(noteClickListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(layoutInflater)
        return binding.root
    }

    private val noteClickListener = object : NoteClickListener {
        override fun onNoteClick(note: NoteModel, position: Int) {
            val actionEditNote =
                NotesFragmentDirections.actionNotesFragmentToCreateNoteFragment(note, position)
            findNavController().navigate(actionEditNote)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_notes, menu)
                menu[0].icon = getIconViewType()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.viewType -> {
                        viewTypeState.value = !viewTypeState.value
                        menuItem.icon = getIconViewType()
                        binding.rvNotes.layoutManager = getLayoutManager()
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        binding.apply {
            rvNotes.adapter = adapter
            rvNotes.layoutManager = getLayoutManager()
            rvNotes.setHasFixedSize(true)

            fabNewNote.setOnClickListener {
                val action = NotesFragmentDirections.actionNotesFragmentToCreateNoteFragment()
                findNavController().navigate(action)
            }
        }

        setFragmentResultListener(REQUEST_KEY_EDIT_NOTE) { _, bundle: Bundle ->
            val note = getNoteFromBundle(bundle)
            val position = bundle.getInt(NOTE_POSITION)
            if (note != null) {
                viewModel.updateNote(note)
                adapter.notifyItemChanged(position, note)
            }
        }

        setFragmentResultListener(REQUEST_KEY_NEW_NOTE) { _, bundle: Bundle ->
            val note = getNoteFromBundle(bundle)
            if (note != null) {
                viewModel.insertNote(note)
                adapter.notes.add(note)
                adapter.setNotes(adapter.notes)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.loadNotes()

                viewModel.state.collectLatest { state ->
                    when (state) {
                        is NoteViewModel.NotesState.GetAllNotes -> {
                            adapter.setNotes(state.notes)
                        }

                        is NoteViewModel.NotesState.Loading -> {

                        }
                    }
                }
            }
        }
    }

    private fun getNoteFromBundle(bundle: Bundle): NoteModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(NOTE_KEY, NoteModel::class.java)
        } else {
            bundle.getParcelable(NOTE_KEY)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null

        clearFragmentResult(REQUEST_KEY_NEW_NOTE)
        clearFragmentResult(REQUEST_KEY_EDIT_NOTE)
    }

    private fun getIconViewType(): Drawable? {
        if (!viewTypeState.value) return getDrawable(R.drawable.baseline_view_list_24)

        return getDrawable(
            R.drawable.baseline_grid_view_24
        )
    }

    private fun getFlexBoxLayoutManager(): FlexboxLayoutManager {
        val lm = FlexboxLayoutManager(requireContext())
        lm.flexWrap = FlexWrap.WRAP
        lm.alignItems = AlignItems.FLEX_START
        lm.justifyContent = JustifyContent.CENTER
        return lm
    }

    private fun getStaggeredLayoutManager(): StaggeredGridLayoutManager {
        val displayMetrics = requireActivity().resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val spanCount = (dpWidth / 150).toInt()
        val lm = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        lm.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        return lm
    }

    private fun getLayoutManager(): RecyclerView.LayoutManager {
        if (!viewTypeState.value) return getStaggeredLayoutManager()
        return LinearLayoutManager(requireContext())
    }
}