import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TrackerUpdateTest {

  @Test
  fun torpedo_impact_a_leau() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Torpedo(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    env.kasakta.life.add(1, 6)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(9, env.trackerKasakta.outdated.size)
  }


  @Test
  fun torpedo_impact_touche() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Torpedo(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched
    env.kasakta.life.add(1, 5)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(17, env.trackerKasakta.outdated.size)
  }


  @Test
  fun torpedo_impact_touche_coule() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Torpedo(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched hard !
    env.kasakta.life.add(1, 4)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(1, env.trackerKasakta.candidates.size)
  }


  @Test
  fun trigger_impact_a_leau() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Trigger(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    env.kasakta.life.add(1, 6)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(9, env.trackerKasakta.outdated.size)
  }


  @Test
  fun trigger_impact_touche() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Trigger(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched
    env.kasakta.life.add(1, 5)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(17, env.trackerKasakta.outdated.size)
  }


  @Test
  fun trigger_impact_touche_coule() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Trigger(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched hard !
    env.kasakta.life.add(1, 4)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(1, env.trackerKasakta.candidates.size)
  }

  @Test
  fun trigger_and_torpedo_impact_touche() {
    val env = EnvTest.generateEnvSmallEmptyTest()

    env.terrible.register(0, Trigger(Vector2D(3, 3)))
    env.terrible.register(0, Torpedo(Vector2D(1, 1)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched
    env.kasakta.life.add(1, 5)
    env.turn = 1

    env.trackerKasakta.updateFireReach(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    Assertions.assertEquals(10, env.trackerKasakta.outdated.size)
  }


}