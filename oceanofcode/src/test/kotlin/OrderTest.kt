import org.junit.jupiter.api.Test

internal class OrderTest {

  @Test
  fun parseOrder() {
    assert(Order.parseOrder("SURFACE") is Surface)
    assert(Order.parseOrder(" SURFACE 1") is SurfaceSector)
    assert(Order.parseOrder("TORPEDO") is LoadTorpedo)

    val test: List<String> = arrayListOf("SURFACE",
        "SURFACE 3",
        "SURFACE 9",
        "MOVE N",
        "MOVE E",
        "MOVE S",
        "MOVE W",
        "TORPEDO")
    test.forEach {
      assert(Order.parseOrder(it).toOrderString() == it)
    }
  }

}