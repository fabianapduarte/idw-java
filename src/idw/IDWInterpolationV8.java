package idw;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.DoubleAdder;
import records.Point;

// Versão 8: Fork/join

class Task extends RecursiveAction {
  private static final int SEQUENTIAL_THRESHOLD = 10000;
  private final List<String> lines;
  private final IDW idwCalculator;

  public Task(List<String> lines, IDW idwCalculator) {
    super();
    this.lines = lines;
    this.idwCalculator = idwCalculator;
  }

  public void processLines() {
    for (String line : lines) {
      int endNumber1 = line.indexOf(",");
      int endNumber2 = line.indexOf(",", endNumber1 + 1);
      int lineX = Integer.parseInt(line.substring(0, endNumber1));
      int lineY = Integer.parseInt(line.substring(endNumber1 + 1, endNumber2));
      double lineValue = Double.parseDouble(line.substring(endNumber2 + 1));
      Point pointReaded = new Point(lineX, lineY);

      idwCalculator.calculateIDW(pointReaded, lineValue);
    }
  }

  @Override
  protected void compute() {
    if (lines.size() <= SEQUENTIAL_THRESHOLD) {
      processLines();
    } else {
      int mid = lines.size() / 2;
      Task firstSubtask = new Task(lines.subList(0, mid), idwCalculator);
      Task secondSubtask = new Task(lines.subList(mid, lines.size()), idwCalculator);

      firstSubtask.fork();
      secondSubtask.compute();
      firstSubtask.join();
    }
  }
}


class IDW {
  private static final int POWER = 2;
  private final DoubleAdder numerator = new DoubleAdder();
  private final DoubleAdder weights = new DoubleAdder();
  private final Point point;

  public IDW(Point point) {
    super();
    this.point = point;
  }

  public void calculateIDW(Point pointReaded, double valueReaded) {
    double distance = this.point.distanceTo(pointReaded);
    double weight = 1.0 / Math.pow(distance, POWER);

    this.numerator.add(valueReaded * weight);
    this.weights.add(weight);
  }

  public double getIDW() {
    return this.numerator.sum() / this.weights.sum();
  }
}


public class IDWInterpolationV8 {
  private static final String FILE = "./data/measurements.txt";

  public static void main(String[] args) throws IOException, InterruptedException {
    long start = System.currentTimeMillis();
    int x = 0, y = 0;

    try {
      x = Integer.parseInt(args[0]);
      y = Integer.parseInt(args[1]);
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
      System.out.println("Coordenadas inválidas.");
      System.exit(1);
    }

    Point point = new Point(x, y);
    IDW idwCalculator = new IDW(point);

    try (ForkJoinPool pool = new ForkJoinPool()) {
      try (BufferedReader reader = Files.newBufferedReader(Path.of(FILE))) {
        List<String> buffer = new ArrayList<>();
        int sizeBuffer = 100_000;
        String line = reader.readLine();

        while (line != null) {
          buffer.add(line);

          if (buffer.size() == sizeBuffer) {
            pool.execute(new Task(new ArrayList<>(buffer), idwCalculator));
            buffer.clear();
          }

          line = reader.readLine();
        }
      }
    }

    double idw = idwCalculator.getIDW();
    System.out.println("IDW: " + String.format("%.1f", idw).replace(',', '.'));

    long end = System.currentTimeMillis();
    long time = (end - start) / 1000;
    System.out.println("Executed in " + time + "s.");
    System.exit(0);
  }
}
