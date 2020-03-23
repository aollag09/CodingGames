import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AggressiveStrategyTest {

  @Test
  fun test_small_empty_env() {
    val env = EnvTest.generateEnvSmallEmptyTest();
    env.opTracker.update(Move(Direction.S))
    env.opTracker.update(Move(Direction.S))
    env.opTracker.update(Move(Direction.S))
    env.opTracker.testPrintMap(false)

    env.submarine.position = Vector2D()
    val strategy = AggressiveStrategy(env.opTracker);
    val order = strategy.next(env.submarine)
    assert(order is Move)
    assertEquals(Direction.S, (order as Move).direction)
  }

  @Test
  fun test_small_empty_env_attack() {
    val env = EnvTest.generateEnvSmallEmptyTest();
    env.opTracker.update(Move(Direction.S))
    env.opTracker.update(Move(Direction.S))
    env.opTracker.update(Move(Direction.S))
    env.opTracker.testPrintMap(false)

    env.submarine.position = Vector2D(1, 3)
    env.submarine.torpedoCoolDown = 3;

    val strategy = AggressiveStrategy(env.opTracker);
    val order = strategy.next(env.submarine)
    assert(order is Torpedo)
  }
}