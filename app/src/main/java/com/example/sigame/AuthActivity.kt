package com.example.sigame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sigame.R

/**
 * Экран авторизации:
 * - Позволяет ввести имя, дату рождения, адрес сервера и порт.
 * - Позволяет выбрать аватар.
 * - При нажатии кнопки "Подключиться" пытается установить соединение и авторизоваться на сервере.
 * После успешной авторизации переходит на игровой экран.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    // Регистрируем контракт для выбора изображения (аватара)
    private val avatarPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            findViewById<ImageView>(R.id.avatarImageView).setImageURI(uri)
            // При необходимости можно сохранить URI или путь к изображению в DataStorage или локально
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Инициализируем хранилище данных
        val dataStorage = DataStorage(this)

        // Создаём ViewModel с фабрикой, чтобы передать зависимость DataStorage
        viewModel = ViewModelProvider(this, AuthViewModelFactory(dataStorage))[AuthViewModel::class.java]

        // Находим необходимые View
        val nameEdit = findViewById<EditText>(R.id.nameEditText)
        val birthDateEdit = findViewById<EditText>(R.id.birthDateEditText)
        val serverEdit = findViewById<EditText>(R.id.serverEditText)
        val selectAvatarButton = findViewById<Button>(R.id.selectAvatarButton)
        val connectButton = findViewById<Button>(R.id.connectButton)
        val avatarImageView = findViewById<ImageView>(R.id.avatarImageView)

        // Загружаем сохранённые данные, если есть
        dataStorage.playerName?.let { nameEdit.setText(it) }
        dataStorage.birthDate?.let { birthDateEdit.setText(it) }
        dataStorage.server?.let { server ->
            val port = dataStorage.port
            if (port > 0) {
                serverEdit.setText("$server:$port")
            }
        }

        // Обработчик выбора аватара
        selectAvatarButton.setOnClickListener {
            avatarPicker.launch("image/*")
        }

        // Обработчик нажатия "Подключиться"
        connectButton.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val birthDate = birthDateEdit.text.toString().trim().takeIf { it.isNotBlank() }
            val serverInput = serverEdit.text.toString().trim()

            // Пытаемся выполнить авторизацию через ViewModel
            viewModel.attemptLogin(name, birthDate, serverInput)
        }

        // Подписываемся на результат авторизации
        viewModel.authResult.observe(this) { result ->
            result.fold(onSuccess = { response ->
                if (response == "OK") {
                    // Успешная авторизация: переходим на экран игры
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    finish() // Закрываем экран авторизации, чтобы не возвращаться назад
                }
            }, onFailure = { error ->
                // Если произошла ошибка, показываем сообщение
                Toast.makeText(this, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

/**
 * Фабрика для AuthViewModel, чтобы можно было передать зависимость DataStorage.
 */
class AuthViewModelFactory(private val dataStorage: DataStorage) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(dataStorage) as T
    }
}
