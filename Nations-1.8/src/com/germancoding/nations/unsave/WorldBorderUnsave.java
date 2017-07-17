package com.germancoding.nations.unsave;

import java.util.HashMap;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_12_R1.WorldBorder;

import com.germancoding.nations.ForceField;
import com.germancoding.nations.Nations;

public class WorldBorderUnsave {

	private HashMap<Player, ForceField> sendMap = new HashMap<Player, ForceField>();

	public void sendBorder(ForceField f, Player p, boolean shrinking, boolean growing) {
		sendMap.put(p, f);
		WorldBorder wb = new WorldBorder();
		CraftPlayer player = (CraftPlayer) p;
		wb.world = ((CraftWorld) player.getWorld()).getHandle();
		wb.setCenter(f.getCenter().getX(), f.getCenter().getZ());
		// System.out.println(f.getCenter().getX() + ":" + f.getCenter().getZ());
		// wb.setDamageAmount(1);
		// wb.setDamageBuffer(-1);
		// wb.a(15);

		int xSize = f.getMax().getBlockX() - f.getMin().getBlockX();
		int zSize = f.getMax().getBlockZ() - f.getMin().getBlockZ();
		int size = xSize > zSize ? xSize : zSize;

		if (!growing) {
			wb.setSize(size);
		} else {
			wb.setSize(1);
		}

		if (shrinking)
			wb.transitionSizeBetween(size, 0, 1000);
		if (growing)
			wb.transitionSizeBetween(0, size, 1000);
		// System.out.println(wb.getSize());
		wb.setWarningDistance(0);
		wb.setWarningTime(0);

		// p.getWorld().getWorldBorder().reset();
		PacketPlayOutWorldBorder worldBorderPacketInit = new PacketPlayOutWorldBorder(wb, EnumWorldBorderAction.INITIALIZE);
		player.getHandle().playerConnection.sendPacket(worldBorderPacketInit);
		// Nations.logger.info("WorldBorder packet send!");
	}

	public void sendPulsingBorder(final ForceField f, final Player p) {
		sendBorder(f, p, false, true);
		Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

			@Override
			public void run() {
				sendBorder(f, p, true, false);
				Nations.scheduler.scheduleSyncDelayedTask(Nations.plugin, new Runnable() {

					@Override
					public void run() {
						removeBorder(p);
					}
				}, 20);
			}
		}, 20);
	}

	public void sendBorder(ForceField f, Player p) {
		sendBorder(f, p, false, false);
	}

	public void optSendBorder(ForceField f, Player p) {
		if (!sendMap.containsKey(p) || !sendMap.get(p).equals(f)) {
			sendBorder(f, p);
		}
	}

	public void clearPlayer(Player p) {
		sendMap.remove(p);
	}

	public void removeBorder(Player p) {
		if (sendMap.containsKey(p)) {
			forceRemoveBorder(p);
		}
	}

	public void forceRemoveBorder(Player p) {
		clearPlayer(p);
		WorldBorder wb = new WorldBorder();
		CraftPlayer player = (CraftPlayer) p;
		wb.world = ((CraftWorld) player.getWorld()).getHandle();

		wb.setCenter(0, 0);
		wb.setSize(60000000);

		PacketPlayOutWorldBorder worldBorderPacketInit = new PacketPlayOutWorldBorder(wb, EnumWorldBorderAction.INITIALIZE);
		player.getHandle().playerConnection.sendPacket(worldBorderPacketInit);
		// Nations.logger.info("WorldBorder removed!");
	}

}
