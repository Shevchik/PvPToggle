package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import com.sleelin.pvptoggle.handlers.RegionHandler;

public class Region extends PvPCommand {

	public Region(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public Region(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		if (args.length > 2) {
			if ((args[1].equalsIgnoreCase("add")) || (args[1].equalsIgnoreCase("a"))) {
				if (args.length > 3) {
					String world = isWorld(sender, args[3]);
					if (world != null)
						addRegion(sender, world, args[2]);
				} else {
					if (sender instanceof Player) {
						addRegion(sender, player.getWorld().getName(), args[2]);
					} else {
						sendUsage(sender);
					}
				}
			} else if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("r"))) {
				if (args.length > 3) {
					String world = isWorld(sender, args[3]);
					if (world != null)
						removeRegion(sender, world, args[2]);
				} else {
					if (sender instanceof Player) {
						removeRegion(sender, player.getWorld().getName(), args[2]);
					} else {
						sendUsage(sender);
					}
				}
			}
		} else {
			sendUsage(sender);
		}

		return true;
	}

	@Override
	protected void sendUsage(CommandSender sender) {
		sender.sendMessage(helpHeader);
		ChatColor messagecolour = ChatColor.GOLD;
		if (plugin.permissionsCheck(sender, "pvptoggle.region.add", true)) {
			if (sender instanceof Player)
				sender.sendMessage(messagecolour + "/pvp region add [region] " + ChatColor.GRAY + "- Adds a region to the current world");
			sender.sendMessage(messagecolour + "/pvp region add [region] [world] " + ChatColor.GRAY + "- Adds a region to the specified world");
		}
		if (plugin.permissionsCheck(sender, "pvptoggle.region.remove", true)) {
			if (sender instanceof Player)
				sender.sendMessage(messagecolour + "/pvp region remove [region] " + ChatColor.GRAY + "- Removes a region from the current world");
			sender.sendMessage(messagecolour + "/pvp region remove [region] [world] " + ChatColor.GRAY + "- Removes a region from the specified world");
		}
	}

	private void addRegion(CommandSender sender, String world, String region) {
		if (plugin.permissionsCheck(sender, "pvptoggle.regions.add", true)) {
			RegionHandler.addRegion(sender, world, args[2]);
			PvPLocalisation.display(sender, args[2], world, "", PvPLocalisation.Strings.WORLDGUARD_REGION_ADDED);
		} else {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
		}
	}

	private void removeRegion(CommandSender sender, String world, String region) {
		if (plugin.permissionsCheck(sender, "pvptoggle.regions.add", true)) {
			RegionHandler.removeRegion(sender, world, args[2]);
			PvPLocalisation.display(sender, args[2], world, "", PvPLocalisation.Strings.WORLDGUARD_REGION_REMOVED);
		} else {
			PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.NO_PERMISSION);
		}
	}

}
