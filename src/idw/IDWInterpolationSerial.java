package idw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class IDWInterpolationSerial {
  private static final String FILE = "./data/measurements.txt";

  private static final int POWER = 2;

  public record Point(int x, int y) {
  }

  public static double calculateInverseDistanceWeighting(Map<Point, Double> mapPoints, int power,
      Point point) {
    double numerator = 0.0;
    double weights = 0.0;

    for (Map.Entry<Point, Double> entry : mapPoints.entrySet()) {
      double distance = getDistance(entry.getKey(), point);

      if (distance == 0.0) {
        return entry.getValue();
      }

      double weight = 1.0 / Math.pow(distance, power);
      numerator += entry.getValue() * weight;
      weights += weight;
    }

    return numerator / weights;
  }

  public static double getDistance(Point p, Point q) {
    return Math.hypot(p.x() - q.x(), p.y() - q.y());
  }

  public static void main(String[] args) throws IOException {
    int x = 0, y = 0;

    try {
      x = Integer.parseInt(args[0]);
      y = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      System.out.println("Coordenadas inválidas.");
      System.exit(1);
    }

    Map<Point, Double> mapPoints = Files.lines(Paths.get(FILE)).map(line -> {
      String[] data = line.split(",");
      int lineX = Integer.parseInt(data[0]);
      int lineY = Integer.parseInt(data[1]);
      double lineValue = Double.parseDouble(data[2]);
      Point linePoint = new Point(lineX, lineY);
      return new AbstractMap.SimpleEntry<>(linePoint, lineValue);
    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (p1, _) -> p1));

    Point point = new Point(x, y);

    double idw = calculateInverseDistanceWeighting(mapPoints, POWER, point);

    System.out.println("IDW: " + String.format(Locale.US, "%.1f", idw));
  }
}
