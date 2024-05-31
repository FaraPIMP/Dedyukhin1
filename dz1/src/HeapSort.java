import java.util.Arrays;

public class HeapSort {

    public static void heapSort(int[] array) {
        int n = array.length;

        // Построить пирамиду
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(array, n, i);
        }

        // Распаковывать пирамиду
        for (int i = n - 1; i >= 0; i--) {
            // Поместить корень в конец
            int temp = array[0];
            array[0] = array[i];
            array[i] = temp;

            // Перестроить пирамиду
            heapify(array, i, 0);
        }
    }

    private static void heapify(int[] array, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        // Найти наибольший элемент среди корня и его дочерних узлов
        if (left < n && array[left] > array[largest]) {
            largest = left;
        }
        if (right < n && array[right] > array[largest]) {
            largest = right;
        }

        // Если наибольший элемент не является корнем, обменять и перестроить пирамиду
        if (largest != i) {
            int temp = array[i];
            array[i] = array[largest];
            array[largest] = temp;

            heapify(array, n, largest);
        }
    }

    public static void main(String[] args) {
        int[] array = {99, 45, 43, 1, 3, 2, 10};
        heapSort(array);

        System.out.println(Arrays.toString(array));
    }
}
