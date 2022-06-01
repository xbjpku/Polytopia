package polytopia.gameplay;

import java.util.ArrayList;

import polytopia.graphics.Visualizable;
import polytopia.gameplay.Player.Tech;

public abstract class Action implements Visualizable{

	public abstract void visualize();

	/* Whether PLAYER can see this ACTION. */
	public abstract boolean isVisibleTo(Player player);

	/* Whether PLAYER can perform this ACTION. */
	public abstract boolean isPerformableTo(Player player);

	/* Return the consequences of this ACTION. 
	   Note that this method does not apply the consequences. */
	public abstract ArrayList<Consequence> getConsequences(Player player);

	/* Apply this ACTION. */
	public abstract void apply(Player player);


}

/* Specific in-game actions. */

class ActionHarvestFruit extends Action {
	
	private Resource subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Harvest Fruit");

	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has ORGANIZATION tech
		return player.getTechs().contains(Tech.ORGANIZATION)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has ORGANIZATION tech and has at least 2 stars,
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		return this.isVisibleTo(player) 
				&& player.getStars() >= 2
				&& subject.getOwnerTile().isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Removal of resource
		new ConseqRemoveResource(subject).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject.getOwnerTile(), subject.getOwnerTile().getOwnerCity(), 1).log(history);
		
		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 2);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Harvest Fruit";
	}

	public ActionHarvestFruit(Resource subject) {this.subject = subject;}
}

class ActionFishing extends Action {
	
	private Resource subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Fishing");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has FISHING tech
		return player.getTechs().contains(Tech.FISHING)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has FISHING tech and has at least 2 stars,
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		return this.isVisibleTo(player) 
				&& player.getStars() >= 2
				&& subject.getOwnerTile().isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Removal of resource
		new ConseqRemoveResource(subject).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject.getOwnerTile(), subject.getOwnerTile().getOwnerCity(), 1).log(history);
		
		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 2);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Fishing";
	}

	public ActionFishing(Resource subject) {this.subject = subject;}

}

class ActionHunting extends Action {
	
	private Resource subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Hunting");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has HUNTING tech
		return player.getTechs().contains(Tech.HUNTING)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has HUNTING tech and has at least 2 stars,
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		return this.isVisibleTo(player) 
				&& player.getStars() >= 2
				&& subject.getOwnerTile().isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Removal of resource
		new ConseqRemoveResource(subject).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject.getOwnerTile(), subject.getOwnerTile().getOwnerCity(), 1).log(history);
		
		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 2);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Hunting";
	}

	public ActionHunting(Resource subject) {this.subject = subject;}

}

class ActionFarming extends Action {
	
	private Resource subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Farming");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has FARMING tech
		return player.getTechs().contains(Tech.FARMING)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has FARMING tech and has at least 5 stars,
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		return this.isVisibleTo(player) 
				&& player.getStars() >= 5
				&& subject.getOwnerTile().isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject.getOwnerTile(), Improvement.ImprovementType.FARM, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject.getOwnerTile(), subject.getOwnerTile().getOwnerCity(), 
								Improvement.ImprovementType.FARM.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Farming";
	}

	public ActionFarming(Resource subject) {this.subject = subject;}

}

class ActionMining extends Action {
	
	private Resource subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Mining");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has MINING tech
		return player.getTechs().contains(Tech.MINING)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has MINING tech and has at least 5 stars,
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		return this.isVisibleTo(player) 
				&& player.getStars() >= 5
				&& subject.getOwnerTile().isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject.getOwnerTile(), Improvement.ImprovementType.MINE, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject.getOwnerTile(), subject.getOwnerTile().getOwnerCity(),
								Improvement.ImprovementType.MINE.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Mining";
	}

	public ActionMining(Resource subject) {this.subject = subject;}

}

class ActionWhaling extends Action {
	
	private Resource subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Whaling");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has WHALING tech
		return player.getTechs().contains(Tech.WHALING)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has WHALING tech,
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		return this.isVisibleTo(player) 
				&& subject.getOwnerTile().isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Removal of resource
		new ConseqRemoveResource(subject).log(history);
		// Gain of stars
		new ConseqGainStars(subject.getOwnerTile(), player, 10).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Whaling";
	}

	public ActionWhaling(Resource subject) {this.subject = subject;}
}

class ActionBuildLumberHut extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Lumber Hut");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has FORESTRY tech, and SUBJECT is FOREST.
		return subject.getTerrainType() == Tile.TerrainType.FOREST
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.FORESTRY)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has FORESTRY tech, and at least 2 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is FOREST.
		return this.isVisibleTo(player) 
				&& player.getStars() >= 2
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.LUMBER_HUT, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject, subject.getOwnerCity(), 
								Improvement.ImprovementType.LUMBER_HUT.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 2);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Lumber Hut";
	}

	public ActionBuildLumberHut(Tile subject) {this.subject = subject;}

}

class ActionBuildPort extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Port");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has SAILING tech, and SUBJECT is SHORE.
		return subject.getTerrainType() == Tile.TerrainType.SHORE
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.SAILING)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has SAILING tech, and at least 10 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is SHORE.
		return this.isVisibleTo(player) 
				&& player.getStars() >= 10
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.PORT, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject, subject.getOwnerCity(),
								 Improvement.ImprovementType.PORT.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 10);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Port";
	}

	public ActionBuildPort(Tile subject) {this.subject = subject;}

}

class ActionBuildSawmill extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Sawmill");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has MATHEMATICS tech, and SUBJECT is FIELD,
		if (!player.getTechs().contains(Tech.MATHEMATICS)
			|| !(subject.getVariation() == null || subject.getVariation() instanceof Resource)
			|| subject.getTerrainType() != Tile.TerrainType.FIELD
			|| !subject.isOwnedBy(player))
			return false;
		
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.LUMBER_HUT)
			return true;
		
		return false;
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has MATHEMATICS tech, and SUBJECT is FIELD, and SUBJECT has adjacent LUMBER_HUT
		// and if PLAYER has at least 5 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		if (this.isVisibleTo(player) 
			&& player.getStars() >= 5
			&& subject.isAccessibleTo(player))
			return true;
		return false;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Sawmill";
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		int level = 0;
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.LUMBER_HUT)
			level++;

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.SAWMILL, level).log(history);
		// Growth of population (-> Upgrade of city)
		while (level-- > 0)
			new ConseqGrowPopulation(subject, subject.getOwnerCity(), 
									Improvement.ImprovementType.SAWMILL.getBaseValue()).log(history);

		return history;
	}

	public ActionBuildSawmill(Tile subject) {this.subject = subject;}

}

class ActionBuildForge extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Forge");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has SMITHERY tech, and SUBJECT is FIELD
		if (!player.getTechs().contains(Tech.SMITHERY) 
			|| !(subject.getVariation() == null || subject.getVariation() instanceof Resource)
			|| subject.getTerrainType() != Tile.TerrainType.FIELD
			|| !subject.isOwnedBy(player))
			return false;
		
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.MINE)
			return true;
		
		return false;
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has SMITHERY tech, and SUBJECT is FIELD, and SUBJECT has adjacent MINE
		// and if PLAYER has at least 5 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		if (this.isVisibleTo(player) 
			&& player.getStars() >= 5
			&& subject.isAccessibleTo(player))
			return true;
		return false;
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		int level = 0;
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.MINE)
			level++;

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.FORGE, level).log(history);
		// Growth of population (-> Upgrade of city)
		while (level-- > 0)
			new ConseqGrowPopulation(subject, subject.getOwnerCity(),
									Improvement.ImprovementType.FORGE.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Forge";
	}

	public ActionBuildForge(Tile subject) {this.subject = subject;}

}

class ActionBuildWindmill extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Windmill");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has CONSTRUCTION tech, and SUBJECT is FIELD
		if (!player.getTechs().contains(Tech.CONSTRUCTION) 
			|| !(subject.getVariation() == null || subject.getVariation() instanceof Resource)
			|| subject.getTerrainType() != Tile.TerrainType.FIELD
			|| !subject.isOwnedBy(player))
			return false;
		
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.FARM)
			return true;
		
		return false;
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has CONSTRUCTION tech, and SUBJECT is FIELD, and SUBJECT has adjacent FARM
		// and if PLAYER has at least 5 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		if (this.isVisibleTo(player) 
			&& player.getStars() >= 5
			&& subject.isAccessibleTo(player))
			return true;
		return false;
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		int level = 0;
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.FARM)
			level++;

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.WINDMILL, level).log(history);
		// Growth of population (-> Upgrade of city)
		while (level-- > 0)
			new ConseqGrowPopulation(subject, subject.getOwnerCity(), 
									Improvement.ImprovementType.WINDMILL.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Windmill";
	}

	public ActionBuildWindmill(Tile subject) {this.subject = subject;}

}

class ActionBuildCustomsHouse extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Customs House");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has TRADE tech, and SUBJECT is FIELD
		if (!player.getTechs().contains(Tech.TRADE) 
			|| !(subject.getVariation() == null || subject.getVariation() instanceof Resource)
			|| subject.getTerrainType() != Tile.TerrainType.FIELD
			|| !subject.isOwnedBy(player))
			return false;
		
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.PORT)
			return true;
		
		return false;
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has TRADE tech, and SUBJECT is FIELD, and SUBJECT has adjacent PORT
		// and if PLAYER has at least 5 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit
		if (this.isVisibleTo(player) 
			&& player.getStars() >= 5
			&& subject.isAccessibleTo(player))
			return true;
		return false;
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		int level = 0;
		ArrayList<Tile> adjTile = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
		for (Tile t : adjTile)
			if (t.isOwnedBy(player)
				&& t.getVariation() instanceof Improvement
				&& ((Improvement)(t.getVariation())).getImprovementType() == Improvement.ImprovementType.PORT)
			level++;

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.CUSTOMS_HOUSE, level).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Customs House";
	}

	public ActionBuildCustomsHouse(Tile subject) {this.subject = subject;}

}

class ActionBuildTemple extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Temple");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has FREE_SPIRIT tech, and SUBJECT is SHORE.
		return subject.getTerrainType() == Tile.TerrainType.FIELD
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.FREE_SPIRIT)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has FREE_SPIRIT tech, and at least 10 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is FIELD.
		return this.isVisibleTo(player) 
				&& player.getStars() >= 10
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.TEMPLE, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject, subject.getOwnerCity(),
								 Improvement.ImprovementType.TEMPLE.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 10);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Temple";
	}

	public ActionBuildTemple(Tile subject) {this.subject = subject;}
}

class ActionBuildForestTemple extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Forest Temple");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has SPIRITUALISM tech, and SUBJECT is FOREST.
		return subject.getTerrainType() == Tile.TerrainType.FOREST
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.SPIRITUALISM)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has SPIRITUALISM tech, and at least 10 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is FOREST.
		return this.isVisibleTo(player) 
				&& player.getStars() >= 10
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.FOREST_TEMPLE, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject, subject.getOwnerCity(),
								 Improvement.ImprovementType.FOREST_TEMPLE.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 10);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Forest Temple";
	}

	public ActionBuildForestTemple(Tile subject) {this.subject = subject;}
}

class ActionBuildAquaTemple extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Aqua Temple");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has AQUATISM tech, and SUBJECT is SHORE/OCEAN.
		return (subject.getTerrainType() == Tile.TerrainType.SHORE || subject.getTerrainType() == Tile.TerrainType.OCEAN)
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.AQUATISM)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has AQUATISM tech, and at least 10 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is SHORE/OCEAN.
		return this.isVisibleTo(player) 
				&& player.getStars() >= 10
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.AQUA_TEMPLE, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject, subject.getOwnerCity(),
								 Improvement.ImprovementType.AQUA_TEMPLE.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 10);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Aqua Temple";
	}

	public ActionBuildAquaTemple(Tile subject) {this.subject = subject;}
}

class ActionBuildMountainTemple extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Mountain Temple");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has MEDITATION tech, and SUBJECT is MOUNTAIN.
		return subject.getTerrainType() == Tile.TerrainType.MOUNTAIN 
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.MEDITATION)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has MEDITATION tech, and at least 10 stars
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is MOUNTAIN.
		return this.isVisibleTo(player) 
				&& player.getStars() >= 10
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Construction of improvement (-> Growth of population -> Upgrade of city)
		new ConseqBuildImprovement(subject, Improvement.ImprovementType.MOUNTAIN_TEMPLE, 1).log(history);
		// Growth of population (-> Upgrade of city)
		new ConseqGrowPopulation(subject, subject.getOwnerCity(),
								 Improvement.ImprovementType.MOUNTAIN_TEMPLE.getBaseValue()).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 10);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Build Mountain Temple";
	}

	public ActionBuildMountainTemple(Tile subject) {this.subject = subject;}
}

class ActionDestroyImprovement extends Action {
	
	private Improvement subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Destroy");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has CONSTRUCTION tech
		return player.getTechs().contains(Tech.CONSTRUCTION)
				&& subject.getOwnerTile().isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has CONSTRUCTION tech
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		return this.isVisibleTo(player) 
				&& subject.getOwnerTile().isAccessibleTo(player);
	}


	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Destruction of improvement (-> Loss of population)
		new ConseqRemoveImprovement(subject).log(history);
		// Loss of population 
		new ConseqLosePopulation(subject.getOwnerTile().getOwnerCity(), 
								subject.getImprovementType().getBaseValue() * subject.getLevel()).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Destroy " + subject.toString();
	}

	public ActionDestroyImprovement(Improvement subject) {this.subject = subject;}

}

class ActionClearForest extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Clear Forest");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has FREE_SPIRIT tech, and SUBJECT is FOREST.
		return subject.getTerrainType() == Tile.TerrainType.FOREST
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.FREE_SPIRIT)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has FREE_SPIRIT tech
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is FOREST.
		return this.isVisibleTo(player) 
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Change of terrain
		new ConseqChangeTerrain(subject, Tile.TerrainType.FIELD).log(history);
		// Gain of stars
		new ConseqGainStars(subject, player, 2).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Clear Forest";
	}

	public ActionClearForest(Tile subject) {this.subject = subject;}

}

class ActionGrowForest extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Grow Forest");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has SPIRITUALISM tech, and SUBJECT is FIELD.
		return subject.getTerrainType() == Tile.TerrainType.FIELD
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.SPIRITUALISM)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has SPIRITUALISM tech
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is FIELD.
		return this.isVisibleTo(player) 
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Change of terrain
		new ConseqChangeTerrain(subject, Tile.TerrainType.FOREST).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Grow Forest";
	}

	public ActionGrowForest(Tile subject) {this.subject = subject;}

}

class ActionBurnForest extends Action {
	
	private Tile subject;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Burn Forest");
	}

	public boolean isVisibleTo(Player player) {
		// if PLAYER has CHIVALRY tech, and SUBJECT is FOREST.
		return subject.getTerrainType() == Tile.TerrainType.FOREST
				&& (subject.getVariation() == null || subject.getVariation() instanceof Resource)
				&& player.getTechs().contains(Tech.CHIVALRY)
				&& subject.isOwnedBy(player);
	}

	public boolean isPerformableTo(Player player) {
		// if PLAYER has CHIVALRY tech
		// and if SUBJECT is owned by PLAYER, and is not occupied by enemy unit, 
		// and is FOREST.
		return this.isVisibleTo(player) 
				&& subject.isAccessibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Change of terrain
		new ConseqChangeTerrain(subject, Tile.TerrainType.FIELD).log(history);
		// Add CROP
		new ConseqRestoreResource(subject, Resource.ResourceType.CROP).log(history);

		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - 5);
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Burn Forest";
	}

	public ActionBurnForest(Tile subject) {this.subject = subject;}
}

class ActionUnlockTech extends Action {
	
	private Tech tech;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("Unlock " + tech.toString());
	}

	public boolean isVisibleTo(Player player) {
		return tech.isUnlockableTo(player);
	}

	public boolean isPerformableTo(Player player) {
		return this.isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {
		
		ArrayList<Consequence> history = new ArrayList<Consequence>();
		new ConseqUnlockTech(tech, player).log(history);
		
		return history;
	}

	public void apply(Player player) {
		player.setStars(player.getStars() - tech.getCost(player));
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return "Unlock " + tech.toString();
	}

	public ActionUnlockTech(Tech tech) {this.tech = tech;}
}

/*
The following are the actions for units
* */
class ActionUpgrade extends Action {

	private Unit unit;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("ActionUpgrade");
	}

	public boolean isVisibleTo(Player player) {
		return unit.recover == 1 && unit.attack == 1 && unit.movable == 1 && unit.killNumber == 3;
	}

	public boolean isPerformableTo(Player player) {
		return this.isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Upgrade
		new ConseqUpgrade(unit).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return unit.toString() + " upgrades";
	}

	public ActionUpgrade(Unit unit) {this.unit = unit;}
}

class ActionMove extends Action {

	private Unit unit;
	private Tile destination;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("ActionMove");
	}

	public boolean isVisibleTo(Player player) {
		return (unit.recover == 1) && (unit.movable != -1) && ((unit.movable == 1 && unit.attack == 1)
				|| (unit.movable == 0 && unit.attack == 0 && unit.Escape));
	}

	public boolean isPerformableTo(Player player) {
		return this.isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Carry or land
		new ConseqMove(unit, destination).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return unit.toString() + " move to " + destination.toString();
	}

	public ActionMove(Unit unit, Tile destination) {this.unit = unit; this.destination = destination;}
}

class ActionAttack extends Action {

	private Unit unit;
	private Unit enemy;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("ActionAttack");
	}

	public boolean isVisibleTo(Player player) {
		return unit.recover == 1 && unit.attack == 1 && unit.searchEnemy().contains(enemy)
				&& ((unit.movable == 0 && unit.Dash) || (unit.movable == 1));
	}

	public boolean isPerformableTo(Player player) {
		return isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Lost in health or death
		new ConseqAttackedOrDeath(unit, enemy).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return unit.toString() + " attack " + enemy.toString();
	}

	public ActionAttack(Unit unit,Unit enemy) {this.unit = unit; this.enemy = enemy;}
}

class ActionRecover extends Action {

	private Unit unit;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("ActionRecover");
	}

	public boolean isVisibleTo(Player player) {
		return (unit.recoverable()) && (unit.attack == 1) && (unit.movable == 1) && (unit.recover == 1);
	}

	public boolean isPerformableTo(Player player) {
		return this.isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Recover
		new ConseqRecover(unit).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return unit.toString() + " recovers";
	}

	public ActionRecover(Unit unit) {this.unit = unit;}
}

class ActionHeal extends Action {

	private Unit unit;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("ActionHeal");
	}

	public boolean isVisibleTo(Player player) {
		return unit.Heal && (unit.attack == 1) && (unit.movable == 1) && (unit.recover == 1);
	}

	public boolean isPerformableTo(Player player) {
		return this.isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Heal
		new ConseqHeal(unit).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return unit.toString() + " heals";
	}

	public ActionHeal(Unit unit) {this.unit = unit;}
}

class ActionConvert extends Action {

	private Unit unit;

	public void visualize() {
		//TODO: GUI and stuff
		System.out.println("ActionConvert");
	}

	public boolean isVisibleTo(Player player) {
		return unit.Convert && (unit.attack == 1) && (unit.movable == 1) && (unit.recover == 1);
	}

	public boolean isPerformableTo(Player player) {
		return this.isVisibleTo(player);
	}

	public ArrayList<Consequence> getConsequences(Player player) {

		ArrayList<Consequence> history = new ArrayList<Consequence>();
		// Convert
		new ConseqConvert(unit).log(history);

		return history;
	}

	public void apply(Player player) {
		Consequence.apply (this.getConsequences(player));
	}

	@Override
	public String toString() {
		return unit.toString() + " converts";
	}

	public ActionConvert(Unit unit) {this.unit = unit;}
}


// Disband units
// (death of units -> gain of stars)

// Train unit
// (spawn unit)
// Capture village
// (capture village -> discover tiles)
// Capture city
// (capture city -> discover tiles)
// Explore ruins
// (random)

// TurnStart
// (gain stars -> unit rest)
// TurnEnd
// ()

// new conseq: spawn unit; capture city; capture village; unit rest