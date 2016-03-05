package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class Toggle extends PvPCommand {

	public Toggle(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public Toggle(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		switch (args.length) {
		case 0:
			togglePlayer(sender, player, player.getWorld().getName(), !plugin.checkPlayerStatus(player, player.getWorld().getName()));
			break;
		case 1:
			// toggle command sender
			if (player != null) {
				togglePlayer(sender, player, player.getWorld().getName(), checkNewValue(args[0]));
			} else {
				PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.CONSOLE_ERROR);
			}
			break;
		case 2:
			// toggle specific player in their current world
			Player retrieved = getPlayer(sender, args[1], true);
			if (retrieved != null) {
				togglePlayer(sender, retrieved, retrieved.getWorld().getName(), checkNewValue(args[0]));
			}
			break;
		case 3:
			// toggle specific player in specific world
			togglePlayer(sender, getPlayer(sender, args[1], true), isWorld(sender, args[2]), checkNewValue(args[0]));
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
		if (plugin.permissionsCheck(sender, "pvptoggle.self.toggle", true)) {
			sender.sendMessage(messagecolour + "/pvp " + ChatColor.GRAY + "- Toggles own PvP status");
			sender.sendMessage(messagecolour + "/pvp on " + ChatColor.GRAY + "- Sets own PvP status to on");
			sender.sendMessage(messagecolour + "/pvp off " + ChatColor.GRAY + "- Sets own PvP status to off");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.other.toggle", true)) {
			sender.sendMessage(messagecolour + "/pvp on [player] " + ChatColor.GRAY + "- Sets player's PvP status to on in current world");
			sender.sendMessage(messagecolour + "/pvp on [player] [world] " + ChatColor.GRAY + "- Sets player's PvP status to on in specified world");
			sender.sendMessage(messagecolour + "/pvp off [player] " + ChatColor.GRAY + "- Sets player's PvP status to off in current world");
			sender.sendMessage(messagecolour + "/pvp off [player] [world] " + ChatColor.GRAY + "- Sets player's PvP status to off in specified world");
		}
	}

	/**
	 * Toggle the PvP status of a player
	 *
	 * @param sender
	 *            - command sender
	 * @param targetplayer
	 *            - name of player to toggle
	 * @param newval
	 *            - new PvP status of player
	 * @param worldname
	 *            - world to toggle in
	 */
	private void togglePlayer(CommandSender sender, Player player, String worldname, boolean newval) {

		if ((player == null) || (worldname == null)) {
			// target not found, return out
			return;
		}

		// check for permission to toggle own or other player's status
		if (!(((plugin.permissionsCheck(sender, "pvptoggle.self.toggle", true)) && (sender.getName().equalsIgnoreCase(player.getName()))) || (plugin.permissionsCheck(sender, "pvptoggle.other.toggle", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return; // no permission, return out
		}

		if (sender.getName().equalsIgnoreCase(player.getName())) {
			// if sender is the target
			if (!WorldGuardRegionCheck(player, "this"))
				return; // no permission to toggle in this region

			if (newval) {
				// if enabling
				plugin.setPlayerStatus(player, player.getWorld().getName(), true);
				PvPLocalisation.display(sender, "", player.getWorld().getName(), PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.PVP_PLAYER_SELF_TOGGLE);
				plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " enabled pvp");
				plugin.setLastAction(player, "toggle");
			} else if (!(newval)) {
				// if disabling
				if (plugin.checkLastAction(player, "toggle", player.getWorld().getName())) {
					plugin.setPlayerStatus(player, player.getWorld().getName(), false);
					PvPLocalisation.display(sender, "", player.getWorld().getName(), PvPLocalisation.Strings.PVP_DISABLED.toString(), PvPLocalisation.Strings.PVP_PLAYER_SELF_TOGGLE);
					plugin.log.info("[PvPToggle] Player " + player.getDisplayName() + " disabled pvp");
				} else {
					PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PLAYER_COOLDOWN);
				}
			}
		} else {
			// if the target is another player
			if (!WorldGuardRegionCheck(player, player.getName() + "'s current "))
				return; // no permission to toggle in this region

			// set message details
			String message = null;
			if (newval) {
				message = PvPLocalisation.Strings.PVP_ENABLED.toString();
			} else {
				message = PvPLocalisation.Strings.PVP_DISABLED.toString();
			}

			// toggle
			if (worldname.equalsIgnoreCase("*")) {
				// if toggling across all worlds
				for (World world : plugin.getServer().getWorlds()) {
					plugin.setPlayerStatus(player, world.getName(), newval);
				}
				PvPLocalisation.display(player, "", "", message, PvPLocalisation.Strings.PVP_PLAYER_GLOBAL_TOGGLE);
				PvPLocalisation.display(sender, player.getName(), "", message, PvPLocalisation.Strings.PVP_PLAYER_GLOBAL_TOGGLE_SENDER);
			} else {
				// if toggling specific world
				plugin.setPlayerStatus(player, worldname, newval);
				PvPLocalisation.display(player, "", worldname, message, PvPLocalisation.Strings.PVP_PLAYER_OTHER_TOGGLE);
				PvPLocalisation.display(sender, player.getName(), worldname, message, PvPLocalisation.Strings.PVP_PLAYER_OTHER_TOGGLE_SENDER);
			}
			plugin.setLastAction(player, "toggle");
		}
	}

	/**
	 * Checks if a player is currently in a WorldGuard region with the PVP flag
	 * set
	 *
	 * @param player
	 *            - who to check for
	 * @return false if in region with flag set, otherwise true
	 */
	private boolean WorldGuardRegionCheck(Player player, String target) {
		if ((Boolean) plugin.getGlobalSetting("worldguard")) {
			return plugin.regionListener.WorldGuardRegionCheck(player, target);
		}
		return true;
	}

}
