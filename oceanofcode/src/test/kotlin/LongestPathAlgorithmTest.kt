import main.kotlin.LongestPathAlgorithm
import main.kotlin.Vector2D
import org.junit.jupiter.api.Test

internal class LongestPathAlgorithmTest {

  @Test
  fun solve() {

    var algo = LongestPathAlgorithm(MapTest.generateMapTest1(), Vector2D());
    println(algo.graph.toString());


  }
}