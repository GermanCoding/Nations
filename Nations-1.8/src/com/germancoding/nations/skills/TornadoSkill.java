package com.germancoding.nations.skills;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.germancoding.nations.ForceField;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;

public class TornadoSkill extends Skill implements Listener {

	public int activate(NationPlayer np) {
		spawnTornado(np, Nations.plugin, np.getBukkitPlayer().getLocation(), Material.DIRT, (byte) 0, np.getBukkitPlayer().getLocation().getDirection(), 0.1D, 60, (long) 20 * 20, false);
		return 20;
	}

	/**
	 * Spawns a tornado at the given location l.
	 * 
	 * @author https://forums.bukkit.org/threads/resource-tornado-spawn-a-tornado -with-zero-client-mods-v3-2.196642/
	 * 
	 * @param spawned
	 *            - The NationPlayer instance who spawned the tornado.
	 * 
	 * @param plugin
	 *            - Plugin instance that spawns the tornado.
	 * @param location
	 *            - Location to spawn the tornado.
	 * @param material
	 *            - The base material for the tornado.
	 * @param data
	 *            - Data for the block.
	 * @param direction
	 *            - The direction the tornado should move in.
	 * @param speed
	 *            - How fast it moves in the given direction. Warning! A number greater than 0.3 makes it look weird.
	 * @param amount_of_blocks
	 *            - The max amount of blocks that can exist in the tornado.
	 * @param time
	 *            - The amount of ticks the tornado should be alive.
	 * @param spew
	 *            - Defines if the tornado should remove or throw out any block it picks up.
	 */
	public static void spawnTornado(final NationPlayer spawned, final JavaPlugin plugin, final Location location, final Material material, final byte data, final Vector direction, final double speed, final int amount_of_blocks, final long time, final boolean spew) {
		// Modify the direction vector using the speed argument.
		if (direction != null) {
			direction.normalize().multiply(speed);
		}

		class VortexBlock {

			Entity entity;

			private boolean removable = true;

			private float ticker_vertical = 0.0f;
			private float ticker_horisontal = (float) (Math.random() * 2 * Math.PI);

			@SuppressWarnings("deprecation")
			public VortexBlock(Location l, Material m, byte d) {

				if (l.getBlock().getType() != Material.AIR) {

					Block b = l.getBlock();
					entity = l.getWorld().spawnFallingBlock(l, b.getType(), b.getData());

					// No longer destroy blocks with the tornado
					/*
					 if (b.getType() != Material.WATER)
					 * b.setType(Material.AIR);
					 */

					removable = false;
				} else
					entity = l.getWorld().spawnFallingBlock(l, m, d);

				addMetadata();
				tick();
			}

			private void addMetadata() {
				if (entity != null)
					entity.setMetadata("vortex", new FixedMetadataValue(plugin, "protected"));
			}

			public void remove() {
				if (removable || (!spew && (entity instanceof FallingBlock))) {
					entity.remove();
				}
				if (entity != null)
					entity.removeMetadata("vortex", plugin);
			}

			@SuppressWarnings("deprecation")
			public VortexBlock tick() {

				double radius = Math.sin(verticalTicker()) * 2;
				float horisontal = horisontalTicker();

				Vector v = new Vector(radius * Math.cos(horisontal), 0.5D, radius * Math.sin(horisontal));

				setVelocity(v);

				// Pick up blocks
				Block b = entity.getLocation().add(v).getBlock();
				if (b.getType() != Material.AIR && b.getType() != Material.WATER && b.getType() != Material.STATIONARY_WATER) {
					return new VortexBlock(b.getLocation(), b.getType(), b.getData());
				}

				Random random = new Random();

				// Pick up other entities
				List<Entity> entities = entity.getNearbyEntities(3.0D, 3.0D, 3.0D);
				for (Entity e : entities) {
					if (e.getType() != EntityType.PLAYER) {
						if (e instanceof LivingEntity) {
							boolean invertNumber1 = random.nextBoolean();
							boolean invertNumber2 = random.nextBoolean();
							int x = random.nextInt(5) + 2;
							if (invertNumber1)
								x = x * -1; // Just learned: -x is also possible. Altough, I will use * -1 :)
							int z = random.nextInt(5) + 2;
							if (invertNumber2)
								z = z * -1;
							e.setVelocity(new Vector(x, 4D, z));
							NationItemStack i = Util.getItemStackByType(spawned, SkillType.TORNADO);
							LevelManager.addExperience(spawned, i, 0.2);
						}
					} else {
						if (e instanceof FallingBlock)
							continue; // Ignore tornado blocks
						Player p = (Player) e;
						NationPlayer np = Nations.instanceOf(p);
						if (np != null) {
							if (!np.getNation().equals(spawned.getNation())) {
								np.setLastdamager(spawned);
								boolean invertNumber1 = random.nextBoolean();
								boolean invertNumber2 = random.nextBoolean();
								int x = random.nextInt(5) + 2;
								if (invertNumber1)
									x = x * -1;
								int z = random.nextInt(5) + 2;
								if (invertNumber2)
									z = z * -1;
								p.setVelocity(new Vector(x, 4D, z));
								NationItemStack i = Util.getItemStackByType(spawned, SkillType.TORNADO);
								LevelManager.addExperience(spawned, i, 1.5);
							}
						}
					}
				}

				return null;
			}

			private void setVelocity(Vector v) {
				entity.setVelocity(v);
			}

			private float verticalTicker() {
				if (ticker_vertical < 1.0f) {
					ticker_vertical += 0.05f;
				}
				return ticker_vertical;
			}

			private float horisontalTicker() {
				// ticker_horisontal = (float) ((ticker_horisontal + 0.8f) %
				// 2*Math.PI);
				return (ticker_horisontal += 0.8f);
			}
		}

		final int id = new BukkitRunnable() {

			private ArrayDeque<VortexBlock> blocks = new ArrayDeque<VortexBlock>();

			public void run() {

				// Spawns 10 blocks at the time, with a maximum of 200 blocks at
				// the same time.
				for (int i = 0; i < 10; i++) {
					if (direction != null) {
						location.add(direction);
						for (ForceField f : ForceField.FIELDS) {
							if (f.isInsideField(location)) {
								spawned.getBukkitPlayer().sendMessage(ChatColor.GOLD + "[Nations] " + ChatColor.RED + "Ein Kraftfeld hat deinen Tornado zurückgeworfen!");
								Vector v = direction;
								v = v.setX(v.getX() * -1);
								v = v.setZ(v.getZ() * -1);
								// v = v.multiply(2);
								// location.setDirection(v);
								direction.setX(v.getX());
								direction.setY(v.getY());
								direction.setZ(v.getZ());
							}
						}
					}

					checkListSize();
					blocks.add(new VortexBlock(location, material, data));
				}

				// Make all blocks in the list spin, and pick up any blocks that
				// get in the way.
				ArrayDeque<VortexBlock> que = new ArrayDeque<VortexBlock>();

				for (VortexBlock vb : blocks) {
					VortexBlock temp = vb.tick();
					if (temp != null) {
						que.add(temp);
					}
				}

				for (VortexBlock vb : que) {
					checkListSize();
					blocks.add(vb);
				}
			}

			// Removes the oldest block if the list goes over the limit.
			private void checkListSize() {
				if (blocks.size() >= amount_of_blocks) {
					VortexBlock vb = blocks.getFirst();
					vb.remove();
					blocks.remove(vb);
					checkListSize();
				}
			}

		}.runTaskTimer(plugin, 5L, 5L).getTaskId();

		// Stop the "tornado" after the given time.
		new BukkitRunnable() {
			public void run() {
				plugin.getServer().getScheduler().cancelTask(id);
			}
		}.runTaskLater(plugin, time);
	}

	/*
	 * @SuppressWarnings("deprecation")
	 * @EventHandler public void onEntityChangeBlock(EntityChangeBlockEvent e) { if(e.getEntity().hasMetadata("vortex")) { new
	 * ChangedBlock(e.getBlock().getLocation(), e.getBlock().getType(), e.getBlock().getData()); } }
	 */

	@Override
	public SkillType getSkillType() {
		return SkillType.TORNADO;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Wirbelt alles", "für 20 Sekunden", "in die Luft!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1800; // 30 min
	}

	@Override
	public Material getItemType() {
		return Material.EYE_OF_ENDER;
	}

	@Override
	public String getFriendlyName() {
		return "Tornado";
	}
}
