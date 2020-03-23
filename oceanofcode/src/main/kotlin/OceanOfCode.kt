import java.util.*
import kotlin.collections.HashSet
import kotlin.math.*
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

  val input = Scanner(System.`in`)
  val width = input.nextInt()
  val height = input.nextInt()
  val myId = input.nextInt()

  if (input.hasNextLine()) {
    input.nextLine()
  }

  // Create main.kotlin.Map
  val map: Map = Map(width, height)
  for (j in 0 until height)
    map.parse(input.nextLine(), j)

  // Initialise environment
  val env: Env = Env(map)
  env.submarine.id = myId

  val start: Vector2D = env.start()
  println(start.getIX().toString() + " " + start.getIY().toString())

  val graph = env.moveGraph(start)
  val longestPath: LongestPath = LongestPath(graph)
  var path: MutableList<Vector2D> = mutableListOf()
  val millis = measureTimeMillis {
    path = longestPath.solve(env.submarine.position)
  }
  System.err.println("Solve longest path in $millis ms")
  System.err.println("Path size : " + path.size)

  // game loop
  while (true) {
    // Update environment
    env.submarine.position = Vector2D(input.nextInt(), input.nextInt())
    env.submarine.life = input.nextInt()
    env.opponent.life = input.nextInt()
    env.submarine.torpedoCoolDown = input.nextInt()
    env.submarine.sonarCoolDown = input.nextInt()
    env.submarine.silenceCoolDown = input.nextInt()
    env.submarine.mineCoolDown = input.nextInt()
    env.submarine.sonarResult = input.next()
    if (input.hasNextLine()) {
      input.nextLine()
    }
    val opponentOrders = input.nextLine()
    val direction = env.submarine.position.direction(path.removeAt(0))

    println("MOVE $direction TORPEDO")

    env.submarineTrail.add(env.submarine.position)
  }


}

class Env(val map: Map) {
  val submarine: Submarine = Submarine()
  val opponent: Opponent = Opponent()
  val submarineTrail: MutableSet<Vector2D> = mutableSetOf()

  /** Create a graph of next movable positions regarding environment */
  fun moveGraph(start: Vector2D = submarine.position): Graph<Vector2D> {
    val graph: Graph<Vector2D> = Graph(true)
    val visited: MutableSet<Vector2D> = submarineTrail.toMutableSet()
    val toVisit: MutableList<Vector2D> = mutableListOf()
    toVisit.add(start)
    while (toVisit.isNotEmpty()) {
      val next: Vector2D = toVisit.removeAt(0)
      if (!visited.contains(next)) {
        for (neigh in map.neigh(next)) {
          graph.addEdge(next, neigh)
          if (!visited.contains(neigh))
            toVisit.add(neigh)
        }
        visited.add(next)
      }
    }
    return graph
  }

  /** Choose the starting point */
  fun start(): Vector2D {
    val waters: Set<Vector2D> = map.getWater()
    var min: Int = 5
    var start: Vector2D = Vector2D()
    for (water in waters) {
      val neigh = map.neigh(water).size
      if (neigh < min) {
        min = neigh
        start = water
      }
    }
    return start
  }
}

class Map(width: Int, height: Int) {
  private val size: Vector2D = Vector2D(width, height)
  private val islands: MutableSet<Vector2D> = HashSet()

  fun isIsland(pos: Vector2D): Boolean {
    return islands.contains(pos)
  }

  fun isWater(pos: Vector2D): Boolean {
    return pos.x >= 0 && pos.x < size.getIX() && pos.y >= 0 && pos.y < size.getIY() && !isIsland(pos)
  }

  fun parse(line: String, j: Int) {
    for (i in line.indices)
      if (line.toCharArray()[i] != '.')
        this.islands.add(Vector2D(i, j))
  }

  fun getWaterSection(section: Int): Set<Vector2D> {
    if (section < 1 || section > 9)
      throw RuntimeException("Section should be between 1 and 9")
    val water: MutableSet<Vector2D> = HashSet()
    val x: Int = section % 3
    val y: Int = section / 3
    for (i in x - 1 until x + 5) {
      for (j in y - 1 until y + 5) {
        val pos: Vector2D = Vector2D(i, j)
        if (isWater(pos))
          water.add(pos)
      }
    }
    return water
  }

  fun getWater(): Set<Vector2D> {
    val water: MutableSet<Vector2D> = mutableSetOf()
    for (x in 0 until size.getIX())
      for (y in 0 until size.getIY())
        if (isWater(Vector2D(x, y)))
          water.add(Vector2D(x, y))
    return water
  }

  fun neigh(pos: Vector2D): Set<Vector2D> {
    val neigh: MutableSet<Vector2D> = mutableSetOf()
    for (dx in -1..1 step 1)
      for (dy in -1..1 step 1)
        if (abs(dx) + abs(dy) == 1)
          if (pos.x + dx >= 0 && pos.x + dx < this.size.x)
            if (pos.y + dy >= 0 && pos.y + dy < this.size.y)
              if (isWater(Vector2D(pos.x + dx, pos.y + dy)))
                neigh.add(Vector2D(pos.x + dx, pos.y + dy))
    return neigh
  }


}

class Submarine() {
  var id: Int = 0
  var position: Vector2D = Vector2D()
  var life: Int = 6
  var torpedoCoolDown: Int = 0
  var sonarCoolDown: Int = 0
  var silenceCoolDown: Int = 0
  var mineCoolDown: Int = 0
  var sonarResult: String = "NA" // Can be Y, N or NA

}

class Opponent {
  var life: Int = 6
}

abstract class Order {

  companion object {

    fun parse(orders: String): List<Order> {
      val out: MutableList<Order> = mutableListOf()
      orders.split("|").forEach { out.add(parseOrder(it)) }
      return out
    }

    private fun parseOrder(order: String): Order {
      order.trim()
      if (order == "SURFACE")
        return Surface()
      if (order.contains("MOVE"))
        return Move(Direction.valueOf(order.substringAfter(" ")))
      if (order == "TORPEDO")
        return LoadTorpedo()
      return Empty()
    }
  }

  abstract fun toOrderString(): String
}

enum class Direction {
  N, S, E, W, NA

}

class Move(private val direction: Direction) : Order() {
  override fun toOrderString(): String {
    return "MOVE " + direction.name
  }
}

class Surface : Order() {
  override fun toOrderString(): String {
    return "SURFACE"
  }
}

class Torpedo(private val target: Vector2D) : Order() {
  override fun toOrderString(): String {
    return "TORPEDO " + target.getIX() + " " + target.getIY()
  }
}

class LoadTorpedo : Order() {
  override fun toOrderString(): String {
    return "TORPEDO"
  }
}

class Empty : Order() {
  override fun toOrderString(): String {
    return ""
  }

}


class SubmarineTracker(private val map: Map) {

  /** Starting candidate positions */
  private val start: Set<Vector2D> = map.getWater()

  /** Tail of all move actions */
  private val trail: MutableList<Direction> = mutableListOf()

  fun update(move: Direction) {
    trail.add(move)
  }

}

class LongestPath(private val graph: Graph<Vector2D>) {

  // Discovered node map
  private val discovered: MutableMap<Vector2D, Boolean> = hashMapOf()

  // Longest distance to reach the node
  private val longestDistance: MutableMap<Vector2D, Int> = hashMapOf()

  // Keep parent pointer
  private val parents: MutableMap<Vector2D, Vector2D> = hashMapOf()


  /** Return the longest path from input source in the graph */
  fun solve(source: Vector2D): MutableList<Vector2D> {

    // clear
    for (node in this.graph.adjacencyMap.keys) {
      longestDistance[node] = 0
      discovered[node] = false
    }

    // Compute longest pathDirection
    longestPath(source, source, 0)

    // Found target of the longest path
    var target: Vector2D = Vector2D()
    var max: Int = -1
    for (node in longestDistance.keys) {
      var distance: Int = longestDistance[node]!!
      if (distance > max) {
        max = distance
        target = node
      }
    }

    // Build path
    val path: MutableList<Vector2D> = mutableListOf()
    var node: Vector2D = target

    while (node != source) {
      path.add(0, node)
      node = parents[node]!!
    }

    return path

  }

  private fun longestPath(father: Vector2D, node: Vector2D, sum: Int) {
    if (!discovered.containsKey(node) || discovered[node] == false) {
      discovered[node] = true

      if (node != father) {
        if (!parents.containsKey(node))
          parents[node] = father
        if (longestDistance[node]!! < sum)
          longestDistance[node] = sum
        else
          parents[node] = father
      }

      for (next in graph.adjacencyMap[node]!!)
        longestPath(node, next, sum + 1)
    }
  }

}


class Graph<T>(private val bidirectional: Boolean) {
  val adjacencyMap: HashMap<T, HashSet<T>> = HashMap()

  fun addEdge(source: T, target: T) {
    // Add edge to source vertex / node.
    adjacencyMap
        .computeIfAbsent(source) { HashSet() }
        .add(target)
    if (!this.bidirectional) {
      adjacencyMap
          .computeIfAbsent(target) { HashSet() }
          .add(source)
    }
  }

  fun size(): Int {
    return adjacencyMap.size
  }

  override fun toString(): String = StringBuffer().apply {
    for (key in adjacencyMap.keys) {
      append("$key -> ")
      append(adjacencyMap[key]?.joinToString(", ", "[", "]\n"))
    }
  }.toString()
}

class Vector2D(var x: Double, var y: Double) {

  constructor(vector2D: Vector2D) : this(vector2D.x, vector2D.y)
  constructor(ix: Int, iy: Int) : this(ix.toDouble(), iy.toDouble())
  constructor() : this(0, 0)

  fun set(vector2D: Vector2D) {
    this.x = vector2D.x
    this.y = vector2D.y
  }

  fun getIX(): Int {
    return x.toInt()
  }

  fun getIY(): Int {
    return y.toInt()
  }

  fun length(): Double {
    return sqrt((x * x + y * y).toDouble())
  }

  fun direction(v: Vector2D): Direction {
    if (x != v.x && y != v.y)
      throw RuntimeException("Impossible to compute diagonal direction")
    if (x == v.x && y == v.y)
      throw RuntimeException("No direction, stay on same position")
    if (v.x > x)
      return Direction.E
    if (v.x < x)
      return Direction.W
    if (v.y > y)
      return Direction.S
    if (v.y < y)
      return Direction.N
    return Direction.NA
  }

  fun apply(direction: Direction) {
    when (direction) {
      Direction.N -> y--
      Direction.S -> y++
      Direction.W -> x--
      Direction.E -> x++
      else -> {
      }
    }
  }

  fun distance(vx: Double, vy: Double): Double {
    var dx = vx
    var dy = vy
    dx -= x
    dy -= y
    return sqrt(dx * dx + dy * dy)
  }

  fun distance(v: Vector2D): Double {
    val vx = v.x - this.x
    val vy = v.y - this.y
    return sqrt((vx * vx + vy * vy).toDouble())
  }

  fun getAngle(): Double {
    return atan2(y, x)
  }

  fun normalize() {
    val magnitude: Double = length()
    x /= magnitude
    y /= magnitude
  }

  fun getNormalized(): Vector2D {
    val magnitude: Double = length()
    return Vector2D(x / magnitude, y / magnitude)
  }

  fun toCartesian(magnitude: Double, angle: Double): Vector2D {
    return Vector2D(magnitude * cos(angle), magnitude * sin(angle))
  }

  fun add(v: Vector2D) {
    x += v.x
    y += v.y
  }

  fun add(vx: Double, vy: Double) {
    x += vx
    y += vy
  }

  fun add(v1: Vector2D, v2: Vector2D): Vector2D {
    return Vector2D(v1.x + v2.x, v1.y + v2.y)
  }

  fun getAdded(v: Vector2D): Vector2D {
    return Vector2D(x + v.x, y + v.y)
  }

  fun subtract(v: Vector2D) {
    x -= v.x
    y -= v.y
  }

  fun subtract(vx: Double, vy: Double) {
    x -= vx
    y -= vy
  }

  fun getSubtracted(v: Vector2D): Vector2D {
    return Vector2D(x - v.x, y - v.y)
  }

  fun multiply(scalar: Double) {
    x *= scalar
    y *= scalar
  }

  fun getMultiplied(scalar: Double): Vector2D {
    return Vector2D(x * scalar, y * scalar)
  }

  fun divide(scalar: Double) {
    x /= scalar
    y /= scalar
  }

  fun getDivided(scalar: Double): Vector2D {
    return Vector2D(x / scalar, y / scalar)
  }

  fun getPerp(): Vector2D {
    return Vector2D(-y, x)
  }

  fun dot(v: Vector2D): Double {
    return x * v.x + y * v.y
  }

  fun dot(vx: Double, vy: Double): Double {
    return x * vx + y * vy
  }

  fun dot(v1: Vector2D, v2: Vector2D): Double {
    return v1.x * v2.x + v1.y * v2.y
  }

  fun cross(v: Vector2D): Double {
    return x * v.y - y * v.x
  }

  fun cross(vx: Double, vy: Double): Double {
    return x * vy - y * vx
  }

  fun cross(v1: Vector2D, v2: Vector2D): Double {
    return v1.x * v2.y - v1.y * v2.x
  }

  fun project(v: Vector2D): Double {
    return this.dot(v) / this.length()
  }

  fun project(vx: Double, vy: Double): Double {
    return this.dot(vx, vy) / this.length()
  }

  fun project(v1: Vector2D, v2: Vector2D): Double {
    return dot(v1, v2) / v1.length()
  }

  fun getProjectedVector(v: Vector2D): Vector2D {
    return getNormalized().getMultiplied(this.dot(v) / this.length())
  }

  fun getProjectedVector(vx: Double, vy: Double): Vector2D {
    return getNormalized().getMultiplied(this.dot(vx, vy) / this.length())
  }

  fun getProjectedVector(v1: Vector2D, v2: Vector2D): Vector2D {
    return v1.getNormalized().getMultiplied(dot(v1, v2) / v1.length())
  }

  fun rotateBy(angle: Double) {
    val cos = cos(angle)
    val sin = sin(angle)
    val rx = x * cos - y * sin
    y = x * sin + y * cos
    x = rx
  }

  fun getRotatedBy(angle: Double): Vector2D {
    val cos = cos(angle)
    val sin = sin(angle)
    return Vector2D(x * cos - y * sin, x * sin + y * cos)
  }

  fun rotateTo(angle: Double) {
    set(toCartesian(length(), angle))
  }

  fun getRotatedTo(angle: Double): Vector2D {
    return toCartesian(length(), angle)
  }

  fun reverse() {
    x = -x
    y = -y
  }

  fun getReversed(): Vector2D {
    return Vector2D(-x, -y)
  }

  fun clone(): Vector2D {
    return Vector2D(x, y)
  }


  override fun toString(): String {
    return "(x=$x, y=$y)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Vector2D

    if (x != other.x) return false
    if (y != other.y) return false

    return true
  }

  override fun hashCode(): Int {
    var result = x.hashCode()
    result = 31 * result + y.hashCode()
    return result
  }

}
