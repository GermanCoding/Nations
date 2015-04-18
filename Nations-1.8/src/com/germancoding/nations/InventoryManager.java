package com.germancoding.nations;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * This class was designed for switching between to inventories. One inventory in the Nations world and one for the other worlds.
 * @deprecated Replaced by Multiverse-Inventories. This class uses serialization by KILL3RTACO and is not up to date with the newest items in Minecraft. 
 * Furthermore the file system used here is outdated and not updated to work with uuids. Using this is not recommended,
 * altough this class may still work correctly.
 * @author Max
 *
 */
@Deprecated
public class InventoryManager {

	@Deprecated
	public static void switchToNations(Player p) {
		File nations = new File(Nations.plugin.getDataFolder(), "/spieler/inv-nations-" + p.getName() + ".dat");
		if (!nations.exists())
			try {
				nations.createNewFile();
			} catch (IOException e) {
				;
			}
		File main = new File(Nations.plugin.getDataFolder(), "/spieler/inv-main-" + p.getName() + ".dat");
		if (!main.exists())
			try {
				main.createNewFile();
			} catch (IOException e) {
				;
			}
		try {
			saveInventory(p.getInventory(), main, true);
		} catch (Throwable e) {
			;
		}
		try {
			p.getInventory().clear();
		} catch (Throwable e) {
			;
		}
		try {
			p.getInventory().setArmorContents(null);
		} catch (Throwable e) {
			;
		}
		try {
			setInventory(p, nations);
		} catch (Throwable e) {
			;
		}
	}

	@Deprecated
	public static void switchToMain(Player p) {
		File nations = new File(Nations.plugin.getDataFolder(), "/spieler/inv-nations-" + p.getName() + ".dat");
		if (!nations.exists())
			try {
				nations.createNewFile();
			} catch (IOException e) {
				;
			}
		File main = new File(Nations.plugin.getDataFolder(), "/spieler/inv-main-" + p.getName() + ".dat");
		if (!main.exists())
			try {
				main.createNewFile();
			} catch (IOException e) {
				;
			}
		try {
			saveInventory(p.getInventory(), nations, true);
		} catch (Throwable e) {
			;
		}
		try {
			p.getInventory().clear();
		} catch (Throwable e) {
			;
		}
		try {
			p.getInventory().setArmorContents(null);
		} catch (Throwable e) {
			;
		}
		try {
			setInventory(p, main);
		} catch (Throwable e) {
			;
		}
	}

	public static void saveInventory(PlayerInventory inv, File file, boolean override) {
		Player p = (Player) inv.getHolder();
		if (inv == null || file == null)
			return;
		if (file.exists() && override)
			file.delete();
		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

		ItemStack[] contents = inv.getContents();
		ItemStack[] armor = inv.getArmorContents();

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null)
				if (item.getType() != Material.AIR)
					conf.set("Slot." + i, item);
		}

		for (int i = 0; i < armor.length; i++) {
			ItemStack item = armor[i];
			if (item != null)
				if (item.getType() != Material.AIR)
					conf.set("ArmorSlot." + i, item);
		}
		for (PotionEffect pot : p.getActivePotionEffects()) {
			conf.set("PotionEffects." + pot.getType().getName() + ".level", pot.getAmplifier());
			conf.set("PotionEffects." + pot.getType().getName() + ".duration", pot.getDuration());
		}

		try {
			conf.save(file);
		} catch (IOException e) {
			return;
		}
	}

	private static ItemStack[] getInventory(File file) {
		if (file == null)
			return null;
		ItemStack[] items = null;

		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

		if (conf.contains("Slot") && conf.isConfigurationSection("Slot")) {
			int size = conf.getInt("Slot", 27);

			items = new ItemStack[size];

			for (int i = 0; i < size; i++) {
				if (conf.contains("Slot." + i))
					items[i] = conf.getItemStack("Slot." + i);
				else
					items[i] = new ItemStack(Material.AIR);
			}
		}

		return items;
	}

	private static ItemStack[] getArmorContents(File file) {
		if (file == null)
			return null;
		ItemStack[] items = null;

		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

		if (conf.contains("ArmorSlot") && conf.isConfigurationSection("ArmorSlot")) {
			int size = conf.getInt("ArmorSlot", 4);

			items = new ItemStack[size];

			for (int i = 0; i < size; i++) {
				if (conf.contains("ArmorSlot." + i))
					items[i] = conf.getItemStack("ArmorSlot." + i);
				else
					items[i] = new ItemStack(Material.AIR);
			}
		}

		return items;
	}

	private static Collection<PotionEffect> getPotionEffects(File file) {
		if (file == null)
			return null;
		Collection<PotionEffect> pots = new HashSet<PotionEffect>();

		PotionEffect pot = null;

		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

		if (conf.contains("PotionEffects") && conf.isConfigurationSection("PotionEffects")) {
			for (String str : conf.getConfigurationSection("PotionEffects").getKeys(false)) {
				int level = conf.getInt("PotionEffects." + str + ".level");
				int duration = conf.getInt("PotionEffects." + str + ".duration");
				pot = new PotionEffect(PotionEffectType.getByName(str), duration, level);
				pots.add(pot);
			}
		}

		return pots;
	}

	public static void setInventory(Player player, File file) {
		if (file == null || player == null)
			return;
		PlayerInventory pi = player.getInventory();
		pi.setArmorContents(getArmorContents(file));
		pi.setContents(getInventory(file));
		player.addPotionEffects(getPotionEffects(file));
	}

}
