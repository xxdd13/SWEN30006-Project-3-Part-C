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


public class HealthNavigation extends Navigation{

	public HealthNavigation(IPathFinder pathfinder) {
		
		super(pathfinder);
		
	}
	
	@Override
	public List<Coordinate> getShortestPath(Coordinate location, Coordinate targetLocation,Map map) {
		
		path = pathfinder.getShortestPath(location, map.healths, map.getMap());
		return path;
	}
	
	
}
