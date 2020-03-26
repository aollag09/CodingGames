import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class MineStrategyTest {

  @Test
  operator fun next() {
    val env = EnvTest.generateEnvTest1()
    env.terrible.position = Vector2D(3, 1)
    env.terrible.mineCoolDown = 0
    val order = MineStrategy(env.terrible, env.trackerKasakta).next()
    assert(order is Mine)
    assert((order as Mine).direction == Direction.W)
  }
}