package polytopia.gameplay;

import java.util.ArrayList;
import java.util.HashSet;

public class Player {

	public enum Faction {
		Xinxi, Imperius, Bardur, Oumaji;
	}

	public enum Tech {
		ORGANIZATION (1, null),
		SHIELDS (2, ORGANIZATION),
		FARMING (2, ORGANIZATION),
		CONSTRUCTION (3, FARMING),

		CLIMBING (1, null),
		MEDITATION (2, CLIMBING),
		MINING (2, CLIMBING),
		PHILOSOPHY (3, MEDITATION),
		SMITHERY (3, MINING),

		FISHING (1, null),
		SAILING (2, FISHING),
		WHALING (2, FISHING),
		NAVIGATION (3, SAILING),
		AQUATISM (3, WHALING),

		HUNTING (1, null),
		ARCHERY (2, HUNTING),
		FORESTRY (2, HUNTING),
		SPIRITUALISM (3, ARCHERY),
		MATHEMATICS (3, FORESTRY),

		RIDING (1, null),
		TRADE (2, RIDING),
		FREE_SPIRIT (2, RIDING),
		CHIVALRY (3, FREE_SPIRIT);
		
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
	}

	public Faction getFaction() {return this.faction;}
	public int getStars() {return this.stars;}
	public void setStars(int stars) {this.stars = stars;}

	public ArrayList<Tech> getTechs() {return this.techs;}
	public City getCapital() {return this.capital;}
	public ArrayList<City> getCities() {return this.cities;}
	public HashSet<Tile> getVision() {return this.vision;}

	public void setCapital(City city) {this.capital = city;}
	public void addCity(City city) {this.cities.add(city);}
	public void addVision(Tile tile) {this.vision.add(tile);}
	public void addTech(Tech tech) {this.techs.add(tech);}
	
}