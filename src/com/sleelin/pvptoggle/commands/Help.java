package com.sleelin.pvptoggle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sleelin.pvptoggle.PvPCommand;
import com.sleelin.pvptoggle.PvPToggle;

public class Help extends PvPCommand {

	public Help(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
		super(plugin, sender, command, label, args);
	}

	public Help(PvPToggle plugin, CommandSender sender) {
		super(plugin, sender);
	}

	@Override
	protected boolean processCommand() {
		if (args.length == 2) {
			// Send help messages
			if (args[1].equalsIgnoreCase("toggle"))
				new Toggle(plugin, sender);
			else if (args[1].equalsIgnoreCase("status"))
				new Status(plugin, sender);
			else if (args[1].equalsIgnoreCase("reset"))
				new Reset(plugin, sender);
			else if (args[1].equalsIgnoreCase("world"))
				new World(plugin, sender);
			else if (args[1].equalsIgnoreCase("global"))
				new Global(plugin, sender);
			else
				sendUsage(sender);
		} else {
			sendUsage(sender);
		}
		return true;
	}

	@Override
	protected void sendUsage(CommandSender sender) {
		sender.sendMessage(helpHeader);
		ChatColor messagecolour = ChatColor.GOLD;
		sender.sendMessage(messagecolour + "/pvp help toggle " + ChatColor.GRAY + "- Show usage information for toggling");
		sender.sendMessage(messagecolour + "/pvp help status " + ChatColor.GRAY + "- Show usage information for checking status");
		sender.sendMessage(messagecolour + "/pvp help reset " + ChatColor.GRAY + "- Show usage information for reset command");
		sender.sendMessage(messagecolour + "/pvp help world " + ChatColor.GRAY + "- Show usage information for world command");
		sender.sendMessage(messagecolour + "/pvp help global " + ChatColor.GRAY + "- Show usage information for global command");
	}

}
