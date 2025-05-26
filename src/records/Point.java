package records;

public record Point(int x, int y) {
  public boolean equalsTo(Point otherPoint) {
    return this.x == otherPoint.x && this.y == otherPoint.y;
  }

  public double distanceTo(Point otherPoint) {
    double dx = this.x - otherPoint.x;
    double dy = this.y - otherPoint.y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
