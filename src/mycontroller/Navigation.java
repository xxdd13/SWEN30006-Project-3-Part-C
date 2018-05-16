package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

public class Navigation {

	HashMap<Coordinate, MapTile> map;
	List<Coordinate> finish;
	List<Coordinate> route;
	IPathFinder pathfinder;
	List<Coordinate> lavas = new ArrayList<>();
	public List<Coordinate> visited = new ArrayList<>();
	
	public Navigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder) {
		
		this.map = map;
		this.pathfinder = pathfinder;
		
		//adds finish tiles
		finish = new ArrayList<>();
		
		map.forEach((k,v) -> {
			if (v.isType(MapTile.Type.FINISH)) {
				finish.add(k);
			}
			if (v instanceof LavaTrap) {
				lavas.add(k);
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
		
	}
	
	
	public List<Coordinate> planRoute(Coordinate location) {
		List<Coordinate> target = new ArrayList<Coordinate>();

		//没踩过的作为目标
		for (Coordinate c : lavas) {
		    if (!visited.contains(c)) {
		    	
		    		/*
		    		if(visited.size()-1 >=0) {
		    			Coordinate lastVisited = visited.get(visited.size()-1);
		    			
		    			//防止u turn
			    		if(Math.abs(lastVisited.x-c.x)> 1 && Math.abs(lastVisited.y-c.y)>1) {
			    			target.add(c);
					    break;
			    		}
		    		}
		    		else {
		    			target.add(c);
					break;
		    		}
		    		
		    		*/
		    	
		    	
		    	
		    		target.add(c);
		    		break;
		    
		    }
		    
		    
		}
		
		System.out.println("new target: "+target);
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
			route = planRoute(location);
		}
		return replan;	
	}
	
	List<Coordinate> getRoute() {
		return this.route;
	}

}
