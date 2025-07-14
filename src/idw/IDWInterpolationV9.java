package idw;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import records.Point;

// Versão 9: Parallel streams
// Criada a partir da versão 8

public class IDWInterpolationV9 {
  private static final String FILE = "./data/measurements.txt";

  static class IDW {
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

  public static void processLines(List<String> lines, IDW idwCalculator) {
    lines.parallelStream().forEach(line -> {
      int endNumber1 = line.indexOf(",");
      int endNumber2 = line.indexOf(",", endNumber1 + 1);
      int lineX = Integer.parseInt(line.substring(0, endNumber1));
      int lineY = Integer.parseInt(line.substring(endNumber1 + 1, endNumber2));
      double lineValue = Double.parseDouble(line.substring(endNumber2 + 1));
      Point pointReaded = new Point(lineX, lineY);

      idwCalculator.calculateIDW(pointReaded, lineValue);
    });
  }

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

    try (BufferedReader reader = Files.newBufferedReader(Path.of(FILE))) {
      List<String> buffer = new ArrayList<>();
      int sizeBuffer = 100_000;
      String line = reader.readLine();

      while (line != null) {
        buffer.add(line);

        if (buffer.size() == sizeBuffer) {
          processLines(new ArrayList<>(buffer), idwCalculator);
          buffer.clear();
        }

        line = reader.readLine();
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
