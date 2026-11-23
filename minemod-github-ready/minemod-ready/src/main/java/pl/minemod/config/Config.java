package pl.minemod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Zapis ustawien do .minecraft/config/minemod.json */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance = new Config();

    public Map<String, Boolean> enabled = new HashMap<>();
    public Map<String, Integer> option = new HashMap<>();
    public Map<String, String> equipped = new HashMap<>();

    public static Config get() { return instance; }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("minemod.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                Config c = GSON.fromJson(Files.readString(p), Config.class);
                if (c != null) instance = c;
            }
        } catch (Exception ignored) {}
        if (instance.enabled == null) instance.enabled = new HashMap<>();
        if (instance.option == null) instance.option = new HashMap<>();
        if (instance.equipped == null) instance.equipped = new HashMap<>();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (Exception ignored) {}
    }
}
