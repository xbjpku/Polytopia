package polytopia.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import polytopia.gameplay.Player.Faction;
import polytopia.utils.RandomName;

public interface TileVariation {
	Tile getOwnerTile();
	Faction getStyle();
	Action[] getActions();
}

class Resource implements TileVariation {
	
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
			
			default:
				this.actions = new Action[] {};		
		}
	}

	@Override
	public String toString() {
		return this.type.toString();
	}

}

class Improvement implements TileVariation {

	private Tile ownerTile;
	private int level;

	public Tile getOwnerTile() {return this.ownerTile;}
	public Faction getStyle() {
		return null;
	}
	public int getLevel() {return this.level;}
	public void setLevel(int level) {this.level = level;}

	public enum ImprovementType {
		FARM (2), MINE (2), PORT (2), LUMBER_HUT (1), 
		WINDMILL (1), FORGE (2), SAWMILL (1), CUSTOMS_HOUSE (0), 
		TEMPLE (1), FOREST_TEMPLE (1), AQUA_TEMPLE (1), MOUNTAIN_TEMPLE (1);

		private int baseValue;
		ImprovementType(int baseValue) {this.baseValue = baseValue;}
		public int getBaseValue() {return this.baseValue;}
	}
	private ImprovementType type;
	public ImprovementType getImprovementType() {return this.type;}


	private Action[] actions;
	public Action[] getActions() {return this.actions;}

	public Improvement(Tile ownerTile, ImprovementType type, int level) {
		this.ownerTile = ownerTile;
		this.type = type;
		this.level = level;
		this.actions = new Action[] {new ActionDestroyImprovement(this)};
	}

	@Override
	public String toString() {
		return this.type.toString();
	}
}