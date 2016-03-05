package com.sleelin.pvptoggle.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sleelin.pvptoggle.PvPToggle;
import com.sleelin.pvptoggle.commands.Global;
import com.sleelin.pvptoggle.commands.Help;
import com.sleelin.pvptoggle.commands.Region;
import com.sleelin.pvptoggle.commands.Reset;
import com.sleelin.pvptoggle.commands.Status;
import com.sleelin.pvptoggle.commands.Toggle;
import com.sleelin.pvptoggle.commands.World;

public class CommandHandler implements CommandExecutor {
	private final PvPToggle plugin;

	public CommandHandler(PvPToggle instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length == 0) {
			if (player != null) {
				new Toggle(plugin, sender, command, label, args).exec();
			} else {
				new Global(plugin, sender, command, label, args).exec();
			}
		} else if ((args[0].equalsIgnoreCase("on")) || (args[0].equalsIgnoreCase("enable")) || (args[0].equalsIgnoreCase("off")) || (args[0].equalsIgnoreCase("disable"))) {
			new Toggle(plugin, sender, command, label, args).exec();
		} else if ((args[0].equalsIgnoreCase("status")) || (args[0].equalsIgnoreCase("s"))) {
			new Status(plugin, sender, command, label, args).exec();
		} else if ((args[0].equalsIgnoreCase("reset")) || (args[0].equalsIgnoreCase("r"))) {
			new Reset(plugin, sender, command, label, args).exec();
		} else if ((args[0].startsWith("w:")) || (args[0].equalsIgnoreCase("world")) || (args[0].equalsIgnoreCase("w"))) {
			new World(plugin, sender, command, label, args).exec();
		} else if ((args[0].equalsIgnoreCase("global")) || (args[0].equalsIgnoreCase("g"))) {
			new Global(plugin, sender, command, label, args).exec();
		} else if (args[0].equalsIgnoreCase("region")) {
			new Region(plugin, sender, command, label, args).exec();
		} else if (args[0].equalsIgnoreCase("help")) {
			new Help(plugin, sender, command, label, args).exec();
		} else {
			new Help(plugin, sender);
		}

		return true;
	}

}
