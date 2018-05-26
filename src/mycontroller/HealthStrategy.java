package mycontroller;

import java.util.List;
import utilities.Coordinate;


public class HealthStrategy  implements INavigationStrategy{
	List<Coordinate> path;
	IPathFinder pathfinder;	
	public HealthStrategy() {
		
		pathfinder=new HealthPathFinder();
	}
	public List<Coordinate> getShortestPath(Coordinate location, Coordinate targetLocation,Map map) {
		path = pathfinder.getShortestPath(location, map.healths, map.getMap());
		return path;
	}
	public List<Coordinate> getRoute() {
		return path;
	}
	
}
