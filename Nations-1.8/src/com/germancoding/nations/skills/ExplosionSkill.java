package com.germancoding.nations.skills;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.SkillManager;
import com.germancoding.nations.tasks.ItemCooldownTask;

@SuppressWarnings("deprecation")
public class ExplosionSkill extends Skill {

	// TODO: Add experience points for using this skill.

	@Override
	public int activate(NationPlayer np) {
		try {
			Location target = getTarget(np.getBukkitPlayer());
			if (target == null)
				throw new Exception("Target is null");
			World w = target.getWorld();
			boolean cancelled = !w.createExplosion(target.getX(), target.getY(), target.getZ(), 12f, false, true);
			if (cancelled) {
				np.getBukkitPlayer().sendMessage(ChatColor.RED + "Die Explosion wurde von irgendetwas aufgehalten! Eventuell ein Kraftfeld?");
				ItemCooldownTask.kill(SkillManager.getIDOfItem(np.getBukkitPlayer().getItemInHand()));
			}
		} catch (Exception e) {
			np.getBukkitPlayer().sendMessage(ChatColor.RED + "Kein Block in Sicht (oder zu weit weg)!");
			ItemCooldownTask.kill(SkillManager.getIDOfItem(np.getBukkitPlayer().getItemInHand()));
		}
		return 1;
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
		return SkillType.EXPLOSION;
	}

	@Override
	public String[] getDescription() {
		String[] desc = { "Erzeugt eine", "große Explosion", "in deiner", "aktuellen Blickrichtung!" };
		return desc;
	}

	@Override
	public int getDefaultCooldown() {
		return 1200; // 20 min
	}

	@Override
	public Material getItemType() {
		return Material.TNT;
	}

	@Override
	public String getFriendlyName() {
		return "Explosion";
	}

}
