package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static common.BuildYml.*;

public class Geyser {

    public void simpleUpdateGeyser(String platform) {
        String latestVersionUrl;
        String originalPlatform = platform;
        platform = displayPlatform(platform);
        latestVersionUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/" + platform;
        String outputFilePath = "plugins/Geyser-" + originalPlatform + ".jar";
        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ignored) {}
    }

    public boolean updateGeyser(String platform) {
        String apiUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new URL(apiUrl));
            JsonNode buildsNode = jsonNode.get("builds");
            if (getDownloadedBuild("Geyser") == -1) {
                simpleUpdateGeyser(platform);
                updateBuildNumber("Geyser", getMaxBuildNumber(buildsNode));
                return true;
            } else if (getDownloadedBuild("Geyser") != getMaxBuildNumber(buildsNode)) {
                simpleUpdateGeyser(platform);
                updateBuildNumber("Geyser", getMaxBuildNumber(buildsNode));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String displayPlatform(String platform) {
        switch (platform) {
            case "BungeeCord" -> {
                return "bungee";
            }
            case "Velocity" -> {
                return "velocity";
            }
            case "Spigot" -> {
                return "spigot";
            }
            default -> {
                return null;
            }
        }
    }
}
