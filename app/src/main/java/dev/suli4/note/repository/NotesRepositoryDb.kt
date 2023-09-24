package dev.suli4.note.repository

import dev.suli4.note.db.dao.NotesDao
import dev.suli4.note.model.NoteModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NotesRepositoryDb @Inject constructor(
    private val notesDao: NotesDao
): NotesRepository {
    override fun getAll(): Flow<List<NoteModel>> {
        return notesDao.getAll()
    }

    override suspend fun insert(note: NoteModel) {
        notesDao.insert(note)
    }

    override suspend fun update(note: NoteModel) {
        notesDao.update(note)
    }

    override suspend fun searchNotes(query: String?): Flow<List<NoteModel>> {
        return notesDao.searchNotes(query)
    }

    override suspend fun delete(note: NoteModel) {
        notesDao.delete(note)
    }
}