package com.sleelin.pvptoggle.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class World extends PvPCommand {

	public World(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public World(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		String worldname = null;
		String action = args[args.length - 1]; // last argument determines what
												// action to perform

		// extract world name
		if (args[0].startsWith("w:")) {
			worldname = isWorld(sender, args[0].substring(2));
		} else if (args.length > 1) {
			worldname = isWorld(sender, args[1]);
		} else {
			// no world specified, send usage
			sendUsage(sender);
		}

		if (worldname != null) {
			if ((action.equalsIgnoreCase("status")) || (worldname.equalsIgnoreCase(action)) || ((worldname.equalsIgnoreCase(action.substring(2))) && (action.startsWith("w:")))) {
				// user requested world status
				getWorldStatus(sender, worldname);
			} else if (action.equalsIgnoreCase("reset")) {
				// reset all players in specified world
				resetWorld(sender, worldname, plugin.getWorldDefault(worldname));
			} else if ((action.equalsIgnoreCase("on")) || (action.equalsIgnoreCase("off"))) {
				// toggle world to new value
				toggleWorld(sender, worldname, checkNewValue(action));
			} else {
				// invalid argument, return command usage
				sendUsage(sender);
			}
		}
		return true;
	}

	@Override
	public void sendUsage(CommandSender sender) {
		sender.sendMessage(helpHeader);
		ChatColor messagecolour = ChatColor.GOLD;
		if (plugin.permissionsCheck(sender, "pvptoggle.world.toggle", true)) {
			sender.sendMessage(messagecolour + "/pvp w:[world] on " + ChatColor.GRAY + "- Sets PvP status of specified world to on");
			sender.sendMessage(messagecolour + "/pvp w:[world] off " + ChatColor.GRAY + "- Sets PvP status of specified world to off");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.world.reset", true)) {
			sender.sendMessage(messagecolour + "/pvp w:[world] reset " + ChatColor.GRAY + "- Resets PvP status of all players in specified world");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.world.status", true)) {
			sender.sendMessage(messagecolour + "/pvp w:[world] " + ChatColor.GRAY + "- Shows PvP status of specified world");
			sender.sendMessage(messagecolour + "/pvp w:[world] status " + ChatColor.GRAY + "- Shows PvP status of specified world");
		}
	}

	/**
	 * Prints PvP status in specified world
	 *
	 * @param sender
	 * @param world
	 */
	private void getWorldStatus(CommandSender sender, String worldname) {
		if (plugin.permissionsCheck(sender, "pvptoggle.world.status", true)) {

			if (worldname != null) {
				if (plugin.getWorldStatus(worldname)) {
					PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_WORLD_STATUS);
				} else {
					PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_WORLD_STATUS);
				}
			}

		} else {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
		}

	}

	/**
	 * Toggles whether PvP is allowed or denied in a world
	 *
	 * @param sender
	 * @param targetworld
	 * @param newval
	 */
	private void toggleWorld(CommandSender sender, String targetworld, boolean newval) {
		if (!((plugin.permissionsCheck(sender, "pvptoggle.world.toggle", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return; // no permission, return out
		}

		if (targetworld != null) {
			setWorldStatus(targetworld, newval);
			if (newval) {
				PvPLocalisation.display(sender, "", targetworld, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_WORLD_TOGGLE_SENDER);
			} else {
				PvPLocalisation.display(sender, "", targetworld, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_WORLD_TOGGLE_SENDER);
			}
		}
	}

	/**
	 * Resets PvP status of every player in a world to specified value
	 *
	 * @param sender
	 *            - who sent the command
	 * @param worldname
	 *            - what world to reset in
	 * @param newval
	 *            - what to reset it to
	 */
	private void resetWorld(CommandSender sender, String worldname, boolean newval) {

		if (!((plugin.permissionsCheck(sender, "pvptoggle.world.reset", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return; // no permission, return out
		}

		// if toggle all players in specific world
		for (Player p : Bukkit.getOnlinePlayers()) {
			plugin.setPlayerStatus(p, worldname, newval);
			plugin.setLastAction(p, "toggle");
			if (newval)
				PvPLocalisation.display(p, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD);
			else
				PvPLocalisation.display(p, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD);
		}
		if (newval)
			PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD_SENDER);
		else
			PvPLocalisation.display(sender, "", worldname, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_RESET_WORLD_SENDER);
	}

}
