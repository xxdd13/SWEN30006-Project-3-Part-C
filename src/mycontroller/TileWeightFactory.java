package mycontroller;

import java.util.HashMap;

import tiles.*;

public class TileWeightFactory {

	private static TileWeightFactory instance = new TileWeightFactory();
	private HashMap<Class<?>, ITileWeight> factory;
	
	public TileWeightFactory() {
		initiliase();
	}
	
	public void initiliase() {
		factory = new HashMap<>();
		factory.put((new MapTile(MapTile.Type.ROAD)).getClass(), new MapTileWeight());
		factory.put((new MapTile(MapTile.Type.FINISH)).getClass(), new MapTileWeight());
		factory.put((new LavaTrap()).getClass(), new LavaWeight());
		factory.put((new HealthTrap()).getClass(), new HealthWeight());
	}
	
	public static TileWeightFactory getInstance() {
		return instance;
	}
	
	public ITileWeight getWeight(MapTile tile) 
	{
		return factory.get(tile.getClass());
	}

}
