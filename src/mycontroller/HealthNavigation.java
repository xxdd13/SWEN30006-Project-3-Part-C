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

public class HealthNavigation extends Navigation{

	
	public HealthNavigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder,List<Coordinate> visitedList) {
		
		super(map, pathfinder,visitedList);
		
	}
	private float calcDistance(Coordinate c1, Coordinate c2) {
		
		return (float) Math.abs(Math.sqrt(Math.pow(c1.x, 2)+Math.pow(c1.y, 2))- Math.sqrt(Math.pow(c2.x, 2)+Math.pow(c2.y, 2)));
	}
	@Override
	public List<Coordinate> planRoute(Coordinate location) {
		
		
		//sort by distance to car, find the closest one
		class HealthTrapComparator implements Comparator<Coordinate> {
		    @Override
		    public int compare(Coordinate a, Coordinate b) {
		        if(calcDistance(location,a) < calcDistance(location,b)) {
		        		return -1;
		        }
		        else if(  (calcDistance(location,a)- calcDistance(location,b)) < 0.0001) {
		        		return 0; 
		        }
		        else {
		        	return 1;
		        }
		    }
		}
		Collections.sort(super.healths, new HealthTrapComparator());
		List<Coordinate> healthTarget = new ArrayList<>();
		healthTarget.add(super.healths.get(1)); //prevent stack near wall
		
		System.out.println("HEALING ! new target: "+healthTarget);
		route = pathfinder.planRoute(location, healthTarget, super.map);
		return route;
	}
	
}
