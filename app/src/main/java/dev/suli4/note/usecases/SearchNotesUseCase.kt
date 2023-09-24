package dev.suli4.note.usecases

import dev.suli4.note.model.NoteModel
import dev.suli4.note.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {

    suspend operator fun invoke(query: String?): Flow<List<NoteModel>> {
        return notesRepository.searchNotes(query)
    }
}

