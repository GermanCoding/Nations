package com.germancoding.nations.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.germancoding.nations.ForceField;
import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;
import com.germancoding.nations.tasks.PvpTask;

public class FlySkill extends Skill{
	
	private ArrayList<Player> noFallDamage = new ArrayList<Player>();

	@Override
	public int activate(final NationPlayer np) {
		final Player p = np.getBukkitPlayer();
		if(PvpTask.isInCooldown(p)) // Fly was probably used to escape. Give points for that! :)
		{
			NationItemStack i = Util.getItemStackByType(np, getSkillType());
			LevelManager.addExperience(np, i, 0.2);
		}
		p.setAllowFlight(true);
		p.setFlying(true);
		final int pid = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nations.plugin, new Runnable() {
			
			@Override
			public void run() {
				for(ForceField f: ForceField.FIELDS)
				{
					if(!f.canPlayerPassField(np, np.getBukkitPlayer().getLocation()))
						return;
				}
				p.setVelocity(p.getLocation().getDirection());
				p.setAllowFlight(true); // Avoid "Flying is not enabled on this server" disconnects.
				p.setFlying(true);
			}
		}, 1L, 1L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Nations.plugin, new Runnable() {
			
			@Override
			public void run() {
				noFallDamage.add(p);
				Bukkit.getScheduler().cancelTask(pid);
				p.setFlying(false);
				p.setAllowFlight(false);
			}
		}, 20 * 30);
		return 30;
	}
	
	@EventHandler
	public void blockFallDamage(EntityDamageEvent e)
	{
		if(e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL)
		{
			Player p = (Player) e.getEntity();
			if(noFallDamage.contains(p))
			{
				noFallDamage.remove(p);
				e.setCancelled(true); // The first fall damage after activating Fly is FREE!
			}
		}
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.FLY;
	}

	@Override
	public String[] getDescription() {
		String[] desc = {"Fliege durch die Luft"};
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 450; // 7,5 min
	}

	@Override
	public Material getItemType() {
		return Material.FEATHER;
	}

	@Override
	public String getFriendlyName() {
		return "Fly";
	}

}
