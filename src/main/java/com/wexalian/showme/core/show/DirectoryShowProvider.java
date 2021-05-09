package com.wexalian.showme.core.show;

import com.google.gson.reflect.TypeToken;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.data.EpisodeData;
import com.wexalian.showme.core.data.SeasonData;
import com.wexalian.showme.core.data.ShowData;
import com.wexalian.showme.core.image.ImageToPNGEncoder;
import com.wexalian.showme.util.FileUtils;
import com.wexalian.showme.util.JsonUtils;
import com.wexalian.showme.util.throwing.Throwing;
import javafx.scene.image.Image;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class DirectoryShowProvider implements IShowProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final String SHOW_PNG = "show.png";
    private static final String SEASON_PNG = "season.png";
    private static final String EPISODE_PNG = "episode.png";
    private static final String SHOW_DATA_JSON = "show_data.json";
    private static final Pattern EPISODE_PATTERN_1 = Pattern.compile(".*([Ss](\\d{1,2})[Ee](\\d{1,2})).*");
    private static final Pattern EPISODE_PATTERN_2 = Pattern.compile(".*episode_(\\d{1,2}).*");
    private static final Pattern EPISODE_PATTERN_3 = Pattern.compile("(\\d{3}) - .*");
    
    private final Path path;
    
    public DirectoryShowProvider(Path path) {
        this.path = path;
    }
    
    @Override
    public List<ShowData> loadShowData(Supplier<Boolean> isCancelledSupplier) {
        List<ShowData> data = new ArrayList<>();
        
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            LOGGER.info("Loading shows from provider '{}'", getName());
            for (Path potentialShow : paths) {
                if (isCancelledSupplier.get()) {
                    return data;
                }
                Path showDataFile = potentialShow.resolve(SHOW_DATA_JSON);
                if (Files.exists(showDataFile)) {
                    ShowData showData = JsonUtils.parseFile(showDataFile, new TypeToken<>() {});
                    
                    if (showData != null) {
                        for (SeasonData seasonData : showData.getSeasonData()) {
                            for (EpisodeData episodeData : seasonData.getEpisodeData()) {
                                checkFile(showData, seasonData, episodeData);
                            }
                        }
                        data.add(showData);
                    }
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Error getting directory stream for path '{}'", path);
        }
        return data;
    }
    
    @Override
    public void saveShowData(Show show, ShowData data) {
        Path showDataPath = getPath(show).resolve(SHOW_DATA_JSON);
        String json = JsonUtils.toJson(data, new TypeToken<>() {});
        if (Strings.isBlank(json)) {
            LOGGER.error("Attempted to write empty JSON to show data for show '{}'", data.getTitle());
            return;
        }
        try {
            if (!Files.exists(showDataPath)) {
                Files.createDirectories(showDataPath.getParent());
            }
            Path path = Files.writeString(showDataPath, json, StandardCharsets.UTF_8);
            LOGGER.debug("Writing '{}' show data to provider '{}'", show.getTitle(), getName());
            if (Files.size(path) == 0) {
                LOGGER.error("Written file '{}' has 0 bytes", path.getFileName().toString(), new RuntimeException("File is 0 bytes"));
            }
        }
        catch (IOException e) {
            LOGGER.error("Error saving show data for show '{}' to '{}'", data.getTitle(), showDataPath.toString());
            LOGGER.catching(Level.ERROR, e);
        }
    }
    
    @Override
    public Path getFolderPath(Episode episode) {
        return ensureFolderPath(getPath(episode), episode);
    }
    
    @Override
    @Nullable
    public Image loadImage(Show show) {
        Path imagePath = getPath(show).resolve(SHOW_PNG);
        if (Files.exists(imagePath)) {
            return new Image("file:" + imagePath);
        }
        return null;
    }
    
    @Override
    @Nullable
    public Image loadImage(Season season) {
        Path imagePath = getPath(season).resolve(SEASON_PNG);
        if (Files.exists(imagePath)) {
            return new Image("file:" + imagePath);
        }
        return null;
    }
    
    @Override
    @Nullable
    public Image loadImage(Episode episode) {
        Path imagePath = getPath(episode).resolve(EPISODE_PNG);
        if (Files.exists(imagePath)) {
            return new Image("file:" + imagePath);
        }
        return null;
    }
    
    @Override
    public void saveImage(Show show, Image image) {
        Path imagePath = getPath(show).resolve(SHOW_PNG);
        try {
            byte[] bytes = new ImageToPNGEncoder(image).pngEncode();
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, bytes);
        }
        catch (Exception e) {
            LOGGER.error("Error saving show image for '{}' at '{}'", show.getTitle(), path);
            LOGGER.catching(Level.ERROR, e);
        }
    }
    
    @Override
    public void saveImage(Season season, Image image) {
        Path imagePath = getPath(season).resolve(SEASON_PNG);
        try {
            byte[] bytes = new ImageToPNGEncoder(image).pngEncode();
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, bytes);
        }
        catch (IOException e) {
            LOGGER.error("Error saving season {} image for '{}' at '{}'", season.getNumber(), season.getShow().getTitle(), path);
            LOGGER.catching(Level.ERROR, e);
        }
    }
    
    @Override
    public void saveImage(Episode episode, Image image) {
        Path imagePath = getPath(episode).resolve(EPISODE_PNG);
        try {
            byte[] bytes = new ImageToPNGEncoder(image).pngEncode();
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, bytes);
        }
        catch (IOException e) {
            Season season = episode.getSeason();
            Object[] params = new Object[]{season.getNumber(), String.format("%02d", episode.getNumber()), season.getShow().getTitle(), path};
            LOGGER.error("Error saving episode {}x{} image for '{}' at '{}'", params);
            LOGGER.catching(Level.ERROR, e);
        }
    }
    
    @Override
    public Path getVideoPath(Episode episode) {
        return getFolderPath(episode).resolve(episode.getFileName());
    }
    
    @Override
    public boolean deleteVideoFile(Episode episode) {
        String fileName = episode.getFileName();
        if (Strings.isBlank(fileName)) {
            fileName = FileUtils.getFileName(episode);
        }
        if (Strings.isNotBlank(fileName)) {
            Path filePath = getFolderPath(episode).resolve(fileName);
            if (Files.isRegularFile(filePath)) {
                Throwing.apply(filePath, Files::deleteIfExists, (t, e) -> LOGGER.debug("Exception caught while deleting video file ({})", t, e));
            }
        }
        return false;
    }
    
    @Override
    public boolean canSaveNewShow() {
        if (Files.exists(path)) {
            return Throwing.get(() -> Files.getFileStore(path).getUsableSpace() > 10e9);
        }
        return false;
    }
    
    @Nonnull
    @Override
    public String getName() {
        return "Directory (" + path + ")";
    }
    
    private Path ensureFolderPath(Path path, Episode episode) {
        Season season = episode.getSeason();
        Show show = season.getShow();
        
        String title = show.getTitle();
        int seasonNum = season.getNumber();
        int episodeNum = episode.getNumber();
        
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            }
            catch (IOException e) {
                LOGGER.error("Error creating directory for '{}' {}x{}", title, seasonNum, episodeNum);
                LOGGER.catching(e);
            }
        }
        return path;
    }
    
    private void checkFile(ShowData data, SeasonData seasonData, EpisodeData episodeData) {
        Path path = getPath(data, seasonData, episodeData);
        if (Strings.isNotBlank(episodeData.getFileName())) {
            if (episodeData.getFileName().endsWith(".mkv")) {
                Path filePath = path.resolve(episodeData.getFileName());
                if (Files.notExists(filePath)) {
                    episodeData.setFileName("");
                    data.setDirty();
                }
            }
            else {
                episodeData.setFileName("");
                data.setDirty();
            }
        }
        if (Strings.isBlank(episodeData.getFileName())) {
            String fileName = FileUtils.getFileName(episodeData.getNumber(), episodeData.getTitle());
            Path filePath = path.resolve(fileName);
            
            if (Files.exists(filePath)) {
                episodeData.setFileName(fileName);
                data.setDirty();
            }
            else {
                try {
                    if (Files.exists(path)) {
                        Optional<Path> videoFilePath = Files.list(path).filter(this::filterFile).findFirst();
                        videoFilePath.ifPresent(file -> Throwing.run(() -> {
                            Path newFilePath = Files.move(file, filePath);
                            if (Files.exists(newFilePath)) {
                                episodeData.setFileName(newFilePath.getFileName().toString());
                            }
                        }));
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean filterFile(Path path) {
        String fileName = path.getFileName().toString();
        
        if (fileName.endsWith(".mkv") || fileName.endsWith(".mp4") || fileName.endsWith(".avi")) {
            if (EPISODE_PATTERN_1.matcher(fileName).matches()) return true;
            if (EPISODE_PATTERN_2.matcher(fileName).matches()) return true;
            if (EPISODE_PATTERN_3.matcher(fileName).matches()) return true;
        }
        
        return false;
    }
    
    private Path getPath(Episode episode) {
        return getPath(episode.getSeason()).resolve("episode_" + format(episode.getNumber()));
    }
    
    private String format(int number) {
        return String.format("%02d", number);
    }
    
    private Path getPath(Season season) {
        return getPath(season.getShow()).resolve("season_" + format(season.getNumber()));
    }
    
    private Path getPath(Show show) {
        return path.resolve(FileUtils.getSlug(show.getTitle()));
    }
    
    private Path getPath(ShowData show, SeasonData season, EpisodeData episode) {
        return path.resolve(FileUtils.getSlug(show.getTitle()))
                   .resolve("season_" + format(season.getNumber()))
                   .resolve("episode_" + format(episode.getNumber()));
    }
    
}
