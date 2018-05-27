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
				
				//try to avoid tile next to a wall, to prevent stuck 
				if(map.get(adjNode.coord).isType(MapTile.Type.WALL) &&
						!map.get(current.coord).isType(MapTile.Type.WALL)) {
					//if adjacent tiles are wall, add small weight to it
					current.setWeight(current.weight+0.25f);
				}
				
				// means this tile is not wall, can be passed 
				if (adjNode.weight < Double.POSITIVE_INFINITY) {
					
					if(settledNodes.containsKey(adjNode.coord)) {
						Node settledNode = settledNodes.get(adjNode.coord);
						if (settledNode.visited == false) {// if it has it and it has not been visited before
							
							// if we have found a better node choice
							if (adjNode.weight < settledNode.weight) {
								unsettledNodes.remove(settledNode);
								unsettledNodes.add(adjNode);
								settledNodes.remove(adjNode.coord);
								settledNodes.put(adjNode.coord, adjNode);
							}
						}
					}

					//if settledNodes doesn't have this adj node, put it in
					else {
						settledNodes.put(adjNode.coord, adjNode);
						unsettledNodes.add(adjNode);
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
		//System.out.println("path: "+ path);
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
