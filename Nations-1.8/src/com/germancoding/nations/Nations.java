package com.germancoding.nations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;

import com.germancoding.nations.listeners.BlockListener;
import com.germancoding.nations.listeners.PlayerListener;
import com.germancoding.nations.skills.Skill;
import com.germancoding.nations.tasks.ClassCooldownTask;
import com.germancoding.nations.tasks.ConfigTask;
import com.germancoding.nations.tasks.ItemCooldownTask;
import com.germancoding.nations.tasks.KitCooldownTask;
import com.germancoding.nations.tasks.NationCooldownTask;
import com.germancoding.nations.tasks.NationItemStackUpdateTask;
import com.germancoding.nations.tasks.PvpTask;
import com.germancoding.nations.tasks.ScoreboardTask;
import com.germancoding.nations.tasks.VisibilityCooldownTask;
import com.germancoding.nations.unsave.PacketUtils;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * Main class of Nations.
 * 
 * @author Max
 * 
 */
public class Nations extends JavaPlugin {
	public static boolean TESTING;
	public static boolean DEBUG;

	public static String AUTHOR_NOTE = "This is a plugin by GermanCoding aka Nummer378";
	public static JavaPlugin plugin;

	public static PluginManager pm;
	public static BukkitScheduler scheduler;
	public static Logger logger;
	public static ScoreboardManager sm;
	private static World nationWorld;
	private static ArrayList<NationPlayer> activePlayers;
	private static int zwergenPoints;
	private static int elfenPoints;
	private static final Object lock = new Object();
	private static int zwergenC;
	private static int elfenC;

	public static boolean containsPlayer(NationPlayer p) {
		synchronized (lock) {
			return activePlayers.contains(p);
		}
	}

	public static int getPointsOfDwarfs() {
		synchronized (lock) {
			return zwergenPoints;
		}
	}

	public static int getPointsOfElfen() {
		synchronized (lock) {
			return elfenPoints;
		}
	}

	public static void setPointsOfElfen(int n) {
		synchronized (lock) {
			elfenPoints = n;
		}
	}

	public static void setPointsOfDwarfs(int n) {
		synchronized (lock) {
			zwergenPoints = n;
		}
	}

	public static Iterator<NationPlayer> getIteratorOfPlayers() {
		synchronized (lock) {
			return activePlayers.iterator();
		}
	}

	public static boolean hasNationWorld() {
		return nationWorld != null;
	}

	public static World getNationWorld() {
		return nationWorld;
	}

	@Nonnull
	public static void setNationWorld(World w) {
		nationWorld = w;
	}

	@Override
	public void onEnable() {
		enable(this);
	}

	@Override
	public void onDisable() {
		disable();
	}

	public static void addPlayer(final Player p, boolean startup) {
		final NationPlayer np = ConfigManager.loadPlayer(p);

		synchronized (lock) {
			activePlayers.add(np);
		}

		ScoreboardHandler.addPlayer(np);

		if (!startup) {
			if (np.hasNation()) {
				ChatColor color = null;
				if (np.getNation().equalsIgnoreCase("Zwerge"))
					color = ChatColor.BLUE;
				else
					color = ChatColor.RED;
				String nation = np.getNation();
				if (nation.equalsIgnoreCase("Zwerge"))
					nation = "Zwergen";
				broadcastMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + np.getBukkitPlayer().getName() + ChatColor.GOLD + " von den " + color + nation + ChatColor.GOLD + " hat Nations " + ChatColor.GREEN + "betreten");
			} else {
				broadcastMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + np.getBukkitPlayer().getName() + ChatColor.GOLD + " hat Nations " + ChatColor.GREEN + "betreten");
			}
			p.sendMessage(ChatColor.GOLD + "[Nations] Dies ist eine frühe Entwicklungsversion die noch nicht zu 100% funktioniert!");
			p.sendMessage(ChatColor.GOLD + "Dies ist noch nicht der fertige Spielmodus, sondern nur eine Entwicklungsversion!");
			p.sendMessage(ChatColor.GOLD + "Für weitere Informationen besuche <Link zurzeit nicht verfügbar>"); // http://forum.aknm-craft.com/index.php?page=Board&boardID=36");

			/*
			 * scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			 * @Override public void run() { np.setCanRecChat(false); try { ConfigManager.greetPlayer(p, ChatColor.GOLD + "[Nations] Willkommen, " +
			 * np.getBukkitPlayer().getName()); } catch (IOException e) { np.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " +
			 * ChatColor.RED + "Fehler beim senden deiner Begr��ung :( " + e); } np.setCanRecChat(true); } }, 60l);
			 */
		}

		updatePlayerListName(np);

		if (Nations.DEBUG)
			Nations.logger.info("[Core] Added player " + p.getName() + " to Nations. Startup: " + startup);

	}

	public static void updatePlayerListName(NationPlayer np) {
		String listName = null;
		Player p = np.getBukkitPlayer();
		if (Nations.instanceOf(p) != null) {
			if (!np.hasNation())
				listName = p.getName();
			else if (np.getNation().equalsIgnoreCase("Zwerge"))
				listName = ChatColor.BLUE + p.getName();
			else
				listName = ChatColor.RED + p.getName();
			if (listName.length() > 16)
				listName = listName.substring(0, 16);
		} else {
			listName = p.getName();
		}
		np.getBukkitPlayer().setPlayerListName(listName);
		if (Nations.DEBUG)
			Nations.logger.info("[Core] Refreshed tablist at player " + p.getName());
	}

	public static void removePlayer(NationPlayer p) {
		if (p != null) {
			if (PvpTask.isInCooldown(p.getBukkitPlayer())) {
				broadcastMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getBukkitPlayer().getName() + " hat das Spiel " + ChatColor.RED + " im Kampf verlassen");
				for (ItemStack item : p.getBukkitPlayer().getInventory().getContents()) {
					if (item != null && item.getType() != Material.AIR) {
						try {
							if (SkillManager.isSkillItem(item)) {
								p.getBukkitPlayer().getInventory().remove(item);
								continue;
							}
						} catch (IllegalItemException e1) {
							;
						}
						p.getBukkitPlayer().getLocation().getWorld().dropItem(p.getBukkitPlayer().getLocation(), item);
					}
				}
				for (ItemStack item : p.getBukkitPlayer().getInventory().getArmorContents()) {
					if (item != null && item.getType() != Material.AIR)
						p.getBukkitPlayer().getLocation().getWorld().dropItem(p.getBukkitPlayer().getLocation(), item);
				}
				p.getBukkitPlayer().getInventory().clear();
				try {
					p.getBukkitPlayer().getInventory().setArmorContents(null);
				} catch (Throwable e1) {
					;
				}
			}
			ConfigManager.savePlayer(p);
			synchronized (lock) {
				activePlayers.remove(p);
			}
			updatePlayerListName(p);
			PlayerMarker.removeItem(p.getBukkitPlayer());
			ScoreboardHandler.removePlayer(p);
			ChatColor color = null;
			if (p.getNation().equalsIgnoreCase("Zwerge"))
				color = ChatColor.BLUE;
			else
				color = ChatColor.RED;
			String nation = p.getNation();
			if (nation.equalsIgnoreCase("Zwerge"))
				nation = "Zwergen";
			broadcastMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getBukkitPlayer().getName() + ChatColor.GOLD + " von den " + color + nation + ChatColor.GOLD + " hat Nations " + ChatColor.RED + "verlassen");
			if (Nations.DEBUG)
				Nations.logger.info("[Core] Removed player " + p.getBukkitPlayer().getName() + " from Nations");
		}
	}

	public static void broadcastMessageToNation(NationPlayer nation, String msg) {
		if (nation == null)
			return;
		broadcastMessageToNation(nation.getNation(), msg);
	}

	public static void broadcastMessage(String msg) {
		for (NationPlayer p : activePlayers) {
			p.getBukkitPlayer().sendMessage(msg);
		}
		if (Nations.DEBUG)
			Nations.logger.info("[Broadcast] [Global] " + msg);
	}

	public static void broadcastMessageToNation(String nation, String msg) {
		for (NationPlayer p : activePlayers) {
			if (p.getNation().equalsIgnoreCase(nation))
				p.getBukkitPlayer().sendMessage(msg);
		}
		if (Nations.DEBUG)
			Nations.logger.info("[Broadcast] [" + nation + "] " + msg);
	}

	public static NationPlayer instanceOf(Player player) {
		for (NationPlayer p : activePlayers) {
			if (p.getBukkitPlayer().equals(player) || p.getBukkitPlayer() == player)
				return p;
		}
		return null;
	}

	// Load and enable data.
	public void enable(JavaPlugin p) {
		plugin = p;
		pm = Bukkit.getPluginManager();
		scheduler = Bukkit.getScheduler();
		logger = plugin.getLogger();
		sm = Bukkit.getScoreboardManager();
		activePlayers = new ArrayList<NationPlayer>();
		boolean continueLoad = ConfigManager.enable(p); // Load the data from disk
		if (!continueLoad) {
			ConfigManager.disable();
			Nations.logger.warning("Nations was disabled in the config file. Abort start!");
			plugin = null;
			pm = null;
			scheduler = null;
			logger = null;
			sm = null;
			activePlayers = null;
			return;
		}
		if (hasNationWorld()) {
			for (Player player : Bukkit.getOnlinePlayers()) // On reloads, add
			// every player who
			// is already
			// online.
			{
				if (player.getWorld().equals(nationWorld) || player.getWorld() == nationWorld) { // I am not sure which comparison is better here, but
																									// both should work.
					addPlayer(player, true);
				}
			}
			scheduler.scheduleSyncRepeatingTask(plugin, new ScoreboardTask(), 20 * 2, 20 * 2);
		}
		PacketUtils.enable();
		pm.registerEvents(new PlayerListener(), plugin);
		pm.registerEvents(new BlockListener(), plugin);
		pm.registerEvents(new InventoryViewHandler(), plugin);
		PlayerMarker marker = new PlayerMarker();
		pm.registerEvents(marker, plugin);
		for (Skill c : Skill.SKILLS) {
			c.register(plugin);
		}
		scheduler.scheduleSyncRepeatingTask(plugin, new ConfigTask(), 20 * 60, 20 * 60 * 3);
		scheduler.scheduleSyncRepeatingTask(plugin, new NationItemStackUpdateTask(), 5, 5);
		// marker.runTaskTimer(plugin, 1, 1);
		Nations.log("Nations aktiviert!");
	}

	// Shutdown, save and clear data.
	public void disable() {
		if (plugin == null) // If Nations was already unloaded
		{
			if (Nations.DEBUG)
				this.getLogger().info("Skipping disable method as we are already unloaded."); // Use the save logger. Normal instance could be null.
			return;
		}
		ScoreboardHandler.disable();
		ConfigManager.saveDataToConfig(); // The important function, saving the data :)
		PlayerMarker.removeItems();
		for (InventoryView view : BlockListener.inv1.values())
			// Close all views in the list to prevent errors.
			view.close();
		BlockListener.inv1.clear();
		BlockListener.inv1 = null;
		for (InventoryView view : BlockListener.inv2.values())
			// The same as in inv1.
			view.close();
		BlockListener.inv2.clear();
		BlockListener.inv2 = null;
		for (String s : BlockListener.criticalPlayers) // The same as in inv1.
		{
			Player p = Bukkit.getPlayer(s);
			if (p != null)
				p.closeInventory();
		}
		PacketUtils.disable();
		BlockListener.criticalPlayers.clear();
		BlockListener.criticalPlayers = null;
		ForceField.FIELDS.clear();
		InventoryViewHandler.close();
		ClassCooldownTask.shutdown();
		ItemCooldownTask.shutdown();
		KitCooldownTask.shutdown();
		NationCooldownTask.shutdown();
		PvpTask.shutdown();
		VisibilityCooldownTask.shutdown();
		ConfigManager.disable();
		pm = null;
		scheduler = null;
		nationWorld = null;
		activePlayers = null;
		Nations.log("Nations deaktiviert!");
		plugin = null;
		logger = null;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean color = sender instanceof Player; // Only send colors to
		// players, not to console
		// (Mostly consoles can not
		// show colors correct).
		if (args == null || args.length < 1) // Show main information when there
		// are no arguments
		{
			String msg = ChatColor.GOLD + getDescription().getFullName() + " von ";
			for (String s : getDescription().getAuthors()) {
				msg += s;
				break;
			}
			if (color)
				sender.sendMessage(msg);
			else
				sender.sendMessage(ChatColor.stripColor(msg));
		} else {
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;
			// And here starts the long, long list of commands with their arguments...
			if (instanceOf(p) == null) {
				p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Begebe dich zunächst in die Nations-Welt!");
				return false;
			}
			NationPlayer np = instanceOf(p);
			if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("chat")) {
				if (np.getChatMode().equals(ChatMode.GLOBAL)) {
					np.setChatMode(ChatMode.NATION);
					p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GREEN + "Du befindest dich nun im Nation Chat");
					return true;
				} else if (np.getChatMode().equals(ChatMode.NATION)) {
					np.setChatMode(ChatMode.GLOBAL);
					p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GREEN + "Du befindest dich nun im globalen Chat");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("klasse")) {
				KitManager.handleKit(np, true);
				return true;
			}
			if (args[0].equalsIgnoreCase("wechseln")) {
				InventoryViewHandler.openSwitch(np);
				return true;
			}
			if (args[0].equalsIgnoreCase("addspawn") && p.isOp()) {
				if (args.length < 2) {
					p.sendMessage("Argument Volk fehlt");
					return false;
				}
				String nation = args[1];
				if (nation.equalsIgnoreCase("Zwerge")) {
					SpawnManager.addLocationZwerge(p.getLocation());
					p.sendMessage("Spawn für die Zwerge hinzugefügt");
					return true;
				} else {
					SpawnManager.addLocationElfen(p.getLocation());
					p.sendMessage("Spawn für die Elfen hinzugefügt");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("testing")) {
				if (np.getBukkitPlayer().isOp()) {
					broadcastMessage(ChatColor.GOLD + "[Nations] Testmodus wurde aktiviert! Cooldowns abgeschaltet!");
					TESTING = true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("wechsel")) {
				if (np.getBukkitPlayer().isOp()) {
					if (args.length < 2) {
						sender.sendMessage("Bitte Spieler angeben");
						return true;
					}
					String player = args[1];
					Player bukkitPlayer = Bukkit.getPlayer(player);
					if (bukkitPlayer == null) {
						sender.sendMessage("Spieler nicht online");
						return true;
					}
					NationPlayer nationPlayer = Nations.instanceOf(bukkitPlayer);
					if (nationPlayer == null) {
						sender.sendMessage("Spieler nicht in Nations!");
						return true;
					}
					if (!nationPlayer.hasNation()) {
						sender.sendMessage("Spieler hat keine Nation!");
						return true;
					}
					String nation = nationPlayer.getNation();
					if (ChatColor.stripColor(nation).equalsIgnoreCase("Elfen")) {
						Nations.broadcastMessageToNation("Elfen", ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + nationPlayer.getBukkitPlayer().getName() + ChatColor.GOLD + " hat dein Volk " + ChatColor.RED + "verlassen!");
						nationPlayer.setNation("Zwerge");
						Nations.setDwarfPlayerCount(Nations.getDwarfPlayerCount() + 1);
						Nations.setElfenPlayerCount(Nations.getElfenPayerCount() - 1);
					} else if (ChatColor.stripColor(nation).equalsIgnoreCase("Zwerge")) {
						Nations.broadcastMessageToNation("Zwerge", ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + nationPlayer.getBukkitPlayer().getName() + ChatColor.GOLD + " hat dein Volk " + ChatColor.RED + "verlassen!");
						nationPlayer.setNation("Elfen");
						Nations.setElfenPlayerCount(Nations.getElfenPayerCount() + 1);
						Nations.setDwarfPlayerCount(Nations.getDwarfPlayerCount() - 1);
					}
					Nations.broadcastMessageToNation(nationPlayer, ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + nationPlayer.getBukkitPlayer().getName() + ChatColor.GOLD + " ist deinem Volk " + ChatColor.GREEN + "beigetreten!");
					Nations.updatePlayerListName(nationPlayer);
					PlayerMarker.updatePlayer(nationPlayer);
					nationPlayer.getBukkitPlayer().teleport(SpawnManager.getFirstNationSpawn(nationPlayer));
					new NationCooldownTask(nationPlayer.getBukkitPlayer().getUniqueId().toString(), 2592000);
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("feld")) {
				if (np.getBukkitPlayer().isOp()) {
					Selection sel = ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")).getSelection(p);
					if (sel == null) {
						p.sendMessage("Bitte markiere zuerst ein Gebiet mit WorldEdit!");
						return true;
					}
					if (args.length < 2) {
						p.sendMessage("Bitte gebe eine Fraktion an!");
						return true;
					}
					String nation = args[1];
					new ForceField(nation, sel.getMinimumPoint(), sel.getMaximumPoint());
					p.sendMessage("Kraftfeld für Fraktion '" + nation + "' hinzugefügt");
					return true;
				}
				return true;
			}
			sender.sendMessage("Fehler: Unbekanntes Argument '" + args[0] + "'");
			return false;
		}
		return false;
	}

	public static void handleNewPlayer(NationPlayer p) {
		Inventory inv = Bukkit.createInventory(p.getBukkitPlayer(), 9, "Wähle dein Volk!");
		ItemStack zwerge = new ItemStack(Material.WATER_BUCKET);
		ItemMeta meta = zwerge.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Zwerge");
		ArrayList<String> lore = new ArrayList<String>();
		if (!Balancer.dwarfsOverfilled()) {
			lore.add("Klicke hier um");
			lore.add("ein Zwerg zu werden!");
		} else {
			lore.add("Die Zwerge sind");
			lore.add("aktuell überfüllt!");
		}
		meta.setLore(lore);
		zwerge.setItemMeta(meta);
		inv.setItem(0, zwerge);
		ItemStack elfen = new ItemStack(Material.LAVA_BUCKET);
		meta = elfen.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Elfen");
		lore = new ArrayList<String>();
		if (!Balancer.elfenOverfilled()) {
			lore.add("Klicke hier um");
			lore.add("ein Elf zu werden!");
		} else {
			lore.add("Die Elfen sind");
			lore.add("aktuell überfüllt!");
		}
		meta.setLore(lore);
		elfen.setItemMeta(meta);
		inv.setItem(8, elfen);
		BlockListener.inv1.put(p.getBukkitPlayer(), p.getBukkitPlayer().openInventory(inv));
	}

	public static int getDwarfPlayerCount() {
		synchronized (lock) {
			return zwergenC;
		}
	}

	public static int getElfenPayerCount() {
		synchronized (lock) {
			return elfenC;
		}
	}

	public static void setDwarfPlayerCount(int n) {
		synchronized (lock) {
			zwergenC = n;
		}
	}

	public static void setElfenPlayerCount(int n) {
		synchronized (lock) {
			elfenC = n;
		}
	}

	public static void log(String msg) {
		plugin.getLogger().info(msg);
	}

}
