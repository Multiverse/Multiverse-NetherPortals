package com.onarandombox.MultiverseNetherPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.event.MVDebugModeEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MVNPCoreListener implements Listener {
    private MultiverseNetherPortals plugin;

    public MVNPCoreListener(MultiverseNetherPortals plugin) {
        this.plugin = plugin;
    }

    private String readFile(final String filename) {
        StringBuilder result;
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            result = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            Logging.severe("Unable to find %s. Here's the traceback: %s", filename, e.getMessage());
            e.printStackTrace();
            result = new StringBuilder(String.format("ERROR: Could not load: %s", filename));
        } catch (IOException e) {
            Logging.severe("Something bad happend when reading %s. Here's the traceback: %s", filename, e.getMessage());
            e.printStackTrace();
            result = new StringBuilder(String.format("ERROR: Could not load: %s", filename));
        }
        return result.toString();
    }

    /**
     * This method is called when Multiverse-Core wants to know what version we are.
     * @param event The Version event.
     */
    @EventHandler
    public void versionEvent(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        event.getDetailedVersionInfo().put("multiverse-netherportals/config.yml", this.readFile(configFile.getAbsolutePath()));
    }

    @EventHandler
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
