package com.example.sigame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AuthViewModel(private val dataStorage: DataStorage) : ViewModel() {

    private val _authResult = MutableLiveData<Result<String>>()
    val authResult: LiveData<Result<String>> = _authResult

    private var connectionManager: ConnectionManager? = null

    fun attemptLogin(name: String, birthDate: String?, serverInput: String) {
        // Примерный парсинг сервера и порта
        val parts = serverInput.split(":")
        if (parts.size != 2) {
            _authResult.postValue(Result.failure(Exception("Некорректный формат адреса.")))
            return
        }

        val host = parts[0]
        val port = parts[1].toIntOrNull() ?: 0
        if (port == 0) {
            _authResult.postValue(Result.failure(Exception("Некорректный порт.")))
            return
        }

        if (name.isBlank()) {
            _authResult.postValue(Result.failure(Exception("Имя не может быть пустым.")))
            return
        }

        // Генерируем UUID, если его нет
        dataStorage.generateAndStoreUUIDIfNeeded()
        val uuid = dataStorage.uuid!!

        CoroutineScope(Dispatchers.IO).launch {
            connectionManager = ConnectionManager(host, port)
            val connected = connectionManager?.connect() == true
            if (!connected) {
                _authResult.postValue(Result.failure(Exception("Не удалось подключиться к серверу.")))
                return@launch
            }

            val loginCommand = "|login $name;${birthDate ?: ""};$uuid"
            val sendSuccess = connectionManager?.sendMessage(loginCommand) == true
            if (!sendSuccess) {
                _authResult.postValue(Result.failure(Exception("Не удалось отправить команду авторизации.")))
                return@launch
            }

            val response = connectionManager?.readResponse()
            withContext(Dispatchers.Main) {
                if (response == "OK") {
                    dataStorage.playerName = name
                    dataStorage.birthDate = birthDate
                    dataStorage.server = host
                    dataStorage.port = port
                    _authResult.value = Result.success("OK")
                } else {
                    _authResult.value = Result.failure(Exception(response ?: "Неизвестная ошибка"))
                }
            }
        }
    }
}
