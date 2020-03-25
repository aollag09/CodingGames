import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TrackerTest {

  @Test
  fun test_small_empty_map() {
    val tracker = Tracker(MapTest.generateSmallEmptyMapTest())
    assertEquals(25, tracker.candidates.size)
    tracker.update(Move(Direction.N))
    assertEquals(5, tracker.outdated.size)
    assertEquals(20, tracker.candidates.size)
  }

  @Test
  fun test_small_empty_map_2_moves() {
    val tracker = Tracker(MapTest.generateSmallEmptyMapTest())
    tracker.update(Move(Direction.N))
    tracker.update(Move(Direction.N))
    assertEquals(15, tracker.candidates.size)
  }

  @Test
  fun test_small_map() {
    val tracker = Tracker(MapTest.generateSmallMapTest())
    tracker.testPrintMap(false)
    assertEquals(17, tracker.candidates.size)
    tracker.update(Move(Direction.N))
    tracker.testPrintMap(false)
    assertEquals(9, tracker.outdated.size)
    assertEquals(8, tracker.candidates.size)
    tracker.update(Move(Direction.E))
    assertEquals(4, tracker.candidates.size)
    tracker.testPrintMap(false)
    tracker.update(Move(Direction.N))
    tracker.testPrintMap(false)
  }

  @Test
  fun test_surface_map1() {
    var tracker = Tracker(MapTest.generateMapTest1())
    tracker.testPrintMap(false)
    tracker.update(SurfaceSector(1))
    assertEquals(17, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(2))
    assertEquals(25, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(3))
    assertEquals(17, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(4))
    assertEquals(9, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(5))
    assertEquals(17, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(6))
    assertEquals(25, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(7))
    assertEquals(25, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(8))
    assertEquals(23, tracker.candidates.size)

    tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(SurfaceSector(9))
    assertEquals(23, tracker.candidates.size)
  }

  @Test
  fun test_surface_map3() {
    val tracker = Tracker(MapTest.generateMapTest3())
    tracker.candidates.clear()
    tracker.candidates.add(Vector2D(0,5))
    tracker.update(SurfaceSector(4))
    tracker.testPrintMap(false)
    assertEquals(1, tracker.candidates.size)
  }

  @Test
  fun test_surface_sector_map3() {
    val tracker = Tracker(MapTest.generateMapTest3())
    tracker.update(SurfaceSector(4))
    tracker.testPrintMap(false)
    assertEquals(25, tracker.candidates.size)
  }

  @Test
  fun test_torpedo_map1() {
    val tracker = Tracker(MapTest.generateMapTest1())
    tracker.update(Torpedo(Vector2D(7, 7)))
    tracker.testPrintMap(false)
    assertEquals(28, tracker.candidates.size)
  }

  @Test
  fun test_torpado_candidates_map2(){
    val tracker = Tracker(MapTest.generateMapTest2())
    tracker.candidates.clear();
    tracker.candidates.addAll(listOf( Vector2D(7,7), Vector2D(6,7), Vector2D(7,8), Vector2D(3, 0)))
    tracker.update(Torpedo(Vector2D(9, 7)))
    tracker.testPrintMap(false)
    assertEquals(3, tracker.candidates.size)
  }

  @Test
  fun test_silence_empty_big_map() {
    val tracker = Tracker(MapTest.generateMapBigEmpty())
    tracker.candidates.clear()
    tracker.candidates.add(Vector2D(7,7))
    tracker.update(Silence())
    tracker.testPrintMap(false)
    assertEquals(17, tracker.candidates.size)
  }

  @Test
  fun test_silence_empty_big_map_with_trail() {
    val tracker = Tracker(MapTest.generateMapBigEmpty())
    tracker.candidates.clear()
    tracker.candidates.add(Vector2D(7,7))
    tracker.trail.add( Direction.N)
    tracker.update(Silence())
    tracker.testPrintMap(false)
    assertEquals(13, tracker.candidates.size)
  }

  @Test
  fun test_silence_map1_with_trail() {
    val tracker = Tracker(MapTest.generateMapTest1())
    tracker.candidates.clear()
    tracker.candidates.add(Vector2D(7,8))
    tracker.trail.add( Direction.N)
    tracker.update(Silence())
    tracker.testPrintMap(false)
    assertEquals(6, tracker.candidates.size)
  }


  @Test
  fun test_silence_empty_big_map_with_trails() {
    val tracker = Tracker(MapTest.generateMapBigEmpty())
    tracker.candidates.clear()
    tracker.candidates.add(Vector2D(9,7))
    tracker.trail.add( Direction.S)
    tracker.trail.add( Direction.W)
    tracker.trail.add( Direction.W)
    tracker.trail.add( Direction.N)
    tracker.update(Silence())
    tracker.testPrintMap(false)
    assertEquals(10, tracker.candidates.size)
  }

  @Test
  fun test_silence_empty_big_map_with_trails_two_candidates() {
    val tracker = Tracker(MapTest.generateMapBigEmpty())
    tracker.candidates.clear()
    tracker.candidates.add(Vector2D(9,7))
    tracker.candidates.add(Vector2D(8,7))
    tracker.trail.add( Direction.S)
    tracker.trail.add( Direction.W)
    tracker.trail.add( Direction.W)
    tracker.trail.add( Direction.N)
    tracker.update(Silence())
    tracker.testPrintMap(false)
    assertEquals(15, tracker.candidates.size)
  }
}