import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class MapTestPCS {

  private val random = Random(1)

  @Test
  fun isIsland() {
    val map = MapTest.generateMapTest2()
    val it = 10000000
    val start = System.currentTimeMillis()
    for (i in 1..it)
      map.isIsland(Vector2D(random.nextInt(map.size.getIX()), random.nextInt(map.size.getIY())))
    val time = System.currentTimeMillis() - start
    println("Performed $it iterations in $time ms")
  }

}