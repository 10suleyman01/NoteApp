package dev.suli4.note.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.suli4.note.model.NoteModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NoteModel>>

    @Query("SELECT * FROM notes WHERE title LIKE :query OR text LIKE :query || '%'")
    fun searchNotes(query: String?): Flow<List<NoteModel>>

    @Insert
    suspend fun insert(note: NoteModel)

    @Update
    suspend fun update(note: NoteModel)

    @Delete
    suspend fun delete(note: NoteModel)

}