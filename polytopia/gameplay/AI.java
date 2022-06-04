package polytopia.gameplay;

import java.util.ArrayList;

import javax.swing.SwingWorker.StateValue;
import javax.xml.namespace.QName;

import polytopia.gameplay.Player.Tech;
import polytopia.gameplay.Tile.TerrainType;

class Actions{
	public ArrayList<Action> actionList;
	Actions(){
		actionList = new ArrayList<Action>();
	}
	Actions(Actions another){
		actionList = another.actionList;
	}
	public void add(Actions another){
		for(int i = 0; i < another.actionList.size(); i ++){
			actionList.add(another.actionList.get(i));
		}
	}
	public void addOne(Action another){
		actionList.add(another);
	}
	public int getSize(){
		return actionList.size();
	}
	public Action getAction(int actionId){
		return actionList.get(actionId);
	}
}

class KnapsackItem{
	int totalReward;
	int cost;
	public KnapsackItem(){
		totalReward = 0;
		cost = 0;
	}
	public KnapsackItem(int t, int c){
		totalReward = t;
		cost = c;
	}
	public void setValue(int t,int c){
		totalReward = t;
		cost = c;
	}
}

public class AI {
	public static int remainStars;
	public static int knapsack[];
	public static int recordItem[];
	public static KnapsackItem itemList[];
	public static int tempKnapsack[];
	public static int vision[] = {0, 0, 0, 0};
	public static int notChanged[] = {0, 0, 0, 0};
	static{
		knapsack = new int[1005];
		tempKnapsack = new int[1005];
		recordItem = new int[1005];
		itemList =  new KnapsackItem[1005];
	}
	public static void GainStarActions(int playerId){
		Player p = Game.players[playerId];
		for(City c : p.getCities())
			for(Tile t : c.getTerritory())
				for(Action a : t.getVariation().getActions())
					for(Consequence con : a.getConsequences(p))
                        
//TODO: Neglect certain actions

						if(con instanceof ConseqGainStars)
						{
							a.apply(p);
							break;
						}
	}
	public static void UnlockTechActions(int playerId){
		Player p = Game.players[playerId];
		int maxStarUse = remainStars / 3;
		int itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
			knapsack[i] = -100000;
			recordItem[i] = -1;
			itemList[i].setValue(0,0);
		}
		int early = 1;
		int earlyAddtition = 5;
		int lowTech = 0;
		for(Tech te : p.getTechs()){
			if(te.rank <= 2 && te != Tech.MEDITATION && te != Tech.WHALING && te != Tech.TRADE) lowTech ++;
		}
		if(lowTech >= 8){
			early = 0;
		}
		for(Tech te : Tech.getUnlockableTechs(p)){
			int cnt = 5;
			if(te == Tech.ORGANIZATION){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Resource && ((Resource)t.getVariation()).getResourceType() == Resource.ResourceType.FRUIT)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.SHIELDS){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getUnit() != null && t.getUnit().getOwnerPlayer() != p)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.FARMING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Resource && ((Resource)t.getVariation()).getResourceType() == Resource.ResourceType.CROP)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.CONSTRUCTION){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Improvement && ((Improvement)t.getVariation()).getImprovementType() == Improvement.ImprovementType.FARM)
							cnt ++;
			}
			if(te == Tech.CLIMBING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getTerrainType() == TerrainType.MOUNTAIN)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.MEDITATION){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getTerrainType() == TerrainType.MOUNTAIN)
							cnt ++;
			}
			if(te == Tech.MINING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Resource && ((Resource)t.getVariation()).getResourceType() == Resource.ResourceType.METAL)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.PHILOSOPHY){
				
			}
			if(te == Tech.SMITHERY){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getUnit() != null && t.getUnit().getOwnerPlayer() != p)
							cnt ++;
			}
			if(te == Tech.FISHING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Resource && ((Resource)t.getVariation()).getResourceType() == Resource.ResourceType.FISH)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.SAILING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getTerrainType() == TerrainType.SHORE)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.WHALING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Resource && ((Resource)t.getVariation()).getResourceType() == Resource.ResourceType.WHALE)
							cnt ++;
			}
			if(te == Tech.NAVIGATION){
				int tilenum = p.getVision().size();
				if(tilenum != vision[playerId])
				{
					notChanged[playerId] = 0;
					vision[playerId] = tilenum;
				}
				else notChanged[playerId]++;
				cnt += notChanged[playerId]*notChanged[playerId];
			}
			if(te == Tech.AQUATISM){
				
			}
			if(te == Tech.HUNTING){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Resource && ((Resource)t.getVariation()).getResourceType() == Resource.ResourceType.ANIMAL)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.ARCHERY){
				cnt += early * earlyAddtition;
			}
			if(te == Tech.FORESTRY){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getTerrainType() == TerrainType.FOREST)
							cnt ++;
				cnt += early * earlyAddtition;
			}
			if(te == Tech.SPIRITUALISM){

			}
			if(te == Tech.MATHEMATICS){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Improvement && ((Improvement)t.getVariation()).getImprovementType() == Improvement.ImprovementType.LUMBER_HUT)
							cnt ++;
			}
			if(te == Tech.RIDING){
				cnt += early * earlyAddtition;
			}
			if(te == Tech.TRADE){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getVariation() instanceof Improvement && ((Improvement)t.getVariation()).getImprovementType() == Improvement.ImprovementType.PORT)
							cnt ++;
			}
			if(te == Tech.FREE_SPIRIT){
				cnt += early * earlyAddtition;
			}
			if(te == Tech.CHIVALRY){
				for(City c : p.getCities())
					for(Tile t : c.getTerritory())
						if(t.getUnit() != null && t.getUnit().getOwnerPlayer() != p)
							cnt ++;
			}
			itemList[itemnum++].setValue(cnt, te.getCost(p));
		}
		for(int i = 0; i < itemnum; i ++){
			for(int j = itemList[i].cost; j <= maxStarUse; j ++){
				if(knapsack[j - itemList[i].cost] + itemList[i].totalReward > knapsack[j]){
					knapsack[j] = knapsack[j - itemList[i].cost] + itemList[i].totalReward;
					recordItem[j] = i;
				}
			}
		}
		int maxKnapsackReward = -1;
		int maxKnapsackSpot = 0;
		for(int i = 0; i < maxStarUse; i ++){
			if(maxKnapsackReward < knapsack[i]){
				maxKnapsackReward = knapsack[i];
				maxKnapsackSpot = i;
			}
		}
		while(maxKnapsackSpot > 0){
			int techId = recordItem[maxKnapsackSpot];
			p.addTech((Tech.getUnlockableTechs(p)).get(techId));
			maxKnapsackSpot = maxKnapsackSpot - itemList[techId].cost;
		}
	}
	public static void CommandUnitActions(int playerId){
		Actions ret = new Actions();
		Player p = Game.players[playerId];

// TODO: Get Unit Actions

	}
	public static void DevelopEconomyActions(int playerId){
		Actions actionList = new Actions();
		Player p = Game.players[playerId];
		int maxValidationUse = remainStars / 2;
		int itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
			knapsack[i] = -100000;
			recordItem[i] = -1;
			itemList[i].setValue(0,0);
		}
		knapsack[0] = 0;
		for(City c : p.getCities())
			for(Tile t : c.getTerritory())
				for(Action a : t.getVariation().getActions()){
					actionList.addOne(a);
					int totalReward = 0;
					int cost = a.getCost();
					for(Consequence con : a.getConsequences(p)){
						if(con instanceof ConseqGainStars){
							System.out.println("still have consequences that add stars");
						}
						totalReward = totalReward + con.getReward();
					}
					itemList[itemnum++].setValue(totalReward, cost);
				}
		for(int i = 0; i < itemnum; i ++){
			for(int j = itemList[i].cost; j <= maxValidationUse; j ++){
				if(knapsack[j - itemList[i].cost] + itemList[i].totalReward > knapsack[j]){
					knapsack[j] = knapsack[j - itemList[i].cost] + itemList[i].totalReward;
					recordItem[j] = i;
				}
			}
		}
		int maxKnapsackReward = -1;
		int maxKnapsackSpot = 0;
		for(int i = 0; i < maxValidationUse; i ++){
			if(maxKnapsackReward < knapsack[i]){
				maxKnapsackReward = knapsack[i];
				maxKnapsackSpot = i;
			}
		}
		while(maxKnapsackSpot > 0){
			int actionId = recordItem[maxKnapsackSpot];
			Action tempAction = actionList.getAction(actionId);
			tempAction.apply(p);
			maxKnapsackSpot = maxKnapsackSpot - itemList[actionId].cost;
		}
		remainStars = p.getStars();
		int maxTileUse = remainStars / 2;
		itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
			knapsack[i] = -100000;
			recordItem[i] = -1;
			itemList[i].setValue(0,0);
		}
		knapsack[0] = 0;
		for(City c : p.getCities())
			for(Tile t : c.getTerritory())
				for(Action a : t.getActions()){
					if(a instanceof ActionClearForest)
						continue;
					actionList.addOne(a);
					int totalReward = 0;
					int cost = a.getCost();
					for(Consequence con : a.getConsequences(p)){
						if(con instanceof ConseqGainStars){
							System.out.println("still have consequences that add stars");
						}
						totalReward = totalReward + con.getReward();
					}
					itemList[itemnum++].setValue(totalReward, cost);
				}
		for(int i = 0; i < itemnum; i ++){
			for(int j = itemList[i].cost; j <= maxTileUse; j ++){
				if(knapsack[j - itemList[i].cost] + itemList[i].totalReward > knapsack[j]){
					knapsack[j] = knapsack[j - itemList[i].cost] + itemList[i].totalReward;
					recordItem[j] = i;
				}
			}
		}
		maxKnapsackReward = -1;
		maxKnapsackSpot = 0;
		for(int i = 0; i < maxTileUse; i ++){
			if(maxKnapsackReward < knapsack[i]){
				maxKnapsackReward = knapsack[i];
				maxKnapsackSpot = i;
			}
		}
		while(maxKnapsackSpot > 0){
			int actionId = recordItem[maxKnapsackSpot];
			Action tempAction = actionList.getAction(actionId);
			tempAction.apply(p);
			maxKnapsackSpot = maxKnapsackSpot - itemList[actionId].cost;
		}
	}
	public static void TrainUnitActions(int playerId){
		Actions actionList = new Actions();
		Player p = Game.players[playerId];
		int maxStarUse = remainStars;
		int itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
			knapsack[i] = -100000;
			tempKnapsack[i] = -100000;
			recordItem[i] = -1;
			itemList[i].setValue(0,0);
		}
		knapsack[0] = 0;
		tempKnapsack[0] = 0;
		for(City c : p.getCities())
		{
			for(Action a : c.getActions())
				if(a.isPerformableTo(p)){
					actionList.addOne(a);
					int totalReward = 0;
					int cost = a.getCost();
					for(Consequence con : a.getConsequences(p)){
						if(con instanceof ConseqGainStars){
							System.out.println("still have consequences that add stars");
						}
						totalReward = totalReward + con.getReward();
					}
					itemList[itemnum++].setValue(totalReward, cost);
					int i = itemnum - 1;
					for(int j = itemList[i].cost; j <= maxStarUse; j ++){
						if(knapsack[j - itemList[i].cost] + itemList[i].totalReward > tempKnapsack[j]){
							tempKnapsack[j] = knapsack[j - itemList[i].cost] + itemList[i].totalReward;
							recordItem[j] = i;
						}
					}
				}
			for(int j = 0; j <=maxStarUse; j ++)
				knapsack[j] = tempKnapsack[j];
		}
		int maxKnapsackReward = -1;
		int maxKnapsackSpot = 0;
		for(int i = 0; i < maxStarUse; i ++){
			if(maxKnapsackReward < knapsack[i]){
				maxKnapsackReward = knapsack[i];
				maxKnapsackSpot = i;
			}
		}
		while(maxKnapsackSpot > 0){
			int actionId = recordItem[maxKnapsackSpot];
			Action tempAction = actionList.getAction(actionId);
			tempAction.apply(p);
			maxKnapsackSpot = maxKnapsackSpot - itemList[actionId].cost;
		}
	}
	public static void decideActionsForAI(int playerId){

        
		remainStars = Game.players[playerId].getStars();
		GainStarActions(playerId);
		remainStars = Game.players[playerId].getStars();
		UnlockTechActions(playerId);
		remainStars = Game.players[playerId].getStars();
		CommandUnitActions(playerId);
		remainStars = Game.players[playerId].getStars();
		DevelopEconomyActions(playerId);
		remainStars = Game.players[playerId].getStars();
		TrainUnitActions(playerId);

        new ActionEndTurn().apply(Game.players[playerId]);
	}
}
