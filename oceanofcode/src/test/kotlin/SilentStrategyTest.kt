import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class SilentStrategyTest {

  @Test
  operator fun next() {
    val tracker = SubmarineTracker(MapTest.generateSmallMapTest());
    val silentStrategy = SilentStrategy(tracker);
    val action = silentStrategy.next(Vector2D(2, 3))
    assertNotNull(action)
    assertEquals(Direction.W, action.direction)
  }
}