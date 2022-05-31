package polytopia.gameplay;

import polytopia.graphics.Visualizable;

import javax.swing.plaf.metal.OceanTheme;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class Unit implements Visualizable {

	private Player ownerPlayer;
	private City ownerCity;

	public enum CharType {
		NONE,
		ARCHER, BATTLESHIP, BOAT, CATAPULT, DEFENDER, MINDBENDER, RIDER, SHIP, SWORDSMAN, WARRIOR;
	}
	public enum ShellType {
		NONE,
		SHIP, BOAT, BATTLESHIP;
	}
	// Attributes
	private CharType type = CharType.NONE;
	private ShellType shellType = ShellType.NONE;
	private CharType carryType = CharType.NONE;
	//
	private int HpMax;
	private int shellHPM;
	private int Hp;
	private int shellHp;
	//
	private int Cost;
	private int shellCost;
	//
	private int AttackPower;
	private int shellATP;
	//
	private int DefendPower;
	private int shellDFP;
	//
	private int MoveRange;
	private int shellMR;
	//
	private int AttackRange;
	private int shellAR;
	// Skills
	public boolean Dash;
	public boolean Fortify;
	public boolean Escape;
	public boolean Heal;
	public boolean Convert;
	public boolean Scout;

	public int movable = 1;
	public int attack = 1;
	public int recover = 1;
	public Tile position;
	private TileMap map;

	// Selection response
	public void visualize(/* GUI component */) {
		System.out.println("Unit selected");
	}

	public Player getOwnerPlayer() {return this.ownerPlayer;}
	public City getOwnerCity() {return this.ownerCity;}

	Unit(TileMap map_, Player ownerPlayer_, City ownerCity_, CharType type_) {
		this.map = map_;
		this.ownerPlayer = ownerPlayer_;
		this.ownerCity = ownerCity_;
		this.type = type_;
		switch (type) {
			case ARCHER: // 	dash	fortify
				Hp = HpMax = 10;
				Cost = 3; 	AttackPower = 2; 	DefendPower = 1;	MoveRange = 1;	AttackRange = 2;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
				/*
			case BATTLESHIP: // dash	scout(S)
				Hp = HpMax = 0;
				Cost = 15; 	AttackPower = 3; 	DefendPower = 3;	MoveRange = 3;	AttackRange = 3;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = true;
				break;
			case BOAT: // 		-
				Hp = HpMax = 0;
				Cost = 0; 	AttackPower = 0;	DefendPower = 1;	MoveRange = 2;	AttackRange = 0;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
				 */
			case CATAPULT: // 	-
				Hp = HpMax = 10;
				Cost = 8; 	AttackPower = 4;	DefendPower = 0;	MoveRange = 1;	AttackRange = 3;
				Dash = false;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case DEFENDER: // 	fortify
				Hp = HpMax = 15;
				Cost = 3; 	AttackPower = 1;	DefendPower = 3;	MoveRange = 1;	AttackRange = 1;
				Dash = false;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case MINDBENDER: //	heal	convert		(detect)
				Hp = HpMax = 10;
				Cost = 5; 	AttackPower = 0;	DefendPower = 1;	MoveRange = 1;	AttackRange = 1;
				Dash = false;	Fortify = false;Escape = false;	Heal = true;	Convert = true;		Scout = false;
				break;
			case RIDER: // 		dash	escape		fortify
				Hp = HpMax = 10;
				Cost = 3; 	AttackPower = 2;	DefendPower = 1;	MoveRange = 2;	AttackRange = 1;
				Dash = true;	Fortify = true;	Escape = true;	Heal = false;	Convert = false;	Scout = false;
				break;
				/*
			case SHIP: //		dash
				Hp = HpMax = 0;
				Cost = 5; 	AttackPower = 2;	DefendPower = 2;	MoveRange = 3;	AttackRange = 2;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
				 */
			case SWORDSMAN: //	dash	fortify
				Hp = HpMax = 15;
				Cost = 5; 	AttackPower = 3;	DefendPower = 3;	MoveRange = 1;	AttackRange = 1;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case WARRIOR: //	dash	fortify
				Hp = HpMax = 10;
				Cost = 2; 	AttackPower = 2;	DefendPower = 2;	MoveRange = 1;	AttackRange = 1;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case NONE:
				Hp = HpMax = Cost = AttackPower = DefendPower = MoveRange = AttackRange = 0;
				Dash = Fortify =	Escape = Heal = Convert = Scout = false;
		}
	}
	public void visualizeAttack(Tile from, Tile to) {
		/* TODO: GUI Visualize Attack, Move,  */
	}
	public void visualizeMove(Tile from, Tile to) {}
	public void visualizeHeal(Tile at) {}
	public void visualizeConvert(Tile at) {}

	public int getHpMax() {
		if(shellType == ShellType.NONE) return HpMax;
		return shellHPM;
	}
	public int getHp() {
		if(shellType == ShellType.NONE) return Hp;
		return shellHp;
	}
	public void setHp(int x) {
		if(shellType == ShellType.NONE) {Hp = x;return;}
		shellHp = x;
	}
	public int getCost() {
		if(shellType == ShellType.NONE) return Cost;
		return shellCost;
	}
	public int getAttackPower() {
		if(shellType == ShellType.NONE) return AttackPower;
		return shellATP;
	}
	public int getDefendPower() {
		if(shellType == ShellType.NONE) return DefendPower;
		return shellDFP;
	}
	public int getAttackRange() {
		if(shellType == ShellType.NONE) return AttackRange;
		return shellAR;
	}
	public int getMoveRange() {
		if(shellType == ShellType.NONE) return MoveRange;
		return shellMR;
	}
	public ArrayList<Tile> getSurroundings(int layer) {
		ArrayList<Tile> surroundings = new ArrayList<>();
		for(int i = 0; i < map.GetSize(); i++)
			for(int j = 0; j < map.GetSize(); j++)
				if(map.getDistance(position, map.getGrid()[i][j]) == layer)
					// Navy then all accessibility, NonNavy then landscape and port in range are accessible
					if(shellType != ShellType.NONE || (shellType == ShellType.NONE && (map.getGrid()[i][j].getTerrainType() == Tile.TerrainType.FIELD
							|| map.getGrid()[i][j].getTerrainType() == Tile.TerrainType.FOREST
							|| (map.getGrid()[i][j].getTerrainType() == Tile.TerrainType.MOUNTAIN
										&& ownerPlayer.getTechs().contains(Player.Tech.CLIMBING)
							|| ((Improvement)map.getGrid()[i][j].getVariation()).getImprovementType()
								== Improvement.ImprovementType.PORT))))
						if(!map.getGrid()[i][j].hasAlly(ownerPlayer))
							surroundings.add(map.getGrid()[i][j]);
		return surroundings;
	}

	public ArrayList<Tile> getMovable() {
		ArrayList<Tile> destination = new ArrayList<>();
		for(int i = 1; i <= getMoveRange(); i++)
			for(Tile tile: getSurroundings(i))
				if(!tile.hasAlly(ownerPlayer) && !tile.hasEnemy(ownerPlayer))
					destination.add(tile);
		return destination;
	}

	public ArrayList<Tile> searchEnemy() {
		ArrayList<Tile> accessibleEnemy = new ArrayList<>();
		for(int i = 1; i <= getAttackRange(); i++)
			for(Tile tile: getSurroundings(i))
				if (tile.hasEnemy(ownerPlayer))
					accessibleEnemy.add(tile);
		return accessibleEnemy;
	}

	public float getDefenseBonus(Unit s) {
		float bonus = 1;
		// Aquatism for water, Archery for forests and Meditation for mountains are defensive terrains
		//if((Improvement)s.position.getVariation() == )

		// Fortify
		if(s.Fortify && s.position.getOwnerCity().getOwnerTile() == s.position) {
			bonus *= 1.25F;
		}
		// Has Wall
		if(s.position.getOwnerCity().getOwnerTile() == s.position && s.position.getOwnerCity().hasWall()) {
			bonus *= 1.25F;
		}
		return bonus;
	}

	public int getRecoveryRate(Unit s) {
		int rate = 1;
		// In capital range
		if(position.getOwnerCity() == ownerCity) {
			rate = 2;
		}
		return rate;
	}

	public void retaliation(Tile enemyTile, int defendResult) {
		if(enemyTile.getUnit().searchEnemy().contains(position)) {
			visualizeAttack(enemyTile, position);
			if(defendResult >= getHp()) {
				position.setUnit(null);
				this.type = CharType.NONE;
				return;
			} else {setHp(getHp() - defendResult);}
		}
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

	public ArrayList<Action> getActions() {
		ArrayList<Action> legalActions = new ArrayList<>();
		int combined = movable << 2 | attack << 1 | recover;
		switch(combined) {
			case 7:
				if(getHp() < getHpMax())
					legalActions.add(new ActionRecover(this));
				if(Heal)
					legalActions.add(new ActionHeal(this));
				if(Convert)
					legalActions.add(new ActionConvert(this));
				for(Tile tile: getMovable())
					legalActions.add(new ActionMove(this, tile));
				for(Tile tile: searchEnemy())
					legalActions.add(new ActionAttack(this, tile.getUnit()));
				break;
			case 1:
				if(Escape)
					for(Tile tile: getMovable())
						legalActions.add(new ActionMove(this, tile));
				break;
			case 3:
				if(Dash)
					for(Tile tile: searchEnemy())
						legalActions.add(new ActionAttack(this, tile.getUnit()));
				break;
		}
		return legalActions;
	}


	public String toString() {
		if(shellType != ShellType.NONE) {
			switch(shellType) {
				case BOAT:
					return ownerPlayer.toString() + "BOAT";
				case SHIP:
					return ownerPlayer.toString() + "SHIP";
				case BATTLESHIP:
					return ownerPlayer.toString() + "BATTLESHIP";
			}
		}
		switch(type) {
			case ARCHER:
				return ownerPlayer.toString() + "ARCHER";
			case CATAPULT:
				return ownerPlayer.toString() + "CATAPULT";
			case DEFENDER:
				return ownerPlayer.toString() + "DEFENDER";
			case MINDBENDER:
				return ownerPlayer.toString() + "MINDBENDER";
			case RIDER:
				return ownerPlayer.toString() + "RIDER";
			case SWORDSMAN:
				return ownerPlayer.toString() + "SWORDSMAN";
			case WARRIOR:
				return ownerPlayer.toString() + "WARRIOR";
		}
		return "None";
	}

}
