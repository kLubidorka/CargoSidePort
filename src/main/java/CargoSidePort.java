import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class CargoSidePort {
    private final int SHIPS_NUM;
    private final int CHANNEL_CAPACITY;
    private final int RESOURCES_NUM;
    private final int CHANNEL_PASS_TIME;

    private final Semaphore[] groupSemaphores;
    private final Semaphore channelSemaphore;
    private final Semaphore[] landingStageSemaphores;
    private final AtomicBoolean[] permissions;

    private final CargoShipGenerator cargoShipGenerator;
    private final Resources resources;
    private final int[] shipsResource;
    private final int[] shipsCapacity;
    private final int[] priorities;
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

        SHIPS_NUM = shipsResources.length;
        RESOURCES_NUM = resourceNames.length;
        CHANNEL_CAPACITY = channelCapacity;
        CHANNEL_PASS_TIME = channelPassTimeMillis;

        // Synchronization primitives
        Semaphore[] firstSemaphoreGroup = new Semaphore[RESOURCES_NUM];
        for (int i = 0; i < RESOURCES_NUM; i++){
            firstSemaphoreGroup[i] = new Semaphore(1, true);
        }
        groupSemaphores = firstSemaphoreGroup;
        channelSemaphore = new Semaphore(CHANNEL_CAPACITY, true);

        Semaphore[] secondSemaphoreGroup = new Semaphore[RESOURCES_NUM];
        for (int i = 0; i < RESOURCES_NUM; i++){
            secondSemaphoreGroup[i] = new Semaphore(1, true);
        }
        landingStageSemaphores = secondSemaphoreGroup;

        AtomicBoolean[] atomicBooleans = new AtomicBoolean[RESOURCES_NUM];
        for (int i = 0; i < RESOURCES_NUM; i++){
            atomicBooleans[i] = new AtomicBoolean(true);
        }
        permissions = atomicBooleans;

        // Port infrastructure
        cargoShipGenerator = new CargoShipGenerator(shipsCapacities, shipsResources, this);
        resources = new Resources(RESOURCES_NUM, resourceNames, resourcesLoadTime);

        PortTerminal[] terminalsInPort = new PortTerminal[RESOURCES_NUM];
        for (int i = 0; i < RESOURCES_NUM; i++){
            terminalsInPort[i] = new PortTerminal(i, resourcesLoadTime[i]);
        }
        terminals = terminalsInPort;
        shipsResource = shipsResources;
        shipsCapacity = shipsCapacities;
        priorities = new int[RESOURCES_NUM];
    }

    public int[] releaseShips(int maxDelayMillis) throws InterruptedException {
        return cargoShipGenerator.releaseShips(maxDelayMillis);
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

    public AtomicBoolean[] getPermissions(){
        return permissions;
    }

    public PortTerminal[] getTerminals(){
        return terminals;
    }

    public int getResourcesNum(){
        return RESOURCES_NUM;
    }

    public synchronized void updatePriorities(int shipId, boolean isReady){
        int resourceId = shipsResource[shipId];
        int delta = shipsCapacity[shipId] * resources.getLoadTimeById(shipsResource[shipId]) * (isReady ? 1 : -1);
        priorities[resourceId] += delta;

    }
}
