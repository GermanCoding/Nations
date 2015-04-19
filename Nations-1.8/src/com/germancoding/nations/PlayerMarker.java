package com.germancoding.nations;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMarker implements Listener {

	public static HashMap<Player, MarkerBlock> markerMap = new HashMap<>();

	/*
	 * @SuppressWarnings("deprecation")
	 * @Override public void run() { Iterator<NationPlayer> i =
	 * Nations.getIteratorOfPlayers(); while (i.hasNext()) { final NationPlayer
	 * p = i.next(); FallingBlock block = null; for (Entity e :
	 * p.getBukkitPlayer().getNearbyEntities(5, 5, 5)) { if (e instanceof
	 * FallingBlock) { if (e.hasMetadata("block-" +
	 * p.getBukkitPlayer().getName())) { block = (FallingBlock) e; break; } } }
	 * if (block == null && (!p.getBukkitPlayer().isSneaking() ||
	 * p.getBukkitPlayer().isDead() || !p.getBukkitPlayer().isOnline())) //
	 * Maybe // we // lost // the // item // somewhere // in // the // world. //
	 * Search // for // it // and // remove // it. {
	 * removeItem(p.getBukkitPlayer(), true); } if
	 * (!p.getBukkitPlayer().isSneaking()) { final FallingBlock newBlock1 =
	 * block; if (newBlock1 == null) { FallingBlock newBlock = p
	 * .getBukkitPlayer() .getWorld()
	 * .spawnFallingBlock(p.getBukkitPlayer().getEyeLocation().add(0, 3, 0),
	 * getBlockData(p).keySet().iterator().next(),
	 * getBlockData(p).get(getBlockData(p).keySet().iterator().next())); //
	 * newItem = (Item) //
	 * p.getBukkitPlayer().getWorld().spawnEntity(p.getBukkitPlayer
	 * ().getEyeLocation().add(0, // 2, 0), EntityType.DROPPED_ITEM);
	 * FallingBlock fallingBlock = newBlock; fallingBlock.setVelocity(new
	 * org.bukkit.util.Vector()); fallingBlock.setFallDistance(0);
	 * fallingBlock.setDropItem(false); newBlock.setMetadata("block-" +
	 * p.getBukkitPlayer().getName(), new FixedMetadataValue(Nations.plugin,
	 * true)); newBlock.setMetadata("noFalling", new
	 * FixedMetadataValue(Nations.plugin, true)); //
	 * fallingBlockMap.put(p.getBukkitPlayer(), // fallingBlock.getEntityId());
	 * } else { FallingBlock fallingBlock = newBlock1;
	 * fallingBlock.setFallDistance(0); // fallingBlock.setDropItem(false);
	 * Location base = p.getBukkitPlayer().getEyeLocation().add(0, 3, 0);
	 * fallingBlock
	 * .setVelocity(fallingBlock.getVelocity().subtract(base.toVector())); //
	 * fallingBlock.teleport(p.getBukkitPlayer().getEyeLocation().add(0, // 3,
	 * 0)); // ((CraftFallingSand) newBlock).getHandle().onGround = // true; //
	 * ((CraftItem) newItem).getHandle().onGround = true; // ((CraftItem)
	 * newItem).getHandle().positionChanged = // false; //
	 * newItem.teleport(p.getBukkitPlayer().getEyeLocation().add(0, // 2, 0));
	 * // ((CraftItem) newItem).getHandle().positionChanged = // false; //
	 * ((CraftItem) newItem).getHandle().velocityChanged = // false; //
	 * if(p.getBukkitPlayer().getPassenger() == null || //
	 * p.getBukkitPlayer().getPassenger().isDead() || //
	 * !(p.getBukkitPlayer().getPassenger() instanceof Item)) // ((CraftItem) //
	 * newItem).getHandle().setPassengerOf(((CraftPlayer) //
	 * p.getBukkitPlayer()).getHandle()); // ((CraftItem)
	 * newItem).getHandle().onGround = true; //
	 * newBlock.teleport(p.getBukkitPlayer().getEyeLocation().add(0, // 2, 0));
	 * // PacketPlayOutEntityTeleport teleportPacket = new //
	 * PacketPlayOutEntityTeleport(fallingBlock.getEntityId(), //
	 * base.getBlockX(), base.getBlockY(), base.getBlockZ(), // (byte)
	 * (base.getYaw() * 256 / 360), (byte) ( // base.getPitch() * 256 / 360),
	 * false); // ((CraftPlayer) //
	 * p.getBukkitPlayer()).getHandle().playerConnection
	 * .sendPacket(teleportPacket); } } else { if (block != null)
	 * removeItem(p.getBukkitPlayer(), true); } } }
	 */

	/*
	 * @EventHandler public void blockBlockCreating(EntityChangeBlockEvent e) {
	 * if (e.getEntity() instanceof FallingBlock) { if
	 * (e.getEntity().hasMetadata("noFalling")) { e.setCancelled(true); } } }
	 */

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent e) {
		if (!Nations.hasNationWorld())
			return;
		if (e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
			return;
		if (!e.getPlayer().getWorld().getName().equals(Nations.getNationWorld().getName()))
			return;
		final NationPlayer np = Nations.instanceOf(e.getPlayer());
		if (np != null) {
			if (np.getBukkitPlayer().isSneaking() || np.getBukkitPlayer().isDead() || !np.getBukkitPlayer().isOnline()) {
				removeItem(e.getPlayer());
				return;
			}
			/*
			 * boolean up = false;
			 * if(e.getTo().getBlockY() > e.getFrom().getBlockY())
			 * if(e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
			 * up = true;
			 * final boolean finalUp = up;
			 */
			Bukkit.getScheduler().runTaskLater(Nations.plugin, new Runnable() {

				@Override
				public void run() {
					if (!markerMap.containsKey(e.getPlayer())) {
						markerMap.put(e.getPlayer(), new MarkerBlock(np));
					}
					MarkerBlock block = markerMap.get(e.getPlayer());
					block.update();
				}
			}, 1);
		}
	}

	/*
	 * @EventHandler
	 * public void onBlockDestroy(BlockBreakEvent e) {
	 * for (Player p : markerMap.keySet()) {
	 * // markerMap.get(p).calculateLocation();
	 * // System.out.println(e.getBlock().getLocation() + " against " +
	 * // markerMap.get(p).currentLocation.getBlock().getLocation());
	 * if (e.getBlock().getLocation().equals(markerMap.get(p).currentLocation.getBlock().getLocation())) {
	 * e.setCancelled(true);
	 * }
	 * }
	 * }
	 */

	public static void removeItems() {
		for (Player p : Bukkit.getOnlinePlayers())
			removeItem(p);
	}

	public static void removeItem(Player p) {
		if (!markerMap.containsKey(p))
			return;
		MarkerBlock block = markerMap.get(p);
		block.currentLocation = null;
		if (block.lastChangedBlock != null)
			block.lastChangedBlock.restore();
		block.player = null;
		markerMap.remove(p);
	}

	public static void updatePlayer(NationPlayer p) {
		removeItem(p.getBukkitPlayer());
	}

}
