package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import tiles.MapTile;
import utilities.Coordinate;

public class DijkstraPathFinder implements IPathFinder {

	
	
	public DijkstraPathFinder() {

	}

	
	@Override
	public List<Coordinate> getShortestPath(Coordinate start, List<Coordinate> targets, HashMap<Coordinate, MapTile> map) {
		HashMap<Coordinate, Node> settledNodes = new HashMap<>();
		Queue<Node> unsettledNodes = new PriorityQueue<>();
		boolean pathFound = false;

		Node current = new Node(null, start);
		current.setWeight(0);
		settledNodes.put(current.coord, current);
		unsettledNodes.add(current);
		
		while (!unsettledNodes.isEmpty()) {
			current = unsettledNodes.remove();
			current.visited = true;
			
			//we have found a path
			if (targets.contains(current.coord)) {
				pathFound = true;
				break;
			}
			// for each adjacent node 
			for (Node adjNode : current.getAdjacentNodes()){
				//set weight for each node depends on their tile type(lava/heal/wall/road)
				setWeight(adjNode, map);
				
				// means this node''s tile is not wall
				if (adjNode.weight < Double.POSITIVE_INFINITY) {
					//if settledNodes doesn't have this adj node, put it in
					if (!settledNodes.containsKey(adjNode.coord)) {
						settledNodes.put(adjNode.coord, adjNode);
						unsettledNodes.add(adjNode);
					}
					// if it has it and it has not been visited before
					else if (settledNodes.get(adjNode.coord).visited == false) {
						// if we have found a better node choice
						if (adjNode.weight < settledNodes.get(adjNode.coord).weight) {
							unsettledNodes.remove(settledNodes.get(adjNode.coord));
							unsettledNodes.add(adjNode);
							settledNodes.remove(adjNode.coord);
							settledNodes.put(adjNode.coord, adjNode);
						}
					}
				}
			};
		}
		
		
		List<Coordinate> path = new ArrayList<>();
		
		//no path found
		if (!pathFound) {
			return null;
		}
		
		//recrate path from first node 
		while (current != null) {
			path.add(0, current.coord);
			current = current.parent;
		}
		System.out.println("path: "+ path);
		return path;
	}
	
	/**
	 * set node's weight based on its tile type
	 * wall will have infinity weight, since can't pass through wall
	 * lava will have more weight than road and heal trap
	 * @param node
	 * @param map
	 */
	public void setWeight(Node node, HashMap<Coordinate, MapTile> map) {
		MapTile tile = map.get(node.coord);
		ITileWeight weights = TileWeightFactory.getInstance().getWeight(tile);
		node.setWeight(weights.getWeight(node, map) + node.parent.weight);
	}

}
