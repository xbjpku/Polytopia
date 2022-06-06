package polytopia.gameplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


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
	public void clear(){
		actionList.clear();
	}
	public boolean isEmpty(){
		if(actionList.size() == 0)
			return true;
		return false;
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
    public static int doSleep = 300;
	public static int remainStars;
	public static int knapsack[][];
	public static int recordItem[][];
	public static KnapsackItem itemList[];
	public static int tempKnapsack[][];
	public static int vision[] = {0, 0, 0, 0};
	public static int notChanged[] = {0, 0, 0, 0};
	public static int value[][];
	public static int used[][];
	public static int size;
	public static int extraValueForCommand;
	static{
		size = Game.map.getSize();
		knapsack = new int[305][1005];
		tempKnapsack = new int[305][1005];
		recordItem = new int[305][1005];
		itemList =  new KnapsackItem[1005];
        for (int i = 0; i < 1005; i++)
            itemList[i] = new KnapsackItem();

		value = new int[size][size];
		used = new int[size][size];
		extraValueForCommand = 2;
	}
	public static void GainStarActions(int playerId){
		Player p = Game.players[playerId];
		for(City c : p.getCities())
			for(Tile t : c.getTerritory()) {
                if (t.getVariation() == null)
                    continue;
                ArrayList<Action> shuffleActions = new ArrayList<>(Arrays.asList(t.getVariation().getActions()));
                Collections.shuffle(shuffleActions);
                for(Action a : shuffleActions){
					if(a.isPerformableTo(p) == false)
						continue;
					if(a instanceof ActionDestroyImprovement)
						continue;
					if(a instanceof ActionClearForest)
						continue;
					for(Consequence con : a.getConsequences(p))
						if(con instanceof ConseqGainStars)
						{
                            if (a.isPerformableTo(p))
							    a.apply(p);
                            if(doSleep > 0) {System.out.println(a.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
							break;
						}
				}
            }
					
	}
	public static void UnlockTechActions(int playerId){
		Player p = Game.players[playerId];
		int maxStarUse = remainStars * (Game.getTurn() + 29)/ (3 * Game.getTurn() + 27);
		int itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
            for(int j = 0; j <= 100; j ++){
                knapsack[j][i] = -100000;
			    recordItem[j][i] = -1;
            }
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
            System.out.printf("%s %d\n", te.toString(), cnt);
		}
        knapsack[0][0] = 0;
		for(int i = 0; i < itemnum; i ++){
            //System.out.printf ("i %d %d %d\n", i, itemList[i].cost, maxStarUse);
			for(int j = maxStarUse; j >= 0; j --){
                knapsack[i+1][j] = knapsack[i][j];
                recordItem[i+1][j] = -1;
                if(j < itemList[i].cost)
                    continue;
                //System.out.printf ("j %d\n", j);
				if(knapsack[i][j - itemList[i].cost] + itemList[i].totalReward > knapsack[i+1][j]){
					knapsack[i+1][j] = knapsack[i][j - itemList[i].cost] + itemList[i].totalReward;
					recordItem[i+1][j] = i;
				}
			}
		}
		int maxKnapsackReward = -1;
		int maxKnapsackSpot = 0;
		for(int i = 0; i < maxStarUse; i ++){
			if(maxKnapsackReward < knapsack[itemnum][i]){
				maxKnapsackReward = knapsack[itemnum][i];
				maxKnapsackSpot = i;
			}
		}

		for(int i = itemnum; i > 0; i --){
			int techId = recordItem[i][maxKnapsackSpot];
            if (techId == -1)
                continue;
			Tech tech = (Tech.getUnlockableTechs(p)).get(techId);
			Action a = new ActionUnlockTech(tech);
            if (a.isPerformableTo(p))
                a.apply(p);
            if(doSleep > 0) {System.out.println(a.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
			maxKnapsackSpot = maxKnapsackSpot - itemList[techId].cost;
		}
	}
	public static void CommandUnitActions(int playerId){
		Actions actionList = new Actions();
		Player p = Game.players[playerId];
		for(Unit u : p.getUnits())
			for(Action a : u.getActions())
				if(a instanceof ActionUnitUpgrade){
                    if (a.isPerformableTo(p))
					    a.apply(p);
                    if(doSleep > 0) {System.out.println(a.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
					break;
				}
		for(Unit u : p.getUnits()){
			if(u.getHealth() <= u.getMaxHealth() * 3 / 10){
				for(Action a : u.getActions())
					if(a instanceof ActionUnitRecover){
                        if (a.isPerformableTo(p))
						    a.apply(p);
                        if(doSleep > 0) {System.out.println(a.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
						break;
					}
			}
		}
		Action maxRewardAction;
		int maxReward = 0;
		while(true){
			actionList.clear();
			maxRewardAction = null;
			maxReward = -1;
			for(int i = 0; i < size; i ++){
				for(int j = 0; j < size; j ++){
					value[i][j] = 0;
					used[i][j] = 0;
				}
			}
			for(Tile t : p.getVision()){
				used[t.getX()][t.getY()] = 1;
			}
			for(int i = 4; i >= 1; i --){
                ArrayList<Tile> border = new ArrayList<Tile>();
				for(Tile t : p.getVision()){
                    if (used[t.getX()][t.getY()] == 0)
                        continue;
					for(Tile t_1 : TileMap.getInnerRing(Game.map.getGrid(), t.getX(), t.getY())){
						int x = t_1.getX();
						int y = t_1.getY();
						if(used[x][y] == 0){
							value[t.getX()][t.getY()] = i;
							border.add(t);
                            break;
						}
					}
				}
                for (Tile t : border)
                    used[t.getX()][t.getY()] = 0;
			}
			for(Unit u : p.getUnits()){
				for(Action a : u.getActions()){
					if(a.isPerformableTo(p) == false)
						continue;
                    //if (a instanceof ActionUnitRecover || a instanceof ActionUnitUpgrade)
                    //    continue;
					actionList.addOne(a);
					int actionvalue = 0;
					if(a instanceof ActionUnitMove){
						Tile t = ((ActionUnitMove)a).getDestination();
						int x = t.getX();
						int y = t.getY();
						actionvalue += value[x][y];
						int flag = 0;
						for(int i = 0; i < size; i ++){
							for(int j = 0; j < size; j ++){
								if(Math.max(Math.abs(x - i), Math.abs(y - j)) == u.getRange()){
									Tile t_1 = Game.map.getGrid()[i][j];
									if(t_1.hasEnemy(p)){
										actionvalue += extraValueForCommand;
										flag = 1;
									}
								}
                                if(flag == 1) break;
							}
							if(flag == 1) break;
						}

					}
					for(Consequence con : a.getConsequences(p)){
						actionvalue += con.getReward();
					}
					if(actionvalue > maxReward){
						maxReward = actionvalue;
						maxRewardAction = a;
					}
				}
			}
			if(actionList.isEmpty())
				break;
			if(maxReward < 0)
				break;
			maxRewardAction.apply(p);
            if(doSleep > 0) {System.out.println(maxRewardAction.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
		}
		for(Unit u : p.getUnits())
			for(Action a : u.getActions())
				if(a instanceof ActionUnitUpgrade){
                    if (a.isPerformableTo(p))
					    a.apply(p);
                    if(doSleep > 0) {System.out.println(a.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
					break;
				}			
	}
	public static void DevelopEconomyActions(int playerId){
		Actions actionList = new Actions();
		Player p = Game.players[playerId];
		int maxValidationUse = remainStars;
		int itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
            for(int j = 0; j <= 100; j ++){
                knapsack[j][i] = -100000;
			    recordItem[j][i]= -1;
            }
			itemList[i].setValue(0,0);
		}
		knapsack[0][0] = 0;
		for(City c : p.getCities())
			for(Tile t : c.getTerritory()) {
                if (t.getVariation() == null)
                    continue;
                ArrayList<Action> shuffleActions = new ArrayList<>(Arrays.asList(t.getVariation().getActions()));
                Collections.shuffle(shuffleActions);
                for(Action a : shuffleActions){
					if(a.isPerformableTo(p) == false)
						continue;
					actionList.addOne(a);
					int totalReward = 0;
					int cost = a.getCost();
					for(Consequence con : a.getConsequences(p)){
						if(con instanceof ConseqGainStars){
							System.out.println("still have consequences that add stars: " + con.toString());
						}
						totalReward = totalReward + con.getReward();
					}
					itemList[itemnum++].setValue(totalReward, cost);
				}
            }
		for(int i = 0; i < itemnum; i ++){
			for(int j = maxValidationUse; j >= 0; j --){
                knapsack[i+1][j] = knapsack[i][j];
                recordItem[i+1][j] = -1;
                if(j < itemList[i].cost)
                    continue;
				if(knapsack[i][j - itemList[i].cost] + itemList[i].totalReward > knapsack[i+1][j]){
					knapsack[i+1][j] = knapsack[i][j - itemList[i].cost] + itemList[i].totalReward;
					recordItem[i+1][j] = i;
				}
			}
		}
		int maxKnapsackReward = -1;
		int maxKnapsackSpot = 0;
		for(int i = 0; i < maxValidationUse; i ++){
			if(maxKnapsackReward < knapsack[itemnum][i]){
				maxKnapsackReward = knapsack[itemnum][i];
				maxKnapsackSpot = i;
			}
		}
		for(int i = itemnum; i > 0; i --){
			int actionId = recordItem[i][maxKnapsackSpot];
            if(actionId < 0)
                continue;
			Action tempAction = actionList.getAction(actionId);
            if (tempAction.isPerformableTo(p))
			    tempAction.apply(p);
            if(doSleep > 0) {System.out.printf("%s, %d\n", tempAction.toString(), actionId);try{Thread.sleep(doSleep);}catch(Exception e){}}
			maxKnapsackSpot = maxKnapsackSpot - itemList[actionId].cost;
		}


		remainStars = p.getStars();
		int maxTileUse = remainStars;
		actionList.clear();
		itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
            for(int j = 0; j <= 100; j ++){
                knapsack[j][i] = -100000;
			    recordItem[j][i]= -1;
            }
			itemList[i].setValue(0,0);
		}
		knapsack[0][0] = 0;
		for(City c : p.getCities())
			for(Tile t : c.getTerritory()) {
                ArrayList<Action> shuffleActions = new ArrayList<>(Arrays.asList(t.getActions()));
                Collections.shuffle(shuffleActions);
                for(Action a : shuffleActions){
					if(a instanceof ActionClearForest)
						continue;
					if(a.isPerformableTo(p) == false)
						continue;
					actionList.addOne(a);
					int totalReward = 0;
					int cost = a.getCost();
					for(Consequence con : a.getConsequences(p)){
						if(con instanceof ConseqGainStars){
							System.out.println("still have consequences that add stars: " + con.toString());
						}
						totalReward = totalReward + con.getReward();
					}
					itemList[itemnum++].setValue(totalReward, cost);
				}
            }

		for(int i = 0; i < itemnum; i ++){
			for(int j = maxTileUse; j >= 0; j --){
                knapsack[i+1][j] = knapsack[i][j];
                recordItem[i+1][j] = -1;
                if(j < itemList[i].cost)
                    continue;
				if(knapsack[i][j - itemList[i].cost] + itemList[i].totalReward > knapsack[i+1][j]){
					knapsack[i+1][j] = knapsack[i][j - itemList[i].cost] + itemList[i].totalReward;
					recordItem[i+1][j] = i;
				}
			}
		}
		maxKnapsackReward = -1;
		maxKnapsackSpot = 0;
		for(int i = 0; i < maxTileUse; i ++){
			if(maxKnapsackReward < knapsack[itemnum][i]){
				maxKnapsackReward = knapsack[itemnum][i];
				maxKnapsackSpot = i;
			}
		}
		for(int i = itemnum; i > 0; i --){
			int actionId = recordItem[i][maxKnapsackSpot];
            if(actionId == -1)
                continue;
			Action tempAction = actionList.getAction(actionId);
            if (tempAction.isPerformableTo(p))
			    tempAction.apply(p);
            if(doSleep > 0) {System.out.printf("%s, %d\n", tempAction.toString(), actionId);try{Thread.sleep(doSleep);}catch(Exception e){}}
			maxKnapsackSpot = maxKnapsackSpot - itemList[actionId].cost;
		}
	}
	public static void TrainUnitActions(int playerId){
		Actions actionList = new Actions();
		Player p = Game.players[playerId];
		int maxStarUse = remainStars * 2 / (3 + Math.min(p.getUnits().size()/5, 3));

		int itemnum = 0;
		for(int i = 0; i <= 1000; i ++){
            for(int j = 0; j <= 100; j ++){
                knapsack[j][i] = -100000;
			    tempKnapsack[j][i] = -100000;
			    recordItem[j][i] = -1;
            }
			itemList[i].setValue(0,0);
		}
		knapsack[0][0] = 0;
        int cntcities = 0;
		for(City c : p.getCities())
		{
            for(int j = maxStarUse; j >= 0; j --){
                knapsack[cntcities+1][j] = knapsack[cntcities][j];
                recordItem[cntcities+1][j] = -1;
            }
			for(Action a : c.getActions())
				if(a.isPerformableTo(p)){
					actionList.addOne(a);
					int totalReward = 0;
					int cost = a.getCost();
					for(Consequence con : a.getConsequences(p)){
						if(con instanceof ConseqGainStars){
							System.out.println("still have consequences that add stars: " + con.toString());
						}
						totalReward = totalReward + con.getReward();
					}
					itemList[itemnum++].setValue(totalReward, cost);
					int i = itemnum - 1;
					for(int j = maxStarUse; j >= itemList[i].cost; j --){
						if(knapsack[cntcities][j - itemList[i].cost] + itemList[i].totalReward > knapsack[cntcities + 1][j]){
							knapsack[cntcities+1][j] = knapsack[cntcities][j - itemList[i].cost] + itemList[i].totalReward;
							recordItem[cntcities+1][j] = i;
						}
					}
				}
            cntcities++;
		}
		int maxKnapsackReward = -1;
		int maxKnapsackSpot = 0;
		for(int i = 0; i < maxStarUse; i ++){
			if(maxKnapsackReward < knapsack[cntcities][i]){
				maxKnapsackReward = knapsack[cntcities][i];
				maxKnapsackSpot = i;
			}
		}
		for(int i = cntcities; i > 0; i --){
			int actionId = recordItem[i][maxKnapsackSpot];
            if(actionId == -1)
                continue;
			Action tempAction = actionList.getAction(actionId);
            if (tempAction.isPerformableTo(p))
			    tempAction.apply(p);
            if(doSleep > 0) {System.out.println(tempAction.toString());try{Thread.sleep(doSleep);}catch(Exception e){}}
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

        System.out.printf ("Turn %d: ", Game.getTurn());
        for (Tech tech : Game.players[playerId].getTechs()){
            System.out.printf ("%s, ", tech.toString());
        }
        System.out.println();

        if(doSleep > 0) {System.out.println();try{Thread.sleep(doSleep);}catch(Exception e){}}
        Action a = new ActionEndTurn();
        a.apply(Game.players[playerId]);      
        
	}
}
