package com.germancoding.nations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO; //Is javax on all JREs installed? I do not know...

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.germancoding.nations.classes.ClassType;
import com.germancoding.nations.classes.NationClass;
import com.germancoding.nations.tasks.ClassCooldownTask;
import com.germancoding.nations.tasks.ItemCooldownTask;
import com.germancoding.nations.tasks.KitCooldownTask;
import com.germancoding.nations.tasks.NationCooldownTask;

/**
 * The ConfigManager loads, manage and saves data from/to disk.
 * 
 * @author Max
 * 
 */
public class ConfigManager {

	@Deprecated
	/**
	 * This function greets the player in chat (Shows his skin via an ImageMessage - http://forums.bukkit.org/threads/lib-imagemessage-v2-1-send-images-to-players-via-the-chat.204902/)
	 * Funny feature, but actually not important.
	 * Note: The skin is cached until the cache file (can be found in %home%/plugins/Nations/players/face_<uuid_of_player>.png) is deleted.
	 * @Deprecated The server (minecraft.aggenkeech.com) has updated, so this function does not longer work correctly. Will update the function later
	 * @param p The player to greet.
	 * @param text A custom text, displayed beside the image
	 * @throws IOException If the connection with the server fails or if the cache file could not be read.
	 */
	public static void greetPlayer(Player p, String text) throws IOException {
		String uuid = p.getUniqueId().toString();
		String baseUrl = "http://minecraft.aggenkeech.com/face.php?s=8&&u=%player%";
		File file = new File(Nations.plugin.getDataFolder(), "/players/face_" + uuid + ".png");
		if (file.exists()) {
			ImageMessage.imgMessage(p, ImageIO.read(file), 10, ImageMessage.ImgChar.DARK_SHADE.getChar(), text);
		} else {
			file.createNewFile();
			String url = baseUrl.replace("%player%", p.getName());
			BufferedImage image = ImageIO.read(new URL(url));
			ImageIO.write(image, "png", file);
			ImageMessage.imgMessage(p, image, 10, ImageMessage.ImgChar.DARK_SHADE.getChar(), text);
		}
	}

	/**
	 * Sets fields to null to prevent memory leaks.
	 */
	public static void disable() {
		plugin = null;
		system = null;
		systemFile = null;
	}

	/**
	 * Loads the data from disk.
	 * 
	 * @param plugin
	 *            The Nations plugin instance.
	 * 
	 * @return Whether Nations should continue load (config value "enable-nations" = true)
	 */
	public static boolean enable(JavaPlugin plugin) {
		ConfigManager.plugin = plugin;
		plugin.getDataFolder().mkdirs();
		systemFile = new File(plugin.getDataFolder(), "system.yml");
		if (!systemFile.exists())
			try {
				systemFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		configFile = new File(plugin.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
				Scanner scanner = new Scanner(Nations.plugin.getResource("config.yml"));
				FileWriter writer = new FileWriter(configFile);
				while (scanner.hasNextLine()) {
					writer.write(scanner.nextLine());
					writer.append("\n");
				}
				scanner.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setConfig(YamlConfiguration.loadConfiguration(configFile));
		system = YamlConfiguration.loadConfiguration(systemFile);
		if (getConfig().getBoolean("enable-nations")) {
			Nations.DEBUG = getConfig().getBoolean("debug");
			String worldString = system.getString("welt", null);
			World w = null;
			if (worldString != null)
				w = Bukkit.getWorld(worldString);
			if (w != null) {
				Nations.setNationWorld(w);
				if (Nations.DEBUG)
					Nations.logger.info("[ConfigManager] Setting Nation world to " + w.getName());
			}
			Nations.setPointsOfElfen(system.getInt("elfen"));
			Nations.setPointsOfDwarfs(system.getInt("zwerge"));
			Nations.setElfenPlayerCount(system.getInt("elfencounter"));
			Nations.setDwarfPlayerCount(system.getInt("zwergencounter"));
			boolean enableSpawns = system.isConfigurationSection("spawns");
			if (enableSpawns) {
				ArrayList<Location> spawns = new ArrayList<Location>();
				boolean enableElfen = system.isConfigurationSection("spawns.elfen");
				if (enableElfen) {
					ConfigurationSection section = system.getConfigurationSection("spawns.elfen");
					Iterator<String> i = section.getKeys(false).iterator();
					while (i.hasNext()) {
						World world = Nations.getNationWorld();
						String number = i.next();
						int x = system.getInt("spawns.elfen." + number + ".x");
						int y = system.getInt("spawns.elfen." + number + ".y");
						int z = system.getInt("spawns.elfen." + number + ".z");
						float yaw = Float.valueOf(system.getString("spawns.elfen." + number + ".yaw"));
						float pitch = Float.valueOf(system.getString("spawns.elfen." + number + ".pitch"));
						spawns.add(new Location(world, x, y, z, yaw, pitch));
					}
					SpawnManager.loadElfenLocations(spawns);
				}
				boolean enableZwerge = system.isConfigurationSection("spawns.zwerge");
				if (enableZwerge) {
					spawns.clear();
					ConfigurationSection section = system.getConfigurationSection("spawns.zwerge");
					Iterator<String> i = section.getKeys(false).iterator();
					while (i.hasNext()) {
						World world = Nations.getNationWorld();
						String number = i.next();
						int x = system.getInt("spawns.zwerge." + number + ".x");
						int y = system.getInt("spawns.zwerge." + number + ".y");
						int z = system.getInt("spawns.zwerge." + number + ".z");
						float yaw = Float.valueOf(system.getString("spawns.zwerge." + number + ".yaw"));
						float pitch = Float.valueOf(system.getString("spawns.zwerge." + number + ".pitch"));
						spawns.add(new Location(world, x, y, z, yaw, pitch));
					}
					SpawnManager.loadZwergeLocations(spawns);
				}
			}
			boolean enableForceFields = system.isConfigurationSection("forceFields");
			if (enableForceFields) {
				ConfigurationSection section = system.getConfigurationSection("forceFields");
				Iterator<String> i = section.getKeys(false).iterator();
				while (i.hasNext()) {
					String id = i.next();
					int minX = system.getInt("forceFields." + id + ".minX");
					int minY = system.getInt("forceFields." + id + ".minY");
					int minZ = system.getInt("forceFields." + id + ".minZ");
					int maxX = system.getInt("forceFields." + id + ".maxX");
					int maxY = system.getInt("forceFields." + id + ".maxY");
					int maxZ = system.getInt("forceFields." + id + ".maxZ");
					String nation = system.getString("forceFields." + id + ".nation");
					Location min = new Location(w, minX, minY, minZ);
					Location max = new Location(w, maxX, maxY, maxZ);
					ForceField field = new ForceField(nation, min, max);
					if (Nations.DEBUG)
						Nations.logger.info("[ConfigManager] Loaded {" + field.toString() + "}");
				}
			}
			File pluginRoot = plugin.getDataFolder();
			if (pluginRoot.isDirectory())
			{
				File[] files = pluginRoot.listFiles();
				if (files != null)
				{
					File itemRoot = new File(plugin.getDataFolder() + "/items");
					if (!itemRoot.isDirectory())
						itemRoot.mkdirs();
					for (File f : files)
					{
						if (f != null && !f.isDirectory() && f.getName().endsWith(".yml") && f.getName().startsWith("item-")) // Check for item files that are in the root folder (old folder, new folder is /items)
						{
							Nations.logger.info("[ConfigManager] Copying " + f.getName() + " to the new directory...");
							File newFile = new File(itemRoot, f.getName());
							try {
								if (!newFile.exists())
									newFile.createNewFile();
								FileInputStream in = new FileInputStream(f);
								FileOutputStream out = new FileOutputStream(newFile);
								int read = 0;
								while ((read = in.read()) != -1)
								{
									out.write(read);
								}
								in.close();
								out.close();
								if(!f.delete())
								{
									f.deleteOnExit();
									Nations.logger.warning("Unable to delete the old file right now, trying to delete it on exit.");
								}
							} catch (IOException e)
							{
								Nations.logger.warning("Failed to copy the file! Please have a look at the folders and " + f.getName() + "! This item will NOT be loaded!!! " + e);
							}
						}
					}
				}

			}
			File userRoot = new File(plugin.getDataFolder() + "/players");
			if (!userRoot.isDirectory())
				userRoot.mkdirs();
			if (userRoot.list() != null) {
				for (String player : userRoot.list()) {
					player = player.replace(".yml", "");
					if (Nations.DEBUG)
						Nations.logger.info("[ConfigManager] Checking player data for " + player);
					List<Integer> itemList = system.getIntegerList("spieler." + player + ".skills");
					if (itemList != null) {
						File itemRoot = new File(plugin.getDataFolder() + "/items");
						if (!itemRoot.isDirectory())
							itemRoot.mkdirs();
						for (int i : itemList) {
							File file = new File(itemRoot, "item-" + i + ".yml");
							if (!file.exists())
								createItemFileDefault(file);
							FileConfiguration config = YamlConfiguration.loadConfiguration(file);
							try {
								NationItemStack.loadItem(config, player);
							} catch (IllegalItemException e) {
								Nations.logger.warning("[ConfigManager] Failed to load item id " + i + " (for player=" + player + ")! " + e);
							}
							if (Nations.DEBUG)
								Nations.logger.info("[ConfigManager] Loaded item id " + i + " bound at player " + player);
						}
					}
				}
			}
			boolean enableCooldowns = system.isConfigurationSection("cooldown");
			if (enableCooldowns) {
				ConfigurationSection section = system.getConfigurationSection("cooldown");
				Iterator<String> i$ = section.getKeys(false).iterator();
				do {
					if (!i$.hasNext())
						break;
					String name = (String) i$.next();
					int classCooldown = system.getInt("cooldown." + name + ".class", 0);
					if (classCooldown != 0) {
						new ClassCooldownTask(name.trim(), classCooldown);
					}
					int kitCooldown = system.getInt("cooldown." + name + ".kit", 0);
					if (kitCooldown != 0) {
						new KitCooldownTask(name.trim(), kitCooldown);
					}
					int nationCooldown = system.getInt("cooldown." + name + ".nation", 0);
					if (nationCooldown != 0) {
						new NationCooldownTask(name.trim(), nationCooldown);
					}
				} while (true);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Saves the data to disk.
	 */
	public static void saveDataToConfig() {
		if (Nations.hasNationWorld())
			system.set("welt", Nations.getNationWorld().getName());
		system.set("elfen", Integer.valueOf(Nations.getPointsOfElfen()));
		system.set("zwerge", Integer.valueOf(Nations.getPointsOfDwarfs()));
		system.set("elfencounter", Integer.valueOf(Nations.getElfenPayerCount()));
		system.set("zwergencounter", Integer.valueOf(Nations.getDwarfPlayerCount()));
		int counter = 0;
		system.set("spawns", null);
		for (Location l : SpawnManager.getAllLocationsFromElfen()) {
			counter++;
			system.set("spawns.elfen." + counter + ".x", l.getBlockX());
			system.set("spawns.elfen." + counter + ".y", l.getBlockY());
			system.set("spawns.elfen." + counter + ".z", l.getBlockZ());
			system.set("spawns.elfen." + counter + ".yaw", l.getYaw());
			system.set("spawns.elfen." + counter + ".pitch", l.getPitch());
		}
		counter = 0;
		for (Location l : SpawnManager.getAllLocationsFromZwerge()) {
			counter++;
			system.set("spawns.zwerge." + counter + ".x", l.getBlockX());
			system.set("spawns.zwerge." + counter + ".y", l.getBlockY());
			system.set("spawns.zwerge." + counter + ".z", l.getBlockZ());
			system.set("spawns.zwerge." + counter + ".yaw", l.getYaw());
			system.set("spawns.zwerge." + counter + ".pitch", l.getPitch());
		}
		counter = 0;
		if (ForceField.FIELDS != null)
			for (ForceField f : ForceField.FIELDS) {
				counter++;
				system.set("forceFields." + counter + ".minX", f.getMin().getBlockX());
				system.set("forceFields." + counter + ".minY", f.getMin().getBlockY());
				system.set("forceFields." + counter + ".minZ", f.getMin().getBlockZ());
				system.set("forceFields." + counter + ".maxX", f.getMax().getBlockX());
				system.set("forceFields." + counter + ".maxY", f.getMax().getBlockY());
				system.set("forceFields." + counter + ".maxZ", f.getMax().getBlockZ());
				system.set("forceFields." + counter + ".nation", f.getNation());
			}
		system.set("cooldown", null);
		for (String s : ClassCooldownTask.values()) {
			if (Nations.DEBUG) {
				Nations.logger.info("[ConfigManager] Saving class cooldown: " + s + ", " + ClassCooldownTask.getRemainTime(s));
			}
			system.set("cooldown." + s + ".class", ClassCooldownTask.getRemainTime(s));
		}
		for (String s : KitCooldownTask.values()) {
			if (Nations.DEBUG) {
				Nations.logger.info("[ConfigManager] Saving kit cooldown: " + s + ", " + KitCooldownTask.getRemainTime(s));
			}
			system.set("cooldown." + s + ".kit", KitCooldownTask.getRemainTime(s));
		}
		for (String s : NationCooldownTask.values()) {
			if (Nations.DEBUG) {
				Nations.logger.info("[ConfigManager] Saving nation cooldown: " + s + ", " + NationCooldownTask.getRemainTime(s));
			}
			system.set("cooldown." + s + ".nation", NationCooldownTask.getRemainTime(s));
		}
		try {
			system.save(systemFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
		while (iterator.hasNext()) {
			savePlayer(iterator.next());
		}
		saveSkillItems();
		if (Nations.DEBUG)
			Nations.logger.info("[ConfigManager] Performed global data save");
	}

	/**
	 * Saves the data of a single player to disk.
	 * 
	 * @param p
	 *            The player to save.
	 */
	public static void savePlayer(NationPlayer p) {
		String volk = p.getNation();
		NationClass klasse = p.getClasS();
		int kills = p.getKills();
		int death = p.getDeath();
		String name = p.getBukkitPlayer().getName();
		File userdata = new File(plugin.getDataFolder() + "/players", p.getBukkitPlayer().getUniqueId().toString() + ".yml");
		if (!userdata.exists()) {
			createFileDefault(userdata);
			/*
			 * p.setNation("null"); p.setClasS("null"); p.setKills(0); p.setDeath(0); p.setChatMode(ChatMode.GLOBAL); return;
			 */
		}
		FileConfiguration conf = YamlConfiguration.loadConfiguration(userdata);
		conf.set("volk", volk);
		if(klasse != null)
			conf.set("klasse", klasse.toString());
		else
			conf.set("klasse", "");
		conf.set("mode", p.getChatMode().toString());
		conf.set("kills", kills);
		conf.set("tode", death);
		conf.set("name", name);
		try {
			conf.save(userdata);
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveSkillItems(p.getBukkitPlayer().getUniqueId().toString());
	}

	/**
	 * Saves the skill items to disk.
	 */
	public static void saveSkillItems() {
		for (String player : NationItemStack.getPlayers())
			saveSkillItems(player);
	}

	/**
	 * Saves the skill items of a specific player to disk.
	 * 
	 * @param playeruuid
	 *            The uuid of the player.
	 */
	public static void saveSkillItems(String playeruuid) {
		// List<Integer> itemList = system.getIntegerList("spieler." + player +
		// ".skills");
		File itemRoot = new File(plugin.getDataFolder() + "/items");
		if (!itemRoot.isDirectory())
			itemRoot.mkdirs();
		ArrayList<Integer> itemList = new ArrayList<Integer>();
		for (NationItemStack i : NationItemStack.getItemsOf(playeruuid)) {
			if (!itemList.contains(i.getID()))
				itemList.add(i.getID());
			else {
				Nations.plugin.getLogger().warning("Für " + playeruuid + " wurden mehrere Items mit ID " + i.getID() + " gefunden!");
				continue;
			}
			File file = new File(itemRoot, "item-" + i.getID() + ".yml");
			if (!file.exists())
				createItemFileDefault(file);
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			config.set("cooldown", ItemCooldownTask.getRemainTime(i.getID()));
			config.set("material", i.getMaterial().toString());
			config.set("id", i.getID());
			config.set("lore", i.getItemMeta().getLore());
			config.set("item", i.getItemMeta().getDisplayName());
			config.set("type", i.getType().toString());
			config.set("level", i.getLevel());
			config.set("experience", i.getExperience());
			config.set("cooldownBoni", i.getCooldownBoni());
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		system.set("spieler." + playeruuid + ".skills", itemList);
		try {
			system.save(systemFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a player configuration from disk. Note: This will always return a new NationPlayer instance with data from disk, even if there is already
	 * an instance of this player.
	 * 
	 * @param p
	 *            The player to load.
	 * @return The new NationPlayer instance for this player. Should be added to the Nations player list.
	 */
	public static NationPlayer loadPlayer(Player p) {
		File userdata = new File(plugin.getDataFolder() + "/players", p.getUniqueId().toString() + ".yml");
		if (!userdata.exists()) {
			createFileDefault(userdata);
		}
		FileConfiguration conf = YamlConfiguration.loadConfiguration(userdata);
		String volk = conf.getString("volk");
		NationClass klasse = null;
		ClassType type = null;
		try{
			type = ClassType.valueOf(conf.getString("klasse", ClassType.CORRUPT.name()));
		}catch(Exception e)
		{
			type = ClassType.CORRUPT;
		}
		if(type == ClassType.CORRUPT)
		{
			p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Fehler beim laden deiner Klasse! Bitte wähle deine Klasse neu! /n wechseln");
			new ClassCooldownTask(p.getUniqueId().toString(), 1); // Create a new one second cooldown. This will kill a currently running class cooldown.
			for(NationItemStack i: NationItemStack.getItemsOf(p.getUniqueId().toString()))
			{
				i.unregister();
			}
		}
		klasse = NationClass.fromClassType(type); // Warning: Could be null, if type is CORRUPT. NationPlayer.hasClass() should handle that.
		ChatMode mode = ChatMode.valueOf(conf.getString("mode"));
		int kills = conf.getInt("kills");
		int death = conf.getInt("tode");
		return new NationPlayer(p, volk, klasse, kills, death, mode);
	}

	/**
	 * Checks whether this player has a configuration on the disk.
	 * 
	 * @param p
	 *            The player to check.
	 * @return Whether this player has a configuration or not.
	 */
	public static boolean isNewPlayer(Player p) {
		File userdata = new File(plugin.getDataFolder() + "/players", p.getUniqueId().toString() + ".yml");
		return !userdata.exists();
	}

	public static FileConfiguration getConfig() {
		return config;
	}

	public static void setConfig(FileConfiguration config) {
		ConfigManager.config = config;
	}

	public static File getConfigFile() {
		return configFile;
	}

	public static void setConfigFile(File configFile) {
		ConfigManager.configFile = configFile;
	}

	private static void createFileDefault(File f) {
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileConfiguration conf = YamlConfiguration.loadConfiguration(f);
		conf.set("volk", "null");
		conf.set("klasse", "null");
		conf.set("kills", 0);
		conf.set("tode", 0);
		conf.set("mode", ChatMode.GLOBAL.toString());
		conf.set("name", "default");
		try {
			conf.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createItemFileDefault(File f) {
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File systemFile;
	private static FileConfiguration system;
	private static JavaPlugin plugin;
	private static File configFile;
	private static FileConfiguration config;
}
