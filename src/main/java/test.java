import java.util.Map;
import java.util.TreeMap;

public class test {
    public static void main(String[] args) {
        Map priorities = new TreeMap<>();
        for (int i = 0; i < 10; i++){
            priorities.put(i * (i % 2 == 0 ? -1 : 1), i);
        }
        for (Map.Entry entry : priorities){
            System.out.println(entry);
        }
        int newPriority = (int) priorities.get(3) * 100;
        priorities.replace(3, newPriority);
    }
}
