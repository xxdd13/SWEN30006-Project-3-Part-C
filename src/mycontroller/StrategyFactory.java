package mycontroller;

public class StrategyFactory {
    protected static StrategyFactory strategyFactory;

    public static StrategyFactory getInstance(){
        if (strategyFactory == null)
            return strategyFactory = new StrategyFactory();
        else return strategyFactory;
    }

    public INavigationStrategy getStrategy(String strategyName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {    
    	return (INavigationStrategy) Class.forName("mycontroller."+strategyName).newInstance();
    }
}