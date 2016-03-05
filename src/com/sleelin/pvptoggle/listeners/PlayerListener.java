package com.sleelin.pvptoggle.listeners;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sleelin.pvptoggle.PvPToggle;

public class PlayerListener implements Listener {

	public static PvPToggle plugin;

	public PlayerListener(PvPToggle instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		for (World world : plugin.getServer().getWorlds()) {
			plugin.checkPlayerStatus(player, world.getName());
		}
	}

}
