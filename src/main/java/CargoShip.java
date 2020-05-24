import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class CargoShip extends Thread {
    private final int capacity;
    private final int shipId;
    private final Semaphore groupSemaphore;
    private final Semaphore channelSemaphore;
    private final Semaphore landingStageSemaphore;
    //private final AtomicBoolean permission;
    private final CargoSidePort port;
    private final PortTerminal terminal;
    private final int channelPassTimeMillis;


    public CargoShip(int inputCapacity, int inputShipId,
                     Semaphore inputGroupSemaphore,
                     Semaphore inputChannelSemaphore,
                     Semaphore inputLandingStageSemaphore,
                     AtomicBoolean inputPermission,
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
        //permission = inputPermission;
        channelPassTimeMillis = port.getChannelPassTime();
    }

    @Override
    public void run() {
        try {
            // Корабль встает в очередь к каналу
            groupSemaphore.acquire();

//            // Корабль запрашивает разрешение на доступ в канал
//            while (!permission.compareAndSet(true, false)) {
//                permission.wait();
//            }
        } catch (InterruptedException ignored) { }

        try {
            // Корабль пытается пройти в канал
            channelSemaphore.acquire();

            // Корабль движется по каналу
            sleep(channelPassTimeMillis);
        } catch (InterruptedException ignored) {
        } finally {
            port.updatePriorities();
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
            port.updatePriorities();
            landingStageSemaphore.release();
        }
    }
}
