package com.example.multimedia;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.multimedia.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Mapas para almacenar los objetos MediaPlayer y las posiciones de los audios
    private Map<Integer, MediaPlayer> mediaPlayers = new HashMap<>();
    private Map<Integer, Integer> audioPositions = new HashMap<>();


    // IDs de las imágenes y audios correspondientes
    private int[] imageIds = {R.id.luffy, R.id.zoro, R.id.brook, R.id.bartolomeo, R.id.chopper, R.id.franky};
    private int[] audioIds = {R.raw.luffy, R.raw.zoro, R.raw.brook, R.raw.bartolomeo, R.raw.chopper, R.raw.franky};


    // Constantes para SharedPreferences y la duración máxima de pausa (para guardar la posicion de los auidios cuando se sale de la aplicación)
    private static final String PREFS_NAME = "AudioPlayerPrefs";
    private static final String LAST_PAUSE_TIME_KEY = "lastPauseTime";
    private static final long MAX_PAUSE_DURATION = 30000; // 30 segundos

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Configurar los listeners para los ImageView
        for (int i = 0; i < imageIds.length; i++) {
            int imageId = imageIds[i];
            int audioId = audioIds[i];

            ImageView imageView = findViewById(imageId);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleAudio(imageId, audioId);
                }
            });
        }

        // Obtener el tiempo de la última pausa
        long lastPauseTime = sharedPreferences.getLong(LAST_PAUSE_TIME_KEY, 0);
        long currentTime = SystemClock.elapsedRealtime();

        // Restaurar posiciones de audio si la pausa fue menor a la duración máxima permitida
        if (currentTime - lastPauseTime < MAX_PAUSE_DURATION) {
            restoreAudioPositions();
        } else {
            resetAudioPositions();
        }
    }

    private void handleAudio(int imageId, int audioId) {
        // Obtener el MediaPlayer asociado a la imagen
        MediaPlayer mediaPlayer = mediaPlayers.get(imageId);
        if (mediaPlayer == null) {
            // Crear un nuevo MediaPlayer si no existe
            mediaPlayer = MediaPlayer.create(this, audioId);
            mediaPlayers.put(imageId, mediaPlayer);
        }

        if (mediaPlayer.isPlaying()) {
            // Pausar y guardar la posición si se está reproduciendo
            audioPositions.put(imageId, mediaPlayer.getCurrentPosition());
            mediaPlayer.pause();
        } else {
            // Reanudar desde la posición guardada si está pausado
            Integer position = audioPositions.get(imageId);
            if (position != null) {
                mediaPlayer.seekTo(position);
            }
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar todos los MediaPlayers y guardar sus posiciones
        for (Map.Entry<Integer, MediaPlayer> entry : mediaPlayers.entrySet()) {
            MediaPlayer mediaPlayer = entry.getValue();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                audioPositions.put(entry.getKey(), mediaPlayer.getCurrentPosition());
                mediaPlayer.pause();
            }
        }

        // Guardar el tiempo actual de pausa en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LAST_PAUSE_TIME_KEY, SystemClock.elapsedRealtime());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Obtener el tiempo de la última pausa
        long lastPauseTime = sharedPreferences.getLong(LAST_PAUSE_TIME_KEY, 0);
        long currentTime = SystemClock.elapsedRealtime();

        // Restaurar posiciones de audio si la pausa fue menor a la duración máxima permitida
        if (currentTime - lastPauseTime < MAX_PAUSE_DURATION) {
            restoreAudioPositions();
        } else {
            resetAudioPositions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar todos los recursos de MediaPlayer
        for (MediaPlayer mediaPlayer : mediaPlayers.values()) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        }
        mediaPlayers.clear();
    }

    private void restoreAudioPositions() {
        // Restaurar las posiciones de los audios guardadas
        for (Map.Entry<Integer, Integer> entry : audioPositions.entrySet()) {
            MediaPlayer mediaPlayer = mediaPlayers.get(entry.getKey());
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(entry.getValue());
            }
        }
    }

    private void resetAudioPositions() {
        // Reiniciar las posiciones de los audios
        audioPositions.clear();
    }
}