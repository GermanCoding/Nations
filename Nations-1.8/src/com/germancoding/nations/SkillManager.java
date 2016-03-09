package com.germancoding.nations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.germancoding.nations.classes.ClassType;
import com.germancoding.nations.classes.NationClass;
import com.germancoding.nations.skills.Skill;
import com.germancoding.nations.skills.SkillType;
import com.germancoding.nations.tasks.ClassCooldownTask;
import com.germancoding.nations.tasks.ItemCooldownTask;
import com.germancoding.nations.unsave.PacketUtils;

public class SkillManager {

	static int easterEggClicked = 0;
	static int pid = -1;

	/**
	 * Called whenever the player clicks with an item in his hand. (Right- and leftclick).
	 * 
	 * @param p
	 *            The (Nation-)player who clicked.
	 * @param item
	 *            The item in his hand.
	 * @return Whether the event handler should cancel the click event.
	 */
	public static boolean handleItem(NationPlayer p, ItemStack item) {
		try {
			if (!isSkillItem(item))
				return false;
		} catch (IllegalItemException e) {
			p.getBukkitPlayer().getInventory().remove(item);
			p.getBukkitPlayer().updateInventory();
			return true;
		}
		NationItemStack stack = NationItemStack.getByID(getIDOfItem(item));
		if (!p.hasClass())
			return true;
		ClassType clasS = p.getClasS().getClassType();
		p.getBukkitPlayer().closeInventory();
		if (isItemForClass(clasS, stack.getType())) {
			if ((isInCooldown(stack)) && (!Nations.TESTING)) {
				int n = ItemCooldownTask.getRemainTime(stack.getID());
				if (easterEggClicked < 20) {
					p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Item in der Cooldown! " + TimeParser.secondsToDate(n));
					easterEggClicked++;
					if (pid != -1)
						pid = Bukkit.getScheduler().scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

							@Override
							public void run() {
								easterEggClicked = 0;
								pid = -1;
							}
						}, 20 * 8);
				} else {
					p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Willst du mich verarschen?");
					easterEggClicked = 0;
				}
				return true;
			} else {
				for (ForceField f : ForceField.FIELDS) {
					if (f.isInsideField(p.getBukkitPlayer().getLocation())) {
						p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Du kannst keine Items in einem Kraftfeld aktivieren!");
						return true;
					}
				}
			}
			useItem(p, stack);
			return true;
		}
		p.getBukkitPlayer().getInventory().remove(item);
		p.getBukkitPlayer().updateInventory();
		return true;
	}

	/**
	 * Launches the ability this item/skill has. Warning: No cooldown checks are performed here! A new cooldown will be started though.
	 * 
	 * @param p
	 *            The player who owns this item.
	 * @param item
	 *            The skill-item which will be activated.
	 */
	public static void useItem(NationPlayer p, NationItemStack item) {
		SkillType type = item.getType();
		ChatColor color = null;
		if (p.getNation().equals("Elfen"))
			color = ChatColor.RED;
		else
			color = ChatColor.BLUE;
		Iterator<NationPlayer> i = Nations.getIteratorOfPlayers();
		for (Skill c : Skill.SKILLS) {
			if (c.getSkillType() == type) {
				while (i.hasNext()) {
					String message = null;
					NationPlayer n = i.next();
					if (n.hasNation()) {
						if (Util.canSee(p.getBukkitPlayer(), n.getBukkitPlayer())) {
							if (n.getNation().equals(p.getNation())) {
								message = ChatColor.GOLD + "[Nations] " + color + p.getBukkitPlayer().getName() + ChatColor.GREEN + " (Verbündeter) aktivierte die Fähigkeit " + ChatColor.GRAY + c.getFriendlyName() + ChatColor.GREEN + "!";
								n.getBukkitPlayer().sendMessage(message);
							} else {
								message = ChatColor.GOLD + "[Nations] " + color + p.getBukkitPlayer().getName() + ChatColor.RED + " (Feind) aktivierte die Fähigkeit " + ChatColor.GRAY + c.getFriendlyName() + ChatColor.RED + "!";
								n.getBukkitPlayer().sendMessage(message);
							}
						}
					}
				}
				new ItemCooldownTask(item.getID(), item.getCooldown());
				int useTime = c.activate(p);
				PacketUtils.setCountdownMessage(p.getBukkitPlayer(), "Restzeit", useTime);
				break;
			}
		}
		// p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] Du hast die Fähigkeit '" + convertEnumToNormalString(type) + "' aktiviert!");
		p.getBukkitPlayer().updateInventory();
	}

	/**
	 * Checks whether this ItemStack is an skill item.
	 * 
	 * @param item
	 *            The item to check.
	 * @return Whether this is an NationItemStack or not.
	 * @throws IllegalItemException
	 *             If the lore of the item is correct, but no instance matches this id. Possible when the item was just deleted or when the item was not correctly loaded.
	 */
	public static boolean isSkillItem(ItemStack item) throws IllegalItemException {
		if (item == null)
			return false;
		if (!item.hasItemMeta())
			return false;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore())
			return false;
		List<String> lore = meta.getLore();
		for (String s : lore) {
			if (s.startsWith("ID:")) {
				int id = getIDOfItem(item);
				if (NationItemStack.getByID(id) == null)
					throw new IllegalItemException("No instance found for item " + id);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the id of this ItemStack. Can be used to retrieve the NationItemStack instance.
	 * 
	 * @param item
	 *            The item which represents the NationItemStack.
	 * @return The id or zero, if no id was found.
	 */
	public static int getIDOfItem(ItemStack item) {
		if (item == null)
			return 0;
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		for (String s : lore) {
			if (s.startsWith("ID:")) {
				return Integer.parseInt(s.replace("ID: ", ""));
			}
		}
		return 0;
	}

	/**
	 * This check is a littlebit ugly. It checks whether the given ClassType is compatible with the given SkillType.
	 * 
	 * @param clasS
	 *            The ClassType to check.
	 * @param skill
	 *            The SkillType to check.
	 * @return Whether class and skill are compatible or not.
	 */
	public static boolean isItemForClass(ClassType clasS, SkillType skill) {
		if (clasS == ClassType.KRIEGER) {
			if (skill == SkillType.BLUTRAUSCH)
				return true;
			if (skill == SkillType.INFLAMMATOR)
				return true;
		}
		if (clasS == ClassType.WINDMAGIER) {
			if (skill == SkillType.TORNADO)
				return true;
			if (skill == SkillType.AUSBRUCH_DER_WINDE)
				return true;
			if (skill == SkillType.FLY)
				return true;
		}
		if (clasS == ClassType.BOGENSCHÜTZE) {
			if (skill == SkillType.PFEILHAGEL)
				return true;
			if (skill == SkillType.PREDATOR)
				return true;
		}
		if (clasS == ClassType.MEDIZINMANN) {
			if (skill == SkillType.UNSTERBLICHKEIT)
				return true;
			if (skill == SkillType.SCHUTZ)
				return true;
		}
		if (clasS == ClassType.TIERMEISTER) {
			if (skill == SkillType.GOLEMBESCHWÖRUNG)
				return true;
			if(skill == SkillType.NACHTDERUNTOTEN)
				return true;
		}
		if (clasS == ClassType.SPRENGMEISTER) {
			if (skill == SkillType.EXPLOSION)
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the given NationItemStack has a remaining cooldown (Non-Zero cooldown).
	 * 
	 * @param stack
	 *            The NationItemStack to check.
	 * @return Whether this item is in cooldown or not.
	 */
	public static boolean isInCooldown(NationItemStack stack) {
		return ItemCooldownTask.getRemainTime(stack.getID()) != 0;
	}

	public static void registerNewSkill(NationPlayer p, SkillType type) {
		ArrayList<String> text = new ArrayList<String>();
		for (Skill c : Skill.SKILLS) {
			if (c.getSkillType() == type) {
				for (String s : c.getDescription()) {
					text.add(s);
				}
				ItemStack item = NationItemStack.createNewItem(p.getBukkitPlayer().getUniqueId().toString(), c.getFriendlyName(), text, c.getItemType(), type);
				p.getBukkitPlayer().getInventory().addItem(item);
				p.getBukkitPlayer().updateInventory();
				break;
			}
		}
	}

	public static void unregisterSkill(NationPlayer p, SkillType type) {
		for (NationItemStack nItem : NationItemStack.getItemsOf(p.getBukkitPlayer().getUniqueId().toString())) {
			if (nItem.getType() == type)
				nItem.unregister();
		}
	}

	public static void selectClass(NationPlayer p, ClassType newClass) {
		if ((ClassCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString()) != 0) && (!Nations.TESTING)) {
			int n = ClassCooldownTask.getRemainTime(p.getBukkitPlayer().getUniqueId().toString());
			p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Klasse wechseln ist erst in " + TimeParser.secondsToDate(n) + " wieder möglich!");
			return;
		}
		if (p.hasClass() && p.getClasS().getClassType() == newClass) {
			p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Du bist bereits Mitglied dieser Klasse!");
			return;
		}
		new ClassCooldownTask(p.getBukkitPlayer().getUniqueId().toString(), 86400);
		if (p.hasClass()) {
			ClassType t = p.getClasS().getClassType();
			for (SkillType type : SkillType.values()) {
				if (isItemForClass(t, type)) {
					unregisterSkill(p, type);
				}
			}
		}
		for (SkillType type : SkillType.values()) {
			if (isItemForClass(newClass, type)) {
				registerNewSkill(p, type);
			}
		}
		p.setClasS(NationClass.fromClassType(newClass));
		KitManager.handleKit(p, false);
		p.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] Du bist jetzt ein " + ChatColor.GRAY + Util.convertEnumToNormalString(newClass));
	}
}
