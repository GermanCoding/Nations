package com.germancoding.nations.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.germancoding.nations.ChangedBlock;
import com.germancoding.nations.IllegalItemException;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.SkillManager;
import com.germancoding.nations.Util;

public class InflammatorSkill extends Skill {

	private ArrayList<NationPlayer> players = new ArrayList<NationPlayer>();

	@EventHandler
	public void blockFireSpread(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.SPREAD) {
			for (NationPlayer p : players) {
				if (!e.getBlock().getWorld().equals(p.getBukkitPlayer().getWorld()))
					continue;
				if (e.getIgnitingBlock().getLocation().distanceSquared(p.getBukkitPlayer().getLocation()) <= 32 * 32) // 32 blocks are a bit overkilled, but... I don't care.
				{
					e.setCancelled(true);
				}
			}
		}
		if (e.getCause() == IgniteCause.FLINT_AND_STEEL) {
			if (e.getPlayer() != null) {
				try {
					if (SkillManager.isSkillItem(e.getPlayer().getInventory().getItemInMainHand())) {
						e.setCancelled(true); // It's so sad that Bukkit ignores our setCancelled() in PlayerInteractEvent. So we have to do everything on our own...
					}
				} catch (IllegalItemException e1) {
					;
				}
			}
		}
	}

	@EventHandler
	public void onFireDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		if (e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK) {
			if (players.contains(Nations.instanceOf((Player) e.getEntity()))) {
				e.setCancelled(true);
				e.getEntity().setFireTicks(0);
			} else {
				for (NationPlayer p : players) {
					if (p.getBukkitPlayer().getWorld().equals(e.getEntity().getWorld())) {
						if (p.getBukkitPlayer().getLocation().distanceSquared(e.getEntity().getLocation()) <= 16 * 16) // TODO: Maybe a better check then just distance comparison?
						{
							Player damaged = (Player) e.getEntity();
							if (Nations.instanceOf(damaged) != null) {
								if (Nations.instanceOf(damaged).getNation().equalsIgnoreCase(p.getNation()))
									e.setCancelled(true);
								else {
									Nations.instanceOf(damaged).setLastdamager(p);
									// Our player burnt another player. Give points for that!
									NationItemStack i = Util.getItemStackByType(p, getSkillType());
									LevelManager.addExperience(p, i, 1);
								}
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int activate(final NationPlayer np) {
		if (!players.contains(np))
			players.add(np);
		Player p = np.getBukkitPlayer();
		Location base = p.getLocation();
		final ArrayList<ChangedBlock> fireBlocks = new ArrayList<ChangedBlock>();
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				Location current = base.clone().add(x, 0, z);
				Block fire = findFirePosition(current);
				fireBlocks.add(new ChangedBlock(fire.getLocation(), Material.AIR, fire.getData()));
				fire.setType(Material.FIRE);
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

			@Override
			public void run() {
				for (ChangedBlock b : fireBlocks) {
					b.restore();
				}
				fireBlocks.clear();
				Bukkit.getScheduler().scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

					@Override
					public void run() {
						players.remove(np);
						np.getBukkitPlayer().setFireTicks(1);
					}
				}, 10L);
			}
		}, 20 * 5);
		return 5;
	}

	private Block findFirePosition(Location base) {
		Block toFind = base.getBlock();
		if (toFind.getType() == Material.AIR) {
			return findFirePositionBelow(toFind);
		} else {
			return findFirePositionUp(toFind);
		}
	}

	private Block findFirePositionBelow(Block b) {
		while (b.getType() == Material.AIR) {
			b = b.getRelative(BlockFace.DOWN);
		}
		b = b.getRelative(BlockFace.UP);
		return b;
	}

	private Block findFirePositionUp(Block b) {
		while (b.getType() != Material.AIR) {
			b = b.getRelative(BlockFace.UP);
		}
		return b;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.INFLAMMATOR;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Entzünde deine Gegner!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 300; // 5 min
	}

	@Override
	public Material getItemType() {
		return Material.FLINT_AND_STEEL;
	}

	@Override
	public String getFriendlyName() {
		return "Inflammator";
	}

}
