package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class Status extends PvPCommand {

	public Status(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public Status(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		switch (args.length) {
		case 1:
			// get command sender's status
			if (player != null) {
				getPlayerStatus(sender, player, player.getWorld().getName());
			} else {
				new Global(plugin, sender, command, label, args).getGlobalStatus(sender); // run
																							// from
																							// console
			}
			break;
		case 2:
			// get player's status in sender's world
			Player retrieved = getPlayer(sender, args[1], true);
			if (retrieved != null) {
				getPlayerStatus(sender, retrieved, retrieved.getWorld().getName());
			}
			break;
		case 3:
			// get player's status in specified world
			getPlayerStatus(sender, getPlayer(sender, args[1], true), isWorld(sender, args[2]));
			break;
		default:
			sendUsage(sender);
		}
		return true;
	}

	@Override
	protected void sendUsage(CommandSender sender) {
		sender.sendMessage(helpHeader);
		ChatColor messagecolour = ChatColor.GOLD;
		if (plugin.permissionsCheck(sender, "pvptoggle.self.status", true)) {
			sender.sendMessage(messagecolour + "/pvp status " + ChatColor.GRAY + "- Shows own PvP status in current world");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.other.status", true)) {
			sender.sendMessage(messagecolour + "/pvp status [player] " + ChatColor.GRAY + "- Shows player's PvP status in current world");
			sender.sendMessage(messagecolour + "/pvp status [player] [world] " + ChatColor.GRAY + "- Shows player's PvP status in specified world");
		}
	}

	/**
	 * Prints target player's status to chat/console
	 *
	 * @param sender
	 *            - command sender
	 * @param target
	 *            - who to check for
	 * @param world
	 *            - what world to check in
	 */
	private void getPlayerStatus(CommandSender sender, Player target, String world) {

		// check for permission to view own or other player's status
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.self.status", true)) && (sender.getName().equalsIgnoreCase(target.getName()))) || (plugin.permissionsCheck(sender, "pvptoggle.other.status", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return;
		}

		if ((target != null) && (world != null)) {
			if (sender.getName().equalsIgnoreCase(target.getName())) {
				// report personal PvP status
				if (plugin.permissionsCheck(target, "pvptoggle.pvp.force", false)) {
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				} else if (plugin.permissionsCheck(target, "pvptoggle.pvp.deny", false)) {
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				} else if (plugin.checkPlayerStatus(target, target.getWorld().getName())) {
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				} else {
					PvPLocalisation.display(sender, "", world, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_STATUS);
				}
			} else {
				// report someone else's PvP status
				if (plugin.permissionsCheck(target, "pvptoggle.pvp.force", false)) {
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				} else if (plugin.permissionsCheck(target, "pvptoggle.pvp.deny", false)) {
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				} else if (plugin.checkPlayerStatus(target, world)) {
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				} else {
					PvPLocalisation.display(sender, target.getName(), world, PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PLAYER_CHECK_OTHER_STATUS);
				}
			}
		}
	}
}
