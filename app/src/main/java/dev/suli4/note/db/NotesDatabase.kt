package dev.suli4.note.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.suli4.note.db.converters.BitmapConverters
import dev.suli4.note.db.dao.NotesDao
import dev.suli4.note.model.NoteModel

@Database(entities = [NoteModel::class], version = 5)
@TypeConverters(BitmapConverters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}