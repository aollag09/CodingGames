import org.junit.jupiter.api.Test

internal class LongestPathTest {

  @Test
  fun dfs() {
    val graph = EnvTest.generateEnvTest1().moveGraph();
    val longestPath: LongestPath = LongestPath(graph);
    val path = longestPath.solve(Vector2D());
    for (vec in path)
      println(vec)
  }


}