package polytopia.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import polytopia.gameplay.Player.Faction;
import polytopia.utils.RandomName;

public class Resource implements TileVariation {
	
	private Tile ownerTile;

	public Tile getOwnerTile() {return this.ownerTile;}
	public Faction getStyle() {
		if (type == ResourceType.FRUIT || type == ResourceType.ANIMAL)
			return this.ownerTile.getStyle();
		else
			return null;
	}

	public enum ResourceType {
		FRUIT, FISH, ANIMAL, METAL, CROP, WHALE, RUINS, VILLAGE;
	}
	private ResourceType type;
	public ResourceType getResourceType() {return this.type;}


	private Action[] actions;
	public Action[] getActions() {return this.actions;}


	public Resource(Tile ownerTile, ResourceType type) {
		this.ownerTile = ownerTile;
		this.type = type;

		switch(type) {
			case FRUIT:
				this.actions = new Action[] {new ActionHarvestFruit(this)};
				break;
			case FISH:
				this.actions = new Action[] {new ActionFishing(this)};
				break;
			case ANIMAL:
				this.actions = new Action[] {new ActionHunting(this)};
				break;
			case METAL:
				this.actions = new Action[] {new ActionMining(this)};
				break;
			case CROP:
				this.actions = new Action[] {new ActionFarming(this)};
				break;
			case WHALE:
				this.actions = new Action[] {new ActionWhaling(this)};
				break;
			
			default:
				this.actions = new Action[] {};		
		}
	}

	@Override
	public String toString() {
		return this.type.toString();
	}

}