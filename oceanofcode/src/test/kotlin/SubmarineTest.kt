import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class SubmarineTest {

  @Test
  fun isTrapDirection() {
    val map = MapTest.generateMapTest2()
    val submarine = Submarine()
    submarine.position = Vector2D(3,12)
    assert(submarine.isTrapDirection(map, Direction.S))
    assert( ! submarine.isTrapDirection(map, Direction.N))
  }

  @Test
  fun add_mine(){
    val submarine = Submarine()
    submarine.position =  Vector2D(7,7 )
    submarine.register(0, Mine( Direction.S))
    assert(submarine.mines.contains(Vector2D(7,8)))
  }
}