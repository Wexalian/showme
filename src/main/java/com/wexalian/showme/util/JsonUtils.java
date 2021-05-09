package com.wexalian.showme.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wexalian.showme.core.torrent.Magnet;
import com.wexalian.showme.core.torrent.eztv.EZTVDeserializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
                                                      .registerTypeAdapter(new TypeToken<List<Magnet>>() {}.getType(), new EZTVDeserializer())
                                                      .create();
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    @Nullable
    public static <T> T fromJson(String content, TypeToken<T> token) {
        try {
            return GSON.fromJson(content, token.getType());
        }
        catch (Exception e) {
            LOGGER.error("Error parsing json for type '{}'", token.getRawType().getName());
            LOGGER.catching(Level.ERROR, e);
        }
        return null;
    }
    
    @Nonnull
    public static <T> String toJson(T object, TypeToken<T> token) {
        return GSON.toJson(object, token.getType());
    }
    
    @Nullable
    public static <T> T parseFile(Path path, TypeToken<T> token) {
        try {
            if (Files.size(path) == 0) {
                LOGGER.error("File '{}' has a size of 0 bytes, this is probably an error", path.toString(), new RuntimeException("File is 0 bytes"));
                return null;
            }
            
            String content = Files.readString(path);
            return GSON.fromJson(content, token.getType());
        }
        catch (Exception e) {
            LOGGER.error("Error parsing json file '{}' for type '{}'", path, token.getRawType().getName());
            LOGGER.catching(Level.ERROR, e);
        }
        return null;
    }
    
    public static <T> void unparseFile(T object, Path path, TypeToken<T> token) {
        try {
            String content = GSON.toJson(object, token.getType());
            Files.writeString(path, content);
        }
        catch (Exception e) {
            LOGGER.error("Error writing json file '{}' for type '{}'", path, token.getRawType().getName());
            LOGGER.catching(Level.ERROR, e);
        }
    }
}
