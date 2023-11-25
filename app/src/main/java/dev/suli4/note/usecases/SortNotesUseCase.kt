package dev.suli4.note.usecases

import dev.suli4.note.db.serializer.SortingModel
import dev.suli4.note.model.NoteModel
import dev.suli4.note.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SortNotesUseCase  @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(sortingModel: SortingModel): Flow<List<NoteModel>> {
        if (sortingModel.field == NoteModel.Fields.Title) {
            if (sortingModel.isAsc) return notesRepository.sortByTitleAsc()
            return notesRepository.sortByTitleDesc()
        } else if (sortingModel.field == NoteModel.Fields.CreatedAt) {
            if (sortingModel.isAsc) return notesRepository.sortByCreatedAtAsc()
            return notesRepository.sortByCreatedAtDesc()
        }
        return notesRepository.sortByTitleAsc()
    }
}
