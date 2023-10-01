package dev.suli4.note.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.suli4.note.db.converters.BitmapConverters
import dev.suli4.note.db.dao.NotesDao
import dev.suli4.note.model.NoteModel

@Database(entities = [NoteModel::class], version = 2)
@TypeConverters(BitmapConverters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}