import org.junit.jupiter.api.Test

internal class InvisibleStrategyTest {

  @Test
  operator fun next() {
    val env = EnvTest.generateEnvTest1()
    InvisibleStrategy( env.terrible, env.trackerKasakta ).apply()
  }
}