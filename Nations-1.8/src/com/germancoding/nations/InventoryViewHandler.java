package com.germancoding.nations;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.germancoding.nations.classes.ClassType;
import com.germancoding.nations.skills.Skill;
import com.germancoding.nations.tasks.ClassCooldownTask;
import com.germancoding.nations.tasks.NationCooldownTask;

public class InventoryViewHandler implements Listener {

	private static HashMap<NationPlayer, Integer> list = new HashMap<NationPlayer, Integer>();

	public static void openSwitch(NationPlayer p) {
		openSelectMenu(p);
	}

	public static void openClassSelectMenu(NationPlayer p) {
		if (!list.containsKey(p))
			list.put(p, 1);
		Inventory inventory = Bukkit.createInventory(null, 9, "Wähle deine Klasse!");
		
		ItemStack krieger = new ItemStack(Material.IRON_SWORD, 1);
		ItemMeta meta = krieger.getItemMeta();
		meta.setDisplayName("Krieger");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Schaden + Nahkampf.");
		lore.add("Einfach und effektiv.");
		lore.add("Fähigkeiten:");
		for(Skill s: Skill.SKILLS)
		{
			if(SkillManager.isItemForClass(ClassType.KRIEGER, s.getSkillType()))
				lore.add("- " + s.getFriendlyName());
		}
		meta.setLore(lore);
		krieger.setItemMeta(meta);
		
		ItemStack windmagier = new ItemStack(Material.EYE_OF_ENDER, 1);
		meta = windmagier.getItemMeta();
		meta.setDisplayName("Windmagier");
		lore.clear();
		lore.add("Wirbeln + fliegen.");
		lore.add("Magie der Winde...");
		lore.add("Fähigkeiten:");
		for(Skill s: Skill.SKILLS)
		{
			if(SkillManager.isItemForClass(ClassType.WINDMAGIER, s.getSkillType()))
				lore.add("- " + s.getFriendlyName());
		}
		meta.setLore(lore);
		windmagier.setItemMeta(meta);
		
		ItemStack bogenschütze = new ItemStack(Material.ARROW, 1);
		meta = bogenschütze.getItemMeta();
		meta.setDisplayName("Bogenschütze");
		lore.clear();
		lore.add("Schaden + Fernkampf.");
		lore.add("Ziele gut und treffe gut.");
		lore.add("Fähigkeiten:");
		for(Skill s: Skill.SKILLS)
		{
			if(SkillManager.isItemForClass(ClassType.BOGENSCHÜTZE, s.getSkillType()))
				lore.add("- " + s.getFriendlyName());
		}
		meta.setLore(lore);
		bogenschütze.setItemMeta(meta);
		
		ItemStack medizinmann = new ItemStack(Material.BONE, 1);
		meta = medizinmann.getItemMeta();
		meta.setDisplayName("Medizinmann");
		lore.clear();
		lore.add("Heilen + Schützen.");
		lore.add("Die Unterstützungsklasse.");
		lore.add("Fähigkeiten:");
		for(Skill s: Skill.SKILLS)
		{
			if(SkillManager.isItemForClass(ClassType.MEDIZINMANN, s.getSkillType()))
				lore.add("- " + s.getFriendlyName());
		}
		meta.setLore(lore);
		medizinmann.setItemMeta(meta);
		ItemStack tiermeister = new ItemStack(Material.RAW_BEEF, 1);
		meta = tiermeister.getItemMeta();
		meta.setDisplayName("Tiermeister");
		lore.clear();
		lore.add("Aktuell noch in der Testphase!");
		lore.add("Der Tierfreund.");
		lore.add("Fähigkeiten:");
		for(Skill s: Skill.SKILLS)
		{
			if(SkillManager.isItemForClass(ClassType.TIERMEISTER, s.getSkillType()))
				lore.add("- " + s.getFriendlyName());
		}
		meta.setLore(lore);
		tiermeister.setItemMeta(meta);
		
		ItemStack sprengmeister = new ItemStack(Material.TNT, 1);
		meta = sprengmeister.getItemMeta();
		meta.setDisplayName("Sprengmeister");
		lore.clear();
		lore.add("Bumm, bumm, bumm!");
		lore.add("Jagt alles in die Luft.");
		lore.add("Fähigkeiten:");
		for(Skill s: Skill.SKILLS)
		{
			if(SkillManager.isItemForClass(ClassType.SPRENGMEISTER, s.getSkillType()))
				lore.add("- " + s.getFriendlyName());
		}
		meta.setLore(lore);
		sprengmeister.setItemMeta(meta);
		inventory.addItem(krieger, windmagier, bogenschütze, medizinmann, tiermeister, sprengmeister);
		p.getBukkitPlayer().openInventory(inventory);
	}

	public static void openNationSwitchMenu(NationPlayer p) {
		Inventory inventory = Bukkit.createInventory(null, 9, "Sicher?");
		ItemStack confirm = new ItemStack(Material.BEACON, 1);
		ItemMeta meta = confirm.getItemMeta();
		meta.setDisplayName("Bestätigen?");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Bist du sicher, dass");
		lore.add("du dein Volk zu");
		if (p.getNation().equalsIgnoreCase("Zwerge"))
			lore.add(ChatColor.RED + "Elfen");
		else
			lore.add(ChatColor.BLUE + "Zwerge");
		lore.add("ändern willst?");
		meta.setLore(lore);
		confirm.setItemMeta(meta);
		list.put(p, 2);
		inventory.setItem(4, confirm);
		p.getBukkitPlayer().openInventory(inventory);
	}

	public static void openSelectMenu(NationPlayer p) {
		list.put(p, 0);
		Inventory inventory = Bukkit.createInventory(null, 9, "Was möchtest du tun?");
		ItemStack klasse = new ItemStack(Material.SLIME_BALL, 1);
		ItemMeta meta = klasse.getItemMeta();
		meta.setDisplayName("Klasse ändern");
		ArrayList<String> lore = new ArrayList<String>();
		boolean cooldown = ClassCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString()) > 0;
		if (cooldown) {
			lore.add("Aktuell in der");
			lore.add("Cooldown!");
		} else {
			lore.add("Klicke hier um die");
			lore.add("Klasse zu ändern!");
		}
		meta.setLore(lore);
		klasse.setItemMeta(meta);
		ItemStack volk = new ItemStack(Material.SLIME_BALL, 1);
		meta = volk.getItemMeta();
		meta.setDisplayName("Volk ändern");
		lore.clear();
		cooldown = NationCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString()) != 0;
		if (cooldown) {
			lore.add("Aktuell in der");
			lore.add("Cooldown!");
		} else {
			lore.add("Klicke hier um dein");
			lore.add("Volk zu ändern!");
		}
		meta.setLore(lore);
		volk.setItemMeta(meta);
		inventory.setItem(0, klasse);
		inventory.setItem(8, volk);
		p.getBukkitPlayer().openInventory(inventory);
	}

	public static void close() {
		for (NationPlayer p : list.keySet()) {
			p.getBukkitPlayer().closeInventory();
		}
		list.clear();
		list = null;
	}

	public static void handleClick(NationPlayer p, int what, ItemStack item) {
		if (what == 0) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasDisplayName()) {
					String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
					if (name.equals("Klasse ändern")) {
						list.put(p, 1);
						p.getBukkitPlayer().closeInventory();
						openClassSelectMenu(p);
					}
					if (name.equalsIgnoreCase("Volk ändern")) {
						if(!Nations.TESTING)
						if ((NationCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString()) > 0)) {
							String time = TimeParser.hoursToDate(NationCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString()));
							p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Aktion zur Zeit nicht möglich! (Cooldown: " + time + ")");
							p.getBukkitPlayer().closeInventory();
							return;
						}
						p.getBukkitPlayer().closeInventory();
						openNationSwitchMenu(p);
					}
				}
			}
		}
		if (what == 1) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasDisplayName()) {
					String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
					ClassType type = null;
					try {
						type = ClassType.valueOf(name.toUpperCase());
					} catch (Throwable e) {
						;
					}
					if (type != null) {
						SkillManager.selectClass(p, type);
						p.getBukkitPlayer().closeInventory();
					}
				}
			}
		}
		if (what == 2) {
			if (item.getItemMeta().hasDisplayName()) {
				String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
				if (name.equalsIgnoreCase("Bestätigen?")) {
					String nation = p.getNation();
					boolean cont = false;
					if (ChatColor.stripColor(nation).equalsIgnoreCase("Elfen") && !Balancer.dwarfsOverfilled()) {
						Nations.broadcastMessageToNation("Elfen", ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getBukkitPlayer().getName() + ChatColor.GOLD + " hat dein Volk " + ChatColor.RED
								+ "verlassen!");
						p.setNation("Zwerge");
						Nations.setDwarfPlayerCount(Nations.getDwarfPlayerCount() + 1);
						Nations.setElfenPlayerCount(Nations.getElfenPayerCount() - 1);
						cont = true;
					} else if (ChatColor.stripColor(nation).equalsIgnoreCase("Zwerge") && !Balancer.elfenOverfilled()) {
						Nations.broadcastMessageToNation("Zwerge", ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getBukkitPlayer().getName() + ChatColor.GOLD + " hat dein Volk " + ChatColor.RED
								+ "verlassen!");
						p.setNation("Elfen");
						Nations.setElfenPlayerCount(Nations.getElfenPayerCount() + 1);
						Nations.setDwarfPlayerCount(Nations.getDwarfPlayerCount() - 1);
						cont = true;
					} else {
						p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Aktion zur Zeit nicht möglich! (Volk überfüllt)");
						p.getBukkitPlayer().closeInventory();
						return;
					}
					if (cont) {
						Nations.broadcastMessageToNation(p, ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getBukkitPlayer().getName() + ChatColor.GOLD + " ist deinem Volk " + ChatColor.GREEN
								+ "beigetreten!");
						p.getBukkitPlayer().teleport(SpawnManager.getFirstNationSpawn(p));
						new NationCooldownTask(p.getBukkitPlayer().getUniqueId().toString(), 2592000);
					}
					Nations.updatePlayerListName(p);
					PlayerMarker.updatePlayer(p);
					p.getBukkitPlayer().closeInventory();
				}
			}
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerChangedWorldEvent e) {
		NationPlayer p = Nations.instanceOf(e.getPlayer());
		if (p != null)
			list.remove(p);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		NationPlayer p = Nations.instanceOf(e.getPlayer());
		if (p != null)
			list.remove(p);
	}

	@EventHandler
	public void onPlayerLeave(PlayerKickEvent e) {
		NationPlayer p = Nations.instanceOf(e.getPlayer());
		if (p != null)
			list.remove(p);
	}

	@EventHandler
	public void onPlayerClose(InventoryCloseEvent e) {
		if (e.getPlayer() instanceof Player) {
			Player pl = (Player) e.getPlayer();
			NationPlayer p = Nations.instanceOf(pl);
			if (p != null && list.containsKey(p)) {
				// int what = list.get(p);
				list.remove(p);
			}
		}
	}

	@EventHandler
	public void onPlayerClickInSwitch(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player pl = (Player) e.getWhoClicked();
			NationPlayer p = Nations.instanceOf(pl);
			if (p != null && list.containsKey(p)) {
				e.setCancelled(true);
				int what = list.get(p);
				if (e.getCurrentItem() != null) {
					handleClick(p, what, e.getCurrentItem());
				}
			}
		}
	}

}
