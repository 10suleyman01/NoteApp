package dev.suli4.note.usecases

import dev.suli4.note.model.NoteModel
import dev.suli4.note.repository.NotesRepository
import javax.inject.Inject

class DeleteNoteUseCase@Inject constructor(
    private val notesRepository: NotesRepository
) {

    suspend operator fun invoke(note: NoteModel) {
        notesRepository.delete(note)
    }

}