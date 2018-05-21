package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mycontroller.DijkstraPathFinder;
import mycontroller.HealthNavigation;
import mycontroller.Navigation;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import world.WorldSpatial.Direction;


public class AIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	
	
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	//private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	private boolean onTrack = false;
	private  float FINAL_CAR_SPEED =(float)3;
	private float CAR_SPEED = FINAL_CAR_SPEED;
	private float STUCK_THRESHOLD = (float) 0.1;
	private float CAR_SPEED_THRESHOLD1 = (float) 1.0;
	private float CAR_SPEED_THRESHOLD2 = (float) 1.1;
	private float CHANGE_AHEAD_SPEED = (float) 1.1;
	private int totalKeys;
	private int currentKey;
	Navigation navigation;
	Navigation navigationOriginal;
	HealthNavigation healthNavigation;
	List<Coordinate> path;
	private List<Coordinate> finishes = new ArrayList<>();
	private boolean halting = false;// default not stopped for heal
	HashMap<Coordinate, MapTile> map = getMap();
	/////////////////////
	
	public Coordinate[] keyList = new Coordinate[getKey()-1];
	
	public AIController(Car car) {
		super(car);
		navigation = new Navigation(map, new DijkstraPathFinder());
		navigationOriginal = navigation;
		healthNavigation = new HealthNavigation(map, new DijkstraPathFinder());
		totalKeys = getKey();
		
	}
	
	Coordinate initialGuess;
	boolean notSouth = true;
	
	private boolean hasAllKeys() {
		for (int i=0;i<(keyList.length-1);i++) {
			if (keyList[i]==null) return false;
		}
		return true;
	}
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
		boolean test = true;
		if(test){
			keyList[0] = new Coordinate("23,15");
			keyList[1] = new Coordinate("16,13");
			keyList[2] = new Coordinate("19,2");
			
		}
		viewGetTraps(currentView);
		if(hasAllKeys()) {  //ALL keys have been found
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			if(getKey()==1) { //got all keys ! go to finish line
				path = navigation.planRoute(currentCoordinate, finishes.get(0));	
			}
			//plan new path
			else if(path==null||path.size()<=0) { //if current path finish or don't have one yet
				System.out.println("current key number: "+(getKey()-1));
				navigation.updateMap(map);
				path = navigation.planRoute(currentCoordinate, keyList[getKey()-2]);			
			}else {
				if(currentCoordinate.equals(path.get(0))) {
					path.remove(0);
				}
				
			}
			if(getHealth()<60 && navigation.getClass()!=healthNavigation.getClass() && !keyInView(currentView)) {
				System.out.println("finding heal !!!!!!!!!!!!!!");
				navigation =healthNavigation;	
				navigation.updateMap(map);
				path = navigation.planRoute(currentCoordinate, null);			
			}
			if(currentTile instanceof HealthTrap ){
				if(getHealth()<95) { //stop to heal
					applyBrake();
					halting = true;
					System.out.println("Halt !!!!!!!!!!!!!!");
				}
				else { //ready to go
					halting=false;
					navigation =new Navigation(this.map, new DijkstraPathFinder());;	
				}
			}
			
			checkStateChange();
			
		
			
			
			//car not stopped and not waiting for heal to finish
			if(!onTrack && !halting) {
				/*if car is not going back then speed up until car speed limit*/
				if(getSpeed() < CAR_SPEED ){
					applyForwardAcceleration();
					
				}
				
				
				if(needGoNorth(currentCoordinate)){//should go to north
					//System.out.println("checking north               current direction:"+getOrientation()+" "+getAngle() +"  "+getSpeed() +"  "+path.get(0)+"  "+currentCoordinate);
					
					
					if(getOrientation().equals(WorldSpatial.Direction.EAST)){
						//applyReverseAcceleration();
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
						
					}
					else if(getOrientation().equals(WorldSpatial.Direction.WEST)){
						
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						//applyLeftTurn(getOrientation(),delta);
						
						
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
						
						
					}
					else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						if(!checkWest(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x+1,currentCoordinate.y);
							path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);	
							
						}
						else if(!checkEast(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x-1,currentCoordinate.y);
							path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
							
						}
					}
					
					
					else{
						System.out.println("wall head"+ checkCorner(currentCoordinate,getOrientation()));
						
						if (checkCorner(currentCoordinate,getOrientation())==0) {
							onTrack = true;
						}
						else if(checkCorner(currentCoordinate,getOrientation())==-1){
							lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
							applyLeftTurn(getOrientation(),delta);
							
							onTrack = false;
						}
						else if(checkCorner(currentCoordinate,getOrientation())==1){
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							applyRightTurn(getOrientation(),delta);
							onTrack = false;
						}
						
						
						/*
						if (getSpeed()<=STUCK_THRESHOLD) {
							System.out.println("stuck ");
							onTrack = false;
							
							if(!checkWest(currentView)) {
								System.out.println("nstuck west ");
								applyReverseAcceleration();
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyRightTurn(getOrientation(),delta);
												
								
								
							}
							if(!checkEast(currentView)) {
								System.out.println("nstuck east ");
								Coordinate newCoordinate = new Coordinate(currentCoordinate.x+1,currentCoordinate.y);
								path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
								
							}
							//preventCornerCollision(getOrientation(),currentCoordinate, delta);
							
													
						}
						*/
						
					}
				}
				else if(needGoSouth(currentCoordinate)) {
					//System.out.println("checking south               current direction:"+getOrientation()+" "+getAngle() );
					
					if(getOrientation().equals(WorldSpatial.Direction.WEST)){
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						//applyRightTurn(getOrientation(),delta);
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						//applyLeftTurn(getOrientation(),delta);
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
						
						
					}
					else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
						System.out.println("U turn ! north -> south");
						CAR_SPEED = 1;
						if(!checkEast(currentView)) {
							applyReverseAcceleration();
							lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
							applyRightTurn(getOrientation(),delta);
							//Coordinate newCoordinate = new Coordinate(currentCoordinate.x+1,currentCoordinate.y);
							//path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
							
							System.out.println("eeeeeeeeeeeee");
							
						}
						else if(!checkWest(currentView)) {
							
							
							

							//lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							//applyRightTurn(getOrientation(),delta);
							
							applyReverseAcceleration();
							lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
							applyLeftTurn(getOrientation(),delta);
							
							
							//Coordinate newCoordinate = new Coordinate(currentCoordinate.x-1,currentCoordinate.y-1);
							//path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);	
							
							System.out.println("wwwwwwwwwwwwwwww");
						}
						
						
						
					}
					
					else{
						
						
						onTrack = true;
						
						
					}
				}
				else if(needGoEast(currentCoordinate)) {
					
					//System.out.println("checking east                current direction:"+getOrientation()+" "+getAngle() +"        "+getSpeed());
					
					if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						//applyRightTurn(getOrientation(),delta);

						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
						
					}
					else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
						//applyForwardAcceleration();
						
						
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						//applyLeftTurn(getOrientation(),delta);
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
						
					}
					else if(getOrientation().equals(WorldSpatial.Direction.WEST)){
						
						System.out.println("U turn west ->  east");
						
						if(!checkNorth(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y+1);
							path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);	
							
						}
						else if(!checkSouth(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y-1);
							path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);	
						}
						
						
					}
					
					
					else{
						onTrack = true;
						
						
						if (getSpeed()<STUCK_THRESHOLD) {
							System.out.println("slow");
							if(!checkNorth(currentView)) {
								applyReverseAcceleration();
								lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
								applyLeftTurn(getOrientation(),delta);
								Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y+1);
								path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);	
								
								System.out.println("nnnnnnnnnnnn");
								
							}
							else if(!checkSouth(currentView)) {
								applyReverseAcceleration();
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyRightTurn(getOrientation(),delta);
								Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y-1);
								path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);	
								
								System.out.println("sssssssssssssssss");
							}
							onTrack = false;
							applyForwardAcceleration();
							
						}
						
						
						
					}
				}
				else if(needGoWest(currentCoordinate)) {
					//System.out.println("checking west 		 current direction:"+getOrientation()+" "+getAngle() + "        "+getSpeed());
					
					
					if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						//applyRightTurn(getOrientation(),delta);
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						applyLeftTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
						//applyReverseAcceleration();
						//lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						//applyLeftTurn(getOrientation(),delta);
						
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						applyRightTurn(getOrientation(),delta);
					}
					else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
						
						System.out.println("U turn east -> west ");
						
						if(!checkNorth(currentView)) {
							
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y+1);
							path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
						}
						else if(!checkSouth(currentView)) {
							Coordinate newCoordinate = new Coordinate(currentCoordinate.x,currentCoordinate.y-1);
							path = navigation.planRoute(newCoordinate, keyList[getKey()-2]);
						}
						
						
					}
					
					else{
						onTrack = true;
						CAR_SPEED=FINAL_CAR_SPEED;

					}
				}
			
			}
			
			// CAR IS ON TRACK
			else {
				//preventCornerCollision(getOrientation(),currentCoordinate, delta);
				
				
				////////////////////////////////////////////////////////////
				/////////////
				//////////// HELP!
				///////////it detects stuck, but unable to make a turn
				//////////
				///////////////////////////////////////////////////////////
				if(getSpeed()<STUCK_THRESHOLD) {
					if(checkCorner(currentCoordinate,getOrientation())==-1){
						applyReverseAcceleration();
						lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
						System.out.println("test RIGHT");
						applyLeftTurn(getOrientation(),delta);
						
					
					}
					else if(checkCorner(currentCoordinate,getOrientation())==1){
						applyReverseAcceleration();
						lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
						System.out.println("test LEFT");
						applyRightTurn(getOrientation(),delta);
						
						
					}
				}
				
				readjust(lastTurnDirection,delta);
				if(isTurningRight){
					
					applyRightTurn(getOrientation(),delta);
				}
				else if(isTurningLeft){
					applyLeftTurn(getOrientation(),delta);
					
				}
				

				else if( needMove(getOrientation(),currentCoordinate)){
					/*check if next 3 coordinate in path, if there is change in front then slow down*/
					if(needTurn(getOrientation(),currentCoordinate,currentView, delta)) {
						
						CAR_SPEED = CAR_SPEED_THRESHOLD1;
						if(getSpeed() > CAR_SPEED_THRESHOLD1) {
							applyBrake();
						}
						else if(CAR_SPEED < CAR_SPEED_THRESHOLD2){
							applyForwardAcceleration();
						}
					}
					/*if there is no turn ahead, remain original car speed*/
					if(!needTurn(getOrientation(),currentCoordinate,currentView, delta) && getSpeed() < CAR_SPEED){
						applyForwardAcceleration();
						CAR_SPEED = FINAL_CAR_SPEED ;
					}
					if(currentView.get(currentCoordinate) instanceof TrapTile &&
							((TrapTile)currentView.get(currentCoordinate)).canAccelerate()&&
							!keyInView(currentView)	) {
							
							
							applyForwardAcceleration();
						
					}
					
				}
				
				else if(!needMove(getOrientation(),currentCoordinate)){
					onTrack = false;
					CAR_SPEED = CHANGE_AHEAD_SPEED;
				}
			
			}
			
			
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		}
		else {//missing keys, keep searching
			if(currentTile instanceof HealthTrap && getHealth()<50) { //stop to heal
				applyBrake();
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
	private boolean keyInView(HashMap<Coordinate, MapTile> currentView) {
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
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
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
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
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
			if(tile.isType(MapTile.Type.WALL)){
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
	

////////////////////////////////////////////////////////////////////////////
	


/////////////////////////////////////////////



	/*check if turning is ahead, if yes then slow down the car speed*/
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
						break;		
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
						System.out.println("turn issue");
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
	 * Check if the car is following next coordinate
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
private void preventCornerCollision(WorldSpatial.Direction orientation, Coordinate currentCoordinate, float delta) {
		
		switch(orientation){
		case EAST:
			EastCornerCollision(currentCoordinate,delta);
			break;
		case NORTH:
			NorthCornerCollision(currentCoordinate,delta);
			break;
		case SOUTH:
			SouthCornerCollision(currentCoordinate,delta);
			break;
		case WEST:
			WestCornerCollision(orientation,currentCoordinate,delta);
			break;
		default:
			
		}
		
	}
	public void EastCornerCollision(Coordinate currentCoordinate, float delta){
		if(!path.isEmpty()) {
			Coordinate next = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
			Coordinate left = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+1);
			Coordinate right = new Coordinate(currentCoordinate.x+1, currentCoordinate.y-1);
			MapTile leftTile = getView().get(left);
			MapTile rightTile = getView().get(right);
			if(leftTile.isType(MapTile.Type.WALL)) {//TOO CLOSE TO LEFT	
				
				lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
				applyRightTurn(getOrientation(),delta);		
				
			}else if(rightTile.isType(MapTile.Type.WALL)) { //too close to right
				
				
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(getOrientation(),delta);	
				
			}		
		}
		
	}
	
	public void NorthCornerCollision(Coordinate currentCoordinate ,float delta){
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
		Coordinate left = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+1);
		Coordinate right = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+1);
		MapTile leftTile = getView().get(left);
		MapTile rightTile = getView().get(right);
		if(!path.isEmpty()) {
			if(leftTile.isType(MapTile.Type.WALL)) {//TOO CLOSE TO LEFT	
				applyForwardAcceleration();
				lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
				applyRightTurn(getOrientation(),delta);		
				
			}else if(rightTile.isType(MapTile.Type.WALL)) { //too close to right
				applyForwardAcceleration();
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(getOrientation(),delta);	
				
			}		
		}
	}
	
	public void SouthCornerCollision(Coordinate currentCoordinate,float delta){
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
		Coordinate left = new Coordinate(currentCoordinate.x+1, currentCoordinate.y-1);
		Coordinate right = new Coordinate(currentCoordinate.x-1, currentCoordinate.y-1);
		MapTile leftTile = getView().get(left);
		MapTile rightTile = getView().get(right);
		if(!path.isEmpty()) {
			if(leftTile.isType(MapTile.Type.WALL)) {//TOO CLOSE TO LEFT	
				lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
				applyRightTurn(getOrientation(),delta);		
			}else if(rightTile.isType(MapTile.Type.WALL)) { //too close to right
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(getOrientation(),delta);	
			}			
		}
	}
	
	
	public void WestCornerCollision(WorldSpatial.Direction orientation,Coordinate currentCoordinate,float delta){
		
		Coordinate next = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
		Coordinate left = new Coordinate(currentCoordinate.x-1, currentCoordinate.y-1);
		Coordinate right = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+1);
		MapTile leftTile = getView().get(left);
		MapTile rightTile = getView().get(right);
		if(!path.isEmpty()) {
			if(leftTile.isType(MapTile.Type.WALL)) {//TOO CLOSE TO LEFT	
				lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
				applyRightTurn(orientation,delta);		
			}else if(rightTile.isType(MapTile.Type.WALL)) { //too close to right
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(orientation,delta);	
			}		
		}
	
	}



	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * checkEast will check up to next coordinate to the right.
	 * checkWest will check up to next coordinate to the left.
	 * checkNorth will check up to next coordinate to the top.
	 * checkSouth will check up to next coordinate below.
	 */
	public boolean needGoEast(Coordinate currentCoordinate){
		// Check tiles to my right
		
		Coordinate next = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
		if(!path.isEmpty() && path.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoWest(Coordinate currentCoordinate){
		// Check tiles to my left
		Coordinate next = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
		if(!path.isEmpty() && path.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoNorth(Coordinate currentCoordinate){
		// Check tiles to towards the top
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
		if(!path.isEmpty() && path.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean needGoSouth(Coordinate currentCoordinate){
		// Check tiles towards the bottom
		
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
		if(!path.isEmpty() && path.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	
	}
	
	
}