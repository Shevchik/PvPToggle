package com.sleelin.pvptoggle.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class Global extends PvPCommand {

	public Global(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public Global(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("status")) {
				// user requested global status
				getGlobalStatus(sender);
			} else if (args[1].equalsIgnoreCase("reset")) {
				// reset all players to global login default across all worlds
				resetGlobal(sender);
			} else if (checkNewValue(args[1]) != null) {
				// toggle to specific global status
				toggleGlobal(sender, checkNewValue(args[1]));
			} else {
				// invalid argument, return command usage
				sendUsage(sender);
			}
		} else {
			// no new value, return global status
			getGlobalStatus(sender);
		}
		return true;
	}

	@Override
	protected void sendUsage(CommandSender sender) {
		sender.sendMessage(helpHeader);
		ChatColor messagecolour = ChatColor.GOLD;
		if (plugin.permissionsCheck(sender, "pvptoggle.global.toggle", true)) {
			sender.sendMessage(messagecolour + "/pvp global on " + ChatColor.GRAY + "- Enables global PvP");
			sender.sendMessage(messagecolour + "/pvp global off " + ChatColor.GRAY + "- Disables global PvP");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)) {
			sender.sendMessage(messagecolour + "/pvp global reset " + ChatColor.GRAY + "- Resets PvP status of all players in all worlds to login default");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)) {
			sender.sendMessage(messagecolour + "/pvp global " + ChatColor.GRAY + "- Shows global PvP status");
			sender.sendMessage(messagecolour + "/pvp global status " + ChatColor.GRAY + "- Shows global PvP status");
		}
	}

	/**
	 * Prints global PvP status to chat/console
	 *
	 * @param sender
	 *            - command sender
	 */
	protected void getGlobalStatus(CommandSender sender) {
		if (plugin.permissionsCheck(sender, "pvptoggle.global.status", true)) {

			if ((Boolean) plugin.getGlobalSetting("enabled")) {
				PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_GLOBAL_STATUS);
			} else {
				PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_GLOBAL_STATUS);
			}

		} else {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
		}

	}

	/**
	 * Toggles whether PvP is allowed or denied globally
	 *
	 * @param sender
	 *            - who sent the toggle request
	 * @param newval
	 *            - what to toggle it to
	 */
	private void toggleGlobal(CommandSender sender, boolean newval) {
		if (!((plugin.permissionsCheck(sender, "pvptoggle.global.toggle", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return; // no permission, return out
		}

		toggleGlobalStatus(newval);
		if (newval) {
			PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_GLOBAL_TOGGLE_SENDER);
		} else {
			PvPLocalisation.display(sender, "", "", PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_GLOBAL_TOGGLE_SENDER);
		}
	}

	/**
	 * Resets PvP status of all players in all worlds to login default
	 *
	 * @param sender
	 */
	private void resetGlobal(CommandSender sender) {
		if (!((plugin.permissionsCheck(sender, "pvptoggle.global.reset", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return; // no permission, return out
		}

		// toggle all players in all worlds
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (World world : plugin.getServer().getWorlds()) {
				plugin.setPlayerStatus(p, world.getName(), plugin.getWorldDefault(world.getName()));
			}
			plugin.setLastAction(p, "toggle");
			PvPLocalisation.display(p, "", "", "", PvPLocalisation.Strings.PVP_RESET_GLOBAL);
		}
		PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PVP_RESET_GLOBAL_SENDER);

	}

}
