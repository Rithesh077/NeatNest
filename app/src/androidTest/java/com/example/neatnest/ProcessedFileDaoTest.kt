package com.example.neatnest

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.neatnest.data.model.ProcessedFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessedFileDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var processedFileDao: ProcessedFileDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        processedFileDao = database.processedFileDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetFile() = runBlocking {
        val processedFile = ProcessedFile("/path/to/test.jpg", "test.jpg", "/path/to/target", "jpg")
        processedFileDao.insert(processedFile)

        val allFiles = processedFileDao.getAll()
        assertEquals(allFiles.first().get(0), processedFile)
    }
}
