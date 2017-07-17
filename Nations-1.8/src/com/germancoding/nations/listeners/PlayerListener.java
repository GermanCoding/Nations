package com.germancoding.nations.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.germancoding.nations.ChatMode;
import com.germancoding.nations.ConfigManager;
import com.germancoding.nations.ForceField;
import com.germancoding.nations.IllegalItemException;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.SkillManager;
import com.germancoding.nations.SpawnManager;
import com.germancoding.nations.InventoryViewHandler;
import com.germancoding.nations.Util;
import com.germancoding.nations.skills.SkillType;
import com.germancoding.nations.tasks.DamageRemoverTask;
import com.germancoding.nations.tasks.PvpTask;
import com.germancoding.nations.tasks.VisibilityCooldownTask;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
		if (!Nations.hasNationWorld())
			return;
		final Player p = e.getPlayer();
		World w = Nations.getNationWorld();
		World current = p.getWorld();
		if (current.getName().equals(e.getFrom().getName()))
			return;
		if (current.equals(w) || current == w) {
			// InventoryManager.switchToNations(p);
			boolean isNew = false;
			if (ConfigManager.isNewPlayer(p))
				isNew = true;
			Nations.addPlayer(p, false);
			if (isNew)
				Nations.handleNewPlayer(Nations.instanceOf(p));
			else {
				final Location spawn = SpawnManager.getFirstNationSpawn(Nations.instanceOf(p));
				if (spawn != null) {
					Bukkit.getScheduler().runTaskLater(Nations.plugin, new Runnable() {

						@Override
						public void run() {
							p.teleport(spawn, TeleportCause.PLUGIN);
						}
					}, 5L);
				}
			}
		} else {
			if (e.getFrom() == w || e.getFrom().equals(w)) {
				// InventoryManager.switchToMain(p);
				NationPlayer np = Nations.instanceOf(p);
				Nations.removePlayer(np);
				for (ItemStack item : p.getInventory().getContents()) {
					try {
						if (SkillManager.isSkillItem(item)) {
							p.getInventory().remove(item);
						}
					} catch (IllegalItemException e1) {
						p.getInventory().remove(item);
					}
				}
				p.updateInventory();
			}
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (!Nations.hasNationWorld())
			return;
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			World w = Nations.getNationWorld();
			World current = p.getWorld();
			if (current.equals(w) || current == w) {
				NationPlayer np = Nations.instanceOf(p);
				if (np == null) {
					e.setCancelled(true);
					return;
				}
				if (!np.hasNation()) {
					e.setCancelled(true);
					return;
				}
				if (np.getBukkitPlayer().hasMetadata("unsterblichkeit")) {
					e.setCancelled(true);
					return;
				}
				if (np.getNation().equalsIgnoreCase("Elfen")) {
					if (p.isSneaking()) {
						Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
						while (iterator.hasNext()) {
							NationPlayer other = iterator.next();
							if (other.hasNation() && !other.getNation().equalsIgnoreCase("Elfen")) {
								other.getBukkitPlayer().showPlayer(p);
							}
						}
						new VisibilityCooldownTask(p, 10);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDoDamage(EntityDamageByEntityEvent e) {
		if (!Nations.hasNationWorld())
			return;
		if (e.isCancelled())
			return;
		Player p = null;
		if (e.getDamager() instanceof Player) {
			p = (Player) e.getDamager();
		} else if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();
			if (arrow.getShooter() instanceof Player) {
				p = (Player) arrow.getShooter();
				if (arrow.hasMetadata("pfeilhagel")) {
					e.setDamage(e.getDamage() * 4);
					if (p != null) {
						NationPlayer nShooter = Nations.instanceOf(p);
						if (nShooter != null) {
							NationItemStack item = Util.getItemStackByType(nShooter, SkillType.PFEILHAGEL);
							LevelManager.addExperience(nShooter, item, 1);
						}
					}
				}
			}
		}
		if (p != null) {
			World w = Nations.getNationWorld();
			World current = p.getWorld();
			if (current.equals(w) || current == w) {
				NationPlayer damagerNation = Nations.instanceOf(p);
				if (damagerNation == null) {
					e.setCancelled(true);
					return;
				}
				if (!damagerNation.hasNation()) {
					e.setCancelled(true);
					return;
				}
				if (p.hasMetadata("unsterblichkeit")) {
					e.setCancelled(true);
					return;
				}
				NationPlayer took = null;
				if (e.getEntity() instanceof Player) {
					Player take = (Player) e.getEntity();
					took = Nations.instanceOf(take);
				}
				if (took != null) {
					if (took.getNation().equals(damagerNation.getNation())) {
						e.setCancelled(true);
						return;
					}
					if (took.getBukkitPlayer().hasMetadata("unsterblichkeit")) {
						e.setCancelled(true);
						return;
					}
					if (damagerNation.getBukkitPlayer().hasMetadata("ABILITY_BLUTRAUSCH")) {
						NationItemStack item = Util.getItemStackByType(damagerNation, SkillType.BLUTRAUSCH);
						LevelManager.addExperience(damagerNation, item, 0.5);
					}
					new PvpTask(took.getBukkitPlayer(), 5);
				}
				new PvpTask(p, 5);
				if (damagerNation.getNation().equalsIgnoreCase("Elfen")) {
					Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
					while (iterator.hasNext()) {
						NationPlayer other = iterator.next();
						if (other.hasNation() && !other.getNation().equalsIgnoreCase("Elfen") && !other.getBukkitPlayer().canSee(p)) {
							if (Util.canSee(p, other.getBukkitPlayer()))
								p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + other.getBukkitPlayer().getName() + ChatColor.GOLD + " kann dich nun wieder " + ChatColor.RED + "sehen");
							other.getBukkitPlayer().showPlayer(p);
						}
					}
					new VisibilityCooldownTask(p, 10);
				} else {
					e.setDamage(e.getDamage() + 3d);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (!Nations.hasNationWorld())
			return;
		final Player p = e.getPlayer();
		World w = Nations.getNationWorld();
		World current = p.getWorld();
		if (current.equals(w) || current == w) {
			NationPlayer np = Nations.instanceOf(p);
			if (np == null)
				return;
			if (!np.hasNation() && !e.getTo().getBlock().getLocation().equals(e.getFrom().getBlock().getLocation())) {
				Nations.handleNewPlayer(np);
				return;
			}
			if (!np.hasNation())
				return;
			if (!np.hasClass() && !e.getTo().getBlock().getLocation().equals(e.getFrom().getBlock().getLocation())) {
				InventoryViewHandler.openClassSelectMenu(np);
			}
			if (!e.getTo().getBlock().getLocation().equals(e.getFrom().getBlock().getLocation())) { // Only update on full block change to save ressources
				for (final ForceField field : ForceField.FIELDS) {
					if (field.isNearField(e.getTo())) {
						if (np.hasNation() && field.getNation().equalsIgnoreCase(np.getNation())) {

							if (field.isInsideField(e.getFrom()) && !field.isInsideField(e.getTo())) {
								np.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + "Du " + ChatColor.RED + "verlässt" + ChatColor.GRAY + " den vom Kraftfeld geschützen Bereich.");
							} else if (field.isInsideField(e.getTo()) && !field.isInsideField(e.getFrom())) {
								np.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + "Du " + ChatColor.GREEN + "betrittst" + ChatColor.GRAY + " den vom Kraftfeld geschützen Bereich.");
							}

						}

						if (field.isCloseToInsideEdge(e.getTo())) {
							// System.out.println("Location is close to edge!");
							Nations.border.removeBorder(p);
						} else if (field.isInsideAndAwayFromEdges(e.getTo())) {
							Nations.border.optSendBorder(field, p);
						}
					}
					if (!field.canPlayerPassField(np, e.getTo())) {
						Nations.border.sendPulsingBorder(field, p);

						np.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Du wurdest von einem Kraftfeld zurückgeworfen!");
						Location min = field.getMin();
						Location max = field.getMax();
						double midX = (min.getX() + max.getX()) / 2;
						// double midY = (min.getY() + max.getY()) / 2;
						double midZ = (min.getZ() + max.getZ()) / 2;
						Location mid = new Location(current, midX, e.getTo().getWorld().getHighestBlockYAt(e.getTo()), midZ);
						Vector direction = np.getBukkitPlayer().getLocation().toVector().subtract(mid.toVector());
						Vector velocity = direction.normalize().multiply(3);
						np.getBukkitPlayer().setVelocity(velocity);
						// np.getBukkitPlayer().teleport(e.getFrom().setDirection(velocity));
						/*
						 * if(field.isInsideField(e.getFrom())) // Our player is already IN the field, get him out!
						 * {
						 * // np.getBukkitPlayer().setVelocity(new Vector(0, 5, 0));
						 * Location min = field.getMin();
						 * Location max = field.getMax();
						 * // Solution 1: Teleport the player to the closest edge of the field
						 * /*
						 * Location closest = null;
						 * Location currentLoc = e.getTo();
						 * if(currentLoc.distanceSquared(min) <= currentLoc.distanceSquared(max))
						 * {
						 * closest = min;
						 * closest.setX(min.getX() - 1);
						 * closest.setZ(min.getZ() - 1);
						 * }
						 * else
						 * {
						 * closest = max;
						 * closest.setX(min.getX() + 1);
						 * closest.setZ(min.getZ() + 1);
						 * }
						 * closest.setY(current.getHighestBlockYAt(closest));
						 * np.getBukkitPlayer().teleport(closest);
						 * // Solution 2: Fix player direction and throw him back
						 * double midX = (min.getX() + max.getX()) / 2;
						 * // double midY = (min.getY() + max.getY()) / 2;
						 * double midZ = (min.getZ() + max.getZ()) / 2;
						 * Location mid = new Location(current, midX, e.getTo().getY(), midZ);
						 * Vector v = np.getBukkitPlayer().getLocation().toVector().subtract(mid.toVector());
						 * np.getBukkitPlayer().setVelocity(v.normalize().multiply(3));
						 * }
						 * else // Throw the player a bit away, looks better then just re-teleport to original location
						 * {
						 * Vector v = np.getBukkitPlayer().getLocation().getDirection();
						 * v = v.setX(v.getX() * - 1);
						 * v = v.setZ(v.getZ() * -1);
						 * np.getBukkitPlayer().setVelocity(v.multiply(2));
						 * Location to = e.getFrom().setDirection(e.getTo().getDirection());
						 * e.setTo(to);
						 * }
						 */
					}
				}
			}
			String nation = np.getNation();
			if (nation.equalsIgnoreCase("Elfen")) {
				if (p.isSneaking()) {
					boolean cooldown = VisibilityCooldownTask.isInCooldown(p);
					Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
					while (iterator.hasNext()) {
						NationPlayer other = iterator.next();
						if (other.hasNation() && !other.getNation().equalsIgnoreCase("Elfen")) {
							if (!cooldown) {
								if (p.getLocation().distance(other.getBukkitPlayer().getLocation()) > 15) {
									if (other.getBukkitPlayer().canSee(p) && Util.canSee(p, other.getBukkitPlayer())) {
										p.sendMessage(ChatColor.GOLD + "[Nations] Du hast dich vor " + ChatColor.GRAY + other.getBukkitPlayer().getName() + ChatColor.GREEN + " versteckt");
									}
									other.getBukkitPlayer().hidePlayer(p);
								} else {
									if (!other.getBukkitPlayer().canSee(p) && Util.canSee(p, other.getBukkitPlayer())) {
										p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + other.getBukkitPlayer().getName() + ChatColor.GOLD + " kann dich nun wieder " + ChatColor.RED + "sehen");
									}
									other.getBukkitPlayer().showPlayer(p);
								}
							}
						}
					}
				} else {
					Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
					while (iterator.hasNext()) {
						NationPlayer other = iterator.next();
						if (other.hasNation() && !other.getNation().equalsIgnoreCase("Elfen")) {
							if (!other.getBukkitPlayer().canSee(p) && Util.canSee(p, other.getBukkitPlayer())) {
								p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + other.getBukkitPlayer().getName() + ChatColor.GOLD + " kann dich nun wieder " + ChatColor.RED + "sehen");
							}
							other.getBukkitPlayer().showPlayer(p);
						}
					}
				}
			} else {
				Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
				while (iterator.hasNext()) {
					NationPlayer other = iterator.next();
					if (other.hasNation() && !other.getNation().equalsIgnoreCase("Zwerge")) {
						if (!p.canSee(other.getBukkitPlayer()) && !VisibilityCooldownTask.isInCooldown(other.getBukkitPlayer())) {
							if (p.getLocation().distance(other.getBukkitPlayer().getLocation()) < 15) {
								if (Util.canSee(p, other.getBukkitPlayer()))
									other.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getName() + ChatColor.GOLD + " kann dich nun wieder " + ChatColor.RED + "sehen");
								p.showPlayer(other.getBukkitPlayer());
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		NationPlayer np = Nations.instanceOf(e.getPlayer());
		if (np == null || !np.hasNation())
			return;
		e.setRespawnLocation(SpawnManager.getSaveSpawnLocation(np));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		World w = Nations.getNationWorld();
		World current = p.getWorld();
		if (current.equals(w) || current == w) {
			NationPlayer np = Nations.instanceOf(p);
			if (np == null || !np.hasNation())
				return;
			List<ItemStack> drops = new ArrayList<ItemStack>();
			drops.addAll(e.getDrops());
			for (ItemStack i : drops) {
				try {
					if (SkillManager.isSkillItem(i)) {
						e.getDrops().remove(i);
					}
				} catch (IllegalItemException e1) {
					;
				}
			}
			Player killer = p.getKiller();
			if (killer == null && np.getLastdamager() != null)
				killer = np.getLastdamager().getBukkitPlayer();
			DamageRemoverTask.cancel(np);
			np.setLastdamager(null);
			ChatColor color = null;
			if (np.getNation().equalsIgnoreCase("Zwerge"))
				color = ChatColor.BLUE;
			else
				color = ChatColor.RED;
			String nation = np.getNation();
			// String cause = "unbekannt";
			// if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null)
			// cause = p.getLastDamageCause().getCause().toString();
			if (nation.equalsIgnoreCase("Zwerge"))
				nation = "Zwergen";
			if (killer == null) {
				// e.setDeathMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getName() + ChatColor.GOLD + " von den " + color + nation + ChatColor.GOLD + " ist an " + ChatColor.RED + cause
				// + " gestorben");
				e.setDeathMessage(e.getDeathMessage().replace(p.getName(), ChatColor.GRAY + p.getName() + ChatColor.WHITE + " von den " + color + nation + ChatColor.WHITE));
				np.setDeath(np.getDeath() + 1);
			} else {
				e.setDeathMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + p.getName() + ChatColor.GOLD + " von den " + color + nation + ChatColor.GOLD + " wurde von " + ChatColor.RED + killer.getName() + ChatColor.GOLD + " getötet");
				Nations.instanceOf(killer).setKills(Nations.instanceOf(killer).getKills() + 1);
				np.setDeath(np.getDeath() + 1);
				if (np.getNation().equalsIgnoreCase("Zwerge")) {
					Nations.setPointsOfElfen(Nations.getPointsOfElfen() + 1);
				} else {
					Nations.setPointsOfDwarfs(Nations.getPointsOfDwarfs() + 1);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		World w = Nations.getNationWorld();
		World current = p.getWorld();
		if (current.equals(w) || current == w) {
			Nations.addPlayer(p, false);
			NationPlayer np = Nations.instanceOf(p);
			if (np == null)
				return;
			if (!np.hasNation())
				return;
			// BarManager.showText(p, ChatColor.RED + "Test", 10);
			// Handle something
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Nations.removePlayer(Nations.instanceOf(e.getPlayer()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		World w = Nations.getNationWorld();
		World current = p.getWorld();
		if (current.equals(w) || current == w) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = e.getClickedBlock();
				Protection pr = LWC.getInstance().findProtection(b);
				if (pr != null) {
					pr.remove();
					e.getPlayer().sendMessage("Sicherung gelöscht.");
				}
			}

			NationPlayer np = Nations.instanceOf(p);
			if (np == null)
				return;
			if (!np.hasNation())
				return;
			if (e.getItem() == null || e.getItem().getType() == Material.AIR)
				return;
			e.setCancelled(SkillManager.handleItem(np, e.getItem()));
			if (e.isCancelled())
				e.setUseItemInHand(Result.DENY);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		try {
			if (SkillManager.isSkillItem(e.getItemDrop().getItemStack())) {
				e.getPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Du kannst keine Fähigkeiten droppen!");
				e.setCancelled(true);
			}
		} catch (IllegalItemException e1) {
			;
		}
	}

	@EventHandler
	public void onItemEnchant(EnchantItemEvent e) {
		try {
			if (SkillManager.isSkillItem(e.getItem())) {
				e.getEnchanter().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Du kannst keine Fähigkeiten enchanten!");
				e.setCancelled(true);
			}
		} catch (IllegalItemException e1) {
			;
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		if (PvpTask.isInCooldown(e.getPlayer())) {
			e.setLeaveMessage(ChatColor.GOLD + "[Nations] " + ChatColor.GRAY + e.getPlayer().getName() + " hat das Spiel " + ChatColor.RED + " im Kampf verlassen");
			for (ItemStack item : e.getPlayer().getInventory().getContents()) {
				if (item != null && item.getType() != Material.AIR) {
					try {
						if (SkillManager.isSkillItem(item)) {
							e.getPlayer().getInventory().remove(item);
							continue;
						}
					} catch (IllegalItemException e1) {
						;
					}
					e.getPlayer().getLocation().getWorld().dropItem(e.getPlayer().getLocation(), item);
				}
			}
			for (ItemStack item : e.getPlayer().getInventory().getArmorContents()) {
				if (item != null && item.getType() != Material.AIR)
					e.getPlayer().getLocation().getWorld().dropItem(e.getPlayer().getLocation(), item);
			}
			e.getPlayer().getInventory().clear();
			try {
				e.getPlayer().getInventory().setArmorContents(null);
			} catch (Throwable e1) {
				;
			}
		}
		Nations.removePlayer(Nations.instanceOf(e.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void asyncChat(AsyncPlayerChatEvent e) {
		onPlayerChat(e.getPlayer(), e.getPlayer().getDisplayName(), e.getMessage(), e);
	}

	public static void onPlayerChat(Player p, String prefixAndName, String msg, AsyncPlayerChatEvent event) {
		World w = Nations.getNationWorld();
		World current = p.getWorld();
		if (current.equals(w) || current == w) {
			NationPlayer np = Nations.instanceOf(p);
			if (np == null || !np.hasNation())
				return;
			ChatColor color = null;
			if (np.getNation().equalsIgnoreCase("Zwerge"))
				color = ChatColor.BLUE;
			else
				color = ChatColor.RED;
			String nation = np.getNation();
			if (nation.equalsIgnoreCase("Zwerge"))
				nation = "Zwerg";
			else
				nation = "Elf";
			String nationPrefix = color + "[" + nation + "]";
			String classPrefix = null;
			if (np.hasClass())
				classPrefix = ChatColor.DARK_GREEN + "[" + Util.normalizeString(np.getClasS().getClassType().name()) + "]";
			if (np.getChatMode().equals(ChatMode.GLOBAL)) {
				String fullMessage = null;
				if (classPrefix != null)
					fullMessage = nationPrefix + ChatColor.RESET + " " + classPrefix + " " + ChatColor.RESET + "<" + prefixAndName + ChatColor.RESET + "> " + ChatColor.RESET + msg;
				else
					fullMessage = nationPrefix + ChatColor.RESET + " " + ChatColor.RESET + prefixAndName + ChatColor.RESET + "" + ChatColor.BOLD + "" + ChatColor.GREEN + ": " + ChatColor.RESET + msg;
				Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
				ArrayList<Player> otherPlayers = new ArrayList<>();
				while (iterator.hasNext()) {
					NationPlayer thisPlayer = iterator.next();
					otherPlayers.add(thisPlayer.getBukkitPlayer());
					if (thisPlayer.canRecChat())
						thisPlayer.getBukkitPlayer().sendMessage(fullMessage);
				}
				event.getRecipients().removeAll(otherPlayers);
			} else if (np.getChatMode().equals(ChatMode.NATION)) {
				String fullMessage = null;
				if (classPrefix != null)
					fullMessage = ChatColor.GOLD + "[Nation] " + nationPrefix + ChatColor.RESET + " " + classPrefix + " " + ChatColor.RESET + "<" + prefixAndName + ChatColor.RESET + "> " + ChatColor.GOLD + msg;
				else
					fullMessage = ChatColor.GOLD + "[Nation] " + nationPrefix + ChatColor.RESET + " " + ChatColor.RESET + "<" + prefixAndName + ChatColor.RESET + "> " + ChatColor.GOLD + msg;
				Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
				event.setCancelled(true);
				while (iterator.hasNext()) {
					NationPlayer thisPlayer = iterator.next();
					if (thisPlayer.hasNation() && thisPlayer.getNation().equalsIgnoreCase(np.getNation())) {
						if (thisPlayer.canRecChat())
							thisPlayer.getBukkitPlayer().sendMessage(fullMessage);
					}
				}
				Nations.plugin.getLogger().info(ChatColor.stripColor(fullMessage));
			}
		} else {
			/*
			 * String fullmsg = ChatColor.GOLD + "[Global] " + prefixAndName + ChatColor.RESET + ChatColor.BOLD + ChatColor.GREEN + ": " + ChatColor.RESET + msg;
			 * Iterator<NationPlayer> iterator = Nations.getIteratorOfPlayers();
			 * while (iterator.hasNext()) {
			 * NationPlayer thisPlayer = iterator.next();
			 * if (thisPlayer.canRecChat())
			 * thisPlayer.getBukkitPlayer().sendMessage(fullmsg);
			 * }
			 */
		}
	}

}
