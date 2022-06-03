package polytopia.gameplay;

import java.util.ArrayList;
import java.util.function.*;

import polytopia.graphics.Visualizable;
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



/**
The following are the consequences for units
*/

class ConseqUnitUpgrade extends Consequence {
	private Unit unit;

	public void visualize() {
		/* Note for Shaw */
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

class ConseqUnitRecover extends Consequence {
	private Unit unit;
	private int amount;

	public void visualize() {
		/* Note for Shaw */
	}

	public void apply() {
		unit.setHealth (Math.max(unit.getMaxHealth(), unit.getHealth() + amount));
		
	}

	public void log(ArrayList<Consequence> history) {
		// log this consequence
		history.add(this);
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
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
		/* Note for Shaw */
	}

	public void apply() {
		// Move to the destination
		Tile origin = unit.getPosition();
		origin.setUnit(null);
		destination.setUnit(unit);
		unit.setPosition(destination);

		// Set UNIT's attackable and movable
		boolean hasDash = false;
		Skill[] skills = unit.getSkills();
		for (int i=0; i<skills.length; i++)
			if (skills[i] == Unit.Skill.DASH) {
				hasDash = true;
				skills[i] = null;
				break;
			}
		
		boolean hasEnemy = !(unit.searchEnemy().isEmpty());

		if (hasDash && hasEnemy)
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
	}

	public void apply() {
		// Apply damage
		enemy.setHealth (enemy.getHealth() - attackResult);

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
				new ConseqUnitRetaliate(enemy, unit, defenseResult);
		}
	}

	public int getReward() {
		//TODO: Bot use this value for making decisions
		return 0;
	}

	public ConseqUnitAttack(Unit unit, Unit enemy) {
		this.unit = unit;
		this.enemy = enemy;

		// Calculate by formula:  https://frothfrenzy.github.io/polytopiacalculator/?short=
		Unit defender = enemy;
		float accelerator = 4.5F;
		float defenseBonus = defender.getDefenseBonus();
		float attackForce = unit.getAttack() * (unit.getHealth() / unit.getMaxHealth());
		float defenseForce = defender.getDefense() * (defender.getHealth() / defender.getMaxHealth()) * defenseBonus;
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
	}

	public void apply() {
		enemy.setHealth (enemy.getHealth() - defenseResult);
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
		return 0;
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
		return 0;
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
			return -10;
		else
			return 10;
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
		player.getUnits().add(boat);
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
		return 0;
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
		return 0;
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
		return 100;
	}

	public ConseqClaimValuableTile(Tile tile) {
		this.tile = tile;
	}

	@Override
	public String toString() {
		return String.format("[Claims %s]", tile.getVariation().toString());
	}
}

