package idw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class IDWInterpolationSerial {
	private static final String FILE = "./data/measurements.txt";

	public record Point(int x, int y, double value) {
	}

	public static double calculateInverseDistanceWeighting(Point[] mapPoints, int power, Point point) {
		double numerator = 0.0;
		double weights = 0.0;

		for (Point mapPoint : mapPoints) {
			double distance = getDistance(mapPoint, point);

			if (distance == 0.0) {
				return mapPoint.value();
			}

			double weight = 1.0 / Math.pow(distance, power);
			numerator += mapPoint.value() * weight;
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

		Point[] mapPoints = Files.lines(Paths.get(FILE)).map(line -> {
			String[] data = line.split(",");
			int lineX = Integer.parseInt(data[0]);
			int lineY = Integer.parseInt(data[1]);
			double lineValue = Double.parseDouble(data[2]);
			return new Point(lineX, lineY, lineValue);
		}).toArray(Point[]::new);

		Point point = new Point(x, y, -1);

		double idw = calculateInverseDistanceWeighting(mapPoints, 2, point);

		System.out.println("IDW: " + String.format(Locale.US, "%.1f", idw));
	}
}
