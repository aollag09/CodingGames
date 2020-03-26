import org.junit.jupiter.api.Test

internal class MineStrategyTest {

  @Test
  operator fun next() {
    val env = EnvTest.generateEnvTest1()
    env.terrible.position = Vector2D(3, 1)
    env.terrible.mineCoolDown = 0
    val order = MineStrategy(env.terrible, env.trackerKasakta).apply()
    assert(order is Mine)
    assert((order as Mine).direction == Direction.W)
  }
}