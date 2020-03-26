import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DefenseStrategyTest {

  @Test
  operator fun next() {
    val env = EnvTest.generateEnvTest1()
    env.turn ++
    env.initTurn()
    env.endTurn()
    env.turn ++
    env.initTurn()
  }
}