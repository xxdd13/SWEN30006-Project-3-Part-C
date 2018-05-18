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
	
	public Navigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder) {
		
		this.map = map;
		this.pathfinder = pathfinder;
		
		finish = new ArrayList<>();
		
		map.forEach((k,v) -> {
			if (v.isType(MapTile.Type.FINISH)) {
				finish.add(k);
			}
			
		});
		
		
	}
	
	
	public List<Coordinate> planRoute(Coordinate location, Coordinate targetLocation) {
		List<Coordinate> target = Arrays.asList(targetLocation);
		System.out.println("new target: "+target+ "                currently at "+location);
		route = pathfinder.planRoute(location, target, map);
		return route;
	}

	
/** updates the map with new traps, and replans route if necessary
 *  returns true when route is replanned  **/
	public boolean updateMap(HashMap<Coordinate, MapTile> view) {
		this.map = view;
		return true;
	}
	
	public List<Coordinate> getRoute() {
		return this.route;
	}


	public void setMap(HashMap<Coordinate, MapTile> newMap) {
		// TODO Auto-generated method stub
		
	}

}
