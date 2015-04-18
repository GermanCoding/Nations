package com.germancoding.nations.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.germancoding.nations.LevelManager;
import com.germancoding.nations.NationItemStack;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;
import com.germancoding.nations.Util;

public class AusbruchDerWindeSkill extends Skill{

	@Override
	public int activate(NationPlayer np) {
		Player p = np.getBukkitPlayer();
		List<Entity> entities = p.getNearbyEntities(10, 256, 10); // Ignore height difference (height = 256).
		for(Entity e: entities)
		{
			if(e instanceof Player)
			{
				Player ep = (Player) e;
				if(Nations.instanceOf(ep) != null)
				{
					NationPlayer npOther = Nations.instanceOf(ep);
					if(np.getNation().equalsIgnoreCase(npOther.getNation()))
						continue;
					npOther.setLastdamager(np);
				}
			}
			Vector direction = e.getLocation().toVector().subtract(p.getLocation().toVector());
			e.setVelocity(direction.setY(direction.getY() + 5)); // Throw it away!
			NationItemStack stack = Util.getItemStackByType(np, this.getSkillType());
			LevelManager.addExperience(np, stack, 1);
		}
		return 3;
	}

	@Override
	public SkillType getSkillType() {
		return SkillType.AUSBRUCH_DER_WINDE;
	}

	@Override
	public String[] getDescription() {
		String[] desc = {"Stößt deine", "Feinde zurück!"};
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 300; // 5 min
	}

	@Override
	public Material getItemType() {
		return Material.WRITTEN_BOOK;
	}

	@Override
	public String getFriendlyName() {
		return "Ausbruch der Winde";
	}

}
