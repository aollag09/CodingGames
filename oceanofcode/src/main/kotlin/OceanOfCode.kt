@file:Suppress("unused", "MemberVisibilityCanBePrivate", "LocalVariableName")

import java.util.*
import kotlin.collections.HashSet
import kotlin.math.*

fun main(args: Array<String>) {

  val input = Scanner(System.`in`)
  val width = input.nextInt()
  val height = input.nextInt()
  val myId = input.nextInt()

  if (input.hasNextLine()) {
    input.nextLine()
  }

  // Create main.kotlin.Map
  val map = Map(width, height)
  for (j in 0 until height)
    map.parse(input.nextLine(), j)

  // Initialise environment
  val env = Env(map)
  env.submarine.id = myId

  val start: Vector2D = env.start()
  println(start.getIX().toString() + " " + start.getIY().toString())


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
    env.opponent.orders = Order.parse(input.nextLine())
    env.opponent.orders.forEach { env.opTracker.update(it) }
    env.opTracker.testPrintMap(true)

    // Compute next action
    val order =
        if (env.opTracker.candidates.size < AggressiveStrategy.MINIMUM_TARGET_FOR_AGGRESSIVE_STRATEGY) {
          AggressiveStrategy(env.opTracker).next(env.submarine)
        } else {
          SilentStrategy(env.myTracker).next(env.submarine)
        }
    if (order is Move)
      println(order.toOrderString() + " TORPEDO")
    else
      println(order.toOrderString())

    // Register action
    env.register(order)
  }
}

class Env(val map: Map) {
  /** My submarine */
  val submarine = Submarine()

  /** My submarine tracker */
  val myTracker = SubmarineTracker(map)

  /** Opponent submarine */
  val opponent = Opponent()

  /** Opponent submarine tracker */
  val opTracker = SubmarineTracker(map)

  /** Create a graph of next movable positions regarding environment */
  fun moveGraph(start: Vector2D = submarine.position): Graph<Vector2D> {
    val graph: Graph<Vector2D> = Graph(false)
    val visited: MutableSet<Vector2D> = submarine.trail.toMutableSet()
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
    var min = 5
    var start = Vector2D()
    for (water in waters) {
      val neigh = map.neigh(water).size
      if (neigh < min) {
        min = neigh
        start = water
      }
    }
    return start
  }

  fun register(order: Order) {
    myTracker.update(order)
    submarine.orders.add(order)
    if (order is Move)
      submarine.trail.add(submarine.position)
  }
}

class Map(width: Int, height: Int) {
  val size: Vector2D = Vector2D(width, height)
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

    var x = 0
    var y = 0
    when {
      (section == 2 || section == 5 || section == 8) -> x = 5
      (section == 3 || section == 6 || section == 9) -> x = 10
    }
    when {
      (section == 4 || section == 5 || section == 6) -> y = 5
      (section == 7 || section == 8 || section == 9) -> y = 10
    }

    for (i in x until x + 5) {
      for (j in y until y + 5) {
        val pos = Vector2D(i, j)
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

  fun neighDiagonal(pos: Vector2D): Set<Vector2D> {
    val neigh: MutableSet<Vector2D> = mutableSetOf()
    for (dx in -1..1 step 1)
      for (dy in -1..1 step 1)
        if (dx != 0 || dy != 0)
          if (pos.x + dx >= 0 && pos.x + dx < this.size.x)
            if (pos.y + dy >= 0 && pos.y + dy < this.size.y)
              if (isWater(Vector2D(pos.x + dx, pos.y + dy)))
                neigh.add(Vector2D(pos.x + dx, pos.y + dy))
    return neigh
  }

  /** A* to compute the path between from vector & to vector in the map with possible forbidden list */
  fun path(from: Vector2D, to: Vector2D, forbidden: Set<Vector2D> = setOf()): List<Vector2D>? {
    val parents = mutableMapOf<Vector2D, Vector2D>()

    // Ordered queue
    val open = PriorityQueue<Vector2D>(kotlin.Comparator { t1, t2 -> (t1.distance(to) - t2.distance(to)).toInt() })
    open.add(from)

    // init g & f score to infinity
    val g = mutableMapOf<Vector2D, Int>()
    for (v in getWater())
      g[v] = Int.MAX_VALUE
    g[from] = 0

    while (open.isNotEmpty()) {
      val current = open.poll()

      // Arrived to target
      if (current == to)
        return buildPath(parents, to, from)

      // Loop on neighbors
      for (neighbor in neigh(current)) {
        if (!forbidden.contains(current)) {

          // Compute current score
          val score = g[current]!!.plus(1)
          if (score < g[neighbor]!!) {

            // New best path
            parents[neighbor] = current
            g[neighbor] = score
            if (!open.contains(neighbor))
              open.add(neighbor)
          }
        }
      }

    }

    // no path has been found :(
    return null
  }

  private fun buildPath(parents: kotlin.collections.Map<Vector2D, Vector2D>, to: Vector2D, from: Vector2D): List<Vector2D> {
    val path = mutableListOf<Vector2D>()
    var current = to
    while (current != from) {
      path.add(0, current)
      current = parents[current] ?: error("Current vector doesn't have parent $current")
    }
    path.add(0, from)
    return path
  }


}

class Submarine {

  companion object {
    const val TORPEDO_RANGE = 4
    private const val TORPEDO_MAX_COOL_DOWN = 3
  }

  var id: Int = 0
  var position: Vector2D = Vector2D()
  var life: Int = 6
  var torpedoCoolDown: Int = 0
  var sonarCoolDown: Int = 0
  var silenceCoolDown: Int = 0
  var mineCoolDown: Int = 0
  var sonarResult: String = "NA" // Can be Y, N or NA

  /** List of orders of the submarine */
  val orders = mutableListOf<Order>()

  /** Trail of my submarine */
  val trail: MutableSet<Vector2D> = mutableSetOf()

  /** Available neigh for next move*/
  fun neigh(map: Map): List<Vector2D> {
    val neigh = mutableListOf<Vector2D>()
    for (n in map.neigh(position))
      if (!trail.contains(n))
        neigh.add(n)
    return neigh
  }

  fun isTorpedoReady(): Boolean {
    return this.torpedoCoolDown >= TORPEDO_MAX_COOL_DOWN
  }
}

class Opponent {
  var life: Int = 6
  var orders: List<Order> = listOf()
}

abstract class Order {

  companion object {

    fun parse(orders: String): List<Order> {
      val out: MutableList<Order> = mutableListOf()
      orders.split("|").forEach { out.add(parseOrder(it)) }
      return out
    }

    fun parseOrder(order: String): Order {
      order.trim()
      if (order == "SURFACE")
        return Surface()
      if (order.contains("SURFACE")) {
        val sector = order.substringAfterLast(" ")
        return SurfaceSector(sector.toInt())
      }
      if (order.contains("MOVE")) {
        val direction = order.substringAfterLast(" ")
        return Move(Direction.valueOf(direction))
      }
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

class Move(val direction: Direction) : Order() {
  override fun toOrderString(): String {
    return "MOVE " + direction.name
  }
}

class Surface : Order() {
  override fun toOrderString(): String {
    return "SURFACE"
  }
}

class SurfaceSector(val sector: Int) : Order() {
  override fun toOrderString(): String {
    return "SURFACE $sector"
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

class SubmarineTracker(val map: Map) {

  /** Starting candidate positions */
  val candidates = mutableSetOf<Vector2D>()

  /** Outdated during the current turn */
  val outdated = mutableSetOf<Vector2D>()

  /** Tail of all move actions */
  private val trail = mutableListOf<Direction>()

  init {
    candidates.addAll(map.getWater())
  }

  fun update(order: Order) {
    outdated.clear()
    if (order is Move)
      updateMove(order)
    if (order is SurfaceSector)
      updateSurface(order)
    // Remove outdated
    candidates.removeAll(outdated)
  }

  fun targets(): List<Vector2D> {
    val targets = mutableListOf<Vector2D>()
    for (candidate in candidates) {
      val pos = Vector2D(candidate)
      for (direction in trail)
        pos.apply(direction)
      targets.add(pos)
    }
    return targets
  }

  /** Evaluate next move to known how many outdated position will be created */
  fun evaluate(direction: Direction): Int {
    var evaluation = 0
    for (target in targets()) {
      val fake = target.clone()
      fake.apply(direction)
      if (!map.isWater(fake))
        evaluation++
    }
    return evaluation
  }

  private fun updateMove(order: Move) {
    trail.add(order.direction)
    for (candidate in candidates) {
      // Check of current candidate is still a valid option
      val snake = Vector2D(candidate)
      for (direction in trail) {
        snake.apply(direction)
        if (!map.isWater(snake)) {
          outdated.add(candidate)
          break
        }
      }
    }
  }

  private fun updateSurface(order: SurfaceSector) {
    trail.clear()
    val sections = map.getWaterSection(order.sector)
    for (section in sections) {
      if (!candidates.contains(section))
        candidates.add(section)
    }
    for (candidate in candidates)
      if (!sections.contains(candidate))
        outdated.add(candidate)
  }

  fun testPrintMap(prod: Boolean) {
    if (prod)
      System.err.println("Candidates : " + candidates.size + ", Outdated : " + outdated.size)
    else
      println("Candidates : " + candidates.size + ", Outdated : " + outdated.size)

    val targets = targets()
    for (y in 0 until map.size.getIY()) {
      var line = ""
      for (x in 0 until map.size.getIX()) {
        line += when {
          map.isIsland(Vector2D(x, y)) -> "x"
          targets.contains(Vector2D(x, y)) -> "o"
          else -> "."
        }
        line += "  "
      }
      if (prod)
        System.err.println(line)
      else
        println(line)
    }
    if (prod)
      System.err.println("")
    else
      println("")
  }
}

class AggressiveStrategy(val opponent: SubmarineTracker) {

  companion object {
    const val MINIMUM_TARGET_FOR_AGGRESSIVE_STRATEGY = 40
  }

  /** Compute the most aggressive next move*/
  fun next(submarine: Submarine): Order {
    // Compute distances of all targets
    val distances = mutableMapOf<Vector2D, Int>()
    for (target in opponent.targets()) {
      val path = opponent.map.path(submarine.position, target, submarine.trail)
      var distance = 0
      if (path != null)
        distance = path.size
      distances[target] = distance
    }
    return attack(submarine, distances, opponent)
  }

  private fun attack(submarine: Submarine, distances: MutableMap<Vector2D, Int>, opponent: SubmarineTracker): Order {
    var order: Order = Empty()

    // Look for best target : showing the most information ...
    var best = Double.MIN_VALUE
    var bestTarget = Vector2D()
    for ((target, distance) in distances) {
      if (target == submarine.position)
        continue;

      // Count targets
      var nbTarget = 1;
      opponent.map.neighDiagonal(target).forEach { if ((distances.keys).contains(it)) nbTarget++ }

      val FACTOR_NB_TARGET = 3
      val FACTOR_DISTANCE = -1.0
      val evaluation = FACTOR_NB_TARGET * nbTarget + FACTOR_DISTANCE * distance

      if (evaluation > best) {
        best = evaluation
        bestTarget = target
      }
    }

    // Fire ?
    if (submarine.isTorpedoReady()) {
      if (best > Double.MIN_VALUE && distances[bestTarget]!! < Submarine.TORPEDO_RANGE) {
        System.err.println("Attack strategy Fire on $bestTarget")
        order = Torpedo(bestTarget)
      }
    }

    // Approach
    if (order is Empty) {
      val path = opponent.map.path(submarine.position, bestTarget)
      if (path != null && path.size > 1) {
        val direction = submarine.position.direction(path[1])
        System.err.println("Attack strategy Move on $bestTarget with direction :$direction")
        order = Move(direction)
      }
    }

    return order
  }

}

class SilentStrategy(val tracker: SubmarineTracker) {

  /** Compute the most silent next move */
  fun next(submarine: Submarine): Order {
    var best = Vector2D()
    var silence = Int.MAX_VALUE
    for (neigh in submarine.neigh(tracker.map)) {
      val evaluation = tracker.evaluate(submarine.position.direction(neigh))
      if (evaluation < silence) {
        silence = evaluation
        best = neigh
      }
    }
    System.err.println("Silent strategy, move to $best")
    return Move(submarine.position.direction(best))
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
    var target = Vector2D()
    var max: Int = -1
    for (node in longestDistance.keys) {
      val distance: Int = longestDistance[node]!!
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
    if (discovered[node] == false) {
      discovered[node] = true

      if (node != father) {
        if (!parents.containsKey(node))
          parents[node] = father
        if (longestDistance[node]!! < sum)
          longestDistance[node] = sum
        else
          parents[node] = father
      }

      if (graph.adjacencyMap.containsKey(node)) {
        for (next in graph.adjacencyMap[node]!!)
          longestPath(node, next, sum + 1)
      }
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
    return sqrt((x * x + y * y))
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
    return sqrt((vx * vx + vy * vy))
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
