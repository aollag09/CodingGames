import main.kotlin.LongestPath
import main.kotlin.Map
import main.kotlin.Vector2D
import org.junit.jupiter.api.Test

internal class LongestPathTest {

  @Test
  fun dfs() {
    val map: Map = MapTest.generateMapTest1();
    val longestPath: LongestPath = LongestPath(map.directedGraph(Vector2D()));
    longestPath.solve(Vector2D());
  }


}