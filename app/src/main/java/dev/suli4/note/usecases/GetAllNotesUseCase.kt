package dev.suli4.note.usecases

import dev.suli4.note.model.NoteModel
import dev.suli4.note.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {

    operator fun invoke(): Flow<List<NoteModel>> {
        return notesRepository.getAll()
    }

}