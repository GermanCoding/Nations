package com.germancoding.nations.tasks;

import com.germancoding.nations.NationItemStack;

public class NationItemStackUpdateTask implements Runnable {

	@Override
	public void run() {
		NationItemStack.updatePlayerItems();
	}

}
