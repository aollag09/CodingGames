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
    val tracker = Tracker(MapTest.generateMapTest1())
    tracker.testPrintMap(false)
    tracker.update(SurfaceSector(1))
    assertEquals(17, tracker.candidates.size)

    tracker.update(SurfaceSector(2))
    assertEquals(25, tracker.candidates.size)

    tracker.update(SurfaceSector(3))
    assertEquals(17, tracker.candidates.size)

    tracker.update(SurfaceSector(4))
    assertEquals(9, tracker.candidates.size)

    tracker.update(SurfaceSector(5))
    assertEquals(17, tracker.candidates.size)

    tracker.update(SurfaceSector(6))
    assertEquals(25, tracker.candidates.size)

    tracker.update(SurfaceSector(7))
    assertEquals(25, tracker.candidates.size)

    tracker.update(SurfaceSector(8))
    assertEquals(23, tracker.candidates.size)

    tracker.update(SurfaceSector(9))
    assertEquals(23, tracker.candidates.size)
  }

}