import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.Comparator

class PriorityQueueTest {

  @Test
  fun test() {
    val to = Vector2D(4, 4)
    val queue = PriorityQueue<Vector2D>(Comparator { t1, t2 -> (t1.distance(to) - t2.distance(to)).toInt() })

    queue.add(Vector2D(1, 1))
    queue.add(Vector2D(3, 3))
    queue.add(Vector2D(2, 2))
    queue.add(Vector2D())

    Assertions.assertEquals(Vector2D(3, 3), queue.poll())
  }
}