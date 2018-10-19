package com.quartz.zielclient.voip;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.quartz.zielclient.R;

import static android.content.Context.AUDIO_SERVICE;

public class SoundPoolManager {

  private boolean playing = false;
  private boolean loaded = false;
  private boolean playingCalled = false;
  private float volume;
  private SoundPool soundPool;
  private int ringingSoundId;
  private int ringingStreamId;
  private int disconnectSoundId;
  private static SoundPoolManager instance;

  /**
   * Manages sound pool for voice recording.
   *
   * @param context The current context.
   */
  private SoundPoolManager(Context context) {
    // AudioManager audio settings for adjusting the volume
    AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
    if (audioManager != null) {
      float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      volume = actualVolume / maxVolume;
    }

    // Load the sounds
    int maxStreams = 1;
    soundPool = new SoundPool.Builder()
        .setMaxStreams(maxStreams)
        .build();

    soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
      loaded = true;
      if (playingCalled) {
        playRinging();
        playingCalled = false;
      }
    });
    ringingSoundId = soundPool.load(context, R.raw.incoming, 1);
    disconnectSoundId = soundPool.load(context, R.raw.disconnect, 1);
  }

  /**
   * Returns instance of sound pool manager.
   *
   * @param context The current context of the app.
   * @return SoundPoolManager Return the sound pool manager.
   */
  public static SoundPoolManager getInstance(Context context) {
    if (instance == null) {
      instance = new SoundPoolManager(context);
    }
    return instance;
  }

  /**
   * Rings the user for the video stream.
   */
  public void playRinging() {
    if (loaded && !playing) {
      if (soundPool != null) {
        ringingStreamId = soundPool.play(ringingSoundId, volume, volume, 1, -1, 1f);
      }
      playing = true;
    } else {
      playingCalled = true;
    }
  }

  /**
   * Stops the video in ringing mode.
   */
  public void stopRinging() {
    if (playing) {
      if (soundPool != null) {
        soundPool.stop(ringingStreamId);
      }
      playing = false;
    }
  }

  /**
   * Plays the video stream.
   */
  public void playDisconnect() {
    if (loaded && !playing) {
      soundPool.play(disconnectSoundId, volume, volume, 1, 0, 1f);
      playing = false;
    }
  }

  /**
   * Release the video stream and tear down all variables.
   */
  public static void release() {
    if (instance != null) {
      SoundPool pool = instance.soundPool;
      if (pool != null) {
        pool.unload(instance.ringingSoundId);
        pool.unload(instance.disconnectSoundId);
        pool.release();
        instance.soundPool = null;
      }
    }

    instance = null;
  }
}