package com.sleelin.pvptoggle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PvPCommand {

	protected PvPToggle plugin;
	protected CommandSender sender;
	protected Player player;
	protected Command command;
	protected String label;
	protected String[] args;
	protected String helpHeader;

	public PvPCommand(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		this.plugin = plugin;
		this.sender = sender;
		if (sender instanceof Player)
			this.player = (Player) sender;
		this.command = command;
		this.label = label;
		this.args = args;
		this.helpHeader = ChatColor.RED + "-----------------[ " + ChatColor.GOLD + "PvPToggle v" + plugin.getDescription().getVersion() + ChatColor.RED + " ]-----------------";
	}

	public PvPCommand(PvPToggle plugin, CommandSender sender) {
		this.helpHeader = ChatColor.RED + "-----------------[ " + ChatColor.GOLD + "PvPToggle v" + plugin.getDescription().getVersion() + ChatColor.RED + " ]-----------------";
		this.plugin = plugin;
		this.sendUsage(sender);
	}

	public PvPCommand exec() {
		this.processCommand();
		return this;
	}

	protected abstract boolean processCommand();

	protected abstract void sendUsage(CommandSender sender);

	/**
	 * Gets player whose name partially matches the input string
	 *
	 * @param sender
	 *            - command sender
	 * @param player
	 *            - input string
	 * @return player whose name partially matches input string
	 */
	protected Player getPlayer(CommandSender sender, String player, boolean notify) {

		List<Player> found = new ArrayList<Player>(); // list of found players

		// look through players to see if any of them match
		for (Player search : Bukkit.getOnlinePlayers()) {
			if ((search.getDisplayName().toLowerCase().contains(player.toLowerCase()) || (search.getName().toLowerCase().contains(player.toLowerCase())))) {
				found.add(search);
			}
		}

		if (found.size() == 1) {
			return found.get(0); // return found player
		} else if (found.size() > 1) {
			// found multiple matches
			if (notify)
				sender.sendMessage("Found " + found.size() + " online players matching that partial name:");
			for (Player p : found) {
				if (notify)
					sender.sendMessage("- " + p.getDisplayName());
			}
		} else {
			// found no matches
			if (notify)
				PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PLAYER_NOT_FOUND);
		}

		return null;
	}

	/**
	 * Converts string enable/disable parameters to booleans
	 *
	 * @param string
	 *            - string enable/disable input parameter
	 * @return boolean true for on, false for off
	 */
	protected static Boolean checkNewValue(String string) {
		Boolean enable = null;
		if ((string.equalsIgnoreCase("on")) || (string.equalsIgnoreCase("enable"))) {
			enable = true;
		} else if ((string.equalsIgnoreCase("off")) || (string.equalsIgnoreCase("disable"))) {
			enable = false;
		}
		return enable;
	}

	/**
	 * Check world name to make sure it's valid
	 *
	 * @param sender
	 *            - who is checking
	 * @param worldname
	 *            - what world name to check
	 * @param notify
	 *            - notify on invalid world
	 * @return completed name of world, or null if no match
	 */
	protected String isWorld(CommandSender sender, String worldname, boolean notify) {
		String output = plugin.checkWorldName(worldname);
		if (output == null)
			if (notify)
				PvPLocalisation.display(sender, null, null, null, PvPLocalisation.Strings.WORLD_NOT_FOUND);
		return output;
	}

	protected String isWorld(CommandSender sender, String worldname) {
		return isWorld(sender, worldname, true);
	}

	protected void toggleGlobalStatus(boolean newval) {
		plugin.toggleGlobalStatus(newval);
	}

	protected void setWorldStatus(String targetworld, boolean newval) {
		plugin.setWorldStatus(targetworld, newval);
	}

}
