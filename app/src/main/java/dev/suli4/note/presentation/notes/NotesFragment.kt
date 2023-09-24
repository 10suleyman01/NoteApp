package dev.suli4.note.presentation.notes

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.R
import dev.suli4.note.databinding.FragmentNotesBinding
import dev.suli4.note.ext.PreferencesKeys
import dev.suli4.note.ext.dataStore
import dev.suli4.note.ext.getDrawable
import dev.suli4.note.ext.getModel
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.MainActivity
import dev.suli4.note.presentation.notes.adapter.NoteAdapter
import dev.suli4.note.presentation.notes.adapter.NoteClickListener
import dev.suli4.note.presentation.notes.adapter.NotesKeyProvider
import dev.suli4.note.viewmodel.NoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

        const val SELECTED_ITEMS_SAVE_STATE = "selected_items"
        const val DELETE_ACTION_SAVE_STATE = "delete_action"

        const val SELECTION_NOTES_ID = "selection-notes"

        const val GRID_VIEW = false
    }

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    private var tracker: SelectionTracker<Long>? = null
    private val shouldToDeleteItems: MutableList<NoteModel> = mutableListOf()

    private val viewTypeState: MutableStateFlow<Boolean> = MutableStateFlow(GRID_VIEW)
    private val selectedItemsState: MutableStateFlow<String> = MutableStateFlow("")
    private val deleteActionIsVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            viewTypeState.value = context?.dataStore!!.data.map { preferences ->
                preferences[PreferencesKeys.ViewTypeSettings]
            }.first() ?: GRID_VIEW
        }

        selectedItemsState.value = savedInstanceState?.getString(SELECTED_ITEMS_SAVE_STATE) ?: ""
        deleteActionIsVisible.value =
            savedInstanceState?.getBoolean(DELETE_ACTION_SAVE_STATE) ?: false

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)

        outState.putString(SELECTED_ITEMS_SAVE_STATE, selectedItemsState.value)
        outState.putBoolean(DELETE_ACTION_SAVE_STATE, deleteActionIsVisible.value)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker?.onRestoreInstanceState(savedInstanceState)
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

        adapter = NoteAdapter()
        adapter.setNoteListener(noteClickListener)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_notes, menu)
                menu.findItem(R.id.viewType).icon = getIconViewType()
                menuItem = menu.findItem(R.id.delete)
                menuItem?.isVisible = deleteActionIsVisible.value

                val searchItem = menu.findItem(R.id.search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchItems(newText)
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.viewType -> {
                        viewTypeState.value = !viewTypeState.value
                        lifecycleScope.launch {
                            updateView(viewTypeState.value)
                        }
                        menuItem.icon = getIconViewType()
                        binding.rvNotes.layoutManager = getLayoutManager()
                    }

                    R.id.search -> {

                    }

                    R.id.delete -> {
                        viewModel.deleteNotes(*shouldToDeleteItems.toTypedArray())
                        tracker?.clearSelection()
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.apply {
            rvNotes.adapter = adapter
            rvNotes.layoutManager = getLayoutManager()
            rvNotes.setHasFixedSize(true)

            tracker = SelectionTracker.Builder(
                SELECTION_NOTES_ID,
                binding.rvNotes,
                NotesKeyProvider(adapter),
                NoteAdapter.ItemLookup(binding.rvNotes),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            ).build()

            savedInstanceState?.let {
                tracker?.onRestoreInstanceState(it)
            }

            adapter.setTracker(tracker)

            tracker?.addObserver(selectionObserver())


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

        setSubTitle(selectedItemsState.value)


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.loadNotes()

                viewModel.state.collectLatest { state ->
                    when (state) {
                        is NoteViewModel.NotesState.GetAllNotes -> {
                            adapter.setNotes(state.notes)
                        }

                        is NoteViewModel.NotesState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun selectionObserver() =
        object : SelectionTracker.SelectionObserver<Long>() {

            override fun onItemStateChanged(key: Long, selected: Boolean) {
                super.onItemStateChanged(key, selected)

                if (selected) {
                    adapter.notes.find { it.id == key }?.let { shouldToDeleteItems.add(it) }
                } else {
                    shouldToDeleteItems.remove(adapter.notes.find { it.id == key })
                }

            }

            override fun onSelectionChanged() {

                val notes: Int? = tracker?.selection?.size()
                notes?.let { size ->
                    if (size > 0) {
                        selectedItemsState.value = "Выбрано: $size"
                        deleteActionIsVisible.value = true
                        binding.fabNewNote.isVisible = false
                    } else {
                        selectedItemsState.value = ""
                        deleteActionIsVisible.value = false
                        binding.fabNewNote.isVisible = true
                    }
                    menuItem?.isVisible = deleteActionIsVisible.value
                    setSubTitle(selectedItemsState.value)
                }
            }
        }


    suspend fun updateView(type: Boolean) {
        context?.dataStore?.edit { settings ->
            settings[PreferencesKeys.ViewTypeSettings] = type
        }
    }

    private fun getNoteFromBundle(bundle: Bundle): NoteModel? {
        return bundle.getModel(NOTE_KEY, NoteModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null

        Log.d("Notes", "onDestroy")

        clearFragmentResult(REQUEST_KEY_NEW_NOTE)
        clearFragmentResult(REQUEST_KEY_EDIT_NOTE)
    }

    private fun getIconViewType(): Drawable? {
        // if current type is GRID then icon to list type
        if (!viewTypeState.value) return getDrawable(R.drawable.baseline_view_list_24)

        return getDrawable(
            R.drawable.baseline_grid_view_24
        )
    }

    private fun setSubTitle(subtitle: String) {
        (requireActivity() as MainActivity).supportActionBar?.subtitle = subtitle
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
        if (viewTypeState.value == GRID_VIEW) return getStaggeredLayoutManager()
        return LinearLayoutManager(requireContext())
    }
}