package mycontroller;


import java.util.HashMap;
import java.util.List;

import controller.CarController;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;
import world.WorldSpatial.Direction;


public class MyAIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	private boolean isFollowingWall = false;
	private WorldSpatial.RelativeDirection lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT; 
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	private int EAST_THRESHOLD = 3;
	
	private boolean onTrack = false;
	private final float FINAL_CAR_SPEED =5f;
	private float CAR_SPEED = 3;
	private float TURN_SPEED_1 = 4.9f;
	private float TURN_SPEED_2 = 5f;
	private List<Coordinate> path;
	private boolean halting = false;
	private Map map = new Map(getMap(),getKey());
	private boolean startGetKey = false; 
	private Direction customOrientation = WorldSpatial.Direction.EAST;
	private boolean exploreMode = true;
	
	public MyAIController(Car car) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super(car);
		//navigation = new Navigation(new DijkstraPathFinder());
		
		
	}
	
	@Override
	public void update(float delta) {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		Coordinate currentCoordinate = new Coordinate(this.getPosition());
		MapTile currentTile = currentView.get(currentCoordinate);
		
		//update map and mark coord in current view as visited
		map.markVisited(currentView);
		map.updateMap(currentView);
		
		//turn off explore mode if all keys has been found
		if (exploreMode && map.hasAllKeys()) exploreMode=false;
		
		if( map.hasAllKeys() || exploreMode) {  //ALL keys have been found
			CAR_SPEED = FINAL_CAR_SPEED;
			if(!startGetKey)startGetKey = true;
			try {
				//plan new path
				if(path==null||path.size()<=0 ) { //if current path finish or don't have one yet
					if(getKey()==1) { //got all keys ! go to finish line
						path = StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, map.finishes.get(0),map);
						
					}else if(exploreMode ){ // might be stuck in a loop , explore unvisited nodes
						path = StrategyFactory.getInstance().getStrategy("ExploreStrategy").getShortestPath(currentCoordinate, null,map);
						if( map.keyInView(currentView, getKey()) ) {
							System.out.println("early");
							path = StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, map.nextKey(getKey()),map);
							path.remove(0);
						}
						
					}else if(getKey() >=1){ //get key
						System.out.println("current key number: "+(getKey()-1));
						path = StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, map.nextKey(getKey()),map);
						path.remove(0);
						//System.out.println("current loc: "+ currentCoordinate+"    " +path.get(0));
						System.out.println("needGoSouth: "+ needGoSouth(currentCoordinate));

					}
					
				}else {
					if(currentCoordinate.equals(path.get(0))) {
						path.remove(0);
					}
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			//go find heal
			if(getHealth()<70 && !halting && !map.keyInView(currentView,getKey())&&!(currentTile instanceof LavaTrap )&&getKey()!=1) {
				try {
					if(StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, null,map)!=null){
						System.out.print("finding heal !!    ");	
						path = StrategyFactory.getInstance().getStrategy("HealthStrategy").getShortestPath(currentCoordinate, null,map);
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}	
					
			}
			//slow down to heal
			if(currentTile instanceof HealthTrap ){
				if(getHealth()<80) { //stop to heal
					if(Math.abs(getSpeed())>0.01) {
						applyReverseAcceleration();
					}else if(Math.abs(getSpeed())<0.01) {
						applyForwardAcceleration();
					}
					
					if(!halting) {
						halting = true;
						System.out.println("Halt !!!!!!!!!!!!!!mode: pathfind");
					}
					
				}
				else { //i'm on a health tile but health is enough, ready to go
					if(halting) {
						halting=false;
						try {
							path = StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, map.nextKey(getKey()),map);
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {}}	
				}
			}
			
			//car not stopped and not waiting for heal to finish
			if(!onTrack && !halting) {
							
				if(needGoNorth(currentCoordinate)){//should go to north
					//need to go north
					
					//need to go from east to north
					if(getMyOrientation().equals(WorldSpatial.Direction.EAST)){
						myTurnLeft(WorldSpatial.Direction.NORTH);
						
					}
					//need to go from west to north
					else if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
						
						myTurnRight(WorldSpatial.Direction.NORTH);
						
						
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.SOUTH)){
						myUTurn(WorldSpatial.Direction.NORTH);
					}
					
					
					else{
						
						onTrack = true;
					}
				}
				else if(needGoSouth(currentCoordinate)) {
					//need go south
					if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
						myTurnLeft(WorldSpatial.Direction.SOUTH);
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.EAST)){
						myTurnRight(WorldSpatial.Direction.SOUTH);
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.NORTH)){
						
						//TURN TO SOUTH
						myUTurn(WorldSpatial.Direction.SOUTH);
					}			
					else{
						onTrack = true;
					}
				}
				else if(needGoEast(currentCoordinate)) {
					//need go east;
					if(getMyOrientation().equals(WorldSpatial.Direction.SOUTH)){
						
						myTurnLeft(WorldSpatial.Direction.EAST);
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.NORTH)){
						if(getY()<(float)(path.get(0).y)) {
							applyForwardAcceleration();
						}
						myTurnRight(WorldSpatial.Direction.EAST);
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
						System.out.println("U turn west ->  east");
						myUTurn(WorldSpatial.Direction.EAST);
					}
					else{
						onTrack = true;
										
					}
				}
				else if(needGoWest(currentCoordinate)) {
					
					//have to go west
					if(getMyOrientation().equals(WorldSpatial.Direction.NORTH)){
						myTurnLeft(WorldSpatial.Direction.WEST);		
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.SOUTH)){			
						myTurnRight(WorldSpatial.Direction.WEST);
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.EAST)){
						System.out.println("U turn east -> west ");
						myUTurn(WorldSpatial.Direction.WEST);
					}
					
					else{
						onTrack = true;
						CAR_SPEED=FINAL_CAR_SPEED;

					}
				}
			
			}
			
			// CAR IS ON TRACK
			else {
				
				if( needMove(getMyOrientation(),currentCoordinate)){
					/*check next few coordinate in path, slow down if need to turn*/
					if(needTurn(getMyOrientation(),currentCoordinate,currentView, delta)) {
						
						CAR_SPEED = TURN_SPEED_1;
						if(getSpeed() > TURN_SPEED_1) {
							applyBrake();
						}
						else if(CAR_SPEED < TURN_SPEED_2){
							applyForwardAcceleration();
						}
					}
					/*if there is no turning in front */
					if(!needTurn(getMyOrientation(),currentCoordinate,currentView, delta) && getSpeed() < CAR_SPEED){
						applyForwardAcceleration();
						CAR_SPEED = FINAL_CAR_SPEED ;
					}

					
				}
				
				else if(!needMove(getMyOrientation(),currentCoordinate)){
					onTrack = false;
					CAR_SPEED = FINAL_CAR_SPEED;
				}

			
			}
			
			
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		}
		else {//missing keys, keep searching
			
			if(checkFinish(customOrientation, currentView)) {
				exploreMode = true;
			}
						
		}
			

	}
	

	
	private boolean checkFinish(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorthFinish(currentView) ;
		case NORTH:
			return checkWestFinish(currentView);
		case SOUTH:
			return checkEastFinish(currentView);
		case WEST:
			return checkSouthFinish(currentView);
		default:
			return false;
		}
		
	}
	
	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL) ){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	//check finish line
	public boolean checkEastFinish(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.FINISH) ){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWestFinish(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.FINISH)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorthFinish(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.FINISH)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouthFinish(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.FINISH)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * new getO for set direction after u turn 
	 */
	public WorldSpatial.Direction getMyOrientation(){
		if(customOrientation!=null) {
			return customOrientation;
		}
		return super.getOrientation();
	}



	/**
	 * @param orientation
	 * @param currentCoordinate 
	 * @param currentView 
	 * @param delta 
	 * @return
	 * check if turning is ahead by compare next few coord in path
	 * if there is a mismatch, it means the car have to turn
	 * to ensure turn will be sucessful, slow down speed
	 * */
	private boolean needTurn(WorldSpatial.Direction orientation, Coordinate currentCoordinate,HashMap<Coordinate, MapTile> currentView ,float delta) {
		boolean flag = false;
		int sizeCheck = 3;
		if (map.keyInView(currentView,getKey())) {
			return true;
		}
		switch(orientation){
		case EAST:
			if(path.size() < sizeCheck) {
				flag = true;						
				break;	
			}
			for(int i=0;i<sizeCheck;i++) {
				if(path.size() > i) {
					Coordinate frontCoord = new Coordinate(currentCoordinate.x+i+1, currentCoordinate.y);
					if(!frontCoord.equals(path.get(i)) ) {
						flag = true;
						break;
					}
				}
			}
			break;
			
	
		case NORTH:
			if(path.size() < sizeCheck) {
				flag = true;						
				break;	
			}
			
			for(int i=0;i<sizeCheck;i++) {
				if(path.size() > i) {	
					Coordinate frontCoord = new Coordinate(currentCoordinate.x, currentCoordinate.y+i+1);
					if(!frontCoord.equals(path.get(i)) ) {
						flag = true;
						return true;	
					}
				}
				
			}
			
			
			break;
		case SOUTH:
			if(path.size() < sizeCheck) {
				flag = true;						
				break;	
			}
			for(int i=0;i<sizeCheck;i++) {
				if(path.size() > i) {
					Coordinate frontCoord = new Coordinate(currentCoordinate.x, currentCoordinate.y-(i+1));
					if(!frontCoord.equals(path.get(i))	) {
						flag = true;
						break;
					}					
				}
			}
			break;
	
		case WEST:	
			if(path.size() < sizeCheck) {
				flag = true;						
				break;	
			}
			for(int i=0;i<sizeCheck;i++) {
				if(path.size() > i) {
					Coordinate frontCoord = new Coordinate(currentCoordinate.x-i-1, currentCoordinate.y);
					if(!frontCoord.equals(path.get(i)) ) {
						flag = true;
						break;
					}
				}
				
			}
			break;
		default:
			break;
		}
		
		
		return flag;
		
	}
	
	/**
	 * check if the car is following path
	 * @param orientation
	 * @param currentCoordinate 
	 * @return
	 */
	private boolean needMove(WorldSpatial.Direction orientation, Coordinate currentCoordinate) {
		
		switch(orientation){
		case EAST:
			return needGoEast(currentCoordinate);
		case NORTH:
			return needGoNorth(currentCoordinate);
		case SOUTH:
			return needGoSouth(currentCoordinate);
		case WEST:
			return needGoWest(currentCoordinate);
		default:
			return false;
		}	
	}


	
	/**
	 * @param currentCoordinate 
	 * compare path with current coord and orientation
	 * determines which direction the car should go
	 * eg needGoEast means the next coord in path is to the east of the car
	 * the car will have to turn
	 */
	public boolean needGoEast(Coordinate currentCoordinate){	
		Coordinate next = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
		if(!path.isEmpty() && path.get(0).equals(next)) {
			if(getMyOrientation().equals(WorldSpatial.Direction.NORTH)){
				Coordinate up = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
				if(		getY()<(float)(path.get(0).y)	 && !map.hasWallAtCoord(up)	) {
					return false;
				}
			}
			if(getMyOrientation().equals(WorldSpatial.Direction.SOUTH)){
				Coordinate down = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
				Coordinate left = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+1);
				if(		getY()>(float)(path.get(0).y)&&!map.hasWallAtCoord(down)	&& map.hasWallAtCoord(left)	) {
					System.out.println(1111);
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoWest(Coordinate currentCoordinate){
		Coordinate next = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
		
		if(!path.isEmpty() && path.get(0).equals(next)) {
			
			if(getMyOrientation().equals(WorldSpatial.Direction.NORTH)){
				
				Coordinate up = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
				if(		getY()>(float)(path.get(0).y-0.2)  && !map.hasWallAtCoord(up)	) {
					return false;
				}
			}
			if(getMyOrientation().equals(WorldSpatial.Direction.SOUTH)){
				
				Coordinate down = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
				if(		getY()>(float)(path.get(0).y-0.2)  && !map.hasWallAtCoord(down)	) {
					return false;
				}
			}

			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoNorth(Coordinate currentCoordinate){
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
		
		
		if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
			Coordinate left = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
			if(		getX()>(float)(path.get(0).x-0.2)  && !map.hasWallAtCoord(left)	) {
				return false;
			}
		}
		if(getMyOrientation().equals(WorldSpatial.Direction.EAST)){
			Coordinate right = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
			if(		getX()<(float)(path.get(0).x+0.2) && !map.hasWallAtCoord(right)	) {
				return false;
			}
		}
		
		if(!path.isEmpty() && path.get(0).equals(next)) {
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoSouth(Coordinate currentCoordinate){
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
		if(!path.isEmpty() && path.get(0).equals(next)) {
			if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
				if(		getX()>(float)(path.get(0).x-0.2)	) {
					return false;
				}
			}
			
			
			return true;
		}
		else {
			return false;
		}
	
	}
	/*
	 * (non-Javadoc)
	 * @see controller.CarController#getOrientation()
	 */
	public WorldSpatial.Direction getOrientation(){
		customOrientation = super.getOrientation();
		return super.getOrientation();
	}
	
	public void myTurnLeft(Direction orientation) {
		customOrientation = orientation;
		
		if(orientation.equals(WorldSpatial.Direction.NORTH)
				|| orientation.equals(WorldSpatial.Direction.EAST)) {
			turnLeft(90f/150f);
			lastTurnDirection=WorldSpatial.RelativeDirection.LEFT;
		}else {
			turnRight(270f/150f);
			lastTurnDirection=WorldSpatial.RelativeDirection.RIGHT;
		}
		
		System.out.print(orientation);System.out.println("  left");
	}
	public void myTurnRight(Direction orientation) {
		customOrientation = orientation;
		
		if(orientation.equals(WorldSpatial.Direction.WEST) ||orientation.equals(WorldSpatial.Direction.SOUTH)) {
			turnLeft(270f/150f);
			lastTurnDirection=WorldSpatial.RelativeDirection.LEFT;
		}else {
			turnRight(90f/150f);
			lastTurnDirection=WorldSpatial.RelativeDirection.RIGHT;
		}
		
		
		System.out.print(orientation);System.out.println("right");
	}
	
	public void myUTurn(Direction orientation) {
		customOrientation = orientation;
		if(lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)) {
			turnLeft(180f/150f);
			lastTurnDirection=WorldSpatial.RelativeDirection.LEFT;
		}else{
			turnRight(180f/150f);
			lastTurnDirection=WorldSpatial.RelativeDirection.RIGHT;
		}
		
	}
	
	
	
	
}