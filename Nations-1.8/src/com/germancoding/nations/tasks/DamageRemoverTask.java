package com.germancoding.nations.tasks;

import java.util.ArrayList;

import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;

public class DamageRemoverTask implements Runnable {

	private final NationPlayer p;
	private final int task;

	private static Object lock = new Object();
	private static ArrayList<DamageRemoverTask> instances = new ArrayList<DamageRemoverTask>();

	public DamageRemoverTask(NationPlayer p) {
		synchronized (lock) {
			DamageRemoverTask found = null;
			for (DamageRemoverTask t : instances) {
				if (t.p.equals(p)) {
					Nations.scheduler.cancelTask(t.task);
					found = t;
					break;
				}
			}
			if (found != null)
				instances.remove(found);
			instances.add(this);
			this.p = p;
			this.task = Nations.scheduler.scheduleSyncRepeatingTask(Nations.plugin, this, 20 * 10L, 20 * 10L);
		}
	}

	@Override
	public void run() {
		if(Nations.DEBUG)
		System.out.println("Setting lastdamager of " + p.getBukkitPlayer().getName() + " to null");
		p.setLastdamager(null);
		synchronized (lock) {
			instances.remove(this);
		}
	}
	
	public static void cancel(NationPlayer p)
	{
		DamageRemoverTask found = null;
		for (DamageRemoverTask t : instances) {
			if (t.p.equals(p)) {
				Nations.scheduler.cancelTask(t.task);
				found = t;
				break;
			}
		}
		if (found != null)
			instances.remove(found);
	}
}
