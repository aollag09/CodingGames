import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AStartTest {


  @Test
  fun small_empty_map() {
    val path = MapTest.generateSmallEmptyMapTest().path(Vector2D(), Vector2D(4, 0));
    assertNotNull(path)
    assertEquals(5, path!!.size)
    assert(path.contains(Vector2D()))
    assert(path.contains(Vector2D(4, 0)))
    assert(path.contains(Vector2D(3, 0)))
    assert(path.contains(Vector2D(2, 0)))
    assert(path.contains(Vector2D(1, 0)))
  }

  @Test
  fun small_map() {
    val path = MapTest.generateSmallMapTest().path(Vector2D(), Vector2D(0, 3));
    assertNotNull(path)
    assertEquals(10, path!!.size)
  }

  @Test
  fun impossible_path() {
    val path = MapTest.generateSmallMapTest().path(Vector2D(), Vector2D(0, 3), listOf(Vector2D(2, 2)));
    assertNull(path)
  }


}