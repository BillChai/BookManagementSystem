package org.example.quocardcodingtest.repository

import org.example.quocardcodingtest.exception.DatabaseException

/**
 * wrap sql execute with db error handler
 */
fun <T> wrapDatabaseOperation(operation: () -> T): T {
    return try {
        operation()
    } catch (e: Exception) {
        throw DatabaseException("Database error occurred", e)
    }
}