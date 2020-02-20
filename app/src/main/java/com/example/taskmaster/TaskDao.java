package com.example.taskmaster;


import androidx.room.Dao;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM task")
    List<Task> getAll();

    @Query("SELECT * FROM task WHERE id= :id")
    Task getOne(long id);

    @Insert
     public void saveTask(Task task);


}
