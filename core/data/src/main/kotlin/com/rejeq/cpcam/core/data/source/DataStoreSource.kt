package com.rejeq.cpcam.core.data.source

import android.util.Log
import androidx.datastore.core.DataStoreFactory
import com.rejeq.cpcam.data.datastore.AppPreferencesKt
import com.rejeq.cpcam.data.datastore.copy
import java.io.File
import java.io.IOException
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.shareIn

/**
 * Core data persistence layer using Proto DataStore.
 * Provides access to app preferences with retry logic and error handling.
 *
 * @property scope CoroutineScope for managing datastore operations
 * @property produceFile File location where preferences will be stored
 */
class DataStoreSource(val scope: CoroutineScope, produceFile: () -> File) {
    private val dataStore = DataStoreFactory.create(
        serializer = DataStoreSerializer(),
        produceFile = produceFile,
    )

    /**
     * Shared flow of app preferences with error handling.
     * Catches IOExceptions and logs them while propagating other exceptions.
     */
    val store = dataStore.data
        .catch { exception ->
            Log.e(TAG, "Exception caught: $exception")

            when (exception) {
                is IOException -> {
                    // TODO: Notify user about error
                    Log.w(TAG, "Ignoring IOException on dataStore")
                }
                else -> throw exception
            }
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(5000), 1)

    /**
     * Attempts to edit preferences with exponential backoff retry logic.
     *
     * @param block Lambda containing preference modifications
     * @return [EditResult] indicating success or failure
     */
    suspend fun tryEdit(block: AppPreferencesKt.Dsl.() -> Unit): EditResult {
        repeat(RETRY_COUNT) { attempt ->
            try {
                dataStore.updateData { it.copy(block) }
                return EditResult.Success
            } catch (e: IOException) {
                Log.e(TAG, "Failed to edit data, attempt ${attempt + 1}", e)
            }

            val backoffTime = (BASE_DELAY * 2.0.pow(attempt)).toLong()
                .coerceAtMost(MAX_DELAY)

            delay(backoffTime)
        }

        return EditResult.FailWrite
    }
}

/**
 * Result of a preferences edit operation.
 */
sealed interface EditResult {
    /** Edit completed successfully */
    object Success : EditResult

    /** Failed to write preferences to disk after retries */
    object FailWrite : EditResult
}

private const val RETRY_COUNT: Int = 3
private const val BASE_DELAY: Long = 500
private const val MAX_DELAY: Long = 5000

private const val TAG = "DataStoreRepository"
