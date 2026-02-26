package com.example.neatnest

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FileMoverTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSourceUri: Uri

    @Mock
    private lateinit var mockTargetDir: DocumentFile

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `copyFileToDirectory should return null when create file fails`() {
        `when`(mockTargetDir.createFile("*/*", "test.txt")).thenReturn(null)

        val result = FileMover.copyFileToDirectory(mockContext, mockSourceUri, mockTargetDir, "test.txt", null)

        assert(result == null)
    }
}
