import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class FireStrategyTest {

  @Test
  fun test_small_empty_env() {
    val env = EnvTest.generateEnvSmallEmptyTest()
    env.trackerKasakta.update(Move(Direction.S))
    env.trackerKasakta.update(Move(Direction.S))
    env.trackerKasakta.update(Move(Direction.S))
    env.trackerKasakta.testPrintMap(false)

    env.terrible.position = Vector2D()
    val strategy = FireStrategy(env.terrible, env.trackerKasakta)
    val order = strategy.apply()
    assert(order is Empty)
  }

  @Test
  fun test_small_empty_env_attack() {
    val env = EnvTest.generateEnvSmallEmptyTest()
    env.trackerKasakta.update(Move(Direction.S))
    env.trackerKasakta.update(Move(Direction.S))
    env.trackerKasakta.update(Move(Direction.S))
    env.trackerKasakta.testPrintMap(false)

    env.terrible.position = Vector2D(1, 3)
    env.terrible.torpedoCoolDown = 0

    val strategy = FireStrategy(env.terrible, env.trackerKasakta)
    val order = strategy.apply()
    assert(order is Torpedo)
  }

  @Test
  fun test_1_env_attack() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.update(SurfaceSector(1))
    env.trackerKasakta.update(Move(Direction.W))
    env.trackerKasakta.testPrintMap(false)

    env.terrible.position = Vector2D(1, 3)
    env.terrible.torpedoCoolDown = 0

    val strategy = FireStrategy(env.terrible, env.trackerKasakta)
    val order = strategy.apply()
    assert(order is Torpedo)
    assertEquals(Vector2D(1, 1), (order as Torpedo).target)
  }

  @Test
  fun test_1_env_attack_do_not_fire_on_me() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.update(SurfaceSector(1))
    env.trackerKasakta.update(Move(Direction.W))
    env.trackerKasakta.testPrintMap(false)

    env.terrible.position = Vector2D(1, 1)
    env.terrible.torpedoCoolDown = 0

    val strategy = FireStrategy(env.terrible, env.trackerKasakta)
    val order = strategy.apply()
    assert(order is Empty)
    //assertNotEquals(Vector2D(1, 1), (order as Torpedo).target)
  }
}