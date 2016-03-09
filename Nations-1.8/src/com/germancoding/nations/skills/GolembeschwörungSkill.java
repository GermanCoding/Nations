package com.germancoding.nations.skills;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;

import com.germancoding.nations.Golem;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;
import com.google.common.collect.Lists;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;

import net.minecraft.server.v1_8_R1.EntityInsentient;

public class GolembeschwörungSkill extends Skill implements Listener{

	@EventHandler
	public void noTrail(final EntityBlockFormEvent e) {
		if (e.getEntity().hasMetadata("golembeschwoerung"))  {
			e.setCancelled(true);
			e.getNewState().setType(Material.AIR);
			final Block toChange = e.getBlock();
			new BukkitRunnable() {

				@Override
				public void run() {
					toChange.setType(Material.AIR);
					toChange.getState().setType(Material.AIR);
					toChange.getState().update(true);
				}
			}.runTaskLater(Nations.plugin, 60L);
		}
	}
	
	@EventHandler
	public void noTrail(EntityChangeBlockEvent e) {
		if (e.getEntity().hasMetadata("golembeschwoerung") && (e.getTo() == Material.SNOW || e.getTo() == Material.SNOW_BLOCK)) {
			e.setCancelled(true);
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


	public void followPlayer(Player player, final LivingEntity entity, double d) {
		final LivingEntity e = entity;
		final Player p = player;
		final float f = (float) d;

		new BukkitRunnable() {

			@Override
			public void run() {
				if (entity == null || entity.isDead() || !entity.isValid())
					this.cancel();
				else
					((EntityInsentient) ((CraftEntity) e).getHandle()).getNavigation().a(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), f);
			}
		}.runTaskTimer(Nations.plugin, 0, 2 * 20);
	}

	public int activate(final NationPlayer np) {
		NationItemStack i = Util.getItemStackByType(np, getSkillType());
		LevelManager.addExperience(np, i, 0.1);
		// World w = np.getBukkitPlayer().getWorld();
		Location l = np.getBukkitPlayer().getLocation();
		Golem golem1 = new Golem(np, l.add(1, 0, 0));
		Golem golem2 = new Golem(np, l.add(1, 0, 1));
		Golem golem3 = new Golem(np, l.add(2, 0, 1));
		Golem golem4 = new Golem(np, l.add(2, 0, 2));

		final ArrayList<Golem> golems = Lists.newArrayList(golem1, golem2, golem3, golem4);

		for (Golem golem : golems) {
			golem.getEntity().setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
			golem.getEntity().setMaxHealth(40d);
			golem.getEntity().setHealth(40d);
			followPlayer(np.getBukkitPlayer(), golem.getEntity(), 2.2);
		}

		/*
		 * snowman1.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		 * snowman2.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		 * snowman3.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		 * snowman4.setMetadata("golembeschwoerung", new FixedMetadataValue(Nations.plugin, Boolean.valueOf(true)));
		 * snowman1.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		 * snowman2.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		 * snowman3.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		 * snowman4.setMetadata("golembeschwoerung-owner", new FixedMetadataValue(Nations.plugin, np.getBukkitPlayer().getName()));
		 * snowman1.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "BESCHUETZER"));
		 * snowman2.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "BESCHUETZER"));
		 * snowman3.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "ANGREIFER"));
		 * snowman4.setMetadata("golembeschwoerung-type", new FixedMetadataValue(Nations.plugin, "ANGREIFER"));
		 * snowman1.setMaxHealth(40d);
		 * snowman1.setHealth(40d);
		 * snowman2.setMaxHealth(40d);
		 * snowman2.setHealth(40d);
		 * snowman3.setMaxHealth(40d);
		 * snowman3.setHealth(40d);
		 * snowman4.setMaxHealth(40d);
		 * snowman4.setHealth(40d);
		 * snowman1.setVelocity(snowman1.getVelocity().multiply(2));
		 */

		Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

			@Override
			public void run() {
				for (Golem s : golems) {
					if (s != null)
						s.getEntity().setHealth(0);
				}
				golems.clear();
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
