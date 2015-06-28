package com.germancoding.nations;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowman;

public class Golem {

	private Snowman entity;
	private NationPlayer owner;

	public Golem(NationPlayer owner, Location spawnAt) {
		setEntity((Snowman) spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SNOWMAN));
		setOwner(owner);
	}

	public Snowman getEntity() {
		return entity;
	}

	public void setEntity(Snowman entity) {
		this.entity = entity;
	}

	public NationPlayer getOwner() {
		return owner;
	}

	public void setOwner(NationPlayer owner) {
		this.owner = owner;
	}

}
