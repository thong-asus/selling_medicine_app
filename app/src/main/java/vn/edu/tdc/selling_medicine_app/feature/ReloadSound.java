package vn.edu.tdc.selling_medicine_app.feature;

import android.content.Context;
import android.media.MediaPlayer;

import vn.edu.tdc.selling_medicine_app.R;

public class ReloadSound {
    private MediaPlayer mediaPlayer;
    private Context context;

    public ReloadSound(Context context) {
        this.context = context;
        this.mediaPlayer = MediaPlayer.create(context, R.raw.reload_sound);
    }

    public void playReloadSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
