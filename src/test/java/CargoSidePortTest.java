import static org.junit.jupiter.api.Assertions.*;


class CargoSidePortTest {

    @org.junit.jupiter.api.Test
    public void easyTest(){

        int[] shipsCapacities = new int[]{10, 20, 30, 40, 10, 20};
        int[] shipsResources = new int[]{0, 1, 2, 2, 3, 1};

        String[] resourceNames = new String[]{"food", "Dynamite", "XXX", "hidden"};
        int[] resourcesLoadTime = new int[]{100, 50, 20, 10};

        int channelCapacity = 3;
        int channelPassTimeMillis = 1000;

        CargoSidePort port = new CargoSidePort(shipsCapacities,
                shipsResources,
                resourceNames,
                resourcesLoadTime,
                channelCapacity,
                channelPassTimeMillis);
        int[] actual = port.releaseShips(500);
        int[] expected = new int[]{10, 40, 70, 10};
        assertNotNull(actual);
        assertArrayEquals(actual, expected);
    }

    @org.junit.jupiter.api.Test
    public void manyShipsTest(){
        int[] shipsCapacities = new int[100];
        int[] shipsResources = new int[100];

        for (int i = 0; i < 100; i++){
            shipsCapacities[i] = (i % 5) * 10 + 5;
            shipsResources[i] = (i + 1) % 5;
        }

        String[] resourceNames = new String[]{"food", "Dynamite", "XXX", "hidden", "wood"};
        int[] resourcesLoadTime = new int[]{10, 30, 20, 10, 11};

        int channelCapacity = 10;
        int channelPassTimeMillis = 30;
        int[] expected = new int[5];

        for(int i = 0; i < 100; i++){
            expected[shipsResources[i]] += shipsCapacities[i];
        }

        CargoSidePort port = new CargoSidePort(shipsCapacities,
                shipsResources,
                resourceNames,
                resourcesLoadTime,
                channelCapacity,
                channelPassTimeMillis);

        int[] actual = port.releaseShips(500);
        assertNotNull(actual);
        assertArrayEquals(actual, expected);
    }
}