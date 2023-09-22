package dev.suli4.note.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.suli4.note.db.NotesDatabase
import dev.suli4.note.db.dao.NotesDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotesDatabase {
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java, "database-notes"
        ).build()
    }

    @Provides
    @Singleton
    fun providesNotesDao(
        database: NotesDatabase
    ): NotesDao {
        return database.notesDao()
    }

}