package mycontroller;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;

import tiles.MapTile;
import utilities.Coordinate;

public class Navigation {

	List<Coordinate> path;
	IPathFinder pathfinder;
	
	public Navigation(IPathFinder pathfinder) {
		
		this.pathfinder = pathfinder;
		
		
	}
	
	
	public List<Coordinate> getShortestPath(Coordinate location, Coordinate targetLocation, Map map) {
		List<Coordinate> target = Arrays.asList(targetLocation);
		System.out.println("new target: "+target+ "                currently at "+location);
		path = pathfinder.getShortestPath(location, target, map.getMap());
		return path;
	}

	
	
	public List<Coordinate> getRoute() {
		return this.path;
	}


}
