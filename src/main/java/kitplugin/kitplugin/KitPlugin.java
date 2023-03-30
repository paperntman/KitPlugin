package kitplugin.kitplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class KitPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            FileManager.setup(getDataFolder().getPath());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        getCommand("kit").setExecutor(new KitCommand());
        getCommand("kit").setTabCompleter(new KitCommand());
    }

    @Override
    public void onDisable() {
        try {
            FileManager.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
