package com.example.sigame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sigame.DataStorage
import com.example.sigame.ConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameViewModel(private val dataStorage: DataStorage) : ViewModel() {

    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> = _connectionStatus

    private var connectionManager: ConnectionManager? = null

    fun connectIfNeeded() {
        val host = dataStorage.server ?: return
        val port = dataStorage.port
        CoroutineScope(Dispatchers.IO).launch {
            connectionManager = ConnectionManager(host, port)
            val connected = connectionManager?.connect() == true
            _connectionStatus.postValue(connected)
        }
    }

    fun sendICommand() {
        val uuid = dataStorage.uuid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            if (connectionManager?.isConnected() == true) {
                connectionManager?.sendMessage("|I $uuid")
            } else {
                _connectionStatus.postValue(false)
            }
        }
    }

    fun checkConnection() {
        _connectionStatus.postValue(connectionManager?.isConnected() == true)
    }
}
