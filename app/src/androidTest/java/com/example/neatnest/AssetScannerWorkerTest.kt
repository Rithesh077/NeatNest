package com.example.neatnest

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class AssetScannerWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testAssetScannerWorker_noPermissions() = runBlocking {
        Mockito.mockStatic(PermissionManager::class.java).use {
            Mockito.`when`(PermissionManager.hasStorageAccess(context)).thenReturn(false)

            val worker = TestListenableWorkerBuilder<AssetScannerWorker>(context).build()
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Failure)
        }
    }
}
