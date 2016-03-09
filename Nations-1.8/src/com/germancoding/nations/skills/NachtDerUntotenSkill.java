package com.germancoding.nations.skills;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.germancoding.nations.Zombie;
import com.germancoding.nations.Nations;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Util;
import com.google.common.collect.Lists;

@SuppressWarnings("deprecation")
public class NachtDerUntotenSkill extends Skill implements Listener {

	private ArrayList<NationPlayer> activePlayers = new ArrayList<NationPlayer>();

	@EventHandler
	public void onTargetSelected(EntityTargetEvent e) {
		if (e.getTarget() != null) {
			if (e.getEntity().hasMetadata("zombiebeschwoerung")) {
				if (e.getTarget() instanceof Player) {
					Player p = (Player) e.getTarget();
					NationPlayer np = Nations.instanceOf(p);

					if (np != null) {
						if (activePlayers.contains(np)) {
							// Cancel
							e.setTarget(findEnemy(p));
						} else {
							String name = (String) e.getEntity().getMetadata("zombiebeschwoerung-owner").get(0).asString();
							Player p1 = Bukkit.getPlayerExact(name);
							if (p1 != null) {
								NationPlayer owner = Nations.instanceOf(p1);
								if(owner != null)
								{
									if(owner.getNation().equals(np.getNation()))
									{
										// Cancel
										e.setTarget(findEnemy(p));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private Entity findEnemy(Player p) {
		for (Player p1 : p.getWorld().getPlayers()) {
			if (Nations.instanceOf(p1) != null) {
				if (!Nations.instanceOf(p1).getNation().equalsIgnoreCase(Nations.instanceOf(p).getNation())) {
					if (p1.getLocation().distance(p.getLocation()) <= 30) {
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
	public int activate(final NationPlayer np) {
		NationItemStack i = Util.getItemStackByType(np, getSkillType());
		LevelManager.addExperience(np, i, 0.1);
		// World w = np.getBukkitPlayer().getWorld();
		Location l = np.getBukkitPlayer().getLocation();
		Zombie zombie1 = new Zombie(np, l.add(1, 0, 0));
		Zombie zombie2 = new Zombie(np, l.add(1, 0, 1));
		Zombie zombie3 = new Zombie(np, l.add(2, 0, 1));
		Zombie zombie4 = new Zombie(np, l.add(2, 0, 2));

		final ArrayList<Zombie> zombies = Lists.newArrayList(zombie1, zombie2, zombie3, zombie4);

		for (Zombie zombie : zombies) {
			zombie.getEntity().setVelocity(zombie.getEntity().getVelocity().multiply(2.7));
			zombie.getEntity().setMetadata("zombiebeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
			zombie.getEntity().setMetadata("zombiebeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
			zombie.getEntity().setMaxHealth(20d);
			zombie.getEntity().setHealth(20d);
		}

		activePlayers.add(np);
		Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

			@Override
			public void run() {
				for (Zombie s : zombies) {
					if (s != null)
						s.getEntity().setHealth(0);
				}
				zombies.clear();
				activePlayers.remove(np);
			}
		}, 20 * 45);
		return 45;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.NACHTDERUNTOTEN;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Beschwoert Zombies die", "deine Feinde angreifen" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1800; // 30 min
	}

	@Override
	public Material getItemType() {
		return Material.ROTTEN_FLESH;
	}

	@Override
	public String getFriendlyName() {
		return "Nacht der Untoten";
	}

}
