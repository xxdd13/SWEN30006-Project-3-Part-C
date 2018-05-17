package mycontroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

public class Navigation {

	HashMap<Coordinate, MapTile> map;
	List<Coordinate> finish;
	List<Coordinate> route;
	IPathFinder pathfinder;
	List<Coordinate> lavas = new ArrayList<>();
	List<Coordinate> healths = new ArrayList<>();
	Coordinate[] keys = new Coordinate[4];
	List<Coordinate>keyList = new ArrayList<>();
	public List<Coordinate> visited = new ArrayList<>();
	
	public Navigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder,List<Coordinate> visitedList) {
		
		this.map = map;
		this.pathfinder = pathfinder;
		this.visited = visitedList;
		
		//adds finish tiles
		finish = new ArrayList<>();
		
		map.forEach((k,v) -> {
			if (v.isType(MapTile.Type.FINISH)) {
				finish.add(k);
			}
			if (v instanceof LavaTrap) {
				int keyNum =((LavaTrap) v).getKey();
				if( keyNum>0) keys[keyNum-1]=k;
				lavas.add(k);
			}
			if (v instanceof HealthTrap) {
				healths.add(k);
			}
		});
		
		//sort lava x first then y
		class CoordComparatorXY implements Comparator<Coordinate> {
		    @Override
		    public int compare(Coordinate a, Coordinate b) {
		        if(a.x<b.x) {
		        		return -1;
		        }
		        else if(a.x==b.x) {
		        		return a.y < b.y ? -1 : a.y == b.y ? 0 : 1;        
		        }
		        else {
		        	return 1;
		        }
		    }
		}
		//先x sort 再 y
		class CoordComparatorYX implements Comparator<Coordinate> {
		    @Override
		    public int compare(Coordinate a, Coordinate b) {
		        if(a.y<b.y) {
		        		return -1;
		        }
		        else if(a.y==b.y) {
		        		return a.x < b.x ? -1 : a.x == b.x ? 0 : 1;        
		        }
		        else {
		        	return 1;
		        }
		    }
		}
		
		
		Collections.sort(lavas, new CoordComparatorXY());
		//Collections.sort(lavas, new CoordComparatorYX());
		keyList = Arrays.asList(keys);
		
	}
	
	
	public List<Coordinate> planRoute(Coordinate location, Coordinate targetLocation) {
		List<Coordinate> target = Arrays.asList(targetLocation);
		
		System.out.println("new target: "+target+ "                currently at "+location);
		route = pathfinder.planRoute(location, target, map);
		return route;
	}

	
/** updates the map with new traps, and replans route if necessary
 *  returns true when route is replanned  **/
	public boolean updateMap(HashMap<Coordinate, MapTile> view, Coordinate location) {
		boolean replan = false;
		for (HashMap.Entry<Coordinate, MapTile> entry: view.entrySet()) {
			if (entry.getValue().isType(MapTile.Type.TRAP) && !map.get(entry.getKey()).isType(MapTile.Type.TRAP)) {
				map.remove(entry.getKey());
				map.put(entry.getKey(), entry.getValue());
				if (route.contains(entry.getKey())) {
					replan = true;
				}
			}
		}
		if (replan == true) {
			route = planRoute(location, location);
		}
		return replan;	
	}
	
	public List<Coordinate> getRoute() {
		return this.route;
	}

}
