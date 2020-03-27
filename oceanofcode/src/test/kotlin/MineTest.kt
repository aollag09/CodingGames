import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MineTest {

  @Test
  fun add_mine() {
    val env = EnvTest.generateEnvTest2()
    env.terrible.position = Vector2D(7, 7)
    env.terrible.register(0, listOf(Move(Direction.W), Mine(Direction.N)))
    env.terrible.mines.forEach { Assertions.assertEquals( Vector2D(6,6), it) }
  }
}