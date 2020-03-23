import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SubmarineTrackerTest{

  @Test
  fun test_small_empty_map(){
    val tracker = SubmarineTracker( MapTest.generateSmallEmptyMapTest());
    assertEquals(25,tracker.candidates.size)
    tracker.update(Move(Direction.N));
    assertEquals(5, tracker.outdated.size );
    assertEquals(20,tracker.candidates.size)
  }

  @Test
  fun test_small_empty_map_2_moves(){
    val tracker = SubmarineTracker( MapTest.generateSmallEmptyMapTest());
    tracker.update(Move(Direction.N));
    tracker.update(Move(Direction.N));
    assertEquals(15,tracker.candidates.size)
  }

  @Test
  fun test_small_map(){
    val tracker = SubmarineTracker( MapTest.generateSmallMapTest());
    tracker.testPrintMap(false)
    assertEquals(17,tracker.candidates.size)
    tracker.update(Move(Direction.N));
    tracker.testPrintMap(false)
    assertEquals(9, tracker.outdated.size )
    assertEquals(8,tracker.candidates.size)
    tracker.update(Move(Direction.E));
    assertEquals(4,tracker.candidates.size)
    tracker.testPrintMap(false)
    tracker.update(Move(Direction.N));
    tracker.testPrintMap(false)
  }

}