package mycontroller;

import java.util.List;
import utilities.Coordinate;

public interface INavigationStrategy {

	List<Coordinate> getRoute();

	List<Coordinate> getShortestPath(Coordinate currentCoordinate, Coordinate coordinate, Map map);
	
}
