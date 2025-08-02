package com.sgionotes.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import com.sgionotes.database.dao.NoteDao;
import com.sgionotes.database.dao.TagDao;
import com.sgionotes.database.entities.NoteEntity;
import com.sgionotes.database.entities.TagEntity;

@Database(
    entities = {NoteEntity.class, TagEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "smartgionotes_database";

    public abstract NoteDao noteDao();
    public abstract TagDao tagDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .allowMainThreadQueries() // Solo para desarrollo, en producción usar AsyncTask o Room with coroutines
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
