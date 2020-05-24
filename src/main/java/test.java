import java.util.Arrays;
import java.util.Comparator;

public class test {
    public static void main(String[] args) {
        int[][] arr = new int[10][2];
        for (int i = 0; i < 10; i++){
            arr[i] = new int[]{i, i * (i % 2 == 0 ? 1 : -1)};
        }
        System.out.println(Arrays.deepToString(arr));

        Arrays.sort(arr, new Comparator<int[]>() {
            @Override
            public int compare(int[] first, int[] second) {
                return second[1] - first[1];
            }
        });

        System.out.println(Arrays.deepToString(arr));
    }
}
