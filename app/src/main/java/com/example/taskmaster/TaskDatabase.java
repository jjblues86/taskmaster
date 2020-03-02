package com.example.taskmaster;

import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Task.class}, exportSchema = false, version = 2)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}
