package com.example.sigame

import kotlinx.coroutines.*
import java.io.*
import java.net.Socket

class ConnectionManager(
    private val host: String,
    private val port: Int
) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            socket = Socket(host, port)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun sendMessage(message: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            writer?.write(message)
            writer?.newLine()
            writer?.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun readResponse(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            reader?.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isConnected(): Boolean = (socket?.isConnected == true && !socket!!.isClosed)

    suspend fun close() = withContext(Dispatchers.IO) {
        try {
            reader?.close()
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
