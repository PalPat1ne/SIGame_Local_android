package com.example.sigame

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sigame.R

class GameActivity : AppCompatActivity() {

    private lateinit var viewModel: GameViewModel
    private lateinit var indicator: ImageView
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val dataStorage = DataStorage(this)
        viewModel = ViewModelProvider(this, GameViewModelFactory(dataStorage))[GameViewModel::class.java]

        indicator = findViewById(R.id.connectionIndicator)
        sendButton = findViewById(R.id.sendButton)
        val playerNameView = findViewById<TextView>(R.id.playerNameTextView)
        playerNameView.text = dataStorage.playerName

        sendButton.setOnClickListener {
            viewModel.sendICommand()
        }

        viewModel.connectionStatus.observe(this) { connected ->
            // Обновляем индикатор
            indicator.setImageResource(if (connected) R.drawable.ic_connected else R.drawable.ic_disconnected)
        }

        viewModel.connectIfNeeded()
    }
}

class GameViewModelFactory(private val dataStorage: DataStorage) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(dataStorage) as T
    }
}
