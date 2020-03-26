import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrderTest {

  @Test
  fun parseOrder() {
    assert(Order.parseOrder("SURFACE") is Surface)
    assert(Order.parseOrder(" SURFACE 1") is SurfaceSector)

    val test: List<String> = arrayListOf("SURFACE",
        "SURFACE 3",
        "SURFACE 9",
        "MOVE N",
        "MOVE E",
        "MOVE S",
        "MOVE W",
        "TORPEDO 1 3",
        "SILENCE",
        "SILENCE N 3",
        "MOVE N TORPEDO",
        "MOVE N SILENCE",
        "MOVE N MINE",
        "MOVE S",
        "SONAR 3",
        "SONAR 9",
        "MINE",
        "MINE N",
        "MINE S",
        "TRIGGER 1 2",
        "TRIGGER 10 12",
        "MSG 1 5",
        "MSG Ca va bien ?")
    test.forEach {
      assertEquals(it, Order.parseOrder(it).toOrderString())
    }
  }

}