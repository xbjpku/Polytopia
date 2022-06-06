package polytopia.gameplay;

import polytopia.graphics.Visualizable;
import polytopia.graphics.Motion;
import polytopia.graphics.Movable;
import polytopia.graphics.Render;
import polytopia.gameplay.Player.Tech;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Unit implements Visualizable, Movable {

	public enum Skill {
		CARRY, FLOAT, DASH, FORTIFY, ESCAPE, HEAL, CONVERT, SCOUT, PERSIST;
	}

	public enum UnitType {
		NONE (0, 0, 0, 0, 0, 0, null, false, null),
		ARCHER (3, 10, 2, 1, 1, 2, new Skill[]{Skill.DASH, Skill.FORTIFY}, true, Tech.ARCHERY), 
		BATTLESHIP (0, 0, 4, 3, 3, 2, new Skill[]{Skill.DASH, Skill.CARRY, Skill.SCOUT, Skill.FLOAT}, true, Tech.NAVIGATION),
		BOAT (0, 0, 1, 1, 2, 2, new Skill[]{Skill.CARRY, Skill.DASH, Skill.FLOAT}, true, null),
		CATAPULT (8, 10, 4, 0, 1, 3, new Skill[]{}, true, Tech.MATHEMATICS),
		DEFENDER (3, 15, 1, 3, 1, 1, new Skill[]{Skill.FORTIFY}, false, Tech.SHIELDS),
		GIANT (10, 40, 5, 4, 1, 1, new Skill[]{}, false, null),
		KNIGHT (8, 10, 3.5f, 1, 3, 1, new Skill[]{Skill.DASH, Skill.FORTIFY, Skill.PERSIST}, false, Tech.CHIVALRY), 
		MINDBENDER (5, 10, 0, 1, 1, 1, new Skill[]{Skill.HEAL, Skill.CONVERT}, false, Tech.PHILOSOPHY), 
		RIDER (3, 10, 2, 1, 2, 1, new Skill[]{Skill.DASH, Skill.FORTIFY, Skill.ESCAPE}, false, Tech.RIDING), 
		SHIP (0, 0, 2, 2, 3, 2, new Skill[]{Skill.DASH, Skill.CARRY, Skill.FLOAT}, true, Tech.SAILING),
		SWORDSMAN (5, 15, 3, 3, 1, 1, new Skill[]{Skill.DASH, Skill.FORTIFY}, true, Tech.SMITHERY), 
		WARRIOR (2, 10, 2, 2, 1, 1, new Skill[]{Skill.DASH, Skill.FORTIFY}, false, null);

		public final int cost;
		public final int maxHealth;
		public final float attack;
		public final int defense;
		public final int movement;
		public final int range;
		public final boolean isRanged;
		public final Skill[] skills; 
		public final Tech prerequisite;

		UnitType (int cost, int maxHealth, float attack, int defense, int movement, int range,
				Skill[] skills, boolean isRanged, Tech prerequisite) {
			this.cost = cost;
			this.maxHealth = maxHealth;
			this.attack = attack;
			this.defense = defense;
			this.movement = movement;
			this.range = range;
			this.skills = skills;
			this.isRanged = isRanged;
			this.prerequisite = prerequisite;
		}
	}
	

	// Attributes
	private int health;
	private UnitType type;
	public Skill[] skills;
	private Unit carryUnit = null;
	private Player ownerPlayer = null;
	private City ownerCity = null;

	private Tile position = null;

	private boolean movable = false;
	private boolean attackable = false;
	
	private int kills = 0;
	private boolean veteran = false;

	private boolean flipped = false;
	public boolean isFlipped() {return this.flipped;}
	public void setFlipped(boolean flipped) {this.flipped = flipped;}

	private Motion motion;
	public void setMotion(Motion motion) {this.motion = motion;}
	public Motion getMotion() {return this.motion;}
	// Selection response
	public void visualize() {

		long current = System.currentTimeMillis();
		Motion t = Motion.getInstanceOfMovableMotion(this, this.position, current, current + 100);
		Render.addMotion(t);
		this.setMotion(t);
		
        int size = Game.map.getSize();
		Action[][] actionMap = new Action[size][size];
		ArrayList<Action> actions = this.getActions();
		ArrayList<Action> visibleActions = new ArrayList<Action>();
		for (Action action : actions) {
			if (action.isVisibleTo(Game.getCurrentPlayer()))
				if (!(action instanceof ActionUnitMove) && !(action instanceof ActionUnitAttack) && !(action instanceof ActionUnitConvert))
					visibleActions.add(action);

			if (!action.isPerformableTo(Game.getHumanPlayer()))
				continue;

			if (action instanceof ActionUnitMove) {
				Tile dest = ((ActionUnitMove)(action)).getDestination();
				Render.setDecorationMap(dest.getY(), dest.getX(), Render.Decoration.UNIT_MOVE);
			}
			if (action instanceof ActionUnitAttack) {
				Tile dest = ((ActionUnitAttack)(action)).getDestination();
				Render.setDecorationMap(dest.getY(), dest.getX(), Render.Decoration.UNIT_ATTACK);
			}
			if (action instanceof ActionUnitConvert) {
				Tile dest = ((ActionUnitConvert)(action)).getDestination();
				Render.setDecorationMap(dest.getY(), dest.getX(), Render.Decoration.UNIT_ATTACK);
			}
		}
		Game.window.showActions(visibleActions);
	}

	public Unit(UnitType type, Player player) {
		this.type = type;
		this.health = type.maxHealth;
		this.skills = new Skill[this.type.skills.length];
		for (int i=0; i<this.type.skills.length; i++)
			this.skills[i] = this.type.skills[i];

		this.ownerPlayer = player;
		player.getUnits().add(this);
	}

	public Player getOwnerPlayer() {return this.ownerPlayer;}
	public void setOwnerPlayer(Player player) {this.ownerPlayer = player;}
	public City getOwnerCity() {return this.ownerCity;}
	public void setOwnerCity(City city) {this.ownerCity = city;}
	public UnitType getType() {return this.type;}
	public void setType(UnitType type) {this.type = type;}
	public Unit getCarryUnit() {return this.carryUnit;}
	public void setCarryUnit(Unit unit) {this.carryUnit = unit;}
	public Tile getPosition() {return this.position;}
	public void setPosition(Tile position) {this.position = position;}

	public boolean isMovable() {return this.movable;}
	public boolean isAttackable() {return this.attackable;}
	public void setMovable(boolean movable) {this.movable = movable;}
	public void setAttackable(boolean attackable) {this.attackable = attackable;}

	public int getKills() {return this.kills;}
	public void setKills(int kills) {this.kills = kills;}
	public boolean isVeteran() {return this.veteran;}
	public void setVeteran() {this.veteran = true;}

	public int getHealth() {return this.health;}
	public void setHealth(int health) {this.health = health;}

	public Skill[] getSkills() {
		return this.skills;
	}
	public boolean hasSkill(Skill skill) {
		for (Skill skill_ : this.skills) {
			if (skill_ == skill)
				return true;
		}
		return false;
	}

	public boolean isRanged() {
		return this.type.isRanged;
	}

	public boolean isNavy() {
		return this.type == UnitType.BOAT || this.type == UnitType.SHIP || this.type == UnitType.BATTLESHIP;
	}

	public int getMaxHealth() {
		if (this.carryUnit == null)
			return this.type.maxHealth + (this.veteran ? 5 : 0);
		else
			return this.carryUnit.type.maxHealth + (this.veteran ? 5 : 0);
	}
	public int getCost() {
		if (carryUnit != null)
			return carryUnit.type.cost;
		return type.cost;
	}
	public float getAttack() {
		return this.type.attack;
	}
	public int getDefense() {
		return this.type.defense;
	}
	public int getMovement() {
		return this.type.movement;
	}
	public int getRange() {
		return this.type.range;
	}

	public ArrayList<Tile> getMovableTiles() {
		ArrayList<Tile> destination = new ArrayList<>();

		class State {
			public Tile tile;
			public int move;
			public State(Tile tile, int move) {
				this.tile = tile;
				this.move = move;
			}
		}

		int [] dx = new int[]{0, 1, 0, -1, -1, -1, 1, 1};
		int [] dy = new int[]{-1, 0, 1, 0, -1, 1, -1, 1};

		LinkedList<State> q = new LinkedList<State>();
		q.add(new State(this.position, this.getMovement()));
		Tile[][] grid = Game.map.getGrid();
		while(!q.isEmpty()) {
			State state = q.remove();
			Tile tile = state.tile;
			int move = state.move;

			if (!destination.contains(tile))
				destination.add(tile);

			// out of moves
			if (move <= 0)
				continue;
			

			for(int i = 0; i < 8; i++) {
				int x = tile.getX() + dx[i];
				int y = tile.getY() + dy[i];
				if(!TileMap.isValid(grid, x, y))
					continue;
				if(grid[x][y].getUnit() != null)
					continue;
				
				// in enemy control zone
				boolean inControl = false;
				for(int j = 0; j < 8; j++) {
					int xx = x + dx[j];
					int yy = y + dy[j];
					if (TileMap.isValid(grid, xx, yy) && grid[xx][yy].hasEnemy(ownerPlayer))
						inControl = true;
					break;
				}

				// Need CLIMBING to go on mountains
				if (grid[x][y].getTerrainType() == Tile.TerrainType.MOUNTAIN 
					&& !ownerPlayer.getTechs().contains(Tech.CLIMBING))
					continue;
				// Need NAVIGATION to go on oceans
				if (grid[x][y].getTerrainType() == Tile.TerrainType.OCEAN 
					&& !ownerPlayer.getTechs().contains(Tech.NAVIGATION))
					continue;
				
				if (!this.isNavy()) {
					// Land unit
					if (grid[x][y].getTerrainType() == Tile.TerrainType.SHORE
						|| grid[x][y].getTerrainType() == Tile.TerrainType.OCEAN)
						// cannot move on SHORE/OCEAN, unless with friendly PORT on
						if (!(grid[x][y].getVariation() instanceof Improvement)
							|| ((Improvement)(grid[x][y].getVariation())).getImprovementType() != Improvement.ImprovementType.PORT
							|| grid[x][y].getOwnerCity().getOwnerPlayer() != ownerPlayer)
								continue;
					
					switch (grid[x][y].getTerrainType()) {
						case FIELD: q.add(new State(grid[x][y], inControl ? 0 : move-1)); break;
						case FOREST: q.add(new State(grid[x][y], inControl ? 0 : move-2)); break;
						case MOUNTAIN: q.add(new State(grid[x][y], inControl ? 0 : move-2)); break;
						case SHORE: q.add(new State(grid[x][y], 0)); break;
						case OCEAN: q.add(new State(grid[x][y], 0)); break;
					}
				}
				else {
					// Navy unit
					switch (grid[x][y].getTerrainType()) {
						case SHORE: q.add(new State(grid[x][y], inControl ? 0 : move-1)); break;
						case OCEAN: q.add(new State(grid[x][y], inControl ? 0 : move-1)); break;
						default: q.add(new State(grid[x][y], 0));
					}
				}

			}
		}
		return destination;
	}

	public ArrayList<Tile> searchEnemy() {
		ArrayList<Tile> accessibleEnemy = new ArrayList<>();
		for(Tile tile: TileMap.getSurroundings(Game.map.getGrid(), position.getX(), position.getY(), getRange()))
			if (tile.hasEnemy(ownerPlayer))
				accessibleEnemy.add(tile);
			
		return accessibleEnemy;
	}

	public float getDefenseBonus() {
		float bonus = 1;
		// Aquatism for water, Archery for forests and Meditation for mountains are defensive terrains
		if(position.isOwnedBy(ownerPlayer) && position.hasTemple()) {
			bonus *= 1.2F;
		}

		// In a friendly city
		if (position.getVariation() instanceof City
			&& ((City)(position.getVariation())).getOwnerPlayer() == ownerPlayer)
		{
			// Fortify
			if(this.hasSkill(Skill.FORTIFY)) 
				bonus *= 1.4F;
			// Has Wall
			if(((City)(position.getVariation())).hasWall())
				bonus *= 1.2F;
		} 
		
		return bonus;
	}

	public int getRecoveryRate() {
		int rate = 1;
		// On friendly territory
		if(position.isOwnedBy(ownerPlayer)) 
			rate = 2;
		return rate;
	}

	public ArrayList<Action> getActions() {
		ArrayList<Action> legalActions = new ArrayList<>();

		legalActions.add(new ActionCaptureValuableTile(this));

		if(type == UnitType.BOAT)
			legalActions.add(new ActionUpgradeBoat(this));
		
		if(type == UnitType.SHIP)
			legalActions.add(new ActionUpgradeShip(this));

		if(hasSkill(Skill.HEAL))
			legalActions.add(new ActionUnitHeal(this));
		
		if(!veteran && kills >= 3)
			legalActions.add(new ActionUnitUpgrade(this));
		
		if (this.getOwnerPlayer().getTechs().contains(Tech.FREE_SPIRIT))
			legalActions.add(new ActionUnitDisband(this));

		if (attackable && movable) {
			if(getHealth() < getMaxHealth())
				legalActions.add(new ActionUnitRecover(this));
		}

		if (attackable) {
			if(hasSkill(Skill.CONVERT)) {
				for(Tile tile: searchEnemy())
					legalActions.add(new ActionUnitConvert(this ,tile.getUnit()));
			}
			else {
				for(Tile tile: searchEnemy())
					legalActions.add(new ActionUnitAttack(this, tile.getUnit()));
			}
		}
		if (movable) {
			//System.out.println("hi");
			//System.out.printf("%d\n", getMovableTiles().size());
			for(Tile tile: getMovableTiles())
				legalActions.add(new ActionUnitMove(this, tile));
		}

		return legalActions;
	}

	public Action pickAction(Player player, Tile tile) {

		for (Action action : this.getActions()) {
			if (!action.isPerformableTo(player))
				continue;
			if (action instanceof ActionUnitMove) {
				Tile dest = ((ActionUnitMove)(action)).getDestination();
				if (dest == tile) 
					return action;
			}
			if (action instanceof ActionUnitAttack) {
				Tile dest = ((ActionUnitAttack)(action)).getDestination();
				if (dest == tile) 
					return action;
			}
			if (action instanceof ActionUnitConvert) {
				Tile dest = ((ActionUnitConvert)(action)).getDestination();
				if (dest == tile) 
					return action;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return String.join("-", type.toString(), ownerPlayer.getFaction().toString());
	}

}