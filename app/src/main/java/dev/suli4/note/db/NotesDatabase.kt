package dev.suli4.note.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.suli4.note.db.dao.NotesDao
import dev.suli4.note.model.NoteModel

@Database(entities = [NoteModel::class], version = 1)
abstract class NotesDatabase: RoomDatabase() {
    abstract fun notesDao(): NotesDao
}