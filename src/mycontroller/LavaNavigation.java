package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

public class LavaNavigation extends Navigation {
	public LavaNavigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder,List<Coordinate> visitedList) {
		
		super(map,pathfinder,visitedList);
		
	}
	
	
	public List<Coordinate> planRoute(Coordinate location) {
		List<Coordinate> target = new ArrayList<Coordinate>();

		//没踩过的作为目标
		for (Coordinate c : keyList) {
			System.out.println(c);
		    if (!visited.contains(c)) {
		    		target.add(c);
		    		break;		    
		    }	    
		}
		
		System.out.println("new target: "+target+ "                currently at "+location);
		route = pathfinder.planRoute(location, target, map);
		return route;
	}

	

}
