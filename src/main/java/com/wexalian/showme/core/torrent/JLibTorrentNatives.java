package com.wexalian.showme.core.torrent;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class JLibTorrentNatives {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final String JLIBTORRENT_VERSION = "1.2.0.18";
    
    private static final String JLIBTORRENT_WINDOWS = "natives/jlibtorrent/x86_64/jlibtorrent-" + JLIBTORRENT_VERSION + ".dll";
    private static final String JLIBTORRENT_MAC = "natives/jlibtorrent/x86_64/libjlibtorrent-" + JLIBTORRENT_VERSION + ".dylib";
    private static final String JLIBTORRENT_LINUX = "natives/jlibtorrent/x86_64/libjlibtorrent-" + JLIBTORRENT_VERSION + ".so";
    
    private JLibTorrentNatives() { }
    
    private static String getJLibTorrentNativesLocation() {
        if (SystemUtils.IS_OS_WINDOWS) return JLIBTORRENT_WINDOWS;
        if (SystemUtils.IS_OS_LINUX) return JLIBTORRENT_LINUX;
        if (SystemUtils.IS_OS_MAC) return JLIBTORRENT_MAC;
        return "";
    }
    
    public static void loadNatives() {
        try {
            if (Strings.isBlank(System.getProperty("jlibtorrent.jni.path"))) {
                loadLibraryFromJar(getJLibTorrentNativesLocation(), s -> System.setProperty("jlibtorrent.jni.path", s));
            }
        }
        catch (Exception e) {
            LOGGER.debug("Error loading natives, downloading disabled!");
            LOGGER.catching(Level.ERROR, e);
        }
    }
    
    private static void loadLibraryFromJar(String path, Consumer<String> toLoad) {
        Path nativesPath = Paths.get(System.getProperty("user.dir")).resolve(path);
        
        if (!Files.exists(nativesPath)) {
            extractNativesFromJar(nativesPath, path);
        }
        
        toLoad.accept(nativesPath.toString());
    }
    
    private static void extractNativesFromJar(Path diskPath, String jarPath) {
        try {
            Files.createDirectories(diskPath.getParent());
            Files.createFile(diskPath);
            
            try (InputStream is = JLibTorrentNatives.class.getResourceAsStream("/" + jarPath)) {
                if(is != null ){
                    Files.copy(is, diskPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        catch (Exception e) {
            LOGGER.debug("Error extracting natives from JAR, downloading disabled!");
            LOGGER.catching(Level.ERROR, e);
        }
    }
}
