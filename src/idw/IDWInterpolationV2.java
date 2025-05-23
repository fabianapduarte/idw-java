package idw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

// Versão 2: Virtual threads

public class IDWInterpolationV2 {
	private static final String FILE = "./data/measurements.txt";

	private static final int POWER = 2;

	public record Point(int x, int y) {
	}

	public record FileSegment(long start, long end) {
	}

	static class IDW {
		private double numerator, weights;
		private Point point;

		public IDW(Point point) {
			super();
			this.point = point;
			this.setNumerator(0);
			this.setWeights(0);
		}

		public double getDistance(Point p, Point q) {
			return Math.hypot(p.x() - q.x(), p.y() - q.y());
		}

		public void calculateIDW(Point pointReaded, double valueReaded) {
			double distance = getDistance(pointReaded, this.point);

			if (distance != 0.0) {
				double weight = 1.0 / Math.pow(distance, POWER);
				synchronized (this) {
					setNumerator(getNumerator() + (valueReaded * weight));
					setWeights(getWeights() + weight);
				}
			}

		}

		public double getIDW() {
			return getNumerator() / getWeights();
		}

		public double getNumerator() {
			return numerator;
		}

		public void setNumerator(double numerator) {
			this.numerator = numerator;
		}

		public double getWeights() {
			return weights;
		}

		public void setWeights(double weights) {
			this.weights = weights;
		}
	}

	public static List<FileSegment> getFileSegment(FileChannel fileChannel, RandomAccessFile raf, int numberOfThreads)
			throws IOException {
		long fileSize = fileChannel.size();
		long chunkSize = fileSize / numberOfThreads;
		long lastLocation = 0;
		List<FileSegment> segments = new ArrayList<>();

		for (int i = 0; i < numberOfThreads; i++) {
			long startSegment;
			long endSegment;

			if (i == 0) {
				startSegment = 0;
			} else {
				lastLocation++;
				startSegment = lastLocation;
			}

			if (i == (numberOfThreads - 1)) {
				endSegment = fileSize;
			} else {
				lastLocation = startSegment + chunkSize;
				raf.seek(lastLocation);
				while (raf.read() != '\n') {
					lastLocation++;
				}
				lastLocation++;
				endSegment = lastLocation;
			}

			segments.add(new FileSegment(startSegment, endSegment));
		}

		return segments;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		int x = 0, y = 0;
		int numberOfThreads = Runtime.getRuntime().availableProcessors();
		final CountDownLatch controller = new CountDownLatch(numberOfThreads);

		try {
			x = Integer.parseInt(args[0]);
			y = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Coordenadas inválidas.");
			System.exit(1);
		}

		Point point = new Point(x, y);

		try (RandomAccessFile raf = new RandomAccessFile(FILE, "r")) {
			FileChannel fileChannel = raf.getChannel();
			List<FileSegment> segments = getFileSegment(fileChannel, raf, numberOfThreads);
			IDW idwCalculator = new IDW(point);

			segments.forEach(segment -> {
				MappedByteBuffer buffer;
				try {
					buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, segment.start(),
							segment.end() - segment.start());
					Runnable runnable = new Runnable() {
						public void run() {
							StringBuilder lineBuilder = new StringBuilder();

							while (buffer.hasRemaining()) {
								char c = (char) buffer.get();

								if (c == '\n') {
									int endNumber1 = lineBuilder.indexOf(",");
									int endNumber2 = lineBuilder.indexOf(",", endNumber1 + 1);
									int lineX = Integer.parseInt(lineBuilder.substring(0, endNumber1));
									int lineY = Integer.parseInt(lineBuilder.substring(endNumber1 + 1, endNumber2));
									double lineValue = Double.parseDouble(lineBuilder.substring(endNumber2 + 1));
									Point pointReaded = new Point(lineX, lineY);

									idwCalculator.calculateIDW(pointReaded, lineValue);

									lineBuilder.delete(0, lineBuilder.length());
								} else {
									lineBuilder.append(c);
								}
							}

							controller.countDown();
						}
					};
					Thread thread = Thread.ofVirtual().name("Thread", 1).start(runnable);
					thread.join();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			});

			controller.await();
			double idw = idwCalculator.getIDW();

			System.out.println("IDW: " + String.format(Locale.US, "%.1f", idw));
		}
	}
}
