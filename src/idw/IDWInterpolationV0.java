package idw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import records.Point;

// Versão 0: Serial com otimização do código

public class IDWInterpolationV0 {
  private static final String FILE = "./data/measurements.txt";

  private static final int POWER = 2;

  static class IDW {
    private double numerator = 0.0, weights = 0.0;
    private final Point point;

    public IDW(Point point) {
      super();
      this.point = point;
    }

    public void calculateIDW(Point pointReaded, double valueReaded) {
      double distance = this.point.distanceTo(pointReaded);
      double weight = 1.0 / Math.pow(distance, POWER);
      this.numerator += (valueReaded * weight);
      this.weights += weight;
    }

    public double getIDW() {
      return this.numerator / this.weights;
    }
  }

  public static void processLine(StringBuilder line, IDW idwCalculator) {
    int endNumber1 = line.indexOf(",");
    int endNumber2 = line.indexOf(",", endNumber1 + 1);
    int lineX = Integer.parseInt(line.substring(0, endNumber1));
    int lineY = Integer.parseInt(line.substring(endNumber1 + 1, endNumber2));
    double lineValue = Double.parseDouble(line.substring(endNumber2 + 1));
    Point pointReaded = new Point(lineX, lineY);

    idwCalculator.calculateIDW(pointReaded, lineValue);
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

    try (RandomAccessFile raf = new RandomAccessFile(FILE, "r")) {
      FileChannel fileChannel = raf.getChannel();
      IDW idwCalculator = new IDW(point);

      try {
        MappedByteBuffer buffer =
            fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        StringBuilder lineBuilder = new StringBuilder();

        while (buffer.hasRemaining()) {
          char c = (char) buffer.get();

          if (c == '\n') {
            processLine(lineBuilder, idwCalculator);
            lineBuilder.delete(0, lineBuilder.length());
          } else {
            lineBuilder.append(c);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      double idw = idwCalculator.getIDW();
      System.out.println("IDW: " + String.format("%.1f", idw).replace(',', '.'));

      long end = System.currentTimeMillis();
      long time = (end - start) / 1000;
      System.out.println("Executed in " + time + "s.");
      System.exit(0);
    }
  }
}
