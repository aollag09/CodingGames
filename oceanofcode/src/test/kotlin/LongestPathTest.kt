import org.junit.jupiter.api.Test

internal class LongestPathTest {

  @Test
  fun test1() {
    val graph = EnvTest.generateEnvTest1().moveGraph();
    val longestPath: LongestPath = LongestPath(graph);
    val path = longestPath.solve(Vector2D());
  }

  @Test
  fun test2() {
    val graph = EnvTest.generateEnvTest2().moveGraph();
    val longestPath: LongestPath = LongestPath(graph);
    val path = longestPath.solve(Vector2D());
  }


}