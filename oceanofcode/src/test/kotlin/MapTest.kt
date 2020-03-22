import main.kotlin.Map
import main.kotlin.Vector2D
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MapTest {

  companion object {
    fun generateMapTest1(): Map {
      val map: Map = Map(15, 15);
      map.parse("...xx........xx", 0);
      map.parse(".............xx", 1);
      map.parse("...........xx..", 2);
      map.parse("..xxx......xx..", 3);
      map.parse("..xxx..........", 4);
      map.parse(".xxxx..........", 5);
      map.parse(".xxxx..........", 6);
      map.parse(".xxxxx..xx.....", 7);
      map.parse(".xxxxx..xx.....", 8);
      map.parse("........xx.....", 9);
      map.parse("........xx.....", 10);
      map.parse("..............x", 11);
      map.parse("..............x", 12);
      map.parse("...............", 13);
      map.parse("...............", 14);
      return map;
    }
  }

  @Test
  fun isIsland() {
    var map: Map = generateMapTest1();
    assert(map.isIsland(Vector2D(4, 0)))
    assert(map.isIsland(Vector2D(1, 5)))
    assert(!map.isIsland(Vector2D(0, 0)))
    assert(!map.isIsland(Vector2D(1, 1)))
  }

  @Test
  fun isWater() {
    var map: Map = generateMapTest1();
    assert(!map.isWater(Vector2D(4, 0)))
    assert(!map.isWater(Vector2D(1, 5)))
    assert(map.isWater(Vector2D(0, 0)))
    assert(map.isWater(Vector2D(1, 1)))
  }

  @Test
  fun getWaterSection() {
  }

  @Test
  fun neigh() {
    var map: Map = generateMapTest1();

    var neigh = map.neigh(Vector2D(4, 13));
    assertEquals(4, neigh.size)

    neigh = map.neigh(Vector2D(0, 0));
    assertEquals(2, neigh.size)
    assert(neigh.contains(Vector2D(1, 0)))
    assert(neigh.contains(Vector2D(0, 1)))

    neigh = map.neigh(Vector2D(14, 0))
    assertEquals(0, neigh.size)
  }


}