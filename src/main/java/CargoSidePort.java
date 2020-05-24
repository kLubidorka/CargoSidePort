import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

public class CargoSidePort {
    private final int RESOURCES_NUM;
    private final int CHANNEL_PASS_TIME;

    private final Semaphore[] groupSemaphores;
    private final Semaphore channelSemaphore;
    private final Semaphore[] landingStageSemaphores;

    private final CargoShipGenerator cargoShipGenerator;
    private final Resources resources;
    private final int[] shipsResource;
    private final int[] shipsCapacity;
    private final int[][] priorities;
    private final PortTerminal[] terminals;

    /**
     *
     * @param shipsCapacities Array of capacities > 0 corresponding to ships
     * @param channelCapacity Highest number of ships that can go through channel simultaneously
     * @param resourcesLoadTime Array of times to load one piece of particular resource in millis
     */
    public CargoSidePort(int[] shipsCapacities, int[] shipsResources,
                         String[] resourceNames, int[] resourcesLoadTime,
                         int channelCapacity, int channelPassTimeMillis){

        assert shipsCapacities.length == shipsResources.length;
        assert resourceNames.length == resourcesLoadTime.length;

        RESOURCES_NUM = resourceNames.length;
        CHANNEL_PASS_TIME = channelPassTimeMillis;

        // Synchronization primitives
        Semaphore[] firstSemaphoreGroup = new Semaphore[RESOURCES_NUM];
        int semaphoreStartValue = RESOURCES_NUM > channelCapacity ? 1 : channelCapacity / RESOURCES_NUM + 1;
        for (int i = 0; i < RESOURCES_NUM; i++){
            firstSemaphoreGroup[i] = new Semaphore(semaphoreStartValue, true);
        }
        groupSemaphores = firstSemaphoreGroup;
        channelSemaphore = new Semaphore(channelCapacity, true);

        Semaphore[] secondSemaphoreGroup = new Semaphore[RESOURCES_NUM];
        for (int i = 0; i < RESOURCES_NUM; i++){
            secondSemaphoreGroup[i] = new Semaphore(1, true);
        }
        landingStageSemaphores = secondSemaphoreGroup;

        // Port infrastructure
        resources = new Resources(RESOURCES_NUM, resourceNames, resourcesLoadTime);

        PortTerminal[] terminalsInPort = new PortTerminal[RESOURCES_NUM];
        for (int i = 0; i < RESOURCES_NUM; i++){
            terminalsInPort[i] = new PortTerminal(i, resourcesLoadTime[i]);
        }
        terminals = terminalsInPort;
        shipsResource = shipsResources;
        shipsCapacity = shipsCapacities;
        cargoShipGenerator = new CargoShipGenerator(shipsCapacities, shipsResources, this);
        priorities = new int[RESOURCES_NUM][2];
        for (int i = 0; i < RESOURCES_NUM; i++){
            priorities[i][0] = i;
        }
    }

    public int[] releaseShips(int maxDelayMillis) {
        try {
            return cargoShipGenerator.releaseShips(maxDelayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getChannelPassTime(){
        return CHANNEL_PASS_TIME;
    }

    public Semaphore[] getGroupSemaphores(){
        return groupSemaphores;
    }

    public Semaphore[] getLandingStageSemaphores(){
        return landingStageSemaphores;
    }

    public Semaphore getChannelSemaphore(){
        return channelSemaphore;
    }

    public PortTerminal[] getTerminals(){
        return terminals;
    }

    public int getResourcesNum(){
        return RESOURCES_NUM;
    }

    public synchronized void updatePriorities(int shipId, boolean isReady){
        int resourceId = shipsResource[shipId];
        int indexInResources = -1;

        for (int i = 0; i < RESOURCES_NUM; i++){
            if (priorities[i][0] == resourceId){
                indexInResources = i;
                break;
            }
        }
        int delta = shipsCapacity[shipId] * resources.getLoadTimeById(shipsResource[shipId]) * (isReady ? 1 : -1);
        priorities[indexInResources][1] += delta;

        if (isReady){
            Arrays.sort(priorities, new Comparator<int[]>() {
                @Override
                public int compare(int[] first, int[] second) {
                    return second[1] - first[1];
                }
            });
            boolean isAnySemaphoreIncremented = false;
            for(int[] elem : priorities){
                int currentResourceId = elem[0];
                if (groupSemaphores[currentResourceId].hasQueuedThreads()){
                    groupSemaphores[currentResourceId].release();
                    isAnySemaphoreIncremented = true;
                    break;
                }
            }
            if(!isAnySemaphoreIncremented){
                for(int[] elem : priorities){
                    int currentResourceId = elem[0];
                    groupSemaphores[currentResourceId].release();
                }
            }
        }
    }
}
