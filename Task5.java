package CROC;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 *Пример вычисления определителя матрицы методом разложения по строке с использованием многопоточности.
 * @author Artem Tuakelnko
 */
public class Task5 {

    // Определяем размер пула потоков как количество доступных процессорных ядер
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Точка старта приложения.
     *
     * @param args стартовые аргументы
     */
    public static void main(String[] args) {
        var matrix = MatrixExample.B;

        // Вызываем метод вычисления определителя с многопоточностью и выводим результат
        printResult("detMultiThread", matrix, () -> detMultiThread(matrix.getMatrix()));
    }

    /**
     * Рекурсивный расчет определителя матрицы методом разложения по строке в многопоточной реализации.
     *
     * @param a матрица
     * @return определитель матрицы
     */
    private static long detMultiThread(long[][] a) {
        var pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        var result = 0L;

        try {
            var futures = new Future[a.length];

            for (var i = 0; i < a.length; i++) {
                var sign = (i % 2 == 0 ? 1 : -1);
                var minor = minor(a, i);
                var finalI = i;

                // Используем ExecutorService для асинхронного выполнения задач
                futures[i] = pool.submit(() -> sign * a[finalI][0] * detOneThread(minor));
            }

            // Суммируем результаты из потоков
            for (var future : futures) {
                try {
                    // Приводим результат к типу long перед сложением
                    result += (Long) future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }

        return result;
    }

    /**
     * Вычисляет минорную матрицу от заданной. Удаляется первый столбец и заданная строка.
     *
     * @param original   матрица, от которой требуется вычислить минорную
     * @param exceptRow  удаляемая строка
     * @return минорная матрица
     */
    public static long[][] minor(final long[][] original, int exceptRow) {
        // Реализация вычисления минорной матрицы
        long[][] minor = new long[original.length-1][original.length-1];
        var minorLength = minor.length;
        for (int i = 0; i < exceptRow; i++) {
            System.arraycopy(original[i], 1, minor[i], 0, minorLength);
        }
        for (int i = exceptRow + 1; i < original.length; i++) {
            System.arraycopy(original[i], 1, minor[i - 1], 0, minorLength);
        }
        return minor;
    }

    /**
     * Рекурсивный расчет определителя матрицы методом разложения по строке в один поток.
     *
     * @param a матрица
     * @return определитель матрицы
     */
    public static long detOneThread(long[][] a) {
        // Реализация вычисления определителя матрицы в один поток
        if (a.length == 1) {
            return a[0][0];
        }
        var result = 0L;
        for (var i = 0; i < a.length; i++) {
            var sign = (i % 2 == 0 ? 1 : -1);
            result = result + sign * a[i][0] * detOneThread(minor(a, i));
        }

        return result;
    }

    /**
     * Выводит в консоль результат работы.
     *
     * @param method   название метода расчета
     * @param matrix   матрица из предложенных для примера
     * @param executor алгоритм расчета определителя матрицы
     */
    private static void printResult(String method, MatrixExample matrix, Supplier<Long> executor) {
        var start = System.currentTimeMillis();
        var det = executor.get();
        var executionTime = (System.currentTimeMillis() - start);
        System.out.println("Method -> " + method);
        System.out.println("Matrix name -> " + matrix.name());
        System.out.println("Matrix dimension -> " + matrix.getMatrix().length);
        System.out.println("Matrix determinant  = " + det + (det != matrix.getDeterminant() ? " ERROR!" : ""));
        System.out.println("Execution time -> " + executionTime);
        System.out.println();
    }
}

