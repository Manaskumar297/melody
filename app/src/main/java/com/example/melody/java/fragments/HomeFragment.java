package com.example.melody.java.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.melody.R;
import com.example.melody.java.Adapters.MusicAdapter;
import com.example.melody.java.Model.Music_files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements MusicAdapter.OnItemClickListener {

    TextView audio_name;
    RecyclerView recyclerView;
   SeekBar seekBar;
    private MusicAdapter musicAdapter = null;
    private MediaPlayer mediaPlayer;
    CircleImageView playbtn;
    private static final String PREF_CURRENT_POSITION = "current_position";
    private static final String PREF_CURRENT_SONG_PATH = "current_song_path";
    private SharedPreferences sharedPreferences;
    private int currentPosition;
    private String currentSongPath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        // Restore the playback state from SharedPreferences
        currentPosition = sharedPreferences.getInt(PREF_CURRENT_POSITION, 0);
        currentSongPath = sharedPreferences.getString(PREF_CURRENT_SONG_PATH, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = rootView.findViewById(R.id.music_library);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        seekBar=rootView.findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
        playbtn=rootView.findViewById(R.id.play_btn);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
        // Retrieve music files from local storage
        List<Music_files> musicList = getLocalMusicFiles();

        // Initialize adapter
        musicAdapter = new MusicAdapter(musicList, getContext());
        recyclerView.setAdapter(musicAdapter);
        audio_name=rootView.findViewById(R.id.audio_name);

        // Set OnClickListener for the CardView
       audio_name .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullScreenDialog();

            }
        });

        // Set the item click listener for the music adapter
        musicAdapter.setOnItemClickListener(this);

        return rootView;
    }
    private void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updatePlayButtonIcon(false);
        } else {
            mediaPlayer.start();
            updatePlayButtonIcon(true);
            updateProgressBar();
        }
    }
    // Method to show dialog box
    private void showFullScreenDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.play_dialog_box, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.FullScreenDialog);
        builder.setView(dialogView);
        builder.setCancelable(true); // Allow clicking outside to dismiss the dialog

        AlertDialog dialog = builder.create();
        dialog.show();

        // Pass MediaPlayer instance and seek bar progress to the dialog
        SeekBar dialogSeekBar = dialogView.findViewById(R.id.dialog_seekbar);
        dialogSeekBar.setMax(mediaPlayer.getDuration());
        dialogSeekBar.setProgress(mediaPlayer.getCurrentPosition());
        ImageButton dialogPlayButton = dialogView.findViewById(R.id.dialog_playbtn);
        dialogPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    updatePlayButtonIcon(false);
                } else {
                    mediaPlayer.start();
                    updatePlayButtonIcon(true);
                    updateProgressBar();
                }
            }
        });

        TextView dialogAudioName = dialogView.findViewById(R.id.song_name);
        dialogAudioName.setText(audio_name.getText().toString());
    }


    // Method to retrieve music files from local storage
    private List<Music_files> getLocalMusicFiles() {
        List<Music_files> musicList = new ArrayList<>();

        // Querying MediaStore to get music files
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA // Path to the music file
        };

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int titleColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                if (titleColumnIndex != -1 && artistColumnIndex != -1 && pathColumnIndex != -1) {
                    do {
                        String title = cursor.getString(titleColumnIndex);
                        String artist = cursor.getString(artistColumnIndex);
                        String path = cursor.getString(pathColumnIndex);
                        // Create Music_files object and add it to the list
                        musicList.add(new Music_files(title, artist, path));
                    } while (cursor.moveToNext());
                } else {
                    Log.e("HomeFragment", "One or more columns not found in cursor.");
                }
            } else {
                Log.e("HomeFragment", "Cursor is empty.");
            }
            cursor.close();
        } else {
            Log.e("HomeFragment", "Cursor is null.");
        }

        return musicList;
    }

    public void onItemClick(Music_files musicFile) {
        audio_name = getView().findViewById(R.id.audio_name);
        audio_name .setText(musicFile.getTitle());
        currentSongPath = musicFile.getPath(); // Update the current song path
        currentPosition = 0; // Reset playback position when a new song is selected
        mediaPlayer.release();
        mediaPlayer = null;

        // Create a new instance of MediaPlayer
        mediaPlayer = new MediaPlayer();
        setupMediaPlayer(currentSongPath, currentPosition); // Set up MediaPlayer with the new song
    }
    private void setupMediaPlayer(String songPath, int playbackPosition) {
        try {
            // Reset MediaPlayer if it's already playing
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            // Set data source, prepare, and seek to the saved playback position
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();

            // Update play button icon to "pause"
            updatePlayButtonIcon(true);

            // Set OnCompletionListener to play the next song when current song completes
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Increment currentPosition to play the next song
                    currentPosition++;
                    // Check if there are more songs in the list
                    if (currentPosition < musicAdapter.getItemCount()) {
                        // Get the next song from the list and play it
                        Music_files nextSong = musicAdapter.getItem(currentPosition);
                        onItemClick(nextSong);
                    } else {
                        // If there are no more songs, stop MediaPlayer
                        mediaPlayer.stop();
                        // Reset currentPosition to the beginning of the list
                        currentPosition = 0;
                    }
                }
            });

            // Start updating progress bar
            updateProgressBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onDestroy() {
        super.onDestroy();
        // Save the current playback position and song path to SharedPreferences when the app is closed
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_CURRENT_POSITION, mediaPlayer.getCurrentPosition());
        editor.putString(PREF_CURRENT_SONG_PATH, currentSongPath);
        editor.putInt("seekBarPosition", seekBar.getProgress());
        editor.apply();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Check if there is a saved song path and playback position
        if (currentSongPath != null) {
            // Set up MediaPlayer with the saved song path and playback position
            setupMediaPlayer(currentSongPath, currentPosition);
            // Set the text of the TextView to the name of the music file
            audio_name.setText(getMusicFileName(currentSongPath));
            // Set the progress of the seek bar to the last saved position
            int savedSeekBarPosition = sharedPreferences.getInt("seekBarPosition", currentPosition);
            seekBar.setProgress(savedSeekBarPosition);
            mediaPlayer.pause();
            updatePlayButtonIcon(false);
        }
    }
    private void updateProgressBar() {
        View rootView = getView();
        if (rootView != null) {
            ProgressBar progressBar = rootView.findViewById(R.id.seek_bar);
            if (progressBar != null) {
                progressBar.setMax(mediaPlayer.getDuration()); // Set maximum progress value

                // Update progress continuously while MediaPlayer is playing
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            progressBar.setProgress(mediaPlayer.getCurrentPosition());
                            updateProgressBar();
                            // Recursive call to update progress continuously
                        }
                    }
                }, 10); // Update progress every 1 second (adjust as needed)
            }
        }
    }
    private void updatePlayButtonIcon(boolean isPlaying) {
        playbtn = getView().findViewById(R.id.play_btn);
        if (isPlaying) {
            playbtn.setImageResource(R.drawable.baseline_pause_24); // Change icon to "pause" when playing
        } else {
            playbtn.setImageResource(R.drawable.play_btn); // Change icon to "play" when paused
        }
    }
    private String getMusicFileName(String filePath) {
        // Split the file path by "/"
        String[] parts = filePath.split("/");
        // Get the last part which represents the file name
        String fileName = parts[parts.length - 1];
        // Remove the file extension, if present
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            fileName = fileName.substring(0, dotIndex);
        }
        return fileName;
    }

}
