import org.junit.jupiter.api.Test

internal class DefenseStrategyTest {

  @Test
  operator fun next() {
    val env = EnvTest.generateEnvTest1()
    env.turn++
    env.terrible.life.add(env.turn, 6)
    env.initTurn()
    env.turn++
    env.terrible.life.add(env.turn, 5)
    env.terrible.silenceCoolDown = 0
    env.initTurn()

    env.trackerKasakta.testPrintMap(false)
    assert(DefenseStrategy(env.terrible).apply() is Silence)
  }
}