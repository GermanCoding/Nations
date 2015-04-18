package com.germancoding.nations.tasks;

import java.util.Iterator;

import com.germancoding.nations.ConfigManager;
import com.germancoding.nations.NationPlayer;
import com.germancoding.nations.Nations;

public class ConfigTask implements Runnable {

	@Override
	public void run() {
		ConfigManager.saveDataToConfig();
		// Workaround for invalid tablist - refresh the tablist on every save ~ 3 minutes
		Iterator<NationPlayer> i = Nations.getIteratorOfPlayers();
		while(i.hasNext())
		{
			NationPlayer p = i.next();
			Nations.updatePlayerListName(p);
		}
	}

}
