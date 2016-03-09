package com.germancoding.nations.unsave;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.germancoding.nations.Nations;

public class PacketUtils implements Listener {
	private static HashMap<String, FakeDragon> players = new HashMap<String, FakeDragon>();
	private static HashMap<String, Integer> timers = new HashMap<String, Integer>();
	private static JavaPlugin plugin;

	public static void enable() {
		Bukkit.getPluginManager().registerEvents(new PacketUtils(), Nations.plugin);
		plugin = Nations.plugin;
	}

	public static void disable() {
		for (String player : players.keySet()) {
			Player p = Bukkit.getPlayer(player);
			if (p != null)
				removeBar(p);
		}
		players = null;
		plugin = null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void PlayerLoggout(PlayerQuitEvent event) {
		quit(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		quit(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent event) {
		handleTeleport(event.getPlayer(), event.getTo().clone());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerRespawnEvent event) {
		handleTeleport(event.getPlayer(), event.getRespawnLocation().clone());
	}

	private void handleTeleport(final Player player, final Location loc) {

		if (!hasBar(player))
			return;

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

			@Override
			public void run() {
				// Check if the player still has a dragon after the two ticks!
				// ;)
				if (!hasBar(player))
					return;

				FakeDragon oldDragon = getDragon(player, "");

				float health = oldDragon.health;
				String message = oldDragon.name;

				UnsaveUtils.sendPacket(player, getDragon(player, "").getDestroyPacket());

				players.remove(player.getName());

				FakeDragon dragon = addDragon(player, loc, message);
				dragon.health = health;

				sendDragon(dragon, player);
			}

		}, 2L);
	}

	private void quit(Player player) {
		removeBar(player);
	}

	public static void setMessage(Player player, String message, boolean fullHealth) {
		FakeDragon dragon = getDragon(player, message);

		dragon.name = cleanMessage(message);
		if (fullHealth)
			dragon.health = FakeDragon.MAX_HEALTH;
		else
			dragon.health = 0;

		cancelTimer(player);

		sendDragon(dragon, player);

	}

	public static void setCountdownMessage(Player player, String message, float percent) {
		FakeDragon dragon = getDragon(player, message);

		dragon.name = cleanMessage(message);
		dragon.health = (percent / 100f) * FakeDragon.MAX_HEALTH;

		cancelTimer(player);

		sendDragon(dragon, player);
	}

	public static void setCountdownMessage(final Player player, String message, int seconds) {
		FakeDragon dragon = getDragon(player, message);

		dragon.name = cleanMessage(message);
		dragon.health = FakeDragon.MAX_HEALTH;

		final float exactHealthStep = (((float) FakeDragon.MAX_HEALTH) / ((float) seconds));
		final int dragonHealthMinus = Math.round(exactHealthStep);

		cancelTimer(player);

		BukkitRunnable runnable = new BukkitRunnable() {

			@Override
			public void run() {
				FakeDragon drag = getDragon(player, "");
				drag.health -= dragonHealthMinus;

				if (drag.health <= exactHealthStep) {
					removeBar(player);
					// cancelTimer(player);
				} else {
					sendDragon(drag, player);
				}
			}
		};
		runnable.runTaskTimer(plugin, 20, 20);
		timers.put(player.getName(), runnable.getTaskId());

		sendDragon(dragon, player);
	}

	public static boolean hasBar(Player player) {
		return players.get(player.getName()) != null;
	}

	public static void removeBar(Player player) {
		if (!hasBar(player))
			return;

		UnsaveUtils.sendPacket(player, getDragon(player, "").getDestroyPacket());

		players.remove(player.getName());

		cancelTimer(player);
	}

	public static void setHealth(Player player, float percent) {
		if (!hasBar(player))
			return;

		FakeDragon dragon = getDragon(player, "");
		dragon.health = (percent / 100f) * FakeDragon.MAX_HEALTH;

		cancelTimer(player);

		sendDragon(dragon, player);
	}

	public static float getHealth(Player player) {
		if (!hasBar(player))
			return -1;

		return getDragon(player, "").health;
	}

	public static String getMessage(Player player) {
		if (!hasBar(player))
			return "";

		return getDragon(player, "").name;
	}

	private static String cleanMessage(String message) {
		if (message.length() > 64)
			message = message.substring(0, 63);

		return message;
	}

	private static void cancelTimer(Player player) {
		Integer timerID = timers.remove(player.getName());

		if (timerID != null) {
			Bukkit.getScheduler().cancelTask(timerID);
		}
	}

	private static void sendDragon(FakeDragon dragon, Player player) {
		UnsaveUtils.sendPacket(player, dragon.getMetaPacket(dragon.getWatcher()));
		UnsaveUtils.sendPacket(player, dragon.getTeleportPacket(player.getLocation().add(0, -150, 0)));
	}

	private static FakeDragon getDragon(Player player, String message) {
		if (hasBar(player)) {
			return players.get(player.getName());
		} else
			return addDragon(player, cleanMessage(message));
	}

	@Deprecated // Disabled for now!
	private static FakeDragon addDragon(Player player, String message) {
		/*
		FakeDragon dragon = UnsaveUtils.newDragon(message, player.getLocation().add(0, -150, 0));

		UnsaveUtils.sendPacket(player, dragon.getSpawnPacket());

		players.put(player.getName(), dragon);

		return dragon;
		*/
		return null;
	}

	@Deprecated // Disabled for now!
	private static FakeDragon addDragon(Player player, Location loc, String message) {
		/*
		FakeDragon dragon = UnsaveUtils.newDragon(message, loc.add(0, -150, 0));

		UnsaveUtils.sendPacket(player, dragon.getSpawnPacket());

		players.put(player.getName(), dragon);

		return dragon;
		*/
		return null;
	}
}
