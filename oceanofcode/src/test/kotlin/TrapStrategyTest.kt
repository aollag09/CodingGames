import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TrapStrategyTest {

  @Test
  fun apply() {
    val env = EnvTest.generateEnvTest2()
    TrapStrategy(env.terrible, env.map).apply()
  }
}