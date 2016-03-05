package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;

public class Reset extends PvPCommand {

	public Reset(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public Reset(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		switch (args.length) {
		case 2:
			// reset specific player across all worlds
			resetPlayer(sender, getPlayer(sender, args[1], true), "*");
			break;
		case 3:
			// reset specific player in specific world
			resetPlayer(sender, getPlayer(sender, args[1], true), isWorld(sender, args[2]));
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
		if (plugin.permissionsCheck(sender, "pvptoggle.other.reset", true)) {
			sender.sendMessage(messagecolour + "/pvp reset [player] " + ChatColor.GRAY + "- Resets player's PvP status to default across all worlds");
			sender.sendMessage(messagecolour + "/pvp reset [player] [world] " + ChatColor.GRAY + "- Resets player's PvP status to default in specified world");
		}
	}

	/**
	 * Resets a player's PvP status to login default in specified, or all worlds
	 *
	 * @param sender
	 *            - who sent the command
	 * @param player
	 *            - target player
	 * @param worldname
	 *            - what world to reset in
	 */
	private void resetPlayer(CommandSender sender, Player player, String worldname) {
		if ((player == null) || (worldname == null)) {
			// invalid player, exit
			return;
		}

		if (!(((plugin.permissionsCheck(sender, "pvptoggle.self.reset", true)) && (sender.getName().equalsIgnoreCase(player.getName()))) || (plugin.permissionsCheck(sender, "pvptoggle.other.reset", true)) || (plugin.permissionsCheck(sender, "pvptoggle.admin", true)))) {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
			return; // no permission, return out
		}

		if (worldname.equalsIgnoreCase("*")) {
			// if resetting in all worlds
			for (World world : plugin.getServer().getWorlds()) {
				plugin.setPlayerStatus(player, world.getName(), plugin.getWorldDefault(world.getName()));
			}
			plugin.setLastAction(player, "toggle");
			PvPLocalisation.display(player, "", "", "", PvPLocalisation.Strings.PVP_RESET_PLAYER_GLOBAL);
			PvPLocalisation.display(sender, player.getName(), "", "", PvPLocalisation.Strings.PVP_RESET_PLAYER_GLOBAL_SENDER);
		} else {
			// if resetting in specific world
			plugin.setPlayerStatus(player, worldname, plugin.getWorldDefault(worldname));
			plugin.setLastAction(player, "toggle");
			PvPLocalisation.display(player, "", worldname, "", PvPLocalisation.Strings.PVP_RESET_PLAYER);
			PvPLocalisation.display(sender, player.getName(), worldname, "", PvPLocalisation.Strings.PVP_RESET_PLAYER_SENDER);
		}

	}

}
