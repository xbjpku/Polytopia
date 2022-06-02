package polytopia.gameplay;

import polytopia.graphics.Visualizable;
import polytopia.gameplay.Player.Tech;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Unit implements Visualizable {

	public enum Skill {
		CARRY, FLOAT, DASH, FORTIFY, ESCAPE, HEAL, CONVERT, SCOUT;
	}

	public enum UnitType {
		NONE (0, 0, 0, 0, 0, 0, null),
		ARCHER (3, 10, 2, 1, 1, 2, new Skill[]{Skill.DASH, Skill.FORTIFY}), 
		BATTLESHIP (0, 0, 4, 3, 3, 2, new Skill[]{Skill.DASH, Skill.CARRY, Skill.SCOUT, Skill.FLOAT}),
		BOAT (0, 0, 1, 1, 2, 2, new Skill[]{Skill.CARRY, Skill.DASH, Skill.FLOAT}),
		CATAPULT (8, 10, 4, 0, 1, 3, new Skill[]{}),
		DEFENDER (3, 15, 1, 3, 1, 1, new Skill[]{Skill.FORTIFY}), 
		MINDBENDER (5, 10, 0, 1, 1, 1, new Skill[]{Skill.HEAL, Skill.CONVERT}), 
		RIDER (3, 10, 2, 1, 2, 1, new Skill[]{Skill.DASH, Skill.FORTIFY, Skill.ESCAPE}), 
		SHIP (0, 0, 2, 2, 3, 2, new Skill[]{Skill.DASH, Skill.CARRY, Skill.FLOAT}),
		SWORDSMAN (5, 15, 3, 3, 1, 1, new Skill[]{Skill.DASH, Skill.FORTIFY}), 
		WARRIOR (2, 10, 2, 2, 1, 1, new Skill[]{Skill.DASH, Skill.FORTIFY});

		public final int cost;
		public final int maxHealth;
		public final int attack;
		public final int defense;
		public final int movement;
		public final int range;
		public final Skill[] skills; 

		UnitType (int cost, int maxHealth, int attack, int defense, int movement, int range,
				Skill[] skills) {
			this.cost = cost;
			this.maxHealth = maxHealth;
			this.attack = attack;
			this.defense = defense;
			this.movement = movement;
			this.range = range;
			this.skills = skills;
		}
	}
	// Attributes
	private int health;
	private UnitType type;
	private Unit carryUnit = null;
	private Player ownerPlayer = null;
	private City ownerCity = null;

	private Tile position = null;

	private boolean movable = false;
	private boolean attackable = false;
	
	private int kills = 0;
	private boolean veteran = false;

	// Selection response
	public void visualize() {
		System.out.println("Unit selected");
	}

	public Unit(UnitType type) {
		this.type = type;
		this.health = type.maxHealth;
	}

	public Player getOwnerPlayer() {return this.ownerPlayer;}
	public void setOwnerPlayer(Player player) {this.ownerPlayer = player;}
	public City getOwnerCity() {return this.ownerCity;}
	public void setOwnerCity(City city) {this.ownerCity = city;}
	public UnitType getType() {return this.type;}
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
		return this.type.skills;
	}
	public boolean hasSkill(Skill skill) {
		for (Skill skill_ : this.type.skills) {
			if (skill_ == skill)
				return true;
		}
		return false;
	}

	public int getMaxHealth() {
		return this.type.maxHealth;
	}
	public int getCost() {
		if (carryUnit != null)
			return carryUnit.type.cost;
		return type.cost;
	}
	public int getAttack() {
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

		int [] dx = new int[]{0, 1, 0, -1};
		int [] dy = new int[]{-1, 0, 1, 0};

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
			
			boolean inControl = false;
			for(int i = 0; i < 4; i++) {
				int x = tile.getX() + dx[i];
				int y = tile.getY() + dy[i];
				if (TileMap.isValid(grid, x, y) && grid[x][y].hasEnemy(ownerPlayer))
					inControl = true;
			}
			// in enemy control zone
			if (inControl)
				continue;

			for(int i = 0; i < 4; i++) {
				int x = tile.getX() + dx[i];
				int y = tile.getY() + dy[i];
				if(!TileMap.isValid(grid, x, y))
					continue;
				if(grid[x][y].getUnit() != null)
					continue;

				// Need CLIMBING to go on mountains
				if (grid[x][y].getTerrainType() == Tile.TerrainType.MOUNTAIN 
					&& !ownerPlayer.getTechs().contains(Tech.CLIMBING))
					continue;
				
				if (this.carryUnit == null) {
					// Land unit
					if (grid[x][y].getTerrainType() == Tile.TerrainType.SHORE
						|| grid[x][y].getTerrainType() == Tile.TerrainType.OCEAN)
						// cannot move on SHORE/OCEAN, unless with PORT on
						if (!(grid[x][y].getVariation() instanceof Improvement)
							|| ((Improvement)(grid[x][y].getVariation())).getImprovementType() != Improvement.ImprovementType.PORT)
								continue;
					
					switch (grid[x][y].getTerrainType()) {
						case FIELD: q.add(new State(grid[x][y], move-1)); break;
						case FOREST: q.add(new State(grid[x][y], move-2)); break;
						case MOUNTAIN: q.add(new State(grid[x][y], move-2)); break;
						case SHORE: q.add(new State(grid[x][y], 0)); break;
						case OCEAN: q.add(new State(grid[x][y], 0)); break;
					}
				}
				else {
					// Navy unit
					switch (grid[x][y].getTerrainType()) {
						case SHORE: q.add(new State(grid[x][y], move-1)); break;
						case OCEAN: q.add(new State(grid[x][y], move-1)); break;
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

/*
	public void retaliation(Tile enemyTile, int defendResult) {
		if(defendResult >= getHp()) {
			position.setUnit(null);
			this.type = CharType.NONE;
			return;
		} else {setHp(getHp() - defendResult);}
	}

	public void kill(Tile enemyTile) {
		enemyTile.setUnit(null);
		visualizeAttack(position, enemyTile);
		// Conquer if near
		if(map.getDistance(position, enemyTile) == 1) {
			visualizeMove(position, enemyTile);
			position = enemyTile;
			enemyTile.setUnit(this);
		}
	}


	public void Upgrade() {
		if(isVeteran || killNumber < 3)
			return;
		killNumber = 0;
		level++;
		HpMax += 5;
		Hp = HpMax;
		recover = movable = attack = 0;
	}
	// Automatically for units that does nothing
	public boolean recoverable() {return getHp() < getHpMax();}
	public void Recover() {
		if((getHp() == getHpMax()) || (movable == 0)) return;

		recover--;
		movable = 0;
		attack = 0;
		setHp(Math.max(getHpMax(), getHp() + 2 * getRecoveryRate(this)));
		visualizeHeal(position);
	}

	// Move to accessible tiles and set
	public void Move(Tile targetTile) {
		// Exit if not movable
		// Exit if occupied
		if(movable == -1 ||(movable == 0 && !(attack == 0 && Escape)) || targetTile.hasAlly(ownerPlayer)
				|| targetTile.hasEnemy(ownerPlayer)) return;
		// Exit if not in range
		for(int i = 1; i <= getMoveRange(); i++)
			if(!this.getSurroundings(i).contains(targetTile))
				return;

		movable--;
		recover = 0;
		visualizeMove(position, targetTile);
		position = targetTile;
		targetTile.setUnit(this);
		// Carry();
		// Land();
	}

	// Attack on accessible enemies and set
	public void Attack(Tile enemyTile) {
		// Exit if already act and cannot dash
		// Exit if enemy is not reachable
		// Exit if there is no enemy on the selected tile
		// Exit if unit cannot dash
		if((attack == 0) || (movable == 0 && !Dash) || (!searchEnemy().contains(enemyTile))
				|| (!enemyTile.hasEnemy(ownerPlayer))) return;

		attack--;
		recover = 0;
		// Calculate on formula:  https://frothfrenzy.github.io/polytopiacalculator/?short=
		Unit defender = enemyTile.getUnit();
		float accelerator = 4.5F;
		float defenseBonus = getDefenseBonus(defender);
		float attackForce = getAttackPower() * (getHp() / getHpMax());
		float defenseForce = getDefendPower() * (defender.getHp() / defender.getHpMax()) * defenseBonus;
		float totalDamage = attackForce + defenseForce;
		int attackResult = Math.round((attackForce / totalDamage) * getAttackPower() * accelerator);
		// Exit if enemy is killed
		if(attackResult >= defender.getHp()) {
			kill(enemyTile);
			return;
		}
		defender.setHp(defender.getHp() - attackResult);
		// Enemy defends
		int defenseResult = Math.round((defenseForce / totalDamage) * defender.getDefendPower() * accelerator);
		retaliation(enemyTile, defenseResult);
	}

	public void Carry() {
		// Exit if position is not a port
		if(((Improvement)position.getVariation()).getImprovementType() != Improvement.ImprovementType.PORT) return;
		// Put on shell
		shellType = ShellType.BOAT;
		shellATP = 0;	shellDFP = 1;	shellMR = 2;	shellAR = 0;
		Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
	}

	public void Land() {
		// Exit if not in a type of ship
		// Exit if not in sea
		if(shellType == ShellType.NONE || (position.getTerrainType() != Tile.TerrainType.OCEAN
				&& position.getTerrainType() != Tile.TerrainType.SHORE)) return;
		// Remove shell
		shellType = ShellType.NONE;
		switch (type) {
			case ARCHER:
			case SWORDSMAN:
			case WARRIOR:
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case CATAPULT:
				Dash = false;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case DEFENDER:
				Dash = false;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case MINDBENDER:
				Dash = false;	Fortify = false;Escape = false;	Heal = true;	Convert = true;		Scout = false;
				break;
			case RIDER:
				Dash = true;	Fortify = true;	Escape = true;	Heal = false;	Convert = false;	Scout = false;
		}
	}

	public void Heal() {
		// Exit if unit cannot heal
		if(!Heal || recover == 0) return;

		recover--;
		for(int i = 1; i <= getAttackRange(); i++)
			for(Tile tile: getSurroundings(i))
				if(tile.hasAlly(ownerPlayer)) {
					tile.getUnit().setHp(Math.max(tile.getUnit().getHpMax(), tile.getUnit().getHp() + 4));
					visualizeHeal(tile);
					movable = 0;
					attack = 0;
				}
	}

	public void Convert() {
		// Exit if unit cannot convert
		if(!Convert || recover == 0) return;

		recover--;
		for(int i = 1; i <= getAttackRange(); i++)
			for(Tile tile: getSurroundings(i))
				if(tile.hasEnemy(ownerPlayer)) {
					Unit traitor = new Unit(map, ownerPlayer, ownerCity, CharType.WARRIOR);
					tile.setUnit(traitor);
					visualizeConvert(tile);
					movable = 0;
					attack = 0;
				}
	}
*/

	public ArrayList<Action> getActions() {
		ArrayList<Action> legalActions = new ArrayList<>();

		if (attackable) {
			if(!veteran && kills >= 3)
				legalActions.add(new ActionUnitUpgrade(this));
			if(getHealth() < getMaxHealth())
				legalActions.add(new ActionUnitRecover(this));
			if(hasSkill(Skill.HEAL))
				legalActions.add(new ActionUnitHeal(this));
			if(hasSkill(Skill.CONVERT)) {
				for(Tile tile: searchEnemy())
					legalActions.add(new ActionUnitConvert(tile.getUnit(), ownerPlayer));
			}
			else {
				for(Tile tile: searchEnemy())
					legalActions.add(new ActionUnitAttack(this, tile.getUnit()));
			}
		}
		if (movable) {
			for(Tile tile: getMovableTiles())
					legalActions.add(new ActionUnitMove(this, tile));
		}

		return legalActions;
	}

	@Override
	public String toString() {
		return String.join("-", ownerPlayer.getFaction().toString(), type.toString());
	}

}