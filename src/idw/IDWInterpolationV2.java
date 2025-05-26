package idw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import records.FileSegment;
import records.Point;

// Versão 2: Virtual threads

public class IDWInterpolationV2 {
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

      synchronized (this) {
        this.numerator += (valueReaded * weight);
        this.weights += weight;
      }
    }

    public double getIDW() {
      return this.numerator / this.weights;
    }
  }

  public static List<FileSegment> getFileSegment(FileChannel fileChannel, RandomAccessFile raf,
      int numberOfThreads) throws IOException {
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
        int segmentIndex = segments.indexOf(segment);
        try {
          MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, segment.start(),
              segment.end() - segment.start());
          Runnable runnable = new Runnable() {
            public void run() {
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

              controller.countDown();
            }
          };
          Thread.ofVirtual().name("Thread " + segmentIndex).start(runnable);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });

      controller.await();

      double idw = idwCalculator.getIDW();
      System.out.println("IDW: " + String.format("%.1f", idw).replace(',', '.'));

      long end = System.currentTimeMillis();
      long time = (end - start) / 1000;
      System.out.println("Executed in " + time + "s.");
      System.exit(0);
    }
  }
}
