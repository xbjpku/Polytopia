package polytopia.gameplay;

import java.util.ArrayList;
import java.util.function.*;

import polytopia.graphics.Visualizable;
import polytopia.gameplay.Player.Tech;

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
			// TODO: Implement Events.
			c.apply();
			idx++;
		}
	}
}

class ConseqRemoveResource extends Consequence {
	private Resource subject;

	public void visualize() {

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
		return 5;
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

class ConseqGrowPopulation extends Consequence {
	private Tile source;
	private City subject;
	private int value;

	public void visualize() {

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

	}

	public void apply() {
		int population = subject.getPopulation() + value;
		// Guaranteed that at most one upgrade each time
		if (population >= subject.getLevel() + 1) {
			population -= subject.getLevel() + 1;
			subject.setLevel(subject.getLevel() + 1);
		}
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
		return 30;
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

		/* Note for Shaw: 
			Tile Jump Animation.
			Make the tile (that SUBJECT is on) shake a bit. 
			BLOCKING: Wait for the animation to complete. */

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
		return -5;
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
		//TODO: Implements Event, so that a Dialog is shown here to inform
		//		human players about this consequence. It is even possible to
		//		insert new consequences with an event, which can be handled 
		//		in Consequence.apply().

		/* Note for Shaw: 
			Tile Press Animation.
			Press the tile (that SUBJECT is on) down (harder than the jump animation),
			then make it bounce back. 
			BLOCKING: Wait for the animation to complete. */
	}

	public void apply() {
		// GrowPopulation has already changed city level and population.

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
				// new ConseqUnitSpawn
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 50;
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

		/* Note for Shaw: 
			Stars Animation:
			Display a star texture (not included yet), and move it from SOURCE to upper
			middle of the *screen*, along a curve, accelerating. 
			NON-BLOCKING: Does not wait for the animation to complete. */

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

		/* Note for Shaw: 
			Fog Animation
			NON-BLOCKING */
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
		return 0;
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

/*
The following are the consequences for units
* */
class ConseqUpgrade extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) upgrades", unit.toString());
	}

	public void apply() {
		unit.Upgrade();
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s upgrades]", unit.toString());
	}

	public ConseqUpgrade(Unit unit) {
		this.unit = unit;
	}
}

class ConseqMove extends Consequence {
	private Unit unit;
	private Tile tile;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("Tile (%d, %d)", tile.getX(), tile.getY());
	}

	public void apply() {
		unit.Move(tile);
	}

	public void log(ArrayList<Consequence> history) {

		// log this consequence
		history.add(this);

		if((unit.position.getTerrainType() == Tile.TerrainType.OCEAN
				|| unit.position.getTerrainType() == Tile.TerrainType.SHORE)
			&& (tile.getTerrainType() != Tile.TerrainType.OCEAN
				&& tile.getTerrainType() != Tile.TerrainType.SHORE)) {
			new ConseqRemoveShell(unit).log(history);
		}

		if(unit.position.getTerrainType() != Tile.TerrainType.OCEAN
				&& unit.position.getTerrainType() != Tile.TerrainType.SHORE
				&& ((Improvement)tile.getVariation()).getImprovementType() == Improvement.ImprovementType.PORT) {
			new ConseqPutOnShell(unit).log(history);
		}

	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s moves to (%d, %d)]", unit.toString(), tile.getX(), tile.getY());
	}

	public ConseqMove(Unit unit, Tile tile) {
		this.unit = unit;
		this.tile = tile;
	}
}

class ConseqRemoveShell extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) lands", unit.toString());
	}

	public void apply() {
		unit.Land();
	}

	public void log(ArrayList<Consequence> history) {
		// No further consequences
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s lands]", unit.toString());
	}

	public ConseqRemoveShell(Unit unit) {
		this.unit = unit;
	}

}

class ConseqPutOnShell extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) docks", unit.toString());
	}

	public void apply() {
		unit.Carry();
	}

	public void log(ArrayList<Consequence> history) {
		// No further consequences
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s docks]", unit.toString());
	}

	public ConseqPutOnShell(Unit unit) {
		this.unit = unit;
	}

}

class ConseqAttackedOrDeath extends Consequence {
	private Unit unit;
	private Unit enemy;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) attacks (%s)", unit.toString(), enemy.toString());
	}

	public void apply() {
		unit.Attack(enemy.position);
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		float accelerator = 4.5F;
		float defenseBonus = unit.getDefenseBonus(enemy);
		float attackForce = unit.getAttackPower() * (unit.getHp() / unit.getHpMax());
		float defenseForce = unit.getDefendPower() * (enemy.getHp() / enemy.getHpMax()) * defenseBonus;
		float totalDamage = attackForce + defenseForce;
		int attackResult = Math.round((attackForce / totalDamage) * unit.getAttackPower() * accelerator);
		// if enemy is killed
		if(attackResult >= enemy.getHp()) {
			new ConseqDie(enemy).log(history);
			new ConseqMove(unit, enemy.position).log(history);
			return;
		}
		// otherwise gets injured and retaliates
		new ConseqInjured(enemy, attackResult).log(history);


		int defenseResult = Math.round((defenseForce / totalDamage) * enemy.getDefendPower() * accelerator);
		new ConseqRetaliate(enemy, unit, defenseResult).log(history);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s attacks (%s)]", unit.toString(), enemy.toString());
	}

	public ConseqAttackedOrDeath(Unit unit, Unit enemy) {
		this.unit = unit;
		this.enemy = enemy;
	}

}

class ConseqDie extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) gets killed", unit.toString());
	}

	public void apply() {unit.kill(unit.position);}

	public void log(ArrayList<Consequence> history) {
		// No further consequences
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s gets killed]", unit.toString());
	}

	public ConseqDie(Unit unit) {
		this.unit = unit;
	}
}

class ConseqInjured extends Consequence {
	private Unit unit;
	private int hurt;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) gets %d decrease in hp", unit.toString(), hurt);
	}

	public void apply() {unit.setHp(unit.getHp() - hurt);}

	public void log(ArrayList<Consequence> history) {
		// No further consequences
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s gets %d decrease in hp]", unit.toString(), hurt);
	}

	public ConseqInjured(Unit unit, int hurt) {
		this.unit = unit;
		this.hurt = hurt;
	}
}

class ConseqRetaliate extends Consequence {
	private Unit unit;
	private Unit enemy;
	private int hurt;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) retaliates on (%s) with defend power of %d", unit.toString(), enemy,toString(), hurt);
	}

	public void apply() {unit.setHp(unit.getHp() - hurt);}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);

		if(hurt >= enemy.getHp()) {
			new ConseqDie(enemy).log(history);
			return;
		}
		new ConseqInjured(enemy, hurt).log(history);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s retaliates on %s with defend power of %d]", unit.toString(), enemy,toString(), hurt);
	}

	public ConseqRetaliate(Unit unit, Unit enemy, int hurt) {
		this.unit = unit;
		this.enemy = enemy;
		this.hurt = hurt;
	}
}

class ConseqRecover extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) recovers", unit.toString());
	}

	public void apply() {
		unit.Recover();
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s recovers]", unit.toString());
	}

	public ConseqRecover(Unit unit) {
		this.unit = unit;
	}
}

class ConseqHeal extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) heals", unit.toString());
	}

	public void apply() {
		unit.Heal();
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s heals]", unit.toString());
	}

	public ConseqHeal(Unit unit) {
		this.unit = unit;
	}
}

class ConseqConvert extends Consequence {
	private Unit unit;

	public void visualize() {
		//TODO: graphics and stuff
		System.out.printf("(%s) converts enemy", unit.toString());
	}

	public void apply() {
		unit.Convert();
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	@Override
	public String toString() {
		return String.format("[%s converts enemy]", unit.toString());
	}

	public ConseqConvert(Unit unit) {
		this.unit = unit;
	}
}