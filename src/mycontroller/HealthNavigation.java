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
	List<Coordinate> healths = new ArrayList<>();

	
	public HealthNavigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder) {
		
		super(map, pathfinder);
		
	}
	private float calcDistance(Coordinate c1, Coordinate c2) {
		
		return (float) Math.sqrt(Math.pow(c1.x-c2.x, 2)  +  Math.pow(c1.y-c2.y, 2) );
	}
	@Override
	public List<Coordinate> planRoute(Coordinate location, Coordinate targetLocation) {

		//sort by distance to car, find the closest one
		class HealthTrapComparator implements Comparator<Coordinate> {
		    @Override
		    public int compare(Coordinate a, Coordinate b) {
		        if(calcDistance(location,a) < calcDistance(location,b)) {
		        		return -1;
		        }
		        else if(  Math.abs(calcDistance(location,a)- calcDistance(location,b)) < 0.0001) {
		        		return 0; 
		        }
		        else {
		        	return 1;
		        }
		    }
		}
		
		//Collections.sort(this.healths, new HealthTrapComparator());

		List<Coordinate> healthTarget = new ArrayList<>();
		//healthTarget.add(this.healths.get(0)); 
		
		
		
		System.out.println("HEALING !                currently at "+location);
		
		route = pathfinder.planRoute(location, this.healths, super.map);
		
		return route;
	}
	public void addHealthSpot(Coordinate c) {
		//only adds if it doesn't have it
		if (!this.healths.contains(c))this.healths.add(c);
	}
	
}
