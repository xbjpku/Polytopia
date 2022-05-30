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
	// Attributes
	private CharType type = CharType.NONE;
	private CharType carryType = CharType.NONE;
	private int HpMax;
	private int Hp;
	private int Cost;
	private int AttackPower;
	private int DefendPower;
	private int MoveRange;
	private int AttackRange;
	private boolean Navy;
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
	private boolean roundEnd = false;

	// Selection response
	public void visualize(/* GUI component */) {
		System.out.println("Unit selected");
	}

	public Player getOwnerPlayer() {return this.ownerPlayer;}
	public City getOwnerCity() {return this.ownerCity;}

	Unit(Player ownerPlayer_,City ownerCity_, CharType type_) {
		this.ownerPlayer = ownerPlayer_;
		this.ownerCity = ownerCity_;
		this.type = type_;
		switch (type) {
			case ARCHER: // 	dash	fortify
				Hp = HpMax = 10;
				Cost = 3; 	AttackPower = 2; 	DefendPower = 1;	MoveRange = 1;	AttackRange = 2;	Navy = false;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case BATTLESHIP: // dash	scout(S)
				Hp = HpMax = 0;
				Cost = 15; 	AttackPower = 3; 	DefendPower = 3;	MoveRange = 3;	AttackRange = 3;	Navy = true;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = true;
				break;
			case BOAT: // 		-
				Hp = HpMax = 0;
				Cost = 0; 	AttackPower = 0;	DefendPower = 1;	MoveRange = 2;	AttackRange = 0;	Navy = true;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case CATAPULT: // 	-
				Hp = HpMax = 10;
				Cost = 8; 	AttackPower = 4;	DefendPower = 0;	MoveRange = 1;	AttackRange = 3;	Navy = false;
				Dash = false;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case DEFENDER: // 	fortify
				Hp = HpMax = 15;
				Cost = 3; 	AttackPower = 1;	DefendPower = 3;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = false;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case MINDBENDER: //	heal	convert		(detect)
				Hp = HpMax = 10;
				Cost = 5; 	AttackPower = 0;	DefendPower = 1;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = false;	Fortify = false;Escape = false;	Heal = true;	Convert = true;		Scout = false;
				break;
			case RIDER: // 		dash	escape		fortify
				Hp = HpMax = 10;
				Cost = 3; 	AttackPower = 2;	DefendPower = 1;	MoveRange = 2;	AttackRange = 1;	Navy = false;
				Dash = true;	Fortify = true;	Escape = true;	Heal = false;	Convert = false;	Scout = false;
				break;
			case SHIP: //		dash
				Hp = HpMax = 0;
				Cost = 5; 	AttackPower = 2;	DefendPower = 2;	MoveRange = 3;	AttackRange = 2;	Navy = true;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case SWORDSMAN: //	dash	fortify
				Hp = HpMax = 15;
				Cost = 5; 	AttackPower = 3;	DefendPower = 3;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case WARRIOR: //	dash	fortify
				Hp = HpMax = 10;
				Cost = 2; 	AttackPower = 2;	DefendPower = 2;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case NONE:
				Hp = HpMax = Cost = AttackPower = DefendPower = MoveRange = AttackRange = 0;
				Navy = Dash = Fortify =	Escape = Heal = Convert = Scout = false;
		}
	}

	public void visualizeAttack(Tile from, Tile to) {
		/* TODO: GUI Visualize Attack, Move,  */
	}
	public void visualizeMove(Tile from, Tile to) {}
	public void visualizeHeal(Tile at) {}
	public void visualizeConvert(Tile at) {}

	public ArrayList<Tile> getSurroundings(int layer) {
		ArrayList<Tile> surroundings = new ArrayList<>();
		for(int i = 0; i < map.GetSize(); i++)
			for(int j = 0; j < map.GetSize(); j++)
				if(map.getDistance(position, map.getGrid()[i][j]) == layer)
					// Navy then all accessibility, NonNavy then landscape and port in range are accessible
					if(Navy || (!Navy && (map.getGrid()[i][j].getTerrainType() == Tile.TerrainType.FIELD
							|| map.getGrid()[i][j].getTerrainType() == Tile.TerrainType.FOREST
							|| (map.getGrid()[i][j].getTerrainType() == Tile.TerrainType.MOUNTAIN
										&& ownerPlayer.getTechs().contains(Player.Tech.CLIMBING)
							|| ((Improvement)map.getGrid()[i][j].getVariation()).getImprovementType()
								== Improvement.ImprovementType.PORT))))
						if(!map.getGrid()[i][j].hasAlly(ownerPlayer))
							surroundings.add(map.getGrid()[i][j]);
		return surroundings;
	}

	public ArrayList<Tile> searchEnemy() {
		ArrayList<Tile> accessibleEnemy = new ArrayList<>();
		for(int i = 1; i <= AttackRange; i++)
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
			if(defendResult >= Hp) {
				position.setUnit(null);
				this.type = CharType.NONE;
				return;
			} else {
				Hp -= defendResult;
			}
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
	public boolean recoverable() {return Hp < HpMax;}
	public void Recover() {
		if((Hp == HpMax) || (movable == 0)) return;

		recover--;
		movable = 0;
		attack = 0;
		Hp = Math.max(HpMax, Hp + 2 * getRecoveryRate(this));
		visualizeHeal(position);
	}
	// Move to accessible tiles and set
	public void Move(Tile targetTile) {
		// Exit if not movable
		// Exit if occupied
		if(movable == -1 ||(movable == 0 && !(attack == 0 && Escape)) || targetTile.hasAlly(ownerPlayer)
				|| targetTile.hasEnemy(ownerPlayer)) return;
		// Exit if not in range
		for(int i = 1; i <= MoveRange; i++)
			if(!this.getSurroundings(i).contains(targetTile))
				return;

		movable--;
		recover = 0;
		visualizeMove(position, targetTile);
		position = targetTile;
		targetTile.setUnit(this);
		Carry();
		Land();
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
		float defenseBonus = getDefenseBonus(enemyTile.getUnit());
		float attackForce = AttackPower * (Hp / HpMax);
		float defenseForce = DefendPower * (defender.Hp / defender.HpMax) * defenseBonus;
		float totalDamage = attackForce + defenseForce;
		int attackResult = Math.round((attackForce / totalDamage) * AttackPower * accelerator);
		// Exit if enemy is killed
		if(attackResult >= defender.Hp) {
			kill(enemyTile);
			return;
		}
		enemyTile.getUnit().Hp -= attackResult;
		// Enemy defends
		int defenseResult = Math.round((defenseForce / totalDamage) * defender.DefendPower * accelerator);
		retaliation(enemyTile, defenseResult);
	}

	public void Carry() {
		// Exit if position is not a port
		if(((Improvement)position.getVariation()).getImprovementType() != Improvement.ImprovementType.PORT) return;
		// Transform
		carryType = type;
		type = CharType.BOAT;
		AttackPower = 0;	DefendPower = 1;	MoveRange = 2;	AttackRange = 0;	Navy = true;
		Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
	}

	public void Land() {
		// Exit if position is not land or type is not a boat
		if((type != CharType.BOAT && type != CharType.BATTLESHIP && type != CharType.SHIP)
				|| position.getTerrainType() == Tile.TerrainType.OCEAN
				|| position.getTerrainType() == Tile.TerrainType.SHORE)
			return;
		// Transform
		type = carryType;
		carryType = CharType.NONE;
		switch (type) {
			case ARCHER: // 	dash	fortify
				Hp = HpMax = 10;
				Cost = 3; 	AttackPower = 2; 	DefendPower = 1;	MoveRange = 1;	AttackRange = 2;	Navy = false;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case BATTLESHIP: // dash	scout(S)
				Hp = HpMax = 0;
				Cost = 15; 	AttackPower = 3; 	DefendPower = 3;	MoveRange = 3;	AttackRange = 3;	Navy = true;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = true;
				break;
			case BOAT: // 		-
				Hp = HpMax = 0;
				Cost = 0; 	AttackPower = 0;	DefendPower = 1;	MoveRange = 2;	AttackRange = 0;	Navy = true;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case CATAPULT: // 	-
				Hp = HpMax = 10;
				Cost = 8; 	AttackPower = 4;	DefendPower = 0;	MoveRange = 1;	AttackRange = 3;	Navy = false;
				Dash = false;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case DEFENDER: // 	fortify
				Hp = HpMax = 15;
				Cost = 3; 	AttackPower = 1;	DefendPower = 3;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = false;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case MINDBENDER: //	heal	convert		(detect)
				Hp = HpMax = 10;
				Cost = 5; 	AttackPower = 0;	DefendPower = 1;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = false;	Fortify = false;Escape = false;	Heal = true;	Convert = true;		Scout = false;
				break;
			case RIDER: // 		dash	escape		fortify
				Hp = HpMax = 10;
				Cost = 3; 	AttackPower = 2;	DefendPower = 1;	MoveRange = 2;	AttackRange = 1;	Navy = false;
				Dash = true;	Fortify = true;	Escape = true;	Heal = false;	Convert = false;	Scout = false;
				break;
			case SHIP: //		dash
				Hp = HpMax = 0;
				Cost = 5; 	AttackPower = 2;	DefendPower = 2;	MoveRange = 3;	AttackRange = 2;	Navy = true;
				Dash = true;	Fortify = false;Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case SWORDSMAN: //	dash	fortify
				Hp = HpMax = 15;
				Cost = 5; 	AttackPower = 3;	DefendPower = 3;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case WARRIOR: //	dash	fortify
				Hp = HpMax = 10;
				Cost = 2; 	AttackPower = 2;	DefendPower = 2;	MoveRange = 1;	AttackRange = 1;	Navy = false;
				Dash = true;	Fortify = true;	Escape = false;	Heal = false;	Convert = false;	Scout = false;
				break;
			case NONE:
				Hp = HpMax = Cost = AttackPower = DefendPower = MoveRange = AttackRange = 0;
				Navy = Dash = Fortify =	Escape = Heal = Convert = Scout = false;
		}
	}

	public void Heal() {
		// Exit if unit cannot heal
		if(!Heal || recover == 0) return;

		recover--;
		for(int i = 1; i <= AttackRange; i++)
			for(Tile tile: getSurroundings(i))
				if(tile.hasAlly(ownerPlayer)) {
					tile.getUnit().Hp = Math.max(tile.getUnit().HpMax, tile.getUnit().Hp + 4);
					visualizeHeal(tile);
					movable = 0;
					attack = 0;
				}
	}

	public void Convert() {
		// Exit if unit cannot convert
		if(!Convert || recover == 0) return;

		recover--;
		for(int i = 1; i <= AttackRange; i++)
			for(Tile tile: getSurroundings(i))
				if(tile.hasEnemy(ownerPlayer)) {
					Unit traitor = new Unit(ownerPlayer, ownerCity, CharType.WARRIOR);
					tile.setUnit(traitor);
					visualizeConvert(tile);
					movable = 0;
					attack = 0;
				}
	}


	public String toString() {
		switch(type) {
			case ARCHER:
				return ownerPlayer.toString() + "ARCHER";
			case BATTLESHIP:
				return ownerPlayer.toString() + "BATTLESHIP";
			case BOAT:
				return ownerPlayer.toString() + "BOAT";
			case CATAPULT:
				return ownerPlayer.toString() + "CATAPULT";
			case DEFENDER:
				return ownerPlayer.toString() + "DEFENDER";
			case MINDBENDER:
				return ownerPlayer.toString() + "MINDBENDER";
			case RIDER:
				return ownerPlayer.toString() + "RIDER";
			case SHIP:
				return ownerPlayer.toString() + "SHIP";
			case SWORDSMAN:
				return ownerPlayer.toString() + "SWORDSMAN";
			case WARRIOR:
				return ownerPlayer.toString() + "WARRIOR";
		}
		return "None";
	}

}
