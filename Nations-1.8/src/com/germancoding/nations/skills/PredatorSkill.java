package com.germancoding.nations.skills;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.germancoding.nations.ForceField;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.SkillManager;
import com.germancoding.nations.Util;
import com.germancoding.nations.tasks.ItemCooldownTask;

@SuppressWarnings("deprecation")
public class PredatorSkill extends Skill {

	@Override
	public int activate(NationPlayer np) {
		final Player p = np.getBukkitPlayer();
		final Entity enemy = findEnemy(p);
		if (enemy != null) {
			final Location start = enemy.getLocation();
			for (ForceField f : ForceField.FIELDS)
				if (f.isInsideField(start)) {
					p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Predator-Start abgebrochen! Feindliches Ziel ist durch ein Kraftfeld geschützt!");
					ItemCooldownTask.kill(SkillManager.getIDOfItem(np.getBukkitPlayer().getItemInHand()));
					return 1;
				}
			start.setY(start.getY() + 50);
			final Vector direction = enemy.getLocation().toVector().subtract(start.toVector());
			Arrow arrow;
			p.getWorld().createExplosion(start.getX(), start.getY(), start.getZ(), 20, false, false);
			arrow = p.getWorld().spawnArrow(start, direction, 40, 0); // Let it rain a predator from the sky!
			arrow.setShooter(p);
			arrow.setMetadata("explosion", new FixedMetadataValue(Nations.plugin, true));
			Bukkit.getScheduler().scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

				@Override
				public void run() {
					p.getWorld().spawnArrow(start, direction, 1, 0);
					start.setX(start.getX() + 1);
					p.getWorld().spawnArrow(start, direction, 1, 0);
				}
			}, 3L);
			NationItemStack i = Util.getItemStackByType(np, getSkillType());
			LevelManager.addExperience(np, i, 1);
		} else {
			p.sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Predator hat kein Ziel gefunden!");
			ItemCooldownTask.kill(SkillManager.getIDOfItem(np.getBukkitPlayer().getItemInHand()));
		}
		return 1;
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent e) {
		if (e.getEntity().hasMetadata("explosion")) {
			BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 20);
			Block hitBlock = null;
			while (iterator.hasNext()) {
				hitBlock = iterator.next();
				if (hitBlock.getType() != Material.AIR) {
					break;
				}
			}
			if (hitBlock == null || hitBlock.getType() == Material.AIR)
				return;
			Location l = hitBlock.getLocation();
			e.getEntity().getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 5, false, false);
			e.getEntity().removeMetadata("explosion", Nations.plugin);
		}
	}

	private boolean isInOpenAir(Location l) {
		if (l.getBlock() != null) {
			while (l.getBlock().getType() != Material.AIR)
				l = l.getBlock().getRelative(BlockFace.UP).getLocation();
			Block highest = l.getWorld().getHighestBlockAt(l);
			if (highest.getY() <= l.getY())
				return true;
		}
		return false;
	}

	private Entity findEnemy(Player p) {
		for (Player p1 : p.getWorld().getPlayers()) {
			if (Nations.instanceOf(p1) != null) {
				if (!Nations.instanceOf(p1).getNation().equalsIgnoreCase(Nations.instanceOf(p).getNation())) {
					if (p1.getLocation().distance(p.getLocation()) <= 80 && isInOpenAir(p1.getLocation())) {
						return p1;
					}
				}
			}
		}
		Block playerLookAt = null;
		try {
			playerLookAt = getTarget(p).getBlock();
		} catch (Exception e1) {
			Nations.logger.warning("Unable to find target block for " + p.getName()); // Bukkit tells us, that the returned block could be null. NEVER
																						// found such a situation though.
		}
		if (playerLookAt != null) {
			List<Entity> entities = playerLookAt.getWorld().getEntities();
			Entity closest = null;
			for (Entity e : entities) {
				if (e.equals(p))
					continue;
				if (e instanceof LivingEntity) {
					if (e.getLocation().distanceSquared(playerLookAt.getLocation()) <= 25) // five blocks ( 5^2 (squared) = 25, math genius!)
					{
						if (!isInOpenAir(e.getLocation()))
							continue;
						if (closest != null) {
							if (e.getLocation().distanceSquared(playerLookAt.getLocation()) < closest.getLocation().distanceSquared(playerLookAt.getLocation()))
								closest = e;
						} else {
							closest = e;
						}
					}
				}
			}
			if (closest != null)
				return closest;
		}
		List<Entity> entities2 = p.getNearbyEntities(30, 30, 30);
		Entity bestMob = null;
		for (Entity e : entities2) {
			if (e.equals(p))
				continue;
			if (e instanceof LivingEntity) {
				if (e instanceof Monster) {
					if (!isInOpenAir(e.getLocation()))
						continue;
					if (bestMob != null) {
						if (e.getLocation().distanceSquared(p.getLocation()) < bestMob.getLocation().distanceSquared(p.getLocation()))
							bestMob = e;
					} else {
						bestMob = e;
					}
				}
			}
		}
		return bestMob;
	}

	public static Location getTarget(LivingEntity entity) throws Exception {
		Block block = entity.getTargetBlock(TRANSPARENT_MATERIALS, 300);
		if (block == null) {
			throw new Exception("Not targeting a block");
		}
		return block.getLocation();
	}

	private static final HashSet<Byte> TRANSPARENT_MATERIALS = new HashSet<Byte>();
	public static final Set<Integer> HOLLOW_MATERIALS = new HashSet<Integer>();

	// Some copy and paste from Essentials. Didn't want to write that on my own.
	static {
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.AIR.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.SAPLING.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.POWERED_RAIL.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.DETECTOR_RAIL.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.LONG_GRASS.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.DEAD_BUSH.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.YELLOW_FLOWER.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.RED_ROSE.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.BROWN_MUSHROOM.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.RED_MUSHROOM.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.TORCH.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.REDSTONE_WIRE.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.SEEDS.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.SIGN_POST.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.WOODEN_DOOR.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.LADDER.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.RAILS.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.WALL_SIGN.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.LEVER.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.STONE_PLATE.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.IRON_DOOR_BLOCK.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.WOOD_PLATE.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.REDSTONE_TORCH_OFF.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.REDSTONE_TORCH_ON.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.STONE_BUTTON.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.SNOW.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.SUGAR_CANE_BLOCK.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.DIODE_BLOCK_OFF.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.DIODE_BLOCK_ON.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.PUMPKIN_STEM.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.MELON_STEM.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.VINE.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.FENCE_GATE.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.WATER_LILY.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.NETHER_WARTS.getId()));
		HOLLOW_MATERIALS.add(Integer.valueOf(Material.CARPET.getId()));
		for (Integer integer : HOLLOW_MATERIALS) {
			TRANSPARENT_MATERIALS.add(Byte.valueOf(integer.byteValue()));
		}
		TRANSPARENT_MATERIALS.add(Byte.valueOf((byte) Material.WATER.getId()));
		TRANSPARENT_MATERIALS.add(Byte.valueOf((byte) Material.STATIONARY_WATER.getId()));
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.PREDATOR;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Lässt eine Rakete", "von oben herabfallen!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 60 * 15; // 15 min
	}

	@Override
	public Material getItemType() {
		return Material.STICK;
	}

	@Override
	public String getFriendlyName() {
		return "Predator";
	}

}
