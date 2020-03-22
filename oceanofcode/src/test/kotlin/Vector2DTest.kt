import Vector2D
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Vector2DTest {

  @Test
  fun length() {
    val vector: Vector2D = Vector2D(1, 0)
    assertEquals(1.0, vector.length())
  }

  @Test
  fun distance() {
    assertEquals(1.0, Vector2D(0, 0).distance(Vector2D(-1, 0)));
  }

  @Test
  fun testEquals() {
    assertEquals(Vector2D(2, 2), Vector2D(2, 2));
    assertEquals(Vector2D(0, 0), Vector2D());
    assertEquals(Vector2D(0, 0), Vector2D(0.0, 0.0));
  }

  @Test
  fun testDirection(){
    assertEquals("N", Vector2D( 1,1).direction( Vector2D(1, 0)))
    assertEquals("E", Vector2D( 1,1).direction( Vector2D(2, 1)))
  }
}