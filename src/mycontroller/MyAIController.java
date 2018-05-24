package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import controller.CarController;
import mycontroller.DijkstraPathFinder;
import mycontroller.HealthNavigation;
import mycontroller.HealthPathFinder;
import mycontroller.Navigation;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
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
	private final float FINAL_CAR_SPEED =(float)3;
	private float CAR_SPEED = FINAL_CAR_SPEED;
	private final float STUCK_THRESHOLD = (float) 0.1;
	private float TURN_SPEED_1 = (float) 3.0;
	private float TURN_SPEED_2 = (float) 3.1;
	private final float SLOW_CAR_SPEED = (float) 3.1;
	private Navigation navigation;
	private HealthNavigation healthNavigation;
	private List<Coordinate> path;
	private List<Coordinate> finishes = new ArrayList<>();
	private boolean halting = false;
	private HashMap<Coordinate, MapTile> map = getMap();
	private boolean startGetKey = false; 
	private Direction customOrientation = WorldSpatial.Direction.EAST;
	
	public Coordinate[] keyList = new Coordinate[getKey()-1];
	
	public MyAIController(Car car) {
		super(car);
		navigation = new Navigation(map, new DijkstraPathFinder());
		healthNavigation = new HealthNavigation(map, new HealthPathFinder());
		
	}
	
	/**
	 * check ifall keys have been obtained
	 * @return
	 */
	private boolean hasAllKeys() {
		for (int i=0;i<(keyList.length-1);i++) {
			if (keyList[i]==null) return false;
		}
		return true;
	}
	/**
	 * update map,since world's getMap() don't have trap tiles
	 * @param view
	 */
	private void updateMap(HashMap<Coordinate, MapTile> view){
		
        for (HashMap.Entry<Coordinate, MapTile> entry: view.entrySet()) {
        	 if(entry.getValue().equals(map.get(entry.getKey()))) {
                 map.remove(entry.getKey());
		 }
		 if (entry.getValue() instanceof LavaTrap) {
		                 LavaTrap lava = new LavaTrap();
		                 map.put(entry.getKey(),lava);
		 }else if (entry.getValue() instanceof HealthTrap) {
		                 HealthTrap health = new HealthTrap();
		                 map.put(entry.getKey(),health);
		 }
		 else {
		                 map.put(entry.getKey(), entry.getValue());
		 }
        } 
	}
	/**
	 * get lava and health in view and update map
	 * @param view
	 */
	private void viewGetTraps(HashMap<Coordinate, MapTile> view) {
		this.updateMap(view);
		view.forEach((key,val) -> {
			
			if (val instanceof LavaTrap) {
				int keyNum =((LavaTrap) val).getKey();
				if( keyNum>0 && keyList[keyNum-1]==null) {
					keyList[keyNum-1]=key;
					System.out.println("found key: "+keyNum+" at "+key);
				} 
			}
			else if (val instanceof HealthTrap) {
				healthNavigation.addHealthSpot(key);
			}
			else if (val.isType(MapTile.Type.FINISH)) {
				finishes.add(key);
			}
		});
	}
	
	@Override
	public void update(float delta) {
		
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		Coordinate currentCoordinate = new Coordinate(this.getPosition());
		MapTile currentTile = currentView.get(currentCoordinate);
		
		//testOnly , jump straight to pathfind
		boolean test = false;
		if(test){
			keyList[0] = new Coordinate("23,15");
			keyList[1] = new Coordinate("16,13");
			keyList[2] = new Coordinate("19,2");
			map=getTestMap();
		}
		
		viewGetTraps(currentView);

		if( hasAllKeys() ) {  //ALL keys have been found
			
			if(!startGetKey)startGetKey = true;
			
			if(getKey()==1) { //got all keys ! go to finish line
				path = navigation.getShortestPath(currentCoordinate, finishes.get(0));	
			}
			//plan new path
			else if(path==null||path.size()<=0 ) { //if current path finish or don't have one yet
				System.out.println("current key number: "+(getKey()-1));
				navigation.updateMap(map);
				path = navigation.getShortestPath(currentCoordinate, keyList[getKey()-2]);			
			}else {
				if(currentCoordinate.equals(path.get(0))) {
					path.remove(0);
				}
				
			}
			//go find heal
			if(getHealth()<60 && navigation.getClass()!=healthNavigation.getClass() && !keyInView(currentView)&&!(currentTile instanceof LavaTrap )) {
				healthNavigation.updateMap(map);
				if(healthNavigation.getShortestPath(currentCoordinate, null)!=null){
					System.out.print("finding heal !!    ");
					navigation =healthNavigation;	
					navigation.updateMap(map);
					path = navigation.getShortestPath(currentCoordinate, null);		
				}	
					
			}
			//slow down to heal
			if(currentTile instanceof HealthTrap ){
				if(getHealth()<100) { //stop to heal
					if(Math.abs(getSpeed())>0.11) {
						applyReverseAcceleration();
					}else if(Math.abs(getSpeed())<0.11) {
						applyForwardAcceleration();
					}
					
					if(!halting) {
						halting = true;
						System.out.println("Halt !!!!!!!!!!!!!!mode: pathfind");
					}
					
				}
				else { //ready to go
					if(halting) {
						halting=false;
						navigation =new Navigation(this.map, new DijkstraPathFinder());	
						path = navigation.getShortestPath(currentCoordinate, keyList[getKey()-2]);
					}
					
						
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
					System.out.println("need go south");
					if(getMyOrientation().equals(WorldSpatial.Direction.WEST)){
						
						//lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						//applyLeftTurn(getOrientation(),delta);
						myTurnLeft(WorldSpatial.Direction.SOUTH);
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.EAST)){
						
						myTurnRight(WorldSpatial.Direction.SOUTH);
						
						
					}
					else if(getMyOrientation().equals(WorldSpatial.Direction.NORTH)){
						System.out.println("U turn ! north -> south");
						
						
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
						/*
						if(!checkNorth(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y+1);
							path = navigation.getShortestPath(newCoordinate, keyList[getKey()-2]);	
						}
						else if(!checkSouth(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y-1);
							path = navigation.getShortestPath(newCoordinate, keyList[getKey()-2]);	
						}
						
						*/
						myUTurn(WorldSpatial.Direction.EAST);
					}
					else{
						onTrack = true;
						
						if (getSpeed()<STUCK_THRESHOLD) {
							if(!checkNorth(currentView)) {
								applyReverseAcceleration();
								lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
								applyLeftTurn(getOrientation(),delta);
								Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y+1);
								path = navigation.getShortestPath(newCoordinate, keyList[getKey()-2]);	
								
							}
							else if(!checkSouth(currentView)) {
								applyReverseAcceleration();
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyRightTurn(getOrientation(),delta);
								Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y-1);
								path = navigation.getShortestPath(newCoordinate, keyList[getKey()-2]);	

							}
							onTrack = false;
							
							
						}
										
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
					/*if there is no turnning in from */
					if(!needTurn(getMyOrientation(),currentCoordinate,currentView, delta) && getSpeed() < CAR_SPEED){
						applyForwardAcceleration();
						CAR_SPEED = FINAL_CAR_SPEED ;
					}
					/*if no key inview and on lava, increase speed to escape*/
					if(currentTile instanceof TrapTile && !keyInView(currentView) &&
							((TrapTile)currentView.get(currentCoordinate)).canAccelerate()
								) {

							applyForwardAcceleration();
	
					}
					
				}
				
				else if(!needMove(getMyOrientation(),currentCoordinate)){
					onTrack = false;
					CAR_SPEED = SLOW_CAR_SPEED;
				}


				if(getSpeed()<STUCK_THRESHOLD && !halting) {
					//System.out.println(path.get(0)+ "  "+ currentCoordinate +"  "+getOrientation());
				}
			
			}
			
			
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		}
		else {//missing keys, keep searching
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
				viewGetTraps(currentView); //search key and health spot in the view
				
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
	 * check corner depends on current orientation
	 * check the left and right tile of the tile infront of the car
	 * @param currentCoordinate
	 * @param orientation
	 * @return
	 */
	private int checkCorner(Coordinate currentCoordinate, Direction orientation) {
		int result = 0;
		Coordinate left;
		Coordinate right;
		switch(orientation){
		case EAST:
			 left  =  new Coordinate(currentCoordinate.x-1,currentCoordinate.y+1);
			 right  =  new Coordinate(currentCoordinate.x-1,currentCoordinate.y-1);
			if (map.get(left).isType(MapTile.Type.WALL)){
				result = -1;
				break;
			}else if(map.get(right).isType(MapTile.Type.WALL)){
				result =1;
				break;
			}
			
			
			break;
		case NORTH:
			 left  =  new Coordinate(currentCoordinate.x-1,currentCoordinate.y+1);
			 right  =  new Coordinate(currentCoordinate.x+1,currentCoordinate.y+1);
			if (map.get(left).isType(MapTile.Type.WALL)){
				result = -1;
				break;
			}else if(map.get(right).isType(MapTile.Type.WALL)){
				result =1;
				break;
			}
			break;
		case SOUTH:
			 left  =  new Coordinate(currentCoordinate.x+1,currentCoordinate.y-1);
			 right  =  new Coordinate(currentCoordinate.x-1,currentCoordinate.y-1);
			if (map.get(left).isType(MapTile.Type.WALL)){
				result = -1;
				break;
			}else if(map.get(right).isType(MapTile.Type.WALL)){
				result =1;
				break;
			}
			break;
		case WEST:
			 left  =  new Coordinate(currentCoordinate.x+1,currentCoordinate.y-1);
			 right  =  new Coordinate(currentCoordinate.x+1,currentCoordinate.y+1);
			if (map.get(left).isType(MapTile.Type.WALL)){
				result = -1;
				break;
			}else if(map.get(right).isType(MapTile.Type.WALL)){
				result =1;
				break;
			}
			break;
		default:
			break;
		
		}
		return result;
	}
	/**
	 * check if there is an active key in view
	 * active means we are currently going to get this key
	 * @param currentView
	 * @return
	 */
	private boolean keyInView(HashMap<Coordinate, MapTile> currentView) {
		if(getKey()==1) { //we have found all keys
			return false;
		}
		MapTile key = currentView.get(keyList[getKey()-2]);
			
		return key != null && (getKey()-2)==((LavaTrap) key).getKey();
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
			return checkNorth(currentView) ||checkNorthFinish(currentView) ;
		case NORTH:
			return checkWest(currentView)||checkWestFinish(currentView);
		case SOUTH:
			return checkEast(currentView)||checkEastFinish(currentView);
		case WEST:
			return checkSouth(currentView)||checkSouthFinish(currentView);
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
		if (keyInView(currentView)) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(keyList[getKey()-2])) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(keyList[getKey()-2])) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(keyList[getKey()-2])) {
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
					if(!frontCoord.equals(path.get(i)) || frontCoord.equals(keyList[getKey()-2])) {
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
			if(		getX()<(float)(path.get(0).x+0.3) && (int)getX() !=World.MAP_WIDTH-1) {
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
		if(orientation.equals(WorldSpatial.Direction.WEST)) {
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