package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

public class GenerateData {
  private static final Path FILE = Path.of("./data/measurements.txt");

  public static void main(String[] args) throws IOException {
    final int SIZE = 100_000_000;
    Random rand = new Random();

    System.out.println("Gerando arquivo...");

    try (BufferedWriter bw = Files.newBufferedWriter(FILE)) {
      for (int i = 0; i < SIZE; i++) {
        int x = rand.nextInt(100_000);
        int y = rand.nextInt(100_000);
        double temp = 30 - 0.005 * y + 0.003 * x + 5 * Math.sin(x / 500.0) + 3 * Math.cos(y / 500.0)
            + (rand.nextDouble() * 2 - 1);
        bw.write(x + "," + y + "," + String.format(Locale.US, "%.1f", temp));
        bw.newLine();

        if (i % 10_000_000 == 0) {
          System.out.println("Gerado: " + i);
        }
      }
    }

    System.out.println("Arquivo gerado com sucesso.");
  }
}
