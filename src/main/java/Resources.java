
public class Resources {
    private final int resourceNum;
    private final String[] resourceNames;
    private final int[] loadTimes;

    public Resources(int inputResourceNum, String[] inputResourceNames, int[] inputLoadSpeed){
        assert inputResourceNames.length == inputResourceNum;
        assert inputLoadSpeed.length == inputResourceNum;

        resourceNum = inputResourceNum;
        resourceNames = inputResourceNames;
        loadTimes = inputLoadSpeed;
    }

    public  String getNameById(int id){
        assert id >= 0;
        assert id < resourceNum;
        return resourceNames[id];
    }

    public int getLoadTimeById(int id){
        assert id >= 0;
        assert id < resourceNum;
        return loadTimes[id];
    }

    public  int getResourceNum() {
        return resourceNum;
    }
}
