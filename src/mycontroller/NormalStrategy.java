package mycontroller;

import java.util.Arrays;

import java.util.List;

import utilities.Coordinate;


/**
 * 
 * strategy to navigate to a given location
 *
 */
public class NormalStrategy implements INavigationStrategy{

	List<Coordinate> path;
	IPathFinder pathfinder;
	
	public NormalStrategy() {
		pathfinder = new DijkstraPathFinder();	
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
