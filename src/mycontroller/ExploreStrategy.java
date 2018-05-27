package mycontroller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import utilities.Coordinate;


/**
 * 
 * strategy to navigate to finishline
 *
 */
public class ExploreStrategy implements INavigationStrategy{

	List<Coordinate> path;
	IPathFinder pathfinder;
	
	
	public ExploreStrategy() {
		pathfinder = new DijkstraPathFinder();	
	}
		
	public List<Coordinate> getRoute() {
		return path;
	}
	

	@Override
	public List<Coordinate> getShortestPath(Coordinate currentCoordinate, Coordinate coordinate, Map map) {
		List<Coordinate> unvisitedCoords = map.getUnvisitedCoords(currentCoordinate);
		
		 Collections.sort(unvisitedCoords, new Comparator<Coordinate>() {
		        @Override
		        public int compare(Coordinate c1, Coordinate c2) {
		            return c1.x - c2.x;
		        }
		    });  
		
		path = pathfinder.getShortestPath(currentCoordinate, unvisitedCoords, map.getMap());
		System.out.println(currentCoordinate+"  "+path);
		return path;
	}


}
