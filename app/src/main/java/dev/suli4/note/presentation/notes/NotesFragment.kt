package dev.suli4.note.presentation.notes

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.suli4.note.R
import dev.suli4.note.databinding.FragmentNotesBinding
import dev.suli4.note.ext.PreferencesKeys
import dev.suli4.note.ext.dataStore
import dev.suli4.note.ext.getDrawableCompat
import dev.suli4.note.model.NoteModel
import dev.suli4.note.presentation.MainActivity
import dev.suli4.note.presentation.notes.adapter.NoteAdapter
import dev.suli4.note.presentation.notes.adapter.NoteClickListener
import dev.suli4.note.presentation.notes.adapter.NotesKeyProvider
import dev.suli4.note.viewmodel.NoteViewModel
import dev.suli4.note.viewmodel.NoteViewModel.Companion.GRID_VIEW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val SELECTION_NOTES_ID = "selection-notes"

    }

    private val viewModel: NoteViewModel by activityViewModels()
    private lateinit var adapter: NoteAdapter

    private var menuDeleteItem: MenuItem? = null
    private var searchItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setViewTypeState(context?.dataStore!!.data.map { preferences ->
                preferences[PreferencesKeys.ViewTypeSettings]
            }.first() ?: false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.hasSelection()) {
                        viewModel.clearSelection()
                    } else {
                        requireActivity().finish()
                    }
                }
            })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.trackerState.value?.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModel.trackerState.value?.onRestoreInstanceState(savedInstanceState)
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

        adapter = NoteAdapter(viewModel)
        adapter.setNoteListener(noteClickListener)

        // init menu
        addMenu()

        binding.apply {
            rvNotes.adapter = adapter
            rvNotes.layoutManager = getLayoutManager()
            rvNotes.setHasFixedSize(true)

            // init selection tracker
            initTracker()

            viewModel.trackerState.value?.addObserver(selectionObserver())

            fabNewNote.setOnClickListener {
                val action = NotesFragmentDirections.actionNotesFragmentToCreateNoteFragment()
                findNavController().navigate(action)
            }

        }

        setSubTitle(viewModel.selectedItemsState.value)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.loadNotes()

                viewModel.state.collectLatest { state ->
                    when (state) {
                        is NoteViewModel.NotesState.GetAllNotes -> {
                            binding.lottieAnimationEmptyNotes.isVisible = state.notes.isEmpty()
                            adapter.setNotes(state.notes)
                            Log.d("Notes", "onViewCreated: set notes")
                        }

                        is NoteViewModel.NotesState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun addMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_notes, menu)
                menu.findItem(R.id.viewType).icon = getIconViewType()

                menuDeleteItem = menu.findItem(R.id.delete)
                menuDeleteItem?.isVisible = viewModel.deleteActionIsVisible.value

                searchItem = menu.findItem(R.id.search)
                val searchView = searchItem?.actionView as SearchView
                val searchQueryState = viewModel.searchQueryState.value

                if (searchQueryState.isNotEmpty()) {
                    searchItem?.expandActionView()
                    searchView.setQuery(searchQueryState, true)
                    searchView.clearFocus()
                    viewModel.searchItems(searchQueryState)
                }

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        viewModel.searchItems(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setQueryState(newText ?: "")
                        viewModel.searchItems(newText)
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.viewType -> {
                        viewModel.setViewTypeState(
                            !viewModel.viewTypeState.value
                        )
                        lifecycleScope.launch {
                            updateView(viewModel.viewTypeState.value)
                        }
                        menuItem.icon = getIconViewType()
                        binding.rvNotes.layoutManager = getLayoutManager()
                        // animation change layout manager spans
                        animateChangeView()
                    }

                    R.id.delete -> {
                        viewModel.deleteNotes(*viewModel.shouldToDeleteItems.value.toTypedArray())
                        viewModel.clearSelection()
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initTracker() {
        viewModel.setTracker(
            SelectionTracker.Builder(
                SELECTION_NOTES_ID,
                binding.rvNotes,
                NotesKeyProvider(adapter),
                NoteAdapter.ItemLookup(binding.rvNotes),
                StorageStrategy.createParcelableStorage(NoteModel::class.java)
            ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            ).build()
        )
    }

    private fun animateChangeView() {
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.duration = 300
        binding.rvNotes.startAnimation(fadeIn)
    }

    private fun selectionObserver(): SelectionTracker.SelectionObserver<NoteModel> =
        object : SelectionTracker.SelectionObserver<NoteModel>() {

            override fun onItemStateChanged(key: NoteModel, selected: Boolean) {
                super.onItemStateChanged(key, selected)

                if (selected) {
                    viewModel.addItem(key)
                } else {
                    viewModel.removeItem(key)
                }

            }

            override fun onSelectionChanged() {
                val notes: Int? = viewModel.trackerState.value?.selection?.size()
                notes?.let { size ->
                    if (size > 0) {
                        viewModel.setItemsSelected("Выбрано: $size")
                        viewModel.setShowDeleteAction(true)
                        binding.fabNewNote.isVisible = false
                        searchItem?.isVisible = false
                    } else {
                        viewModel.setItemsSelected()
                        viewModel.setShowDeleteAction(false)
                        binding.fabNewNote.isVisible = true
                        searchItem?.isVisible = true
                    }
                    menuDeleteItem?.isVisible = viewModel.deleteActionIsVisible.value
                    setSubTitle(viewModel.selectedItemsState.value)
                }
            }
        }


    suspend fun updateView(type: Boolean) {
        context?.dataStore?.edit { settings ->
            settings[PreferencesKeys.ViewTypeSettings] = type
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun getIconViewType(): Drawable? {
        // if current type is GRID then icon to list type
        if (!viewModel.viewTypeState.value) return requireContext().getDrawableCompat(R.drawable.baseline_view_list_24)

        return requireContext().getDrawableCompat(
            R.drawable.baseline_grid_view_24
        )
    }

    private fun setSubTitle(subtitle: String) {
        (requireActivity() as MainActivity).supportActionBar?.subtitle = subtitle
    }

    private fun getStaggeredLayoutManager(isLinear: Boolean): StaggeredGridLayoutManager {
        val displayMetrics = requireActivity().resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val spanCount = if (isLinear) 1 else (dpWidth / 150).toInt()
        val lm = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        lm.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        return lm
    }

    private fun getLayoutManager(): StaggeredGridLayoutManager {
        if (viewModel.viewTypeState.value == GRID_VIEW) return getStaggeredLayoutManager(GRID_VIEW)
        return getStaggeredLayoutManager(!GRID_VIEW)
    }
}