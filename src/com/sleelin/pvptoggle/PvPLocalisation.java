package com.sleelin.pvptoggle;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PvPLocalisation {

	private static FileConfiguration customConfig = null;
	private static File customConfigFile = null;
	private static PvPToggle plugin = null;

	public static void loadProcedure(PvPToggle plugin) {
		PvPLocalisation.plugin = plugin;
		reloadCustomConfig();
		writeDefaults();
		loadValues();
	}

	public static enum Strings {
		CONSOLE_ERROR("&cCannot run this command from the console!"),
		NO_PERMISSION("&cYou don't have permission to do that!"),
		PLAYER_NOT_FOUND("&cCouldn't find a player matching that name!"),
		PLAYER_COOLDOWN("&cYou were just in combat, can't disable PvP yet!"),
		PLAYER_WARMUP("&cYou only just enabled PvP!"),
		PLAYER_AUTOENABLE("&6PvP automatically enabled due to combat!"),
		PLAYER_CHECK_STATUS("&6PvP Status in world %world%: %status%"),
		PLAYER_CHECK_OTHER_STATUS("&6%target%'s PvP Status in world %world%: %status%"),
		PLAYER_CHECK_WORLD_STATUS("&6World-wide PvP Status in %world%: %status%"),
		PLAYER_CHECK_GLOBAL_STATUS("&6Global PvP Status: %status%"),
		PVP_ENABLED("enabled"),
		PVP_DISABLED("disabled"),
		PVP_FORCED("forced"),
		PVP_DENIED("denied"),
		PVP_DENY_DISABLED("&cYou have PvP disabled!"),
		PVP_DENY_OPPONENT("&c%target% has PvP disabled!"),
		PVP_DENY_WORLD("&cPvP is disabled in world %world%!"),
		PVP_DENY_GLOBAL("&cGlobal PvP is disabled!"),
		PVP_PLAYER_SELF_TOGGLE("&6PvP %status% in world %world%!"),
		PVP_PLAYER_OTHER_TOGGLE("&6PvP %status% in world %world% by %sender%!"),
		PVP_PLAYER_GLOBAL_TOGGLE("&6PvP %status% across all worlds by %sender%!"),
		PVP_WORLD_TOGGLE(""),
		PVP_GLOBAL_TOGGLE(""),
		PVP_PLAYER_OTHER_TOGGLE_SENDER("&6Successfully %status% PvP for player %target% in world %world%!"),
		PVP_PLAYER_GLOBAL_TOGGLE_SENDER("&6Successfully %status% PvP for player %target% across all worlds!"),
		PVP_WORLD_TOGGLE_SENDER("&6Successfully %status% world-wide PvP in %world%"),
		PVP_GLOBAL_TOGGLE_SENDER("&6Successfully %status% global PvP!"),
		PVP_RESET_PLAYER("&6PvP status reset to login default in world %world% by %sender%"),
		PVP_RESET_PLAYER_GLOBAL("&6PvP status reset to login default across all worlds by %sender%"),
		PVP_RESET_WORLD("&6PvP %status% in world %world% by %sender%!"),
		PVP_RESET_GLOBAL("&6PvP globally reset across all worlds by %sender%!"),
		PVP_RESET_PLAYER_SENDER("&6Successfully reset PvP status of %target% to default in world %world%!"),
		PVP_RESET_PLAYER_GLOBAL_SENDER("&6Successfully reset PvP status of %target% to default!"),
		PVP_RESET_WORLD_SENDER("&6Successfully reset all players PvP to %status% in world %world%"),
		PVP_RESET_GLOBAL_SENDER("&6Successfully reset all players PvP across all worlds!"),
		WORLDGUARD_REGION_ENTERED("&6Forced PvP %status% region '%target%' entered"),
		WORLDGUARD_REGION_EXIT("&6Forced PvP %status% region '%target%' exited"),
		WORLDGUARD_TOGGLE_DENIED("&cPvP is %status% in %target% region!"),
		WORLDGUARD_REGION_ADDED("&6Region %target% added to world %world%"),
		WORLDGUARD_REGION_REMOVED("&6Region %target% removed from world %world%"),
		WORLD_NOT_FOUND("&cNo world matching that name!");

		private Strings(final String text) {
			this.text = text;
		}

		private String text;

		@Override
		public String toString() {
			return text;
		}
	}

	private static void writeDefaults() {
		for (Strings string : Strings.values()) {
			if (!getCustomConfig().isSet(string.name()))
				getCustomConfig().set(string.name(), string.text);
		}
		saveCustomConfig();
	}

	private static void loadValues() {
		for (Strings string : Strings.values()) {
			string.text = getCustomConfig().getString(string.name(), string.text);
		}
	}

	/**
	 * Parse a given text string and replace the variables/colour codes.
	 *
	 * @param input
	 *            - string to replace variables/colours in
	 * @param search
	 *            - variables to replace
	 * @param replace
	 *            - values to insert in place of variables
	 * @return input string with variables and colour codes replaced by values
	 */
	private static String replaceVars(String input, String[] search, String[] replace) {
		if (search.length != replace.length)
			return "";
		for (int i = 0; i < search.length; i++) {
			if (replace[i] != null)
				input = input.replace(search[i], replace[i]);
		}
		return input.replaceAll("(&([a-f0-9]))", "\u00A7$2");
	}

	/**
	 * Display a string to the given command sender with variables and colour
	 * codes replaced
	 *
	 * @param sender
	 *            - user who called function
	 * @param target
	 *            - who the function is targeting
	 * @param world
	 *            - what world the target is in
	 * @param status
	 *            - status of target/world
	 * @param consoleError
	 *            - which string to replace variables/colour codes in
	 */
	public static void display(CommandSender sender, String target, String world, String status, Strings string) {
		String[] search = { "%sender%", "%target%", "%world%", "%status%" };
		String[] replace = { sender.getName(), target, world, status };
		if (!string.toString().equalsIgnoreCase(""))
			sender.sendMessage(replaceVars(string.toString(), search, replace));
	}

	/*
	 * Methods for dealing with custom YAML config files, credit to Bukkit
	 * Developer Portal for this section
	 */

	private static void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(plugin.getDataFolder(), "localisations.yml");
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

}
