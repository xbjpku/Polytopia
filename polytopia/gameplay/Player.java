package polytopia.gameplay;

import java.util.ArrayList;
import java.util.HashSet;

public class Player {

	public enum Faction {
		Xinxi, Imperius, Bardur, Oumaji;
	}

	public enum Tech {
		ORGANIZATION (1, null),  		// fruit (early)
		SHIELDS (2, ORGANIZATION),		// enemy (early)
		FARMING (2, ORGANIZATION),		// crop (early)
		CONSTRUCTION (3, FARMING),		// farm 

		CLIMBING (1, null),				// mountain (early)
		MEDITATION (2, CLIMBING),		// mountain
		MINING (2, CLIMBING),			// metal (early)
		PHILOSOPHY (3, MEDITATION),		
		SMITHERY (3, MINING),			// enemy

		FISHING (1, null),				// fish (early)
		SAILING (2, FISHING),			// shore (early)
		WHALING (2, FISHING),			// whale
		NAVIGATION (3, SAILING),		// discover tile
		AQUATISM (3, WHALING),			

		HUNTING (1, null),				// animal (early)
		ARCHERY (2, HUNTING),			// (early)	
		FORESTRY (2, HUNTING),			// forest (early)
		SPIRITUALISM (3, ARCHERY),		
		MATHEMATICS (3, FORESTRY),		// lumberhut

		RIDING (1, null),				// (early)	
		TRADE (2, RIDING),				// port
		FREE_SPIRIT (2, RIDING),		// (early)
		CHIVALRY (3, FREE_SPIRIT);		// enemy
		
		int rank;
		Tech prerequisite;

		Tech (int rank, Tech prerequisite) {
			this.rank = rank;
			this.prerequisite = prerequisite;
		}

		public int getCost(Player player) {
			int cost = 4 + player.getCities().size() * this.rank;
			if (player.getTechs().contains(Tech.PHILOSOPHY))
				cost = (int)(Math.ceil(cost * 2.0/3));
			return cost;
		}

		public boolean isUnlockableTo(Player player) {
			if (player.getTechs().contains(this))
				return false;

			if (this.prerequisite == null || player.getTechs().contains(this.prerequisite)) {
				if (this.getCost(player) <= player.getStars())
					return true;
			}
			return false;
		}

		public static ArrayList<Tech> getUnlockableTechs(Player player) {
			ArrayList<Tech> techs =  player.getTechs();

			ArrayList<Tech> newTechs = new ArrayList<Tech>();
			for (Tech tech : Tech.values()) {
				if (!techs.contains(tech) && tech.isUnlockableTo(player)) {
					newTechs.add(tech);
				}
			}
			return newTechs;
		}
		
	}

	private Faction faction;
	private int stars;
	private ArrayList<Tech> techs;
	private City capital = null;
	private ArrayList<City> cities = null;
	private ArrayList<Unit> units = null;
	private HashSet<Tile> vision = null;

	public Player(String factionName) {
		this.faction = Faction.valueOf(factionName);
		this.stars = 5;
		this.techs = new ArrayList<Tech>();
		switch(this.faction) {
			case Xinxi: this.techs.add(Tech.CLIMBING); break;
			case Imperius: this.techs.add(Tech.ORGANIZATION); break;
			case Bardur: this.techs.add(Tech.HUNTING); break;
			case Oumaji: this.techs.add(Tech.RIDING); break;
		}
		this.cities = new ArrayList<City>();
		this.vision = new HashSet<Tile>();
		this.units = new ArrayList<Unit>();
		this.actions = new Action[] {new ActionEndTurn()};
	}

	public Faction getFaction() {return this.faction;}
	public int getStars() {return this.stars;}
	public void setStars(int stars) {this.stars = stars;}

	public ArrayList<Tech> getTechs() {return this.techs;}
	public City getCapital() {return this.capital;}
	public ArrayList<City> getCities() {return this.cities;}
	public ArrayList<Unit> getUnits() {return this.units;}
	public HashSet<Tile> getVision() {return this.vision;}

	public void setCapital(City city) {this.capital = city;}
	public void addCity(City city) {this.cities.add(city);}
	public void addVision(Tile tile) {this.vision.add(tile);}
	public void addTech(Tech tech) {this.techs.add(tech);}

	private Action[] actions;
	public Action[] getActions() {
		// Player-level actions include UnlockTech and EndTurn
		return this.actions;
	}
	
}