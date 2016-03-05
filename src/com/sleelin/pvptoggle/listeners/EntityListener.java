package com.sleelin.pvptoggle.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;

import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPLocalisation.Strings;
import com.sleelin.pvptoggle.PvPToggle;

public class EntityListener implements Listener {

	public static PvPToggle plugin;

	public EntityListener(PvPToggle instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (event instanceof EntityDamageByEntityEvent) {
				boolean proceed = false;
				Player damager = null;
				EntityDamageByEntityEvent edbye = (EntityDamageByEntityEvent) event;

				if (edbye.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) edbye.getDamager();
					if (projectile.getShooter() instanceof Player) {
						damager = (Player) projectile.getShooter();
						proceed = true;
					}
				} else if (edbye.getDamager() instanceof Player) {
					damager = (Player) edbye.getDamager();
					proceed = true;
				}

				if (proceed) {
					eventMagic(player, damager, event, "EntityDamage", true);
				}
			}

		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void PotionHit(PotionSplashEvent event) {
		if (event.isCancelled())
			return;
		boolean firstdamage = true;
		ThrownPotion potion = event.getPotion();
		if (potion.getShooter() instanceof Player) {
			Player damager = (Player) potion.getShooter();
			for (LivingEntity entity : event.getAffectedEntities()) {
				if (entity instanceof Player) {
					if (!((Player) entity).equals(damager)) {
						eventMagic((Player) entity, damager, event, "PotionSplash", firstdamage);
						firstdamage = false;
					}
				}
			}
		}
	}

	private void eventMagic(Player player, Player damager, Event event, String type, boolean first) {
		boolean cancellable = false;
		boolean genabled = (Boolean) plugin.getGlobalSetting("enabled");
		if (!(plugin.getWorldStatus(player.getWorld().getName())) || (!genabled)) {
			if (!(plugin.getWorldStatus(player.getWorld().getName()))) {
				if (first)
					PvPLocalisation.display(damager, "", player.getWorld().getName(), "", PvPLocalisation.Strings.PVP_DENY_WORLD);
			}
			if (!genabled) {
				if (first)
					PvPLocalisation.display(damager, "", "", "", PvPLocalisation.Strings.PVP_DENY_GLOBAL);
			}
			cancellable = true;
		}
		if (!plugin.permissionsCheck(player, "pvptoggle.pvp.force", false)) {
			boolean targetenabled = plugin.checkPlayerStatus(player, player.getWorld().getName());
			boolean damagerenabled = plugin.checkPlayerStatus(damager, player.getWorld().getName());
			if ((!plugin.checkLastAction(damager, "combat", damager.getWorld().getName())) && (!(plugin.permissionsCheck(damager, "pvptoggle.pvp.bypasswarmup", false)))) {
				if (first)
					PvPLocalisation.display(damager, "", "", "", PvPLocalisation.Strings.PLAYER_WARMUP);
				cancellable = true;
			}
			if ((plugin.permissionsCheck(damager, "pvptoggle.pvp.autoenable", false)) && (!damagerenabled)) {
				plugin.setPlayerStatus(damager, damager.getWorld().getName(), true);
				damagerenabled = true;
				PvPLocalisation.display(damager, "", "", "", PvPLocalisation.Strings.PLAYER_AUTOENABLE);
			}
			if ((!targetenabled) || (!damagerenabled)) {
				Strings message = null;
				if (!targetenabled) {
					message = PvPLocalisation.Strings.PVP_DENY_OPPONENT;
				}
				if (!damagerenabled) {
					message = PvPLocalisation.Strings.PVP_DENY_DISABLED;
				}
				if (first)
					PvPLocalisation.display(damager, player.getName(), player.getWorld().getName(), "", message);
				cancellable = true;
			} else {
				plugin.setLastAction(damager, "combat");
				plugin.setLastAction(player, "combat");
			}
		}
		if (cancellable) {
			if (type.equalsIgnoreCase("EntityDamage")) {
				((EntityDamageEvent) event).setCancelled(true);
			} else if (type.equalsIgnoreCase("PotionSplash")) {
				((PotionSplashEvent) event).setCancelled(true);
			}
		}
	}

}
