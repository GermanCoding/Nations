package com.germancoding.nations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.germancoding.nations.listeners.BlockListener;
import com.germancoding.nations.skills.Skill;
import com.germancoding.nations.skills.SkillType;
import com.germancoding.nations.tasks.ItemCooldownTask;

/**
 * This class represents skill items. Skill items are specific items created by Nations which can be activated. Every skill item is bound to one
 * player which have the item always in his inventory.
 * 
 * @author Max
 *
 */
public class NationItemStack {

	private ItemMeta meta;
	private Object localLock = new Object(); // Lock für das Objekt, wird nur von den Gettern und Settern benutzt
	private static Object mainLock = new Object(); // Globaler Lock zum schützen der Instanzen
	private String playerUuid;
	private int ID;
	private SkillType type;
	private Material material;
	private int level;
	private double experience;
	private int cooldownBoni;
	private int defaultCooldown;
	private int cooldown;
	private static ArrayList<NationItemStack> instances = new ArrayList<NationItemStack>(); // Wichtiges Objekt, muss immer synchronisiert sein, da
																							// sehr viele Zugriffe

	/**
	 * <b> Note: Do not use this constructor to create new skill items. Use createNewItem() instead. </b>
	 * Creates a new NationItemStack with the given parameters. The instance is automatically added to the registry and should update on the next
	 * ticks.
	 * If the player does not have the item in his inventory it will be automatically added.
	 * This instance is also active when the player is not online.
	 * 
	 * @param playerUuid
	 *            The uuid of the owner/player of this item.
	 * @param meta
	 *            MetaData for the item, should contain descriptions, status of the item and id of the item.
	 * @param ID
	 *            The ID of this item. Should match with the MetaData.
	 * @param material
	 *            The material/item shown in the inventory of the player.
	 * @param type
	 *            Type of this item.
	 */
	public NationItemStack(String playerUuid, ItemMeta meta, int ID, Material material, SkillType type, int level, double experience, int cooldownBoni) {
		this.setPlayerUuid(playerUuid);
		this.setItemMeta(meta);
		this.setID(ID);
		this.setMaterial(material);
		this.setType(type);
		for (Skill s : Skill.SKILLS) {
			if (s.getSkillType() == type) {
				this.setDefaultCooldown(s.getDefaultCooldown());
				break;
			}
		}
		this.setLevel(level);
		this.setExperience(experience);
		this.setCooldownBoni(cooldownBoni);
		this.setCooldown(getDefaultCooldown() - getCooldownBoni());
		synchronized (mainLock) {
			instances.add(this);
		}
	}

	/**
	 * Loads an NationItemStack from disk.
	 * 
	 * @param config
	 *            Config which contains the data.
	 * @param playerUuid
	 *            UUUID of the owner of the NationItemStack.
	 * @throws IllegalItemException
	 *             In case the config is invalid.
	 */
	public static void loadItem(FileConfiguration config, String playerUuid) throws IllegalItemException {
		int cooldown = config.getInt("cooldown", 0);
		int ID = config.getInt("id", -1);
		if (ID < 0)
			throw new IllegalItemException("Invalid FileConfiguration (playerUUUID = " + playerUuid + " ! Invalid ID!");
		Material material = Material.valueOf(config.getString("material", "AIR"));
		if (material == Material.AIR)
			throw new IllegalItemException("Invalid FileConfiguration for item id " + ID + "! No material found!");
		List<String> lore = config.getStringList("lore");
		String itemName = config.getString("item", "__INVALID__");
		if (itemName == null || itemName.equalsIgnoreCase("__INVALID__"))
			throw new IllegalItemException("Invalid FileConfiguration for item id " + ID + "! No item-name found!");
		SkillType type = SkillType.valueOf(config.getString("type", SkillType.BLUTRAUSCH.toString()));
		ItemStack stack = createItem(itemName, lore, material);
		int level = config.getInt("level");
		double experience = config.getDouble("experience");
		int cooldownBoni = config.getInt("cooldownBoni");
		NationItemStack item = new NationItemStack(playerUuid, stack.getItemMeta(), ID, material, type, level, experience, cooldownBoni);
		if (cooldown > 0) {
			new ItemCooldownTask(item.getID(), cooldown);
		}
	}

	/**
	 * @return Returns the UUIDs of all players that have one or more NationItemStacks in the registry.
	 */
	public static List<String> getPlayers() {
		ArrayList<String> players = new ArrayList<String>();
		synchronized (mainLock) {
			for (NationItemStack i : instances) {
				if (!players.contains(i.getPlayerUuid()))
					players.add(i.getPlayerUuid());
			}
		}
		return players;
	}

	/**
	 * @return Returns an ID that is actually not used by any NationItemStacks and should be used for the next NationItemStack. Always the lowest
	 *         number that is not registered.
	 */
	public static int getFreeID() {
		int lowest = 1;
		int highest = 0;
		synchronized (mainLock) {
			for (NationItemStack s : instances) {
				if (s.getID() > highest)
					highest = s.getID();
			}
			boolean exit = false;
			int round = 0;
			while (!exit) {
				for (NationItemStack s : instances) {
					if (s.getID() == lowest)
						lowest++;
					if (lowest > highest) {
						exit = true;
						break;
					}
				}
				round++;
				if (round >= instances.size())
					break;
			}
		}
		return lowest;
	}

	/**
	 * Creates a brand new NationItemStack with the given parameters. The NationItemStack is automatically added to the registry and will be updated
	 * on the next ticks.
	 * 
	 * @param playerUuid
	 *            The UUID of the player who should own this item.
	 * @param name
	 *            The name of this item.
	 * @param text
	 *            Any text, e.g descriptions that will be displayed as lore of the item.
	 * @param material
	 *            Material for this item.
	 * @param type
	 *            Type for this item.
	 * @return The ItemStack even created. This ItemStack can be added to the players inventory, if you do not do that the plugin will automatically
	 *         add it on the next update tick.
	 *         Note: This function also creates a NationItemStack (which is added to the registry). This object is not returned but you can get it via <code>getItemsOf(playerUuid)</code>.
	 */
	public static ItemStack createNewItem(String playerUuid, String name, List<String> text, Material material, SkillType type) {
		int ID = getFreeID();
		ArrayList<String> lore = new ArrayList<String>();
		lore.addAll(text);
		lore.add("Fähigkeit bereit!");
		lore.add("-------");
		lore.add("ID: " + ID);
		ItemStack newItem = createItem(name, lore, material);
		new NationItemStack(playerUuid, newItem.getItemMeta(), ID, material, type, 0, 0, 0);
		return newItem;
	}

	/**
	 * Creates an normal ItemStack with the given parameters. Nothing else.
	 * 
	 * @param name
	 *            Name of the ItemStack
	 * @param lore
	 *            Lore of the ItemStack
	 * @param type
	 *            Type/Material of the ItemStack
	 * @return A new ItemStack with the given parameters.
	 */
	public static ItemStack createItem(String name, List<String> lore, Material type) {
		ItemStack newItem = new ItemStack(type);
		ItemMeta meta = newItem.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		newItem.setItemMeta(meta);
		return newItem;
	}

	/**
	 * Gets the NationItemStack that has this ID.
	 * 
	 * @param ID
	 *            The ID to search for.
	 * @return A NationItemStack with the given ID or null if no object was found.
	 */
	public static NationItemStack getByID(int ID) {
		synchronized (mainLock) {
			for (NationItemStack i : instances) {
				if (i.getID() == ID)
					return i;
			}
		}
		return null;
	}

	/**
	 * Gets all NationItemStacks actually registered for this player.
	 * 
	 * @param playerUuid
	 *            The player to search for.
	 * @return A list with all NationItemStacks this player has. Never returns null, only empty lists are possible.
	 */
	public static ArrayList<NationItemStack> getItemsOf(String playerUuid) {
		ArrayList<NationItemStack> list = new ArrayList<NationItemStack>();
		synchronized (mainLock) {
			for (NationItemStack i : instances) {
				if (i.getPlayerUuid().equals(playerUuid)) {
					list.add(i);
				}
			}
		}
		return list;
	}

	/**
	 * @return The current ItemMeta for this item.
	 */
	public ItemMeta getItemMeta() {
		synchronized (localLock) {
			return meta;
		}
	}

	/**
	 * @param meta
	 *            Sets the ItemMeta for this item. Call this function (from outside, its called main) only if you know what you are doing, corruption is very likely!
	 */
	public void setItemMeta(ItemMeta meta) {
		synchronized (localLock) {
			this.meta = meta;
		}
	}

	/**
	 * @return The UUID of the player which owns this item.
	 */
	public String getPlayerUuid() {
		synchronized (localLock) {
			return playerUuid;
		}
	}

	/**
	 * Sets the UUID of the player which owns this item. Changing the UUID could force the plugin to change the owner of this item (Old owner loses
	 * the item in his inventory,
	 * new owner will get this item into his inventory). Should be possible, but is not yet tested!
	 * 
	 * @param player
	 */
	public void setPlayerUuid(String player) {
		synchronized (localLock) {
			this.playerUuid = player;
		}
	}

	/**
	 * @return Gets the ID of this item.
	 */
	public int getID() {
		synchronized (localLock) {
			return ID;
		}
	}

	/**
	 * @param n
	 *            Sets the ID of this item to <code>n</code>. Never change to ID's that are already in use!
	 */
	public void setID(int n) {
		synchronized (localLock) {
			ID = n;
		}
	}

	/**
	 * @return The material of this item.
	 */
	public Material getMaterial() {
		synchronized (localLock) {
			return material;
		}
	}

	/**
	 * @param type
	 *            Sets the type of this item.
	 */
	public void setType(SkillType type) {
		synchronized (localLock) {
			this.type = type;
		}
	}

	/**
	 * @return Gets the type of this item.
	 */
	public SkillType getType() {
		synchronized (localLock) {
			return type;
		}
	}

	/**
	 * @param material
	 *            Changes the material of this item.
	 */
	public void setMaterial(Material material) {
		synchronized (localLock) {
			this.material = material;
		}
	}

	/**
	 * Removes this item from the registry and from the player inventory.
	 */
	public void unregister() {
		synchronized (mainLock) {
			instances.remove(this);
		}
		if (ItemCooldownTask.getRemainTime(this.getID()) != 0) {
			ItemCooldownTask.kill(this.getID());
		}
		Player p = Bukkit.getPlayer(UUID.fromString(getPlayerUuid()));
		if (p != null && p.getInventory() != null && p.isOnline()) {
			for (ItemStack item : p.getInventory().getContents()) {
				if (item != null) {
					try {
						if (SkillManager.isSkillItem(item)) {
							int ID = SkillManager.getIDOfItem(item);
							if (ID == this.getID()) {
								p.getInventory().remove(item);
								p.updateInventory();
							}
						}
					} catch (IllegalItemException e) {
						p.getInventory().remove(item);
						p.updateInventory();
					}
				}
			}
		}
	}

	/**
	 * Performs a sync between the items in the inventory of the players and the NationItemStack objects (Updates primarily the cooldown in the lore).
	 * Nations calls this function automatically.
	 */
	public static void updatePlayerItems() {
		synchronized (mainLock) {
			for (NationItemStack s : instances) {
				int i = ItemCooldownTask.getRemainTime(s.getID());
				if (i == 0) {
					s.setItemReady();
				} else {
					s.setItemCooldown(i);
				}
				Player p = Bukkit.getPlayer(UUID.fromString(s.getPlayerUuid()));
				if (p != null)
					if (Nations.instanceOf(p) == null)
						continue;
				boolean found = false;
				if (p != null && p.getInventory() != null && p.isOnline()) {
					for (ItemStack item : p.getInventory().getContents()) {
						if (item != null) {
							try {
								if (SkillManager.isSkillItem(item)) {
									int ID = SkillManager.getIDOfItem(item);
									if (getByID(ID) == null || !getByID(ID).getPlayerUuid().equals(p.getUniqueId().toString())) {
										if (Nations.DEBUG)
											Nations.logger.info("[ItemStack] Removed a skill item from " + p.getPlayer().getName() + "'s inventory. Cause: Null or invalid owner.");
										p.getInventory().remove(item);
										p.updateInventory();
										continue;
									}
									if (ID == s.getID()) {
										if (!found) {
											if (item.getAmount() > 1)
												item.setAmount(1);
											if (item.getType() != s.getMaterial())
												item.setType(s.getMaterial());
											item.setItemMeta(s.getItemMeta());
											found = true;
										} else {
											synchronized (BlockListener.invLock3) {
												if (BlockListener.criticalPlayers.contains(p.getUniqueId().toString()) == false) {
													if (Nations.DEBUG)
														Nations.logger.info("[ItemStack] Removed a skill item from " + p.getPlayer().getName() + "'s inventory. Cause: Duplicate item.");
													p.getInventory().remove(item);
													p.updateInventory();
												}
											}
										}
									}
								}
							} catch (IllegalItemException e) {
								if (Nations.DEBUG)
									Nations.logger.info("[ItemStack] Removed a skill item from " + p.getPlayer().getName() + "'s inventory. Cause: IllegalItemException catched.");
								p.getInventory().remove(item);
								p.updateInventory();
							}
						}
					}
					ItemStack[] items1 = p.getInventory().getArmorContents().clone();
					for (ItemStack item : items1) {
						if (item != null) {
							try {
								if (SkillManager.isSkillItem(item)) {
									ItemStack[] list = p.getInventory().getArmorContents();
									for (int i1 = 0; i1 < list.length; i1++) {
										ItemStack item1 = list[i1];
										if (item.equals(item1)) {
											if (Nations.DEBUG)
												Nations.logger.info("[ItemStack] Removed a skill item from " + p.getPlayer().getName() + "'s armor.");
											list[i1] = new ItemStack(Material.AIR);
											p.getInventory().setArmorContents(list);
											p.updateInventory();
											break;
										}
									}
								}
							} catch (IllegalItemException e) {
								p.getInventory().remove(item);
								p.updateInventory();
							}
						}
					}
					if (!found) {
						synchronized (BlockListener.invLock3) {
							if (!BlockListener.criticalPlayers.contains(p.getUniqueId().toString())) {
								p.getInventory().addItem(createItem(s.getItemMeta().getDisplayName(), s.getItemMeta().getLore(), s.getMaterial()));
								p.updateInventory();
								if (Nations.DEBUG)
									Nations.logger.info("[ItemStack] Added a skill item to " + p.getPlayer().getName() + ". Cause: Item not found.");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the lore to ("Item ready"). Called when the cooldown of the item is 0.
	 */
	public void setItemReady() {
		for (String s : getItemMeta().getLore()) {
			if (ChatColor.stripColor(s).startsWith("Cooldown:")) {
				List<String> lore = getItemMeta().getLore();
				lore.set(lore.indexOf(s), "Fähigkeit bereit!");
				getItemMeta().setLore(lore);
			}
		}
	}

	/**
	 * @param n
	 *            Sets the cooldown in the lore to <code>n</code>.
	 */
	public void setItemCooldown(int n) {
		for (String s : getItemMeta().getLore()) {
			if (ChatColor.stripColor(s).startsWith("Cooldown:") || ChatColor.stripColor(s).startsWith("Fähigkeit bereit!")) {
				int hour = n / 60 / 60;
				int min = (n - hour * 60 * 60) / 60;
				int sec = (n - hour * 60 * 60 - min * 60);
				String mins = "" + min;
				if (min < 10)
					mins = "0" + min;
				String secs = "" + sec;
				if (sec < 10)
					secs = "0" + sec;
				List<String> lore = getItemMeta().getLore();
				lore.set(lore.indexOf(s), "Cooldown: " + mins + ":" + secs);
				getItemMeta().setLore(lore);
			}
		}
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getExperience() {
		return experience;
	}

	public void setExperience(double experience) {
		this.experience = experience;
	}

	public int getCooldownBoni() {
		return cooldownBoni;
	}

	public void setCooldownBoni(int cooldownBoni) {
		this.cooldownBoni = cooldownBoni;
	}

	public int getDefaultCooldown() {
		return defaultCooldown;
	}

	public void setDefaultCooldown(int defaultCooldown) {
		this.defaultCooldown = defaultCooldown;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

}
