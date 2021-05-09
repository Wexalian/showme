package com.wexalian.showme.util;

import com.wexalian.showme.core.Episode;

import java.text.Normalizer;

public class FileUtils {
    
    public static String getFileName(Episode episode) {
        return getFileName(episode.getNumber(), episode.getTitle());
    }
    
    public static String getFileName(int number, String title) {
        return "episode_" + String.format("%02d", number) + "_" + getSlug(title) + ".mkv";
    }
    
    public static String getSlug(String title) {
        String result = Normalizer.normalize(title.toLowerCase(), Normalizer.Form.NFD);
        result = result.toLowerCase()//
                       .replaceAll("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+", "")//
                       .replace(' ', '_')//
                       .replace('-', '_')//
                       .replaceAll("[^a-zA-Z_\\d]+", "")//
                       .replaceAll("_{2,}", "_");//
        return result;
    }
}
