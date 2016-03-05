package com.sleelin.pvptoggle;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sleelin.pvptoggle.handlers.CommandHandler;
import com.sleelin.pvptoggle.handlers.RegionHandler;
import com.sleelin.pvptoggle.listeners.EntityListener;
import com.sleelin.pvptoggle.listeners.PlayerListener;
import com.sleelin.pvptoggle.listeners.RegionListener;
import com.sleelin.pvptoggle.listeners.WorldListener;

/**
 * PvPToggle
 *
 * @author sleelin
 *
 */

public class PvPToggle extends JavaPlugin {

	// Initialise basic tools used by updater and logger
	public Logger log = Bukkit.getLogger();

	// Instantiate listeners
	private final PlayerListener playerListener = new PlayerListener(this);
	private final EntityListener entityListener = new EntityListener(this);
	private final WorldListener worldListener = new WorldListener(this);
	public RegionListener regionListener;

	// Create settings variables HashMaps
	private HashMap<String, Object> globalsettings = new HashMap<String, Object>();
	protected HashMap<String, PvPWorld> worlds = new HashMap<String, PvPWorld>();
	private HashMap<Player, PvPAction> lastaction = new HashMap<Player, PvPAction>();

	// PvPToggle World class for storing world-specific settings
	public class PvPWorld {
		int cooldown;
		int warmup;
		boolean enabled;
		boolean logindefault;
		HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
	}

	// PvPToggle Action class for storing player last toggle and last combat
	// info
	public class PvPAction {
		Long time;
		String action;

		public PvPAction(Long itime, String iaction) {
			time = itime;
			action = iaction;
		}
	}

	@Override
	public void onEnable() {
		log.info("[" + this.getDescription().getName() + "] Loading...");

		// Load configuration files
		PvPLocalisation.loadProcedure(this);
		this.loadProcedure();

		// Register event listeners
		this.getServer().getPluginManager().registerEvents(this.playerListener, this);
		this.getServer().getPluginManager().registerEvents(this.entityListener, this);
		this.getServer().getPluginManager().registerEvents(this.worldListener, this);

		// Register command handlers
		if ((((String) this.globalsettings.get("command")).equalsIgnoreCase("tpvp")) || ((String) this.globalsettings.get("command")).equalsIgnoreCase("pvpt")) {
			getCommand((String) this.globalsettings.get("command")).setExecutor(new CommandHandler(this));
		} else {
			getCommand("pvp").setExecutor(new CommandHandler(this));
		}

		// Register worldguard listener
		if ((Boolean) this.globalsettings.get("worldguard")) {
			RegionHandler.loadProcedure(this);
			regionListener = new RegionListener(this);
			this.getServer().getPluginManager().registerEvents(this.regionListener, this);
		}

		System.out.println("[" + this.getDescription().getName() + "] v" + this.getDescription().getVersion() + " enabled!");
	}

	@Override
	public void onDisable() {
		worlds.clear();
		globalsettings.clear();
		lastaction.clear();
		log.info("[PvPToggle] Disabled");
	}

	public PvPToggle getHandler() {
		return this;
	}

	/**
	 * Create initial config if it doesn't exist, and load values from the
	 * config if it does exist!
	 */
	private void loadProcedure() {

		// Set config defaults, automatically updating from v1.1.0 and below
		if (!this.getConfig().isSet("plugin.enabled"))
			this.getConfig().set("plugin.enabled", !this.getConfig().getBoolean("globalDisabled", false));
		if (!this.getConfig().isSet("plugin.debug"))
			this.getConfig().set("plugin.debug", this.getConfig().getBoolean("debug", false));
		if (!this.getConfig().isSet("plugin.updateinterval"))
			this.getConfig().set("plugin.updateinterval", this.getConfig().getInt("updateinterval", 21600));
		if (!this.getConfig().isSet("plugin.command"))
			this.getConfig().set("plugin.command", "pvp");
		if (!this.getConfig().isSet("plugin.worldguard-integration"))
			this.getConfig().set("plugin.worldguard-integration", false);

		// Remove redundant nodes
		this.getConfig().set("cooldown", null);
		this.getConfig().set("warmup", null);
		this.getConfig().set("globalDisabled", null);
		this.getConfig().set("debug", null);
		this.getConfig().set("updateinterval", null);

		// Save config
		this.saveConfig();

		// Load config variables or set if nonexistent
		globalsettings.put("enabled", this.getConfig().getBoolean("plugin.enabled", true));
		globalsettings.put("debug", this.getConfig().getBoolean("plugin.debug", false));
		globalsettings.put("updateinterval", this.getConfig().getInt("plugin.updateinterval", 21600));
		globalsettings.put("command", this.getConfig().getString("plugin.command", "pvp"));
		globalsettings.put("citizens", false);
		globalsettings.put("worldguard", this.getConfig().getBoolean("plugin.worldguard-integration", false));

		// Load each world
		for (World world : this.getServer().getWorlds()) {
			loadWorld(world);
		}

		// Load each player's initial action
		for (Player player : this.getServer().getOnlinePlayers()) {
			lastaction.put(player, new PvPAction((long) 0, "login"));
		}

		// Set up WorldGuard hooks
		if ((this.getServer().getPluginManager().getPlugin("WorldGuard") != null) && (this.getServer().getPluginManager().getPlugin("WorldGuard") instanceof WorldGuardPlugin)) {
			log.info("[" + this.getDescription().getName() + "] WorldGuard Plugin detected...");
			if ((Boolean) globalsettings.get("worldguard")) {
				log.info("[" + this.getDescription().getName() + "] WorldGuard integration enabled!");
			} else {
				log.info("[" + this.getDescription().getName() + "] WorldGuard integration disabled via options!");
			}
		}
	}

	/**
	 * Load world-specific settings from config file into worlds array
	 *
	 * @param world
	 */
	public void loadWorld(World world) {

		PvPWorld pvpworld = new PvPWorld();

		// Set per-world settings
		if (!this.getConfig().isSet("worlds." + world.getName() + ".enabled"))
			this.getConfig().set("worlds." + world.getName() + ".enabled", this.getConfig().getBoolean("worlds." + world.getName() + ".pvpenabled", true));
		if (!this.getConfig().isSet("worlds." + world.getName() + ".default"))
			this.getConfig().set("worlds." + world.getName() + ".default", this.getConfig().getBoolean("worlds." + world.getName() + ".logindefault", true));
		if (!this.getConfig().isSet("worlds." + world.getName() + ".cooldown"))
			this.getConfig().set("worlds." + world.getName() + ".cooldown", this.getConfig().getInt("cooldown", 0));
		if (!this.getConfig().isSet("worlds." + world.getName() + ".warmup"))
			this.getConfig().set("worlds." + world.getName() + ".warmup", this.getConfig().getInt("warmup", 0));

		// Remove redundant nodes
		this.getConfig().set("worlds." + world.getName().toString() + ".pvpenabled", null);
		this.getConfig().set("worlds." + world.getName().toString() + ".logindefault", null);

		this.saveConfig();

		// Load world settings
		pvpworld.cooldown = this.getConfig().getInt("worlds." + world.getName() + ".cooldown", 0);
		pvpworld.warmup = this.getConfig().getInt("worlds." + world.getName() + ".warmup", 0);
		pvpworld.enabled = this.getConfig().getBoolean("worlds." + world.getName() + ".enabled", true);
		pvpworld.logindefault = this.getConfig().getBoolean("worlds." + world.getName() + ".default", true);

		// Put all players in the world settings with the default PvP status
		for (Player player : this.getServer().getOnlinePlayers()) {
			pvpworld.players.put(player, pvpworld.logindefault);
		}

		// Save the world settings
		worlds.put(world.getName(), pvpworld);

		log.info("[" + this.getDescription().getName() + "] found and loaded world " + world.getName());

	}

	/**
	 * Set new world status
	 *
	 * @param world
	 *            - name of the world as a string
	 * @param enabled
	 *            - whether or not the world should be enabled or disabled
	 */
	protected void setWorldStatus(String world, boolean enabled) {
		worlds.get(world).enabled = enabled;
	}

	/**
	 * Return specified world's pvp status
	 *
	 * @param world
	 *            - name of the world as a string
	 * @return whether or not PvP is enabled in the world
	 */
	public boolean getWorldStatus(String world) {
		if (world != null)
			return true;
		return worlds.get(world).enabled;
	}

	/**
	 * Return specified world's pvp status
	 *
	 * @param world
	 *            - name of the world as a string
	 * @return whether or not PvP is enabled in the world
	 */
	public boolean getWorldDefault(String world) {
		if (world != null) {
			return worlds.get(world).logindefault;
		} else {
			return true;
		}
	}

	public PvPWorld getWorld(String world) {
		return worlds.get(world);
	}

	/**
	 * Completes partial worldname
	 *
	 * @param targetworld
	 * @return completed worldname, or null
	 */
	public String checkWorldName(String targetworld) {
		String output = null;
		for (World world : this.getServer().getWorlds()) {
			if (world.getName().toLowerCase().contains(targetworld.toLowerCase())) {
				output = world.getName();
				break;
			}
		}
		return output;
	}

	/**
	 * Set the PvP status of a player in a specified world
	 *
	 * @param player
	 *            - who to set the status for
	 * @param world
	 *            - what world to set it in
	 * @param status
	 *            - the new status of the player
	 */
	public void setPlayerStatus(Player player, String world, boolean status) {
		if ((checkWorldName(world) != null) && (player != null)) {
			worlds.get(checkWorldName(world)).players.put(player, status);
		}
	}

	/**
	 * Check status of a player in specified world
	 *
	 * @param player
	 *            - who to check
	 * @param world
	 *            - what world to check in
	 * @return whether PvP is enabled or disabled in specified world
	 */
	public boolean checkPlayerStatus(Player player, String world) {

		// If player not in records (after reload), add to records
		if (!(worlds.get(world).players.containsKey(player))) {
			lastaction.put(player, new PvPAction((long) 0, "login"));
			worlds.get(world).players.put(player, worlds.get(world).logindefault);
		}

		// If forced or denied, return out
		if (this.permissionsCheck(player, "pvptoggle.pvp.force", false))
			return true;
		if (this.permissionsCheck(player, "pvptoggle.pvp.deny", false))
			return false;

		// Return player PvP status
		return worlds.get(world).players.get(player);
	}

	/**
	 * Retrieves a global setting so the array of settings does not have to be
	 * exposed
	 *
	 * @param setting
	 *            - what setting to retrieve
	 * @return the value of the setting
	 */
	public Object getGlobalSetting(String setting) {
		return globalsettings.get(setting);
	}

	/**
	 * Sets a global setting so the array of settings does not have to be
	 * exposed
	 *
	 * @param setting
	 *            - what setting to set
	 * @param value
	 *            - what to set the setting to
	 */
	protected void setGlobalSetting(String setting, Object value) {
		globalsettings.put(setting, value);
	}

	/**
	 * Toggles whether global PvP is enabled or disabled using private
	 * setGlobalSetting function
	 *
	 * @param newval
	 */
	protected void toggleGlobalStatus(Boolean newval) {
		setGlobalSetting("enabled", newval);
	}

	/**
	 * Check global PvP status
	 *
	 * @return global enabled status
	 */
	public Boolean checkGlobalStatus() {
		return (Boolean) globalsettings.get("enabled");
	}

	/**
	 * Set the last action of a player, and time they performed the action
	 *
	 * @param player
	 *            - who performed the action
	 * @param action
	 *            - the action that was performed
	 */
	public void setLastAction(Player player, String action) {
		lastaction.put(player, new PvPAction(new GregorianCalendar().getTime().getTime(), action));
	}

	/**
	 * Checks whether or not it has been longer than the specified cooldown
	 * period since last player PvP
	 *
	 * @param player
	 *            - whose cooldown to check
	 * @return boolean true for wait over, false for still waiting
	 */
	public boolean checkLastAction(Player player, String action, String world) {
		GregorianCalendar cal = new GregorianCalendar();
		Long difference = cal.getTime().getTime() - lastaction.get(player).time;
		int before = 0;
		if (action.equalsIgnoreCase("combat")) {
			if (lastaction.get(player).action.equalsIgnoreCase("toggle")) {
				before = difference.compareTo(((long) worlds.get(world).warmup) * 1000);
			}
		} else if (action.equalsIgnoreCase("toggle")) {
			if (lastaction.get(player).action.equalsIgnoreCase("combat")) {
				before = difference.compareTo(((long) worlds.get(world).cooldown) * 1000);
			}
		}
		if (before >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Custom function to check the permissions of a user against legacy
	 * Permissions, SuperPermissions and OP status
	 *
	 * @param sender
	 *            - who's permissions to check
	 * @param permissions
	 *            - what permission to check for
	 * @param opdefault
	 *            - what to do if the player is an OP
	 * @return whether or not a user has the specified permission
	 */
	public boolean permissionsCheck(CommandSender sender, String permissions, boolean opdefault) {

		boolean haspermissions = opdefault;
		Player player;

		// Check for console (always has permission)
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			return true;
		}

		// Do permissions checking
		if ((Boolean) globalsettings.get("debug"))
			log.info(player.getName().toString() + "/" + permissions + "/Start: " + haspermissions);

		haspermissions = player.hasPermission(permissions);
		if ((Boolean) globalsettings.get("debug"))
			log.info(player.getName().toString() + "/" + permissions + "/Before*: " + haspermissions);
		if (player.hasPermission("*")) {
			haspermissions = opdefault;
		}
		if ((Boolean) globalsettings.get("debug"))
			log.info(player.getName().toString() + "/" + permissions + "/After*: " + haspermissions);

		if ((Boolean) globalsettings.get("debug"))
			log.info(player.getName().toString() + "/" + permissions + "/Final: " + haspermissions);
		return haspermissions;
	}

}