package mycontroller;

import java.util.HashMap;


public class StrategyFactory {
	private static StrategyFactory instance = new StrategyFactory();
	private HashMap<String, INavigationStrategy> factory;
    
    public StrategyFactory() {
		initiliase();
	}
	
	public void initiliase() {
		factory = new HashMap<>();
		factory.put("ExploreStrategy", new ExploreStrategy());
		factory.put("NormalStrategy", new NormalStrategy());
		factory.put("HealthStrategy", new HealthStrategy());
	}

    public static StrategyFactory getInstance(){
    	return instance;
    }

    public INavigationStrategy getStrategy(String strategyName){    
    	return factory.get(strategyName);
    }
}