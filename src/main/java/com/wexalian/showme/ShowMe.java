package com.wexalian.showme;

import com.google.gson.reflect.TypeToken;
import com.wexalian.showme.core.settings.Settings;
import com.wexalian.showme.gui.GuiLoader;
import com.wexalian.showme.util.JsonUtils;
import com.wexalian.showme.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ShowMe {
    private static final Path SETTINGS_PATH = Paths.get("settings.json");
    private static final Settings SETTINGS = Utils.makeIfAndMapOrElse(SETTINGS_PATH, Files::exists, ShowMe::parseFile, Settings::new);
    
    public static Settings getSettings() {
        return SETTINGS;
    }
    
    public static void main(String[] args) {
        GuiLoader.start(args);
        
        JsonUtils.unparseFile(SETTINGS, SETTINGS_PATH, new TypeToken<>() {});
    }
    
    private static Settings parseFile(Path path) {
        return JsonUtils.parseFile(path, new TypeToken<>() {});
    }
}
