package com.germancoding.nations.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftSnowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.adamki11s.pathing.Tile;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;

public class GolembeschwörungSkill extends Skill implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
			return;
		if (map.containsKey(e.getPlayer().getName())) {
			ArrayList<Snowman> newList = new ArrayList<Snowman>();
			ArrayList<Snowman> list = map.get(e.getPlayer().getName());
			newList.addAll(list);
			for (Snowman s : list) {
				if (s == null || s.isDead()) {
					newList.remove(s);
				}
			}
			map.put(e.getPlayer().getName(), newList);
			Player enemyPlayer = null;
			Iterator<NationPlayer> i = Nations.getIteratorOfPlayers();
			while (i.hasNext()) {
				NationPlayer np = i.next();
				if ((np.getNation().equals(Nations.instanceOf(e.getPlayer()).getNation())) || (!e.getPlayer().hasLineOfSight(np.getBukkitPlayer())))
					continue;
				enemyPlayer = np.getBukkitPlayer();
				System.out.println(np.getBukkitPlayer().getName());
				break;
			}
			LivingEntity closest = null;
			List<Entity> mobs = e.getPlayer().getNearbyEntities(10d, 10d, 10d);
			for (Entity mob : mobs) {
				if (e.getPlayer().hasLineOfSight(mob)) {
					if (mob instanceof Monster) {
						if (closest != null) {
							if (mob.getLocation().distanceSquared(e.getPlayer().getLocation()) < closest.getLocation().distanceSquared(e.getPlayer().getLocation()))
								closest = (LivingEntity) mob;
						} else {
							closest = (LivingEntity) mob;
						}
					}
				}
			}
			LivingEntity newEnemy = null;
			if (enemyPlayer != null)
				newEnemy = enemyPlayer;
			else
				newEnemy = closest;
			for (Snowman s : newList) {
				if (s.getTarget() == null || s.getTarget().isDead() || !s.getTarget().isValid())
				{
					if (s.getTarget() instanceof Player)
					{
						Player target = (Player) s.getTarget();
						if (map.containsKey(target.getName()) && newEnemy != null)
							s.setTarget(newEnemy);
					}
					else
						s.setTarget(newEnemy);
				}
				/*
				 * if (s.getTarget() == null && !pidMap.containsKey(s)) { //
				 * Walk to our player try { // create our pathfinder // A
				 * littlebit ugly, we have to use the block UNDER the //
				 * entities to avoid InvalidPathExceptions Location start =
				 * findSavePosition(s.getLocation()).getLocation().clone();
				 * Pathfinder path = new Pathfinder(start,
				 * findSavePosition(e.getPlayer().getLocation()).getLocation(),
				 * 100); // get the list of nodes to walk to as a Tile object
				 * ArrayList<Tile> route = path.iterate(); // get the result of
				 * the path trace PathingResult result =
				 * path.getPathingResult(); switch (result) { case SUCCESS: //
				 * We have a way, let's walk! // Same here: Avoid
				 * InvalidPathExceptions by using // the block under the entity
				 * walk(s, route, start.clone()); break; case NO_PATH:
				 * System.out.println("No Path :("); continue; } } catch
				 * (InvalidPathException e1) { // InvalidPathException will be
				 * thrown if start or end // block is air if
				 * (e1.isStartNotSolid()) { System.out.println("Invalid Start");
				 * continue; } if (e1.isEndNotSolid()) {
				 * System.out.println("Invalid End"); continue; } } }
				 */
			}

		}
	}

	@SuppressWarnings("unused")
	private static Block findSavePosition(Location base) {
		Block toFind = base.getBlock();
		if (toFind.getType() == Material.AIR) {
			return findSavePositionBelow(toFind);
		} else {
			return findSavePositionUp(toFind);
		}
	}

	@SuppressWarnings("deprecation")
	private static Block findSavePositionBelow(Block b) {
		while (b.getType() == Material.AIR) {
			b = b.getRelative(BlockFace.DOWN);
		}
		if (canBlockBeWalkedThrough(b.getTypeId()))
			b = b.getRelative(BlockFace.DOWN);
		return b;
	}

	@SuppressWarnings("deprecation")
	private static Block findSavePositionUp(Block b) {
		while (b.getType() != Material.AIR) {
			b = b.getRelative(BlockFace.UP);
		}
		b = b.getRelative(BlockFace.DOWN);
		if (canBlockBeWalkedThrough(b.getTypeId()))
			b = b.getRelative(BlockFace.DOWN);
		return b;
	}

	private static boolean canBlockBeWalkedThrough(int id) {
		return (id == 0 || id == 6 || id == 50 || id == 63 || id == 30 || id == 31 || id == 32 || id == 37 || id == 38 || id == 39 || id == 40 || id == 55 || id == 66 || id == 75 || id == 76 || id == 78);
	}

	private static HashMap<Integer, Integer> pidMap = new HashMap<>();

	private static void startWalking(final Snowman snowman, final Player p) {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (snowman.isDead() || !snowman.isValid()) {
					this.cancel();
					return;
				}
				if (snowman.getTarget() == null)
				{
					walk2(snowman, p);
				}
				/*
				 * if (!pidMap.containsKey(snowman)) {
				 * if (snowman.getTarget() == null) {
				 * // Walk to our player
				 * try {
				 * // create our pathfinder
				 * // A littlebit ugly, we have to use the block UNDER
				 * // the
				 * // entities to avoid InvalidPathExceptions
				 * final Location start = findSavePosition(snowman.getLocation()).getLocation().clone();
				 * Pathfinder path = new Pathfinder(start, findSavePosition(p.getLocation()).getLocation(), 100);
				 * // get the list of nodes to walk to as a Tile object
				 * final ArrayList<Tile> route = path.iterate();
				 * // get the result of the path trace
				 * PathingResult result = path.getPathingResult();
				 * switch (result) {
				 * case SUCCESS:
				 * // We have a way, let's walk!
				 * // Same here: Avoid InvalidPathExceptions by
				 * // using
				 * // the block under the entity
				 * new BukkitRunnable() {
				 * @Override
				 * public void run() {
				 * System.out.println("Executing new path for snowman " + snowman.getEntityId());
				 * walk(snowman, route, start.clone());
				 * }
				 * }.runTask(Nations.plugin);
				 * break;
				 * case NO_PATH:
				 * System.out.println("No Path :(");
				 * break;
				 * }
				 * } catch (InvalidPathException e1) {
				 * // InvalidPathException will be thrown if start or
				 * // end
				 * // block is air
				 * if (e1.isStartNotSolid()) {
				 * System.out.println("Invalid Start");
				 * }
				 * if (e1.isEndNotSolid()) {
				 * System.out.println("Invalid End");
				 * }
				 * }
				 * }
				 * }
				 */
			}
		}.runTaskTimerAsynchronously(Nations.plugin, 20 * 2, 20 * 2); // Run async,
		// performance
		// heavy
		// function
		// (path finding
		// algorithms are
		// not the
		// fastest :) )
	}

	public static void walk2(final Snowman snowman, final Player p) {
		new BukkitRunnable() {

			@Override
			public void run() {
				// if (!pidMap.containsKey(snowman.getEntityId()))
				// pidMap.put(snowman.getEntityId(), Integer.valueOf(this.getTaskId()));
				// ((CraftSnowman) snowman).getHandle().setGoalTarget(((CraftPlayer) p).getHandle());
				((CraftSnowman) snowman).getHandle().getNavigation().a(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 3.5); // Add a new path to the navigator
				// try {
				// ((CraftSnowman) snowman).getHandle().getNavigation().getClass().getMethod("c", null).invoke(((CraftSnowman) snowman).getHandle().getNavigation(), null);
				// } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// e.printStackTrace();
				// }
				// ((CraftSnowman) snowman).getHandle().getNavigation().c(); // Execute path
				// snowman.setTarget(p);
			}
		}.runTask(Nations.plugin);
	}

	public static void walk(final Snowman snowman, final ArrayList<Tile> route, final Location start) {
		final Iterator<Tile> iterator = route.iterator();
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!pidMap.containsKey(snowman.getEntityId()))
					pidMap.put(snowman.getEntityId(), Integer.valueOf(this.getTaskId()));
				if (iterator.hasNext()) {
					if (snowman.isDead() || !snowman.isValid()) {
						pidMap.remove(snowman.getEntityId());
						this.cancel();
						return;
					}
					Tile tile = iterator.next();
					int x = tile.getLocation(start).getBlockX();
					int y = tile.getLocation(start).getBlockY();
					int z = tile.getLocation(start).getBlockZ();
					// Location dest = new Location(snowman.getWorld(), x, y, z);
					if (snowman.getLocation().getBlockY() >= y + 0.5) {
						((CraftSnowman) snowman).getHandle().getControllerMove().a(x, y, z, 2);
					} else {
						((CraftSnowman) snowman).getHandle().getControllerJump().a();
						((CraftSnowman) snowman).getHandle().getControllerMove().a(x, y, z, 2);
					}
					// System.out.println("Moving " + snowman.getEntityId() +
					// " to next tile. Distance (fast math, squared) left: " +
					// snowman.getLocation().distanceSquared(dest));
				}
				if (!iterator.hasNext()) {
					this.cancel();
					pidMap.remove(snowman.getEntityId());
				}
			}
		}.runTaskTimer(Nations.plugin, 10L, 10L);
	}

	@EventHandler
	public void noTrail2(org.bukkit.event.entity.EntityChangeBlockEvent e)
	{
		if (e.getEntity().hasMetadata("golembeschwoerung")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void noShooting(ProjectileLaunchEvent e)
	{
		if (e.getEntity().getShooter() != null)
		{
			if (e.getEntity().getShooter() instanceof Snowman)
			{
				Snowman shooter = (Snowman) e.getEntity().getShooter();
				if (shooter.hasMetadata("golembeschwoerung"))
				{
					if (shooter.getTarget() != null && shooter.getTarget() instanceof Player)
					{
						Player target = (Player) shooter.getTarget();
						if (map.containsKey(target.getName()))
							e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void noTrail(EntityBlockFormEvent e) {
		if (e.getEntity().hasMetadata("golembeschwoerung")) {
			e.setCancelled(true);
			e.getNewState().setType(Material.AIR);
			final Block toChange = e.getBlock();
			Bukkit.getScheduler().scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

				@Override
				public void run() {
					toChange.setType(Material.AIR);
					toChange.getState().setType(Material.AIR);
					toChange.getState().update(true);
				}
			}, 60L);
		}
	}

	@EventHandler
	public void onSnowmanDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Snowman) {
			Snowman s = (Snowman) e.getEntity();
			if (s.hasMetadata("golembeschwoerung")) {
				if (e.getCause() == DamageCause.DROWNING || e.getCause() == DamageCause.CONTACT || e.getCause() == DamageCause.MELTING)
					e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onSnowmanDamage(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Snowball) {
			Snowball s = (Snowball) e.getDamager();
			if (s.getShooter() instanceof Snowman) {
				Snowman man = (Snowman) s.getShooter();
				if (man.hasMetadata("golembeschwoerung")) {
					e.setDamage(10);
				}
			}
		}
	}

	private static HashMap<String, ArrayList<Snowman>> map = new HashMap<String, ArrayList<Snowman>>();

	public int activate(final NationPlayer np) {
		np.getBukkitPlayer().sendMessage("Die Golem Fähigkeit ist noch nicht fertiggestellt und enthält möglicherweise Bugs.");
		NationItemStack i = Util.getItemStackByType(np, getSkillType());
		LevelManager.addExperience(np, i, 0.1);
		World w = np.getBukkitPlayer().getWorld();
		Location l = np.getBukkitPlayer().getLocation();
		Snowman snowman1 = (Snowman) w.spawnEntity(l.add(1, 0, 0), EntityType.SNOWMAN);
		Snowman snowman2 = (Snowman) w.spawnEntity(l.add(1, 0, 1), EntityType.SNOWMAN);
		Snowman snowman3 = (Snowman) w.spawnEntity(l.add(2, 0, 1), EntityType.SNOWMAN);
		Snowman snowman4 = (Snowman) w.spawnEntity(l.add(2, 0, 2), EntityType.SNOWMAN);
		snowman1.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		snowman2.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		snowman3.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		snowman4.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		snowman1.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		snowman2.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		snowman3.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		snowman4.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		snowman1.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "BESCHUETZER"));
		snowman2.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "BESCHUETZER"));
		snowman3.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "ANGREIFER"));
		snowman4.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "ANGREIFER"));
		snowman1.setMaxHealth(40d);
		snowman1.setHealth(40d);
		snowman2.setMaxHealth(40d);
		snowman2.setHealth(40d);
		snowman3.setMaxHealth(40d);
		snowman3.setHealth(40d);
		snowman4.setMaxHealth(40d);
		snowman4.setHealth(40d);
		snowman1.setVelocity(snowman1.getVelocity().multiply(2));
		ArrayList<Snowman> list = new ArrayList<Snowman>();
		list.add(snowman1);
		list.add(snowman2);
		list.add(snowman3);
		list.add(snowman4);
		startWalking(snowman1, np.getBukkitPlayer());
		startWalking(snowman2, np.getBukkitPlayer());
		startWalking(snowman3, np.getBukkitPlayer());
		startWalking(snowman4, np.getBukkitPlayer());
		map.put(np.getBukkitPlayer().getName(), list);
		Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

			@Override
			public void run() {
				ArrayList<Snowman> list = map.get(np.getBukkitPlayer().getName());
				if (list != null)
					for (Snowman s : list) {
						if (s != null)
							s.remove();
					}
				map.remove(np.getBukkitPlayer().getName());
			}
		}, 20 * 45);
		return 45;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.GOLEMBESCHWÖRUNG;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Beschwört 4 Golems die", "dich verteidigen und", "Feinde angreifen!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1800; // 30 min
	}

	@Override
	public Material getItemType() {
		return Material.PORK;
	}

	@Override
	public String getFriendlyName() {
		return "Golembeschwörung";
	}
}
