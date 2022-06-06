package polytopia.gameplay;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.*;
import java.awt.Color;

import polytopia.graphics.Visualizable;
import polytopia.graphics.Motion;
import polytopia.graphics.Render;
import polytopia.graphics.Motion.MotionType;
import polytopia.gameplay.Player.Tech;
import polytopia.gameplay.Unit.Skill;

public abstract class Consequence implements Visualizable {

	public abstract void visualize();
	public abstract void apply();
	public abstract void log(ArrayList<Consequence> history);
	public abstract int getReward();

	public static void apply(ArrayList<Consequence> history) {
		int idx = 0;
		while (idx < history.size()) {
			Consequence c = history.get(idx);
			c.visualize();
			c.apply();
			idx++;
		}
	}
}

class ConseqRemoveResource extends Consequence {
	private Resource subject;

	public void visualize() {

		if (!Game.getHumanPlayer().getVision().contains(subject.getOwnerTile()))
			return;

		/* Note for Shaw: 
			Fog Animation 
			(use the FOG texture, make a pattern like this:
				O	O
				 \ /
				 / \
				O	O
			where “O” is a small fog texture. Make this pattern expand a bit, while fading.)
			Around 0.5s, centered at the tile.
			NON-BLOCKING: Does not wait for the animation to complete. */
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject.getOwnerTile(), current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		Tile tile = subject.getOwnerTile();
		tile.setVariation(null);
	}

	public void log(ArrayList<Consequence> history) {
		// No further consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return -1;
	}

	public ConseqRemoveResource(Resource subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		return String.format("[Remove Resource (%s)]", subject.toString());
	}
}

class ConseqRestoreResource extends Consequence {
	private Tile subject;
	private Resource.ResourceType type;

	public void visualize() {

		/* Note for Shaw: 
			No animation needed. */
	}

	public void apply() {
		subject.setVariation(new Resource(subject, type));
	}

	public void log(ArrayList<Consequence> history) {
		// No further consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	public ConseqRestoreResource(Tile subject, Resource.ResourceType type) {
		this.subject = subject;
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("[Restore Resource (%s)]", type.toString());
	}
}

class ConseqBuildImprovement extends Consequence {
	private Tile subject;
	private Improvement.ImprovementType type;
	private int level;

	public void visualize() {

		if (!Game.getHumanPlayer().getVision().contains(subject))
			return;

		/* Note for Shaw: 
			Fog Animation 
			(use the FOG texture, make a pattern like this:
				O	O
				 \ /
				 / \
				O	O
			where “O” is a small fog texture. Make this pattern expand a bit, while fading.)
			Around 0.5s, centered at the tile. 
			NON-BLOCKING: Does not wait for the animation to complete.*/
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		subject.setVariation(new Improvement(subject, type, level));

		Consumer<Improvement.ImprovementType> process =
		(adjType) -> {
			ArrayList<Tile> adj = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
			for (Tile tile : adj) {
				if (tile.getVariation() instanceof Improvement
					&& tile.getOwnerCity().getOwnerPlayer() == subject.getOwnerCity().getOwnerPlayer()
					&& ((Improvement)(tile.getVariation())).getImprovementType() == adjType)
				{
					Improvement improvement = (Improvement)(tile.getVariation());
					improvement.setLevel (improvement.getLevel() + 1);
				}
			}
		};
		switch (type) {
			case FARM: process.accept(Improvement.ImprovementType.WINDMILL); break;
			case MINE: process.accept(Improvement.ImprovementType.FORGE); break;
			case LUMBER_HUT: process.accept(Improvement.ImprovementType.SAWMILL); break;
			case PORT: process.accept(Improvement.ImprovementType.CUSTOMS_HOUSE); break;
		}
	}

	public void log(ArrayList<Consequence> history) {
		// Can cause RemoveResource, if subject has a resource
		if (subject.getVariation() instanceof Resource) {
			Consequence newConseq = new ConseqRemoveResource((Resource)(subject.getVariation()));
			newConseq.log(history);
		}

		// log this consequence
		history.add(this);

		// Can cause GrowPopulation, if adjacent Improvements are upgraded
		Consumer<Improvement.ImprovementType> process =
		(adjType) -> {
			ArrayList<Tile> adj = TileMap.getInnerRing(Game.map.getGrid(), subject.getX(), subject.getY());
			for (Tile tile : adj) {
				if (tile.getVariation() instanceof Improvement
					&& tile.getOwnerCity().getOwnerPlayer() == subject.getOwnerCity().getOwnerPlayer()
					&& ((Improvement)(tile.getVariation())).getImprovementType() == adjType)
				{
					Improvement improvement = (Improvement)(tile.getVariation());
					if (adjType.getBaseValue() > 0) {
						Consequence newConseq = new ConseqGrowPopulation(tile, tile.getOwnerCity(), adjType.getBaseValue());
						newConseq.log(history);
					}
				}
			}
		};
		switch (type) {
			case FARM: process.accept(Improvement.ImprovementType.WINDMILL); break;
			case MINE: process.accept(Improvement.ImprovementType.FORGE); break;
			case LUMBER_HUT: process.accept(Improvement.ImprovementType.SAWMILL); break;
			case PORT: process.accept(Improvement.ImprovementType.CUSTOMS_HOUSE); break;
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 1;
	}

	public ConseqBuildImprovement(Tile subject, Improvement.ImprovementType type, int level) {
		this.subject = subject;
		this.type = type;
		this.level = level;
	}

	@Override
	public String toString() {
		return String.format("[Build Improvement (%s, %d)]", type.toString(), level);
	}
}

class ConseqRemoveImprovement extends Consequence {
	private Improvement subject;

	public void visualize() {
		
		if (!Game.getHumanPlayer().getVision().contains(subject.getOwnerTile()))
			return;

		/* Note for Shaw: 
			Fog Animation 
			(use the FOG texture, make a pattern like this:
				O	O
				 \ /
				 / \
				O	O
			where “O” is a small fog texture. Make this pattern expand a bit, while fading.)
			Around 0.5s, centered at the tile.
			NON-BLOCKING: Does not wait for the animation to complete. */
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject.getOwnerTile(), current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		Tile tile = subject.getOwnerTile();
		subject.getOwnerTile().setVariation(null);

		Consumer<Improvement.ImprovementType> process =
		(adjType) -> {
			ArrayList<Tile> adj = TileMap.getInnerRing(Game.map.getGrid(), tile.getX(), tile.getY());
			for (Tile t : adj) {
				if (t.getVariation() instanceof Improvement
					&& t.getOwnerCity().getOwnerPlayer() == tile.getOwnerCity().getOwnerPlayer()
					&& ((Improvement)(t.getVariation())).getImprovementType() == adjType)
				{
					Improvement improvement = (Improvement)(t.getVariation());
					improvement.setLevel (improvement.getLevel() - 1);
				}
			}
		};
		switch (subject.getImprovementType()) {
			case FARM: process.accept(Improvement.ImprovementType.WINDMILL); break;
			case MINE: process.accept(Improvement.ImprovementType.FORGE); break;
			case LUMBER_HUT: process.accept(Improvement.ImprovementType.SAWMILL); break;
			case PORT: process.accept(Improvement.ImprovementType.CUSTOMS_HOUSE); break;
		}
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Can cause RestoreResource, if subject is built on one
		if (subject.getImprovementType() == Improvement.ImprovementType.FARM) {
			Consequence newConseq = new ConseqRestoreResource(subject.getOwnerTile(), Resource.ResourceType.CROP);
			newConseq.log(history);
		} else if (subject.getImprovementType() == Improvement.ImprovementType.MINE) {
			Consequence newConseq = new ConseqRestoreResource(subject.getOwnerTile(), Resource.ResourceType.METAL);
			newConseq.log(history);
		}

		// Can cause LosePopulation, if adjacent Improvements are downgraded
		Consumer<Improvement.ImprovementType> process =
		(adjType) -> {
			Tile thisTile = subject.getOwnerTile();
			ArrayList<Tile> adj = TileMap.getInnerRing(Game.map.getGrid(), thisTile.getX(), thisTile.getY());
			for (Tile tile : adj) {
				if (tile.getVariation() instanceof Improvement
					&& tile.getOwnerCity().getOwnerPlayer() == thisTile.getOwnerCity().getOwnerPlayer()
					&& ((Improvement)(tile.getVariation())).getImprovementType() == adjType)
				{
					Improvement improvement = (Improvement)(tile.getVariation());
					if (adjType.getBaseValue() > 0) {
						Consequence newConseq = new ConseqLosePopulation(tile.getOwnerCity(), adjType.getBaseValue());
						newConseq.log(history);
					}
				}
			}
		};
		switch (subject.getImprovementType()) {
			case FARM: process.accept(Improvement.ImprovementType.WINDMILL); break;
			case MINE: process.accept(Improvement.ImprovementType.FORGE); break;
			case LUMBER_HUT: process.accept(Improvement.ImprovementType.SAWMILL); break;
			case PORT: process.accept(Improvement.ImprovementType.CUSTOMS_HOUSE); break;
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return -5;
	}

	public ConseqRemoveImprovement(Improvement subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		return String.format("[Remove Improvement (%s)]", subject.toString());
	}
}

class ConseqCaptureVillage extends Consequence {
	private Player player;
	private Tile subject;

	public void visualize() {

		if (!Game.getHumanPlayer().getVision().contains(subject))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		City city = new City(Game.map.getGrid(), subject, player);
		subject.setVariation(city);
		player.addCity(city);

		// transfer unit to the new city
		Unit unit = subject.getUnit();
		City oldCity = unit.getOwnerCity();
		if (oldCity != null) 
			oldCity.getUnits().remove(unit);
		city.getUnits().add(unit);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 100;
	}

	public ConseqCaptureVillage(Player player, Tile subject) {
		this.player = player;
		this.subject = subject;
	}

	@Override
	public String toString() {
		return String.format("[Captures Village (%d, %d)]", subject.getX(), subject.getY());
	}
}

class ConseqCaptureCity extends Consequence {
	private Player player;
	private Tile subject;

	public void visualize() {

		if (!Game.getHumanPlayer().getVision().contains(subject))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		City city = (City)(subject.getVariation());
		Player oldPlayer = city.getOwnerPlayer();

		for (Unit unit : city.getUnits()) {
			unit.setOwnerCity(null);
			if (unit.getCarryUnit() != null)
				unit.getCarryUnit().setOwnerCity(null);
		}
		city.getUnits().clear();

		oldPlayer.getCities().remove(city);
		player.getCities().add(city);

		city.setOwnerPlayer(player);

		// transfer unit to the new city
		Unit unit = subject.getUnit();
		City oldCity = unit.getOwnerCity();
		if (oldCity != null) 
			oldCity.getUnits().remove(unit);
		city.getUnits().add(unit);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Can cause Discover Tile
		City city = (City)(subject.getVariation());
		for (Tile tile : city.getTerritory()) 
			if (!player.getVision().contains(tile))
				new ConseqDiscoverTile(tile, player).log(history);


		// NOTE: This implementation is flawed: if capturing a city changes the
		// level of adjacency-based improvements, ConseqGrowPopulation and 
		// ConseqLosePopulation should be chained as well. However, that is relatively
		// minor, and we hold it for now.

		// Can cause EndOfGame
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 100;
	}

	public ConseqCaptureCity(Player player, Tile subject) {
		this.player = player;
		this.subject = subject;
	}

	@Override
	public String toString() {
		return String.format("[Captures City (%d, %d)]", subject.getX(), subject.getY());
	}
}

class ConseqExploreRuins extends Consequence {
	private Player player;
	private Tile subject;
	private String prompt = null;

	public void visualize() {

		if (!Game.getHumanPlayer().getVision().contains(subject))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		subject.setVariation(null);

		// TODO: Inform player by PROMPT
		System.out.println(prompt);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// The Random Reward is logged as subsequent consequences.

		/* List of rewards:
		 * [0] Gain 10 Stars
		 * [1] Random tech
		 * [2] Gain 3 population for capital
		 * [3] Spawn a giant
		 */
		Random rnd = new Random((int)System.currentTimeMillis());
		reroll:
		switch (rnd.nextInt(4)) {
			case 0: 
				new ConseqGainStars(subject, player, 10).log(history);
				prompt = "Gain Stars";
				break;
			case 1: 
				ArrayList<Tech> techs = Tech.getUnlockableTechs(player);
				if (techs.size() == 0)
					break reroll;
				Tech tech = techs.get(rnd.nextInt(techs.size()));
				new ConseqUnlockTech(tech, player).log(history);
				prompt = "Unlock " + tech.toString();
				break;
			case 2:
				new ConseqGrowPopulation(subject, player.getCapital(), 1).log(history);
				new ConseqGrowPopulation(subject, player.getCapital(), 1).log(history);
				new ConseqGrowPopulation(subject, player.getCapital(), 1).log(history);
				prompt = "Grow Population";
				break;
			case 3:
				if (subject.getTerrainType() == Tile.TerrainType.SHORE ||
					subject.getTerrainType() == Tile.TerrainType.OCEAN)
					break reroll;
				new ConseqUnitSpawn (subject, Unit.UnitType.GIANT, player, null).log(history);
				prompt = "Spawn Giant";
				break;
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 100;
	}

	public ConseqExploreRuins(Player player, Tile subject) {
		this.player = player;
		this.subject = subject;
	}

	@Override
	public String toString() {
		return String.format("[Explore Ruins]", subject.getX(), subject.getY());
	}
}

class ConseqGrowPopulation extends Consequence {
	private Tile source;
	private City subject;
	private int value;

	public void visualize() {

		if (Game.getHumanPlayer() != Game.getCurrentPlayer())
			return;

		/* Note for Shaw: 
			Population Animation
			(use the Faction texture, like /resources/Xinxi/Xinxi.png, which is not yet added
			to Texture.java. You need to add some entries to Texture.XML, and use getTextureByName()
			to get that texture.
			Make that texture move from SOURCE to SUBJECT (the tile it is on), along a curve.
			BLOCKING: Wait for the animation to complete.

			(Then) 
			Tile Jump Animation.
			Make the tile (that SUBJECT is on) shake (down and up) a bit. 
			NON-BLOCKING: Does not wait for the animation to complete. */
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("POPULATION-" + source.getOwnerCity().getOwnerPlayer().getFaction().toString(),
			source, source.getOwnerCity().getOwnerTile(),  current, current + 500);
		Render.addMotion(t);
		
		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}

		current = System.currentTimeMillis();
		t = Motion.getInstanceOfMovableMotion(subject.getOwnerTile(), subject.getOwnerTile(), current, current + 200);
		Render.addMotion(t);
		subject.getOwnerTile().setMotion(t);

		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}
	}

	public void apply() {
		int population = subject.getPopulation() + value;
		subject.setPopulation(population);
	}

	public void log(ArrayList<Consequence> history) {

		// Calculate population growth so far
		int population = subject.getPopulation();
		int level = subject.getLevel();
		for (Consequence c : history) {
			if (c instanceof ConseqGrowPopulation) 
				population += ((ConseqGrowPopulation)(c)).getValue();
			if (c instanceof ConseqLosePopulation)
				population -= ((ConseqLosePopulation)(c)).getValue();
			
			if (population >= level + 1) {
				population -= level + 1;
				level += 1;
			}
		}

		// log this consequence
		history.add(this);

		// Can cause UpgradeCity, if population growth reaches threshold
		population += value;
		if (population >= level + 1) {
			// upgrade indeed
			new ConseqUpgradeCity(subject, level + 1).log(history);
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 2*value + (subject.getLevel() < 3 ? 1 : 0);
	}

	public int getValue() {return this.value;}

	public ConseqGrowPopulation(Tile source, City subject, int value) {
		this.source = source;
		this.subject = subject;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("[Grow Population (%s, %d)]", subject.toString(), value);
	}
}

class ConseqLosePopulation extends Consequence {
	private City subject;
	private int value;

	public void visualize() {

		if (Game.getHumanPlayer() != Game.getCurrentPlayer())
			return;

		/* Note for Shaw: 
			Tile Jump Animation.
			Make the tile (that SUBJECT is on) shake a bit. 
			BLOCKING: Wait for the animation to complete. */
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfMovableMotion(subject.getOwnerTile(), subject.getOwnerTile(), current, current + 200);
		Render.addMotion(t);
		subject.getOwnerTile().setMotion(t);
		
		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}

	}

	public void apply() {
		subject.setPopulation(subject.getPopulation() - value);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return -2*value;
	}

	public int getValue() {return this.value;}

	public ConseqLosePopulation(City subject, int value) {
		this.subject = subject;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("[Lose Population (%s, %d)]", subject.toString(), value);
	}
}

class ConseqUpgradeCity extends Consequence {
	private City subject;
	private int newLevel;

	public void visualize() {

		if (Game.getHumanPlayer() != Game.getCurrentPlayer())
			return;

		//TODO: Implements Event, so that a Dialog is shown here to inform
		//		human players about this consequence. It is even possible to
		//		insert new consequences with an event, which can be handled 
		//		in Consequence.apply().

		/* Note for Shaw: 
			Tile Press Animation.
			Press the tile (that SUBJECT is on) down (harder than the jump animation),
			then make it bounce back. 
			BLOCKING: Wait for the animation to complete. */
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfMovableMotion(subject.getOwnerTile(), subject.getOwnerTile(), current, current + 400);
		t.setMotionType(MotionType.PRESSED);
		Render.addMotion(t);
		subject.getOwnerTile().setMotion(t);
		
		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}
	}

	public void apply() {
		// Guaranteed that at most one upgrade each time
		int population = subject.getPopulation();
		if (population >= subject.getLevel() + 1) {
			population -= subject.getLevel() + 1;
		}
		subject.setPopulation(population);
		subject.setLevel(subject.getLevel() + 1);

		switch (newLevel) {
			case 0:
			case 1:
				// should not happen; do nothing
				break;
			case 2:
				// Add workshop
				subject.addWorkshop();
				break;
			case 3:
				break;
			case 4:
				Tile center = subject.getOwnerTile();
				for (Tile tile : TileMap.getOuterRing(Game.map.getGrid(), center.getX(), center.getY())) {
					// claim tiles that are not yet claimed
					if (tile.getOwnerCity() == null) {
						tile.setOwnerCity(subject);
						subject.getTerritory().add(tile);
					}
				}
				break;
			default:
		}
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Upgrades have different effects depending on newLevel
		switch (newLevel) {
			case 0:
			case 1:
				// should not happen; do nothing
				break;
			case 2:
				// Add workshop; not a consequence though
				break;
			case 3:
				// Gain 5 stars
				new ConseqGainStars(subject.getOwnerTile(), subject.getOwnerPlayer(), 5).log(history);
				break;
			case 4:
				// Expand territory; can cause Discovery of Tile
				Tile center = subject.getOwnerTile();
				Player player = subject.getOwnerPlayer();
				for (Tile tile : TileMap.getOuterRing(Game.map.getGrid(), center.getX(), center.getY())) {
					// will discover the outer ring, if still undiscovered
					if (!player.getVision().contains(tile)) {
						new ConseqDiscoverTile(tile, player).log(history);
					}
						
				}
				break;
			default:
				// Get a Gaint unit
				new ConseqUnitSpawn (subject.getOwnerTile(), Unit.UnitType.GIANT, 
									subject.getOwnerPlayer(), null).log(history);
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return newLevel;
	}

	public ConseqUpgradeCity(City subject, int newLevel) {
		this.subject = subject;
		this.newLevel = newLevel;
	}

	@Override
	public String toString() {
		return String.format("[Upgrade City (%s, %d)]", subject.toString(), newLevel);
	}
}

class ConseqGainStars extends Consequence {
	private Tile source;
	private Player subject;
	private int amount;

	public void visualize() {

		if (Game.getHumanPlayer() != Game.getCurrentPlayer())
			return;

		/* Note for Shaw: 
			Stars Animation:
			Display a star texture (not included yet), and move it from SOURCE to upper
			middle of the *screen*, along a curve, accelerating. 
			NON-BLOCKING: Does not wait for the animation to complete. */
		new Thread(()->{

			for (int i = 0; i < amount; i++) {
				long current = System.currentTimeMillis();
				Motion t = Motion.getInstanceOfTextureMotion("STAR", source,  current, current + 5000);
				Render.addMotion(t);
				try {Thread.sleep(100);} catch(Exception e){}
			}
			
		}).start();

	}

	public void apply() {
		subject.setStars(subject.getStars() + amount);
	}

	public void log(ArrayList<Consequence> history) {
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return amount;
	}

	public ConseqGainStars(Tile source, Player subject, int amount) {
		this.source = source;
		this.subject = subject;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return String.format("[Gain Stars (%d)]", amount);
	}
}

class ConseqChangeTerrain extends Consequence {
	private Tile subject;
	private Tile.TerrainType type;

	public void visualize() {

		if (!Game.getHumanPlayer().getVision().contains(subject))
			return;

		/* Note for Shaw: 
			Fog Animation
			NON-BLOCKING */
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		subject.setTerrainType(type);
	}

	public void log(ArrayList<Consequence> history) {
		// Can cause RemoveResource, if subject has resource
		if (subject.getVariation() instanceof Resource) {
			Resource.ResourceType rtype = ((Resource)(subject.getVariation())).getResourceType();
			if (!(rtype == Resource.ResourceType.RUINS || rtype == Resource.ResourceType.VILLAGE)) {
				new ConseqRemoveResource((Resource)(subject.getVariation())).log(history);
			}
		}

		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	public ConseqChangeTerrain(Tile subject, Tile.TerrainType type) {
		this.subject = subject;
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("[Change Terrain (%s)]", type.toString());
	}
}

class ConseqUnlockTech extends Consequence {
	private Tech tech;
	private Player player;

	public void visualize() {
		/* Note for Shaw: 
			Nothing to be done. */
		
		// TODO: Hint
	}

	public void apply() {
		player.addTech(tech);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	public ConseqUnlockTech(Tech tech, Player player) {
		this.tech = tech;
		this.player = player;
	}

	@Override
	public String toString() {
		return String.format("[Unlock tech %s]", tech.toString());
	}
}

class ConseqDiscoverTile extends Consequence {
	private Tile subject;
	private Player player;

	public void visualize() {
		/* Note for Shaw: 
			Fog Animation
			NON-BLOCKING */
		
		if(player != Game.getHumanPlayer())
			return;
			
		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", subject, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		player.addVision(subject);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// TODO: encountering enemy unit
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 1;
	}

	public ConseqDiscoverTile(Tile subject, Player player) {
		this.subject = subject;
		this.player = player;
	}

	@Override
	public String toString() {
		return String.format("[Discover (%d, %d)]", subject.getX(), subject.getY());
	}
}



/**
The following are the consequences for units
*/

class ConseqUnitUpgrade extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", unit.getPosition(), current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {

		unit.setVeteran();
		unit.setHealth(unit.getMaxHealth());

	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 100;
	}

	public ConseqUnitUpgrade(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.format("[%s upgrades]", unit.toString());
	}
}

class ConseqUpgradeBoat extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", unit.getPosition(), current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		unit.setType(Unit.UnitType.SHIP);
		unit.skills = new Skill[ Unit.UnitType.SHIP.skills.length];
		for (int i=0; i<unit.skills.length; i++) {
			unit.skills[i] = Unit.UnitType.SHIP.skills[i];
		}
		
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 5;
	}

	public ConseqUpgradeBoat(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.format("[%s upgrades to ship]", unit.toString());
	}
}

class ConseqUpgradeShip extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", unit.getPosition(), current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		unit.setType(Unit.UnitType.BATTLESHIP);
		unit.skills = new Skill[ Unit.UnitType.BATTLESHIP.skills.length];
		for (int i=0; i<unit.skills.length; i++) {
			unit.skills[i] = Unit.UnitType.BATTLESHIP.skills[i];
		}
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Can cause Discover Tile
		int range = 2;
		Player player = unit.getOwnerPlayer();
		Tile position = unit.getPosition();
		for (Tile tile : TileMap.getSurroundings(Game.map.getGrid(), position.getX(), position.getY(), range)) {
			if (!player.getVision().contains(tile)) {
				new ConseqDiscoverTile(tile, player).log(history);
			}		
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 20;
	}

	public ConseqUpgradeShip(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.format("[%s upgrades to battleship]", unit.toString());
	}
}


class ConseqUnitRecover extends Consequence {
	private Unit unit;
	private int amount;

	public void visualize() {
		/* Note for Shaw */
		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		int value = Math.min((unit.getMaxHealth() - unit.getHealth()), amount);
		if (value > 0) {
			long current = System.currentTimeMillis();
			Motion t = Motion.getInstanceOfStringMotion(String.valueOf(value), unit.getPosition(), current, current + 500, Color.CYAN);
			Render.addMotion(t);
		}
	}

	public void apply() {
		unit.setHealth (Math.min(unit.getMaxHealth(), unit.getHealth() + amount));
		
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 1;
	}

	public ConseqUnitRecover(Unit unit, int amount) {
		this.unit = unit;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return String.format("[%s recovers by %d]", unit.toString(), amount);
	}
}

class ConseqUnitMove extends Consequence {
	private Unit unit;
	private Tile destination;

	public void visualize() {
		
		if (destination.getY()-unit.getPosition().getY() > destination.getX()-unit.getPosition().getX())
			unit.setFlipped(false);
		if (destination.getY()-unit.getPosition().getY() < destination.getX()-unit.getPosition().getX())
			unit.setFlipped(true);

		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		long current = System.currentTimeMillis();
		long elapsed = TileMap.getDistance(unit.getPosition(), destination)*100;
		Motion t = Motion.getInstanceOfMovableMotion(unit, unit.getPosition(), destination, current, current + elapsed);
		Render.addMotion(t);
		unit.setMotion(t);
		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}
	}

	public void apply() {
		// Move to the destination
		Tile origin = unit.getPosition();
		origin.setUnit(null);
		destination.setUnit(unit);
		unit.setPosition(destination);

		// Set UNIT's attackable and movable
		boolean hasDash = false;
		boolean hasPersist = false;
		Skill[] skills = unit.getSkills();
		for (int i=0; i<skills.length; i++) {
			if (skills[i] == Unit.Skill.DASH) {
				hasDash = true;
				skills[i] = null;
			}
			if (skills[i] == Unit.Skill.PERSIST)
				hasPersist = true;
		}
		
		boolean hasEnemy = !(unit.searchEnemy().isEmpty());

		if (hasDash && hasEnemy)
			unit.setAttackable(true);
		else if (hasPersist && hasEnemy)
			unit.setAttackable(true);
		else
			unit.setAttackable(false);

		unit.setMovable(false);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Can cause claim of City/Village/Ruins
		if (destination.getVariation() instanceof Resource) {
			if (((Resource)(destination.getVariation())).getResourceType() == Resource.ResourceType.VILLAGE
				|| ((Resource)(destination.getVariation())).getResourceType() == Resource.ResourceType.RUINS)
				new ConseqClaimValuableTile(destination).log(history);
		}
		else if (destination.getVariation() instanceof City) {
			if (((City)(destination.getVariation())).getOwnerPlayer() != unit.getOwnerPlayer())
				new ConseqClaimValuableTile(destination).log(history);
		}

		// Can cause Carry/Land
		if (unit.isNavy() 
			&& destination.getTerrainType() != Tile.TerrainType.SHORE
			&& destination.getTerrainType() != Tile.TerrainType.OCEAN)
			new ConseqUnitLand(unit).log(history);
		
		if (!unit.isNavy()
			&& destination.getTerrainType() == Tile.TerrainType.SHORE)
			new ConseqUnitCarry(unit).log(history);

		// Can cause Discover of Tile
		int range = 1;
		if (destination.getTerrainType() == Tile.TerrainType.MOUNTAIN)
			range = 2;
		if (unit.hasSkill(Skill.SCOUT))
			range = 2;

		Player player = unit.getOwnerPlayer();
		for (Tile tile : TileMap.getSurroundings(Game.map.getGrid(), destination.getX(), destination.getY(), range)) {
			if (!player.getVision().contains(tile)) {
				new ConseqDiscoverTile(tile, player).log(history);
			}		
		}

	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	public ConseqUnitMove(Unit unit, Tile destination) {
		this.unit = unit;
		this.destination = destination;
	}

	@Override
	public String toString() {
		return String.format("[%s Move to (%d, %d)]", unit.toString(), destination.getX(), destination.getY());
	}
}

class ConseqUnitAttack extends Consequence {
	private Unit unit;
	private Unit enemy;
	private int attackResult;
	private int defenseResult;

	public void visualize() {
		/* Note for Shaw */

		Tile unitTile = unit.getPosition();
		Tile enemyTile = enemy.getPosition();

		if (enemyTile.getY()-unitTile.getY() > enemyTile.getX()-unitTile.getX()) {
			unit.setFlipped(false);
			enemy.setFlipped(true);
		}
		if (enemyTile.getY()-unitTile.getY() < enemyTile.getX()-unitTile.getX()) {
			unit.setFlipped(true);
			enemy.setFlipped(false);
		}

		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		long current = System.currentTimeMillis();
		Motion t;
		switch(unit.getType()) {
			case ARCHER:
			case BOAT:
			case SHIP:
				t = Motion.getInstanceOfTextureMotion("ARROW", unit.getPosition(), enemy.getPosition(),  current, current + 500);
				t.setMotionType(MotionType.TRANSLATE); 
				break;
			case CATAPULT:
			case BATTLESHIP:
				t = Motion.getInstanceOfTextureMotion("BULLET", unit.getPosition(), enemy.getPosition(),  current, current + 500);
				break;
			default: 
				t = Motion.getInstanceOfTextureMotion("SWORD", unit.getPosition(), enemy.getPosition(),  current, current + 300);
		}
		Render.addMotion(t);
		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}

		current = System.currentTimeMillis();
		t = Motion.getInstanceOfStringMotion(String.valueOf(attackResult), enemy.getPosition(), current, current + 500, Color.RED);
		Render.addMotion(t);

	}

	public void apply() {
		// Apply damage
		enemy.setHealth (enemy.getHealth() - attackResult);
		if (enemy.getHealth() <= 0)
			unit.setKills(unit.getKills()+1);

		// Set UNIT's attackable and movable
		boolean hasEscape = false;
		boolean hasPersist = false;
		Skill[] skills = unit.getSkills();
		for (int i=0; i<skills.length; i++) {
			if (skills[i] == Unit.Skill.ESCAPE) {
				hasEscape = true;
				skills[i] = null;
			}
			if (skills[i] == Unit.Skill.PERSIST) {
				hasPersist = true;
			}
		}

		if (hasEscape)
			unit.setMovable(true);
		else
			unit.setMovable(false);
		
		if (hasPersist && enemy.getHealth() <= 0)
			unit.setAttackable(true);
		else
			unit.setAttackable(false);

	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		if (enemy.getHealth() <= attackResult) {

			new ConseqUnitDeath(enemy, unit.getOwnerPlayer()).log(history);

			// Try moving
			Tile destination = enemy.getPosition();
			Player player = unit.getOwnerPlayer();

			// Need CLIMBING to go on mountains
			if (destination.getTerrainType() == Tile.TerrainType.MOUNTAIN 
				&& !player.getTechs().contains(Tech.CLIMBING))
				return;
			// Don't move if attack is ranged
			if (unit.isRanged())
				return;
			// Don't move if enemy is on SHORE/OCEAN, without friendly PORT
			if (destination.getTerrainType() == Tile.TerrainType.SHORE
				|| destination.getTerrainType() == Tile.TerrainType.OCEAN)
				if (!(destination.getVariation() instanceof Improvement)
					|| ((Improvement)(destination.getVariation())).getImprovementType() != Improvement.ImprovementType.PORT
					|| destination.getOwnerCity().getOwnerPlayer() != player)
					return;
			
			new ConseqUnitMove (unit, destination).log(history);
		}
		else {
			// retaliate if within range, has attack, and in discovered tile
			Player player = enemy.getOwnerPlayer();
			Tile from = enemy.getPosition();
			Tile to = unit.getPosition();

			if (enemy.getAttack() > 0
				&& TileMap.getDistance(from, to) <= enemy.getRange()
				&& player.getVision().contains(to))
				new ConseqUnitRetaliate(enemy, unit, defenseResult).log(history);
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return attackResult / 2;
	}

	public ConseqUnitAttack(Unit unit, Unit enemy) {
		this.unit = unit;
		this.enemy = enemy;

		// Calculate by formula:  https://frothfrenzy.github.io/polytopiacalculator/?short=
		Unit defender = enemy;
		float accelerator = 4.5F;
		float defenseBonus = defender.getDefenseBonus();
		float attackForce = unit.getAttack() * (1.0F * unit.getHealth() / unit.getMaxHealth());
		float defenseForce = defender.getDefense() * (1.0F * defender.getHealth() / defender.getMaxHealth()) * defenseBonus;
		float totalDamage = attackForce + defenseForce;
		
		attackResult = Math.round((attackForce / totalDamage) * unit.getAttack() * accelerator);
		defenseResult = Math.round((defenseForce / totalDamage) * defender.getDefense() * accelerator);
	}

	@Override
	public String toString() {
		return String.format("[%s attacks %s, atk:%d]", unit.toString(), enemy.toString(), 
														attackResult);
	}
}

class ConseqUnitRetaliate extends Consequence {
	private Unit unit;
	private Unit enemy;
	private int defenseResult;

	public void visualize() {
		/* Note for Shaw */

		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		long current = System.currentTimeMillis();
		Motion t;
		switch(unit.getType()) {
			case ARCHER:
			case BOAT:
			case SHIP:
				t = Motion.getInstanceOfTextureMotion("ARROW", unit.getPosition(), enemy.getPosition(),  current, current + 500);
				t.setMotionType(MotionType.TRANSLATE); 
				break;
			case CATAPULT:
			case BATTLESHIP:
				t = Motion.getInstanceOfTextureMotion("BULLET", unit.getPosition(), enemy.getPosition(),  current, current + 500);
				break;
			default: 
				t = Motion.getInstanceOfTextureMotion("SWORD", unit.getPosition(), enemy.getPosition(),  current, current + 300);
		}
		Render.addMotion(t);
		synchronized(t){
			try{
				t.wait();
			}catch(Exception e){}
		}

		current = System.currentTimeMillis();
		t = Motion.getInstanceOfStringMotion(String.valueOf(defenseResult), enemy.getPosition(), current, current + 500, Color.RED);
		Render.addMotion(t);
	}

	public void apply() {
		enemy.setHealth (enemy.getHealth() - defenseResult);
		if (enemy.getHealth() <= 0)
			unit.setKills(unit.getKills()+1);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		if (enemy.getHealth() <= defenseResult) {
			new ConseqUnitDeath(enemy, enemy.getOwnerPlayer()).log(history);
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return defenseResult / 2;
	}

	public ConseqUnitRetaliate(Unit unit, Unit enemy, int defenseResult) {
		this.unit = unit;
		this.enemy = enemy;
		this.defenseResult = defenseResult;
	}

	@Override
	public String toString() {
		return String.format("[%s retaliates %s, def:%d]", unit.toString(), enemy.toString(), 
															defenseResult);
	}
}

class ConseqUnitConvert extends Consequence {
	private Unit unit;
	private Unit enemy;

	public void visualize() {
		/* Note for Shaw */

		Tile unitTile = unit.getPosition();
		Tile enemyTile = enemy.getPosition();

		if (!Game.getHumanPlayer().getVision().contains(unit.getPosition()))
			return;

		if (enemyTile.getY()-unitTile.getY() > enemyTile.getX()-unitTile.getX()) {
			unit.setFlipped(false);
			enemy.setFlipped(true);
		}
		if (enemyTile.getY()-unitTile.getY() < enemyTile.getX()-unitTile.getX()) {
			unit.setFlipped(true);
			enemy.setFlipped(false);
		}

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", enemy.getPosition(), current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {
		Player newOwner = unit.getOwnerPlayer();
		Player oldOwner = enemy.getOwnerPlayer();
		City oldCity = enemy.getOwnerCity();

		oldOwner.getUnits().remove(enemy);
		enemy.setOwnerPlayer(newOwner);
		newOwner.getUnits().add(enemy);
		if (oldCity != null) {
			oldCity.getUnits().remove(enemy);
		}
		enemy.setOwnerCity(null);

		if (enemy.getCarryUnit() != null) {
			enemy.getCarryUnit().setOwnerPlayer(newOwner);
			enemy.getCarryUnit().setOwnerCity(null);
		}
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Can cause Discover Tile
		int range = 1;
		Tile destination = enemy.getPosition();
		if (destination.getTerrainType() == Tile.TerrainType.MOUNTAIN)
			range = 2;
		if (enemy.hasSkill(Skill.SCOUT))
			range = 2;

		Player player = unit.getOwnerPlayer();
		for (Tile tile : TileMap.getSurroundings(Game.map.getGrid(), destination.getX(), destination.getY(), range)) {
			if (!player.getVision().contains(tile)) {
				new ConseqDiscoverTile(tile, player).log(history);
			}		
		}

		// Can cause Claim of city/vill/ruins
		if (destination.getVariation() instanceof Resource) {
			if (((Resource)(destination.getVariation())).getResourceType() == Resource.ResourceType.VILLAGE
				|| ((Resource)(destination.getVariation())).getResourceType() == Resource.ResourceType.RUINS)
				new ConseqClaimValuableTile(destination).log(history);
		}
		else if (destination.getVariation() instanceof City) {
			new ConseqClaimValuableTile(destination).log(history);
		}

	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return enemy.getCost() * 2;
	}

	public ConseqUnitConvert(Unit unit, Unit enemy) {
		this.unit = unit;
		this.enemy = enemy;
	}

	@Override
	public String toString() {
		return String.format("[%s converts %s]", unit.toString(), enemy.toString());
	}
}

class ConseqUnitDeath extends Consequence {
	private Unit unit;
	private Player killer;

	public void visualize() {
		/* Note for Shaw */
	}

	public void apply() {
		Player player = unit.getOwnerPlayer();
		City city = unit.getOwnerCity();
		Tile position = unit.getPosition();

		position.setUnit(null);
		player.getUnits().remove(unit);
		if (city != null)
			city.getUnits().remove(unit);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// TODO: Can cause claim city
		Tile position = unit.getPosition();
		if (position.getVariation() instanceof City
			&& unit.getOwnerPlayer() != killer
			&& ((City)(position.getVariation())).getOwnerPlayer() == killer)
			new ConseqClaimValuableTile(position).log(history);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		if (killer == unit.getOwnerPlayer())
			return -unit.getCost()*2;
		else
			return unit.getCost()*2;
	}

	public ConseqUnitDeath(Unit unit, Player killer) {
		this.unit = unit;
		this.killer = killer;
	}

	@Override
	public String toString() {
		return String.format("[%s dies]", unit.toString());
	}
}

class ConseqUnitCarry extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
		// No animation
	}

	public void apply() {
		Player player = unit.getOwnerPlayer();
		City city = unit.getOwnerCity();
		Tile position = unit.getPosition();

		Unit boat = new Unit(Unit.UnitType.BOAT, player);
		boat.setCarryUnit(unit);
		boat.setHealth (unit.getHealth());
		boat.setKills (unit.getKills());
		if (unit.isVeteran())
			boat.setVeteran();

		player.getUnits().remove(unit);
		boat.setOwnerCity(city);
		if (city != null) {
			city.getUnits().remove(unit);
			city.getUnits().add(boat);
		}
		
		boat.setAttackable(false);
		boat.setMovable(false);

		boat.setPosition(position);
		position.setUnit(boat);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return Math.min(Game.getTurn() / 10, 3);
	}

	public ConseqUnitCarry(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.format("[%s gets on boat]", unit.toString());
	}
}

class ConseqUnitLand extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
		// No animation
	}

	public void apply() {
		Player player = unit.getOwnerPlayer();
		City city = unit.getOwnerCity();
		Tile position = unit.getPosition();

		Unit passenger = unit.getCarryUnit();

		passenger.setHealth (unit.getHealth());
		passenger.setKills (unit.getKills());
		if (unit.isVeteran())
			passenger.setVeteran();

		player.getUnits().remove(unit);
		player.getUnits().add(passenger);
		if (city != null) {
			city.getUnits().remove(unit);
			city.getUnits().add(passenger);
		}
		
		passenger.setAttackable(false);
		passenger.setMovable(false);

		passenger.setPosition(position);
		position.setUnit(passenger);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		if (unit.getType() == Unit.UnitType.SHIP)
			return -2 + Math.min(2, Game.getTurn() / 20);
		if (unit.getType() == Unit.UnitType.BATTLESHIP)
			return -5 + Math.min(5, Game.getTurn() / 20);
		return 0;
	}

	public ConseqUnitLand(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.format("[%s lands]", unit.toString());
	}
}

class ConseqUnitRest extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
		// No animation
	}

	public void apply() {

		unit.setMovable(true);
		unit.setAttackable(true);

		Unit.Skill[] skills = unit.getType().skills;
		for (int i = 0; i < skills.length; i++) {
			unit.getSkills()[i] = skills[i];
		}
		
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		// Can cause UnitRecover
		if(unit.isAttackable() && unit.isMovable())
			new ConseqUnitRecover(unit, 2*unit.getRecoveryRate()).log(history);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	public ConseqUnitRest(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.format("[%s rests]", unit.toString());
	}
}

class ConseqUnitSpawn extends Consequence {
	private Player player;
	private City city;
	private Tile tile;
	private Unit.UnitType type;

	public void visualize() {
		/* Note for Shaw */
		if (!Game.getHumanPlayer().getVision().contains(tile))
			return;

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfTextureMotion("FOG", tile, current, current + 400);
		Render.addMotion(t);
	}

	public void apply() {

		Unit newUnit = new Unit(type, player);
		newUnit.setOwnerCity(city);
		if (city != null)
			city.getUnits().add(newUnit);
		
		newUnit.setPosition(tile);
		tile.setUnit(newUnit);
		
	}

	public void log(ArrayList<Consequence> history) {

		// Can cause UnitMove/ UnitDeath
		if (tile.getUnit() != null) {
			Unit oldUnit = tile.getUnit();
			Tile destination = null;
			Tile[][] grid = Game.map.getGrid();

			for (Tile t : TileMap.getInnerRing(grid, tile.getX(), tile.getY())) {
				// Need CLIMBING to go on mountains
				if (t.getTerrainType() == Tile.TerrainType.MOUNTAIN 
					&& !oldUnit.getOwnerPlayer().getTechs().contains(Tech.CLIMBING))
					continue;
				// Need NAVIGATION to go on oceans
				if (t.getTerrainType() == Tile.TerrainType.OCEAN 
					&& !oldUnit.getOwnerPlayer().getTechs().contains(Tech.NAVIGATION))
					continue;
				
				if (!oldUnit.isNavy()) {
					// Land unit
					if (t.getTerrainType() == Tile.TerrainType.SHORE
						|| t.getTerrainType() == Tile.TerrainType.OCEAN)
						// cannot move on SHORE/OCEAN, unless with friendly PORT on
						if (!(t.getVariation() instanceof Improvement)
							|| ((Improvement)(t.getVariation())).getImprovementType() != Improvement.ImprovementType.PORT
							|| t.getOwnerCity().getOwnerPlayer() != oldUnit.getOwnerPlayer())
								continue;
				}
				destination = t;
				break;
			}

			if (destination != null)
				new ConseqUnitMove (oldUnit, destination).log(history);
			else
				new ConseqUnitDeath (oldUnit, player).log(history);
		}

		// log this consequence
		history.add(this);
		
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		boolean preferRanged = true;
		int preferDefense = 0;
		for (Tile t : TileMap.getSurroundings(Game.map.getGrid(), tile.getX(), tile.getY(), 2))
			if (t.hasEnemy(player)) {
				preferRanged = false;
				preferDefense++;
			}


		switch (type) {
			case WARRIOR:
				return 2 + Math.max((20 - Game.getTurn())/20, 0);
			case ARCHER:
				return 2 + (preferRanged ? 1 : 0);
			case CATAPULT:
				return 6 + (preferRanged ? 0 : -4);
			case SWORDSMAN:
				return 4;
			case RIDER:
				return 4 - preferDefense/2;
			case MINDBENDER:
				return 5 - preferDefense;
			case KNIGHT:
				return 6 - preferDefense/2;
			case DEFENDER:
				return 2 + preferDefense;
			case GIANT:
				return 10;
			default:
				return 0;
		}
	}

	public ConseqUnitSpawn(Tile tile, Unit.UnitType type, Player player, City city) {
		this.tile = tile;
		this.type = type;
		this.player = player;
		this.city = city;
	}

	@Override
	public String toString() {
		return String.format("[%s spawns at (%d, %d)]", type.toString(), tile.getX(), tile.getY());
	}
}

/* For better guiding unit movement. */
class ConseqClaimValuableTile extends Consequence {
	private Tile tile;

	public void visualize() {
		/* Empty */
	}

	public void apply() {
		/* Empty */
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		//This should be worth a lot of points
		return 20;

	}

	public ConseqClaimValuableTile(Tile tile) {
		this.tile = tile;
	}

	@Override
	public String toString() {
		return String.format("[Claims %s]", tile.getVariation().toString());
	}
}

