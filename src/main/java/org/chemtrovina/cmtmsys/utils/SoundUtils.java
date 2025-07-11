package org.chemtrovina.cmtmsys.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import java.net.URL;

public class SoundUtils {
    public static void playSound(String fileName) {
        try {
            URL soundURL = SoundUtils.class.getResource("/org/chemtrovina/cmtmsys/sounds/" + fileName);
            if (soundURL == null) {
                System.err.println("Không tìm thấy file âm thanh: " + fileName);
                return;
            }
            Media sound = new Media(soundURL.toString());
            MediaPlayer player = new MediaPlayer(sound);
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
