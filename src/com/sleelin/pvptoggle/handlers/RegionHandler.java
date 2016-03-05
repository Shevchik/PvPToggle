package com.sleelin.pvptoggle.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.sleelin.pvptoggle.PvPToggle;

public class RegionHandler {

	private static FileConfiguration customConfig = null;
	private static File customConfigFile = null;
	private static PvPToggle plugin = null;
	public static HashMap<String, ArrayList<String>> worldregions = new HashMap<String, ArrayList<String>>();

	public static void loadProcedure(PvPToggle plugin) {
		RegionHandler.plugin = plugin;
		reloadCustomConfig();
		loadValues();
	}

	public static void writeValues() {
		for (World world : RegionHandler.plugin.getServer().getWorlds()) {
			getCustomConfig().set(world.getName(), worldregions.get(world.getName()));
		}
		saveCustomConfig();
	}

	public static void loadValues() {

		worldregions.clear();

		for (World world : RegionHandler.plugin.getServer().getWorlds()) {
			worldregions.put(world.getName(), new ArrayList<String>());
			java.util.List<String> regions = getCustomConfig().getStringList(world.getName());
			for (String region : regions) {
				worldregions.get(world.getName()).add(region);
			}
		}

		plugin.log.info("[" + plugin.getDescription().getName() + "] Loaded tagged region information from regions file");

	}

	/*
	 * Methods for dealing with custom YAML config files, credit to Bukkit
	 * Developer Portal for this section
	 */

	private static void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(plugin.getDataFolder(), "regions.yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	}

	private static FileConfiguration getCustomConfig() {
		if (customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	private static void saveCustomConfig() {
		if (customConfig == null || customConfigFile == null) {
			return;
		}
		try {
			customConfig.save(customConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}

	public static void addRegion(CommandSender sender, String world, String region) {
		if (plugin.permissionsCheck(sender, "pvptoggle.regions.add", true)) {
			if (!worldregions.containsKey(world))
				worldregions.put(world, new ArrayList<String>());
			worldregions.get(world).add(region);
			writeValues();
		} else {

		}
	}

	public static void removeRegion(CommandSender sender, String world, String region) {
		if (worldregions.containsKey(world)) {
			worldregions.get(world).remove(region);
			writeValues();
		}
	}

	public static boolean isApplicableRegion(String world, String region) {
		if (worldregions.containsKey(world)) {
			if (worldregions.get(world).contains(region))
				return true;
		}
		return false;
	}

}
