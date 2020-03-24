import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EnvTest {

  companion object {
    fun generateEnvTest1(): Env {
      return Env(MapTest.generateMapTest1())
    }

    fun generateEnvTest2(): Env {
      return Env(MapTest.generateMapTest2())
    }

    fun generateEnvSmallEmptyTest(): Env {
      return Env(MapTest.generateSmallEmptyMapTest())
    }
  }


  @Test
  fun torpedo_impact_a_leau() {
    val env = generateEnvSmallEmptyTest()

    env.terrible.register(0, Torpedo(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    env.kasakta.life.add(1, 6)
    env.turn = 1

    env.trackerKasakta.testPrintMap(false)
    env.trackerKasakta.updateTorpedo(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    assertEquals(9, env.trackerKasakta.outdated.size)
  }


  @Test
  fun torpedo_impact_touche() {
    val env = generateEnvSmallEmptyTest()

    env.terrible.register(0, Torpedo(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched
    env.kasakta.life.add(1, 5)
    env.turn = 1

    env.trackerKasakta.testPrintMap(false)
    env.trackerKasakta.updateTorpedo(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    assertEquals(17, env.trackerKasakta.outdated.size)
  }


  @Test
  fun torpedo_impact_touche_coule() {
    val env = generateEnvSmallEmptyTest()

    env.terrible.register(0, Torpedo(Vector2D(3, 3)))
    env.kasakta.life.add(0, 6)
    // enemy has been touched hard !
    env.kasakta.life.add(1, 4)
    env.turn = 1

    env.trackerKasakta.testPrintMap(false)
    env.trackerKasakta.updateTorpedo(env.turn, env.terrible, env.kasakta)
    env.trackerKasakta.testPrintMap(false)
    assertEquals(1, env.trackerKasakta.candidates.size)
  }

  @Test
  fun test_surface() {
    val env = generateEnvTest1()
    env.kasakta.register(0, SurfaceSector(1))
    env.endTurn();
    env.initTurn()
    assertEquals(17, env.trackerKasakta.candidates.size)
  }
}
