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
	private WorldSpatial.RelativeDirection lastTurnDirection = null; 
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
	private boolean exploreMode;
	
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
		map.markVisited(currentView);
		map.updateMap(currentView);
		if (exploreMode && map.hasAllKeys()) exploreMode=false;
		if( map.hasAllKeys() || exploreMode) {  //ALL keys have been found
			CAR_SPEED = FINAL_CAR_SPEED;
			if(!startGetKey)startGetKey = true;
			try {
				if(exploreMode &&(path==null||path.size()<=0) ){
					path = StrategyFactory.getInstance().getStrategy("ExploreStrategy").getShortestPath(currentCoordinate, null,map);
				}
				else if(getKey()==1) { //got all keys ! go to finish line
						path = StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, map.finishes.get(0),map);
				}
				//plan new path
				else if(path==null||path.size()<=0 ) { //if current path finish or don't have one yet
					System.out.println("current key number: "+(getKey()-1));
					path = StrategyFactory.getInstance().getStrategy("NormalStrategy").getShortestPath(currentCoordinate, map.nextKey(getKey()),map);			
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
				return;
			}
			if(currentTile instanceof HealthTrap && getHealth()<80 && !checkWallAhead(getOrientation(),currentView)) { //stop to heal
				if(Math.abs(getSpeed())>0.5) {
					applyReverseAcceleration();
				}else if(Math.abs(getSpeed())<0.5) {
					applyForwardAcceleration();
				}
				
				halting = true;
				System.out.println("Halt !!!!!!!!!!!!!!");
				
			}
			else {
				halting = false;
				map.updateMap(currentView); //search key and health spot in the view
				
				checkStateChange();

				// If you are not following a wall initially, find a wall to stick to!
				if(!isFollowingWall){
					if(getSpeed() < CAR_SPEED){
						applyForwardAcceleration();
					}
					// Turn towards the north
					if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					if(checkNorth(currentView)){
						// Turn right until we go back to east!
						if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							applyRightTurn(getOrientation(),delta);
						}
						else{
							isFollowingWall = true;
						}
					}
				}
				// Once the car is already stuck to a wall, apply the following logic
				else{
					
					// Readjust the car if it is misaligned.
					readjust(lastTurnDirection,delta);
					
					if(isTurningRight){
						applyRightTurn(getOrientation(),delta);
					}
					else if(isTurningLeft){
						// Apply the left turn if you are not currently near a wall.
						if(!checkFollowingWall(getOrientation(),currentView)){
							applyLeftTurn(getOrientation(),delta);
						}
						else{
							isTurningLeft = false;
						}
					}
					// Try to determine whether or not the car is next to a wall.
					else if(checkFollowingWall(getOrientation(),currentView)){
						// Maintain some velocity
						if(getSpeed() < CAR_SPEED){
							applyForwardAcceleration();
						}
						// If there is wall ahead, turn right!
						if(checkWallAhead(getOrientation(),currentView)){
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							isTurningRight = true;	
						}

					}
					// This indicates that I can do a left turn if I am not turning right
					else{
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						isTurningLeft = true;
					}
				}
			}
			
			
		}
		
		
		
		
		

	}
	
	
	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		if(lastTurnDirection != null){
			if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
				adjustRight(getOrientation(),delta);
			}
			else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
				adjustLeft(getOrientation(),delta);
			}
		}
		
	}
	
	/**
	 * orient car to a degree if misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(getAngle() > WorldSpatial.NORTH_DEGREE){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE){
				turnRight(delta);
			}
			break;
		case WEST:
			if(getAngle() > WorldSpatial.WEST_DEGREE){
				turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(getAngle() < WorldSpatial.NORTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(getAngle() < WorldSpatial.SOUTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(getAngle() < WorldSpatial.WEST_DEGREE){
				turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = getOrientation();
		}
		else{
			if(previousState != getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = getOrientation();
			}
		}
	}
	
	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnLeft(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnRight(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnRight(delta);
			}
			break;
		default:
			break;
		
		}
		
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
		default:
			return false;
		
		}
	}
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);// ||checkNorthFinish(currentView) ;
		case NORTH:
			return checkWest(currentView);//||checkWestFinish(currentView);
		case SOUTH:
			return checkEast(currentView);//||checkEastFinish(currentView);
		case WEST:
			return checkSouth(currentView);//||checkSouthFinish(currentView);
		default:
			return false;
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(map.nextKey(getKey()))	) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(map.nextKey(getKey()))	) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(	map.nextKey(getKey()))	) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(	map.nextKey(getKey()))	) {
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
				if(		getY()<(float)(path.get(0).y)	) {
					return false;
				}
			}
			if(getMyOrientation().equals(WorldSpatial.Direction.SOUTH)){
				if(		getY()>(float)(path.get(0).y-0.2)	) {
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
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoNorth(Coordinate currentCoordinate){
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
		
		if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
			if(		getX()>(float)(path.get(0).x-0.2)&& path.get(0).x >0	) {
				return false;
			}
		}
		if(getMyOrientation().equals(WorldSpatial.Direction.EAST)){
			if(		getX()<(float)(path.get(0).x+0.9) && World.MAP_WIDTH-2-(int)getX()==0) {
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
		}else {
			turnRight(270f/150f);
		}
		
		System.out.print(orientation);System.out.println("left");
	}
	public void myTurnRight(Direction orientation) {
		customOrientation = orientation;
		if(orientation.equals(WorldSpatial.Direction.WEST) ||orientation.equals(WorldSpatial.Direction.SOUTH)) {
			turnLeft(270f/150f);
		}else {
			turnRight(90f/150f);
		}
		
		
		System.out.print(orientation);System.out.println("right");
	}
	
	public void myUTurn(Direction orientation) {
		customOrientation = orientation;
		turnLeft(180f/150f);
		
	}
	
	
	
	
}