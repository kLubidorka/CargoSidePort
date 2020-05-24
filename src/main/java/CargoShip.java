import java.util.concurrent.Semaphore;

public class CargoShip extends Thread {
    private final int capacity;
    private final int shipId;
    private final Semaphore groupSemaphore;
    private final Semaphore channelSemaphore;
    private final Semaphore landingStageSemaphore;
    private final CargoSidePort port;
    private final PortTerminal terminal;
    private final int channelPassTimeMillis;


    public CargoShip(int inputCapacity, int inputShipId,
                     Semaphore inputGroupSemaphore,
                     Semaphore inputChannelSemaphore,
                     Semaphore inputLandingStageSemaphore,
                     CargoSidePort inputPort,
                     PortTerminal inputTerminal) {
        assert inputCapacity > 0;

        capacity = inputCapacity;
        shipId = inputShipId;

        groupSemaphore = inputGroupSemaphore;
        channelSemaphore = inputChannelSemaphore;
        landingStageSemaphore = inputLandingStageSemaphore;

        port = inputPort;
        terminal = inputTerminal;
        channelPassTimeMillis = port.getChannelPassTime();
    }

    @Override
    public void run() {
        try {
            // Корабль встает в очередь к каналу
            groupSemaphore.acquire();
        } catch (InterruptedException ignored) { }

        try {
            // Корабль пытается пройти в канал
            channelSemaphore.acquire();

            // Корабль движется по каналу
            System.out.println(String.format("Ship %d entered channel", shipId));
            sleep(channelPassTimeMillis);
        } catch (InterruptedException ignored) {
        } finally {
            port.updatePriorities(shipId, false);
            channelSemaphore.release();
        }

        try {
            // Корабль встает в очередь на загрузку у своего причала
            landingStageSemaphore.acquire();

            // Корабль загружается на причале
            terminal.loadResourcesToShip(capacity);

            // Корабль загружен и уходит в море
            System.out.println(String.format("Ship %d loaded and ready", shipId));
        } catch (InterruptedException ignored) {
        } finally {
            port.updatePriorities(shipId, true);
            landingStageSemaphore.release();
        }
    }
}
