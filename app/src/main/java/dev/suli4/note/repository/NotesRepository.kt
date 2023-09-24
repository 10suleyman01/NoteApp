package dev.suli4.note.repository

import dev.suli4.note.model.NoteModel
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAll(): Flow<List<NoteModel>>

    suspend fun insert(note: NoteModel)

    suspend fun update(note: NoteModel)

    suspend fun searchNotes(query: String?): Flow<List<NoteModel>>

    suspend fun delete(note: NoteModel)
}