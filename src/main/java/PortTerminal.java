public class PortTerminal {
    private final int resourceId;
    private final int resourceLoadTime;
    private int resourcesLoadedOut;

    PortTerminal(int inputResourceId, int inputResourceLoadTime){
        resourceId = inputResourceId;
        resourceLoadTime= inputResourceLoadTime;
        resourcesLoadedOut = 0;
    }

    public int getResourceId(){
        return resourceId;
    }

    public void loadResourcesToShip(int amount) throws InterruptedException {
        Thread.sleep(amount * resourceLoadTime);
        resourcesLoadedOut += amount;
    }

    public int getResourceLoadTime(){
        return resourceLoadTime;
    }

    public int getResourcesLoadedOut(){
        return resourcesLoadedOut;
    }
}
