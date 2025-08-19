package net.timeux.chrono;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.*;

public class SnapshotManager {

    private final Plugin plugin;

    public SnapshotManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void createSnapshot(String name) throws IOException {
        World source = Bukkit.getWorlds().get(0);
        Path src = source.getWorldFolder().toPath();
        Path dest = Bukkit.getWorldContainer().toPath().resolve(name);
        if (Files.exists(dest)) {
            throw new IOException("Snapshot existiert bereits");
        }
        Files.walk(src).forEach(path -> {
            try {
                Path target = dest.resolve(src.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        WorldCreator wc = new WorldCreator(name);
        Bukkit.createWorld(wc);
    }
}
