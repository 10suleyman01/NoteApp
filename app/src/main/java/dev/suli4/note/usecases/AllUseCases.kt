package dev.suli4.note.usecases

data class AllUseCases(
    val getAllNotesUseCase: GetAllNotesUseCase,
    val insertNoteUseCase: InsertNoteUseCase,
    val searchNotesUseCase: SearchNotesUseCase,
    val editNoteUseCase: EditNoteUseCase,
    val deleteNoteUseCase: DeleteNoteUseCase,
)