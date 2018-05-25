package mycontroller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import controller.CarController;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;


public class Map {
	private HashMap<Coordinate, MapTile> map;
	private HashMap<Coordinate, Boolean> visitedMap = new HashMap<>();
	private int totalKeys;
	private Coordinate[] keyList;
	public List<Coordinate> healths = new ArrayList<>();
	public List<Coordinate> finishes = new ArrayList<>();
	
	public Map(HashMap<Coordinate, MapTile> initialMap,int totalKeys) {
		this.map = initialMap;
		this.totalKeys = totalKeys;
		keyList = new Coordinate[totalKeys-1];
		
		for (HashMap.Entry<Coordinate, MapTile> entry: map.entrySet()) {
	    		Coordinate k = entry.getKey();
	    		System.out.println(k);
	    		visitedMap.put(k, false);
		}
		
	}
	public boolean hasAllKeys() {
		for (int i=0;i<(keyList.length-1);i++) {
			if (keyList[i]==null) return false;
		}
		return true;
	}
	public Coordinate  nextKey(int keyNumber) {
		return keyList[keyNumber-2];
	}
	public void updateMap(HashMap<Coordinate, MapTile> view){
		
		
		
        for (HashMap.Entry<Coordinate, MapTile> entry: view.entrySet()) {
        		
        		Coordinate k = entry.getKey();
        		MapTile v = entry.getValue();
        		
        		
	        	if(v.equals(map.get(k))) {
	                 map.remove(k);
			}
			if (v instanceof LavaTrap) {
			     LavaTrap lava = new LavaTrap();
			     map.put(k,lava);
			     
			     int keyNum =((LavaTrap) v).getKey();
			     
			 	 if( keyNum>0 && keyList[keyNum-1]==null) {
			 	 keyList[keyNum-1]=k;
			 	 System.out.println("found key: "+keyNum+" at "+k);
			 	 } 
			}else if (v instanceof HealthTrap) {
			     HealthTrap health = new HealthTrap();
			     map.put(k,health);
			     healths.add(k);            
			}
			else {
				if(v.isType(MapTile.Type.FINISH)) finishes.add(k);
			     map.put(k, v);
			}
        } 
	}
	public HashMap<Coordinate, MapTile> getMap(){
		return this.map;
	}
	
	public boolean keyInView(HashMap<Coordinate, MapTile> currentView, int keyNum) {
		if(keyNum==1) { //we have found all keys
			return false;
		}
		MapTile key = currentView.get(keyList[keyNum-2]);
			
		return key != null && (keyNum-2)==((LavaTrap) key).getKey();
	}
	
	public void markVisited(Coordinate coord) {
		if(visitedMap.get(coord) == false) {
			visitedMap.put(coord,true);
			
		}
		
	}

}
