package com.sleelin.pvptoggle.listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import com.sleelin.pvptoggle.handlers.RegionHandler;

public class RegionListener implements Listener {

	public static PvPToggle plugin;
	private static HashMap<String, String[]> playerstatus = new HashMap<String, String[]>();

	public RegionListener(PvPToggle instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		if ((Boolean) plugin.getGlobalSetting("worldguard")) {
			Player player = event.getPlayer();
			WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			Boolean inRegion = false;

			ApplicableRegionSet set = worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(BukkitUtil.toVector(player.getLocation().getBlock()));

			regionloop: for (ProtectedRegion region : set) {
				if (RegionHandler.isApplicableRegion(player.getWorld().getName(), region.getId())) {
					if (playerstatus.containsKey(player.getName())) {
						if (playerstatus.get(player.getName())[1].equalsIgnoreCase(region.getId())) {
							inRegion = true;
							break regionloop;
						}
					}
					for (Flag<?> flag : region.getFlags().keySet()) {
						if (flag.getName().equals("pvp")) {
							if (region.getFlag(flag).equals(StateFlag.State.ALLOW)) {
								plugin.setPlayerStatus(player, player.getWorld().getName(), true);
								playerstatus.put(player.getName(), new String[] { PvPLocalisation.Strings.PVP_ENABLED.toString(), region.getId() });
								PvPLocalisation.display(player, region.getId(), null, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
							} else if (region.getFlag(flag).equals(StateFlag.State.DENY)) {
								plugin.setPlayerStatus(player, player.getWorld().getName(), false);
								playerstatus.put(player.getName(), new String[] { PvPLocalisation.Strings.PVP_DENIED.toString(), region.getId() });
								PvPLocalisation.display(player, region.getId(), null, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
							}
							inRegion = true;
							break regionloop;
						}
					}
				}
			}
			if (!inRegion) {
				if (playerstatus.containsKey(player.getName())) {
					PvPLocalisation.display(player, playerstatus.get(player.getName())[1], null, playerstatus.get(player.getName())[0], PvPLocalisation.Strings.WORLDGUARD_REGION_EXIT);
					playerstatus.remove(player.getName());
				}
			}
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
	public boolean WorldGuardRegionCheck(Player player, String target) {
		if ((Boolean) plugin.getGlobalSetting("worldguard")) {
			if (playerstatus.containsKey(player.getName())) {
				if (playerstatus.get(player.getName())[0].equals(PvPLocalisation.Strings.PVP_ENABLED.toString())) {
					PvPLocalisation.display(player, target, null, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.WORLDGUARD_TOGGLE_DENIED);
				} else if (playerstatus.get(player.getName())[0].equals(PvPLocalisation.Strings.PVP_DENIED.toString())) {
					PvPLocalisation.display(player, target, null, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.WORLDGUARD_TOGGLE_DENIED);
				}
				return false;
			}
		}
		return true;
	}
}
