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