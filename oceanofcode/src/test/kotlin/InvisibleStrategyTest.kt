import org.junit.jupiter.api.Test

internal class InvisibleStrategyTest {

  @Test
  fun pcs_without_trap() {
    val env = EnvTest.generateEnvTest1()
    env.terrible.position = Vector2D(7,7)
    val it = 100
    val start = System.currentTimeMillis()
    for( i in 1..it)
    InvisibleStrategy( env.terrible, env.trackerKasakta, env.turn).apply()
    val time = System.currentTimeMillis() - start
    println("Performed $it iterations in $time ms")
  }


  @Test
  fun pcs_with_trap() {
    val env = EnvTest.generateEnvTest1()
    env.terrible.position = Vector2D(7,7)
    env.turn = 10
    val it = 100
    val start = System.currentTimeMillis()
    for( i in 1..it)
      InvisibleStrategy( env.terrible, env.trackerKasakta, env.turn).apply()
    val time = System.currentTimeMillis() - start
    println("Performed $it iterations in $time ms")
  }
}