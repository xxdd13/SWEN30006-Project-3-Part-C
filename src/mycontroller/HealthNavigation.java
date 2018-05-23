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
		List<Coordinate> healthTarget = new ArrayList<>();
		
		
		route = pathfinder.planRoute(location, this.healths, super.map);
		return route;
	}
	public void addHealthSpot(Coordinate c) {
		//only adds if it doesn't have it
		if (!this.healths.contains(c))this.healths.add(c);
	}
	
}
