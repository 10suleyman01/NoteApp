package dev.suli4.note.di

import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.suli4.note.db.dao.NotesDao
import dev.suli4.note.presentation.notes.adapter.NoteAdapter
import dev.suli4.note.repository.NotesRepository
import dev.suli4.note.repository.NotesRepositoryDb
import dev.suli4.note.usecases.AllUseCases
import dev.suli4.note.usecases.DeleteNoteUseCase
import dev.suli4.note.usecases.EditNoteUseCase
import dev.suli4.note.usecases.GetAllNotesUseCase
import dev.suli4.note.usecases.InsertNoteUseCase
import dev.suli4.note.usecases.SearchNotesUseCase
import dev.suli4.note.viewmodel.NoteViewModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepositoryDatabase(
        notesDao: NotesDao
    ): NotesRepository {
        return NotesRepositoryDb(notesDao)
    }

    @Provides
    @Singleton
    fun provideUseCases(
        notesRepository: NotesRepository
    ): AllUseCases {
        return AllUseCases(
            getAllNotesUseCase = GetAllNotesUseCase(notesRepository),
            insertNoteUseCase = InsertNoteUseCase(notesRepository),
            searchNotesUseCase = SearchNotesUseCase(notesRepository),
            editNoteUseCase = EditNoteUseCase(notesRepository),
            deleteNoteUseCase = DeleteNoteUseCase(notesRepository)
        )
    }
}