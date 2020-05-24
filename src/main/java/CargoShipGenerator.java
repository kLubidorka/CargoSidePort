import java.util.concurrent.Semaphore;

public class CargoShipGenerator {
    private final int SHIP_NUM;
    private final Semaphore[] groupSemaphores;
    private final Semaphore channelSemaphore;
    private final Semaphore[] landingStageSemaphores;
    private final int[] shipCapacities;
    private final int[] shipResourceIds;
    private final CargoSidePort port;
    private final PortTerminal[] terminals;

    public CargoShipGenerator(int[] inputShipCapacities,
                              int[] inputShipResourceIds,
                              CargoSidePort inputPort) {
        assert inputShipCapacities.length == inputShipResourceIds.length;
        SHIP_NUM = inputShipCapacities.length;

        shipCapacities = inputShipCapacities;
        shipResourceIds = inputShipResourceIds;
        port = inputPort;

        groupSemaphores = port.getGroupSemaphores();
        channelSemaphore = port.getChannelSemaphore();
        landingStageSemaphores = port.getLandingStageSemaphores();
        terminals = port.getTerminals();
    }

    public int[] releaseShips(int maxDelayMillis) throws InterruptedException {
        assert maxDelayMillis >= 0;
        CargoShip[] ships = new CargoShip[SHIP_NUM];
        int[] resourcesLoaded = new int[port.getResourcesNum()];

        // Создаются все корабли
        for (int shipId = 0; shipId < SHIP_NUM; shipId++) {
            int resourceId = shipResourceIds[shipId];
            int capacity = shipCapacities[shipId];

            System.out.println(String.format("Ship %d with capacity %d for resource %d released", shipId,
                    capacity, resourceId));
            ships[shipId] = new CargoShip(capacity, shipId,
                    groupSemaphores[resourceId],
                    channelSemaphore,
                    landingStageSemaphores[resourceId],
                    port, terminals[resourceId]);
        }

        // Выпускаются все корабли
        for (CargoShip ship : ships){
            ship.start();
            Thread.sleep((long) (maxDelayMillis * Math.random()));
        }

        for (int shipId = 0; shipId < SHIP_NUM; shipId++){
            ships[shipId].join();
            resourcesLoaded[shipResourceIds[shipId]] += shipCapacities[shipId];
            Thread.sleep((long) (maxDelayMillis * Math.random()));
        }

        return resourcesLoaded;
    }
}
