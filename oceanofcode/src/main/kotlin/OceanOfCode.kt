@file:Suppress("unused", "MemberVisibilityCanBePrivate", "LocalVariableName")

import java.util.*
import kotlin.collections.HashSet
import kotlin.math.*
import kotlin.random.Random

fun main(args: Array<String>) {

  val input = Scanner(System.`in`)
  val width = input.nextInt()
  val height = input.nextInt()
  @Suppress("UNUSED_VARIABLE") val myId = input.nextInt()

  if (input.hasNextLine()) {
    input.nextLine()
  }

  // Create Map
  val map = Map(width, height)
  for (j in 0 until height)
    map.parse(input.nextLine(), j)

  // Initialise environment
  val env = Env(map)

  // Compute start position
  val start: Vector2D = env.start()
  println(start.getIX().toString() + " " + start.getIY().toString())

  // game loop
  while (true) {
    // Update environment
    env.turn++
    env.terrible.position = Vector2D(input.nextInt(), input.nextInt())
    env.terrible.life.add(env.turn, input.nextInt())
    env.kasakta.life.add(env.turn, input.nextInt())
    env.terrible.torpedoCoolDown = input.nextInt()
    env.terrible.sonarCoolDown = input.nextInt()
    env.terrible.silenceCoolDown = input.nextInt()
    env.terrible.mineCoolDown = input.nextInt()
    env.terrible.sonarResult = input.next()
    if (input.hasNextLine()) {
      input.nextLine()
    }
    env.initTurn()
    env.kasakta.register(env.turn, Order.parse(input.nextLine()))
    env.trackerKasakta.update(env.kasakta.orders.get(env.turn))

    // Strategy to compute next moves based on environment
    val strategy = Strategy(env)
    val orders = strategy.next()
    println(Order.toString(orders))

    // Register action
    env.terrible.register(env.turn, orders)
    env.endTurn()
  }
}

class Env(val map: Map) {
  /** Id of the turn */
  var turn = 0

  /** My submarine, Le Terrible ! */
  val terrible = Submarine()

  /** My submarine tracker */
  val trackerTerrible = Tracker(map)

  /** Opponent submarine */
  val kasakta = Submarine()

  /** Opponent submarine tracker */
  val trackerKasakta = Tracker(map)

  /** Choose the starting point */
  fun start(): Vector2D {
    val waters: Set<Vector2D> = map.getWater()
    val i = Random(12345).nextInt(0, waters.size - 1)
    val list = mutableListOf<Vector2D>()
    list.addAll(waters)
    return list[i]
  }

  /** Initialize turn after input updates */
  fun initTurn() {
    trackerKasakta.updateTorpedoReach(turn, terrible, kasakta)
    trackerTerrible.updateTorpedoReach(turn, kasakta, terrible)
  }

  fun endTurn() {
    // Update trackers
    trackerTerrible.update(terrible.orders.get(turn))
    trackerKasakta.testPrintMap(true)
  }

  /** Create a graph of next movable positions regarding environment */
  fun moveGraph(start: Vector2D = terrible.position): Graph<Vector2D> {
    val graph: Graph<Vector2D> = Graph()
    val visited: MutableSet<Vector2D> = terrible.trail.toMutableSet()
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


  /** List position in around target */
  fun torpedoRange(target: Vector2D): Set<Vector2D> {
    val range = mutableSetOf<Vector2D>()
    val open = PriorityQueue<Vector2D>(kotlin.Comparator
    { t1, t2 -> (t1.distance(target) - t2.distance(target)).toInt() })
    val dist = mutableMapOf<Vector2D, Int>()
    dist[target] = 0
    open.add(target)
    while (open.isNotEmpty()) {
      val current = open.poll()

      // Is in the range
      if (dist[current]!! <= 4) {
        range.add(current)
        for (neighbour in neigh(current)) {

          // Update distance
          if (dist[neighbour] != null)
            dist[neighbour] = min(dist[neighbour]!!, dist[current]!! + 1)
          else
            dist[neighbour] = dist[current]!! + 1

          if (!range.contains(neighbour) && !open.contains(neighbour))
            open.add(neighbour)
        }
      }
    }
    return range
  }

  /** A* to compute the path between from vector & to vector in the map with possible forbidden list */
  fun path(from: Vector2D, to: Vector2D, forbidden: Set<Vector2D> = setOf()): List<Vector2D>? {
    val parents = mutableMapOf<Vector2D, Vector2D>()

    // quick stop
    if (forbidden.contains(to) || from == to)
      return null

    // Ordered queue
    val open = PriorityQueue<Vector2D>(kotlin.Comparator
    { t1, t2 -> (t1.distance(to) - t2.distance(to)).toInt() })
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

  private fun buildPath(parents: kotlin.collections.Map<Vector2D, Vector2D>,
                        to: Vector2D, from: Vector2D): List<Vector2D> {
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

  /** Current position of the submarine, if known */
  var position = Vector2D()

  /** History of life */
  val life = LifeHistory()

  /** Map of orders of the submarine, key turn id and value list of orders */
  val orders = OrderHistory()

  /** Trail of my submarine */
  val trail = mutableSetOf<Vector2D>()

  /** Active mines in the map */
  val mines = mutableSetOf<Vector2D>();

  /** Cooldowns */
  var torpedoCoolDown = -1
  var sonarCoolDown = -1
  var silenceCoolDown = -1
  var mineCoolDown = -1
  var sonarResult = "NA" // Can be Y, N or NA

  /** Available neigh for next move*/
  fun neigh(map: Map): List<Vector2D> {
    val neigh = mutableListOf<Vector2D>()
    for (n in map.neigh(position))
      if (!trail.contains(n))
        neigh.add(n)
    return neigh
  }

  /** Register an order for a specific turn */
  fun register(turn: Int, order: Order) {
    // add to trail move position
    if (order is Move)
      trail.add(position)
    if (order is Surface || order is SurfaceSector)
      trail.clear()
    orders.add(turn, order)
  }

  /** Register orders for a specific turn */
  fun register(turn: Int, orders: List<Order>) {
    orders.forEach { register(turn, it) }
  }

  fun isTorpedoReady(): Boolean {
    return this.torpedoCoolDown == 0
  }

  fun isSilenceReady(): Boolean {
    return this.silenceCoolDown == 0
  }

  fun isMineReady(): Boolean {
    return this.mineCoolDown == 0
  }

}

class Tracker(val map: Map) {

  /** Starting candidate positions */
  val candidates = mutableSetOf<Vector2D>()

  /** Outdated during the current turn */
  val outdated = mutableSetOf<Vector2D>()

  /** Tail of all directions */
  val trail = mutableListOf<Direction>()

  init {
    candidates.addAll(map.getWater())
  }

  fun update(order: Order) {
    update(listOf(order))
  }

  fun update(orders: List<Order>) {
    outdated.clear()
    for (order in orders) {
      if (order is Move)
        updateMove(order)
      if (order is SurfaceSector)
        updateSurfaceSector(order)
      if (order is Surface)
        updateSurface()
      if (order is Torpedo)
        updateTorpedoLaunch(order)
      if (order is Silence)
        updateSilence()
    }
    // Remove outdated
    candidates.removeAll(outdated)
  }

  fun outdate(candidate: Vector2D?) {
    if (candidate != null) {
      candidates.remove(candidate)
      outdated.add(candidate)
    }
  }

  fun outdateAllExcept(subset: List<Vector2D>) {
    for (candidate in candidates) {
      if (!subset.contains(candidate))
        outdated.add(candidate)
    }
    candidates.removeAll(outdated)
  }

  /** Map of target => candidate */
  fun targetMap(): kotlin.collections.Map<Vector2D, Vector2D> {
    val targetMap = mutableMapOf<Vector2D, Vector2D>()
    for (candidate in candidates) {
      val pos = Vector2D(candidate)
      for (direction in trail)
        pos.apply(direction)
      targetMap[pos] = candidate
    }
    return targetMap
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

  private fun updateSurface() {
    val targets = targets()
    candidates.clear()
    candidates.addAll(targets)
    trail.clear()
  }

  private fun updateSurfaceSector(order: SurfaceSector) {
    updateSurface()
    val sections = map.getWaterSection(order.sector)
    for (candidate in candidates)
      if (!sections.contains(candidate))
        outdated.add(candidate)
  }

  fun updateTorpedoLaunch(order: Torpedo) {
    val region = map.torpedoRange(order.target)
    val targetMap = targetMap()
    for ((target, candidate) in targetMap)
      if (!region.contains(target))
        outdated.add(candidate)
  }

  fun updateSilence() {
    // Remove trail, keep targets and in 4 directions around
    val newCandidates = mutableSetOf<Vector2D>()

    for (candidate in candidates) {
      val snakeIt = Vector2D(candidate)
      val snake = mutableListOf<Vector2D>(Vector2D(snakeIt))
      for (direction in trail) {
        snakeIt.apply(direction)
        snake.add(0, Vector2D(snakeIt))
      }
      val target = snake[0]

      newCandidates.add(target) // add current target
      for (delta in 1..4)
        if (!addSilenceCandidate(target.getAdded(Vector2D(delta, 0)), snake, newCandidates))
          break
      for (delta in 1..4)
        if (!addSilenceCandidate(target.getAdded(Vector2D(-delta, 0)), snake, newCandidates))
          break
      for (delta in 1..4)
        if (!addSilenceCandidate(target.getAdded(Vector2D(0, delta)), snake, newCandidates))
          break
      for (delta in 1..4)
        if (!addSilenceCandidate(target.getAdded(Vector2D(0, -delta)), snake, newCandidates))
          break

    }
    candidates.clear()
    trail.clear()
    candidates.addAll(newCandidates)
  }

  private fun addSilenceCandidate(candidate: Vector2D, snake: List<Vector2D>, newCandidates: MutableSet<Vector2D>): Boolean {
    if (map.isWater(candidate))
      if (!snake.contains(candidate)) {
        newCandidates.add(candidate)
        return true
      }
    return false
  }

  /** Compute the torpedo impact on tracker */
  fun updateTorpedoReach(turn: Int, from: Submarine, to: Submarine) {
    // Check torpedo impact in previous turn
    var torpedo: Torpedo? = null
    from.orders.get(turn - 1).forEach { if (it is Torpedo) torpedo = it }

    if (torpedo != null) {
      val target: Vector2D = torpedo!!.target
      // Look if enemy has surfaced
      var surfaced = false
      to.orders.get(turn - 1).forEach { if (it is SurfaceSector || it is SurfaceSector) surfaced = true }

      // Compute delta of life
      var deltaLife = to.life.get(turn - 1) - to.life.get(turn)
      if (surfaced)
        deltaLife -= 1

      // Register impact of tracker
      val candidate = targetMap()
      when (deltaLife) {
        0 -> {
          // A L'EAU, remove all candidates in the zone
          outdate(candidate[target])
          map.neighDiagonal(target).forEach { outdate(candidate[it]) }
        }
        1 -> {
          // TOUCHE, keep only candidates in the area
          outdate(candidate[target])
          val candidates = mutableListOf<Vector2D>()
          map.neighDiagonal(target).forEach {
            if (candidate[it] != null)
              candidates.add(candidate.getValue(it))
          }
          outdateAllExcept(candidates)
        }
        2 -> {
          // TOUCHE COULE, NICE ! keep only target candidate !
          outdateAllExcept(listOf(candidate.getValue(target)))
        }
      }
    }

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

class Strategy(val env: Env) {

  fun next(): List<Order> {
    val orders = mutableListOf<Order>()

    // Fire
    val fire = FireStrategy(env.terrible, env.trackerKasakta).next()
    if (fire !is Empty)
      orders.add(fire)

    // Surface action
    val surface = TrapStrategy(env.terrible, env.map).next()
    if (surface !is Empty)
      orders.add(surface)
    else {
      // Compute move action
      var move: Order
      move = AggressiveNaiveApproach().next(env.terrible, env.trackerKasakta)
      if (move is Empty)
        move = SilentStrategy(env.trackerTerrible).next(env.terrible)
      if (move is Empty)
        move = SurfaceStrategy().next()

      // Load weapon on move action
      if (move is Move)
        LoadStrategy(env.terrible).load(move)

      if (move !is Empty)
        orders.add(move)
    }

    // Defense strategy
    val defense = DefenseStrategy(env.terrible).next()
    if (defense !is Empty)
      orders.add(defense)

    return orders
  }

}

class LoadStrategy(val submarine: Submarine) {

  fun load(order: Move) {
    if (!submarine.isTorpedoReady())
      order.weapon = Weapon.TORPEDO
    else if (!submarine.isSilenceReady())
      order.weapon = Weapon.SILENCE
    else if (!submarine.isMineReady())
      order.weapon = Weapon.MINE
  }

}

class DefenseStrategy(val submarine: Submarine) {

  fun next(): Order {
    var order: Order = Empty()
    val turn = submarine.life.size()
    if (turn >= 2) {
      var deltaLife = submarine.life.get(turn - 1) - submarine.life.get(turn)

      // Check if submarine has surfaced
      var surface = false
      submarine.orders.get(turn - 1).forEach { if (it is Surface || it is SurfaceSector) surface = true }
      if (surface)
        deltaLife--

      // Run silence operation
      if (deltaLife > 0 && submarine.isSilenceReady())
        order = silence()

    }
    return order
  }

  private fun silence(): Silence {
    // Fake move
    return Silence(Direction.N, 0)
  }

}

class FireStrategy(val submarine: Submarine, val tracker: Tracker) {

  companion object {
    const val MINIMUM_TARGET_FOR_FIRE_STRATEGY = 15
  }

  /** Compute the most aggressive next move*/
  fun next(): Order {
    var order: Order = Empty()
    if (tracker.candidates.size < MINIMUM_TARGET_FOR_FIRE_STRATEGY)
      order = fire()
    return order
  }

  /** Try to fire a torpedo and best target */
  private fun fire(): Order {
    var order: Order = Empty()
    if (!submarine.isTorpedoReady())
      return order

    // Look for best target : showing the most information ...
    var best = 0.2 // min evaluation to fire
    var bestTarget: Vector2D? = null
    val targets = tracker.targets()
    val nbTotalTargets = targets.size
    for (target in targets) {

      // Count percentage of targets will be touch
      var nbTarget = 1
      tracker.map.neighDiagonal(target).forEach { if (targets.contains(it)) nbTarget++ }
      val targetPercentage = nbTarget.toDouble() / nbTotalTargets

      // Evaluate distance
      val quickDistance = submarine.position.distance(target)
      if (quickDistance <= Submarine.TORPEDO_RANGE + 1) {
        // Target may be reachable
        val realPath = tracker.map.path(submarine.position, target)
        if (realPath != null) {
          val realDistance = realPath.size
          if (realDistance <= Submarine.TORPEDO_RANGE + 1) {
            // Target is reachable, launch evaluation
            var evaluation = targetPercentage
            if (realDistance == 0)
              continue // will not fire on me please
            if (quickDistance < 1.5)
              evaluation -= 0.5 // accept to fire next to me but with penalty

            if (evaluation > best) {
              best = evaluation
              bestTarget = target
            }
          }
        }
      }
    }

    // Target found
    if (bestTarget != null) {
      System.err.println("Attack strategy Fire on $bestTarget")
      order = Torpedo(bestTarget)
    }

    return order
  }

}

class AggressiveNaiveApproach() {

  companion object {
    const val MINIMUM_TARGET_FOR_AGGRESSIVE_STRATEGY = 25
  }

  /** Try an approach on the best target */
  fun next(submarine: Submarine, opponent: Tracker): Order {
    val targets = opponent.targets()
    if (targets.size < MINIMUM_TARGET_FOR_AGGRESSIVE_STRATEGY) {
      var best = Int.MAX_VALUE
      var bestDirection = Direction.NA
      for (target in targets) {
        val quickDist = submarine.position.distance(target)
        if (quickDist < best) {
          val path = opponent.map.path(submarine.position, target, submarine.trail)
          if (path != null && path.size > 1) {
            val realDist = path.size
            if (realDist < best) {
              best = realDist
              bestDirection = submarine.position.direction(path[1])
            }
          }
        }
      }
      if (bestDirection != Direction.NA) {
        System.err.println("Aggressive MOVE strategy, move to $bestDirection at distance $best")
        return Move(bestDirection)
      }
    }
    return Empty()
  }
}

class SilentStrategy(val tracker: Tracker) {

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

class SurfaceStrategy {
  fun next(): Order {
    return Surface()
  }
}

class TrapStrategy(val submarine: Submarine, val map: Map) {
  fun next(): Order {
    return if (submarine.neigh(map).isEmpty())
      Surface()
    else
      Empty()
  }
}

class MineStrategy(val submarine: Submarine, val tracker: Tracker) {

  fun next(): Order {
    var order: Order = Empty()
    if (submarine.isMineReady()) {
      var best = 0;
      var target: Vector2D? = null
      for (direction in Direction.values()) {
        if (direction != Direction.NA) {
          val targetIt = submarine.position.getApplied(direction)
          if (tracker.map.isWater(targetIt)) {
            if (!submarine.mines.contains(targetIt)) {
              val exposition = tracker.map.neighDiagonal(targetIt).size
              if (exposition > best) {
                best = exposition
                target = targetIt
              }
            }
          }
        }
      }
      if (target != null) {
        order = Mine(submarine.position.direction(target))
      }
    }
    return order;
  }

}

abstract class Order {

  companion object {

    fun toString(orders: List<Order>): String {
      var s = ""
      for (order in orders)
        s += order.toOrderString() + " | "
      return s.removeSuffix(" | ")
    }

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
        val params = order.split(" ")
        val direction = Direction.valueOf(params[1])
        var weapon = Weapon.NA
        if (params.size > 2)
          weapon = Weapon.valueOf(params[2])
        return Move(direction, weapon)
      }
      if (order.contains("TORPEDO")) {
        val params = order.substringAfter(" ")
        val x = params.substringBefore(" ").toInt()
        val y = params.substringAfter(" ").toInt()
        return Torpedo(Vector2D(x, y))
      }
      if (order == "SILENCE")
        return Silence()
      if (order.contains("SILENCE")) {
        val params = order.substringAfter(" ")
        val direction = Direction.valueOf(params.substringBefore(" "))
        val distance = params.substringAfter(" ").toInt()
        return Silence(direction, distance)
      }
      if (order.contains("SONAR")) {
        return Sonar(order.substringAfter(" ").toInt())
      }
      if (order.contains("MINE")) {
        val params = order.split(" ")
        var direction: Direction? = null
        if (params.size > 1)
          direction = Direction.valueOf(params[1])
        return Mine(direction)
      }
      if (order.contains("TRIGGER")) {
        val params = order.split(" ")
        val target = Vector2D(params[1].toInt(), params[2].toInt())
        return Trigger(target)
      }
      return Empty()
    }
  }

  abstract fun toOrderString(): String
}

enum class Direction {
  N, S, E, W, NA
}

enum class Weapon {
  TORPEDO, SONAR, SILENCE, MINE, NA
}

class Move(val direction: Direction, var weapon: Weapon = Weapon.NA) : Order() {
  override fun toOrderString(): String {
    return if (weapon != Weapon.NA)
      "MOVE " + direction.name + " " + weapon.name
    else
      "MOVE " + direction.name
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

class Torpedo(val target: Vector2D) : Order() {
  override fun toOrderString(): String {
    return "TORPEDO " + target.getIX() + " " + target.getIY()
  }
}

class Silence(val direction: Direction, val distance: Int) : Order() {

  constructor() : this(Direction.NA, 0)

  override fun toOrderString(): String {
    return if (direction == Direction.NA)
      "SILENCE"
    else
      "SILENCE $direction $distance"
  }
}

class Sonar(val sector: Int) : Order() {
  override fun toOrderString(): String {
    return "SONAR $sector"
  }
}

class Mine(val direction: Direction? = null) : Order() {
  override fun toOrderString(): String {
    return if (direction == null) "MINE" else "MINE " + direction.name
  }
}

class Trigger(val target: Vector2D) : Order() {
  override fun toOrderString(): String {
    return "TRIGGER " + target.getIX() + " " + target.getIY()
  }
}

class Empty : Order() {
  override fun toOrderString(): String {
    return ""
  }

}

class LifeHistory {

  private val history = mutableMapOf<Int, Int>()

  fun add(turn: Int, life: Int) {
    history[turn] = life
  }

  fun get(turn: Int): Int {
    return if (turn <= 0)
      6
    else
      history[turn]!!
  }

  fun size(): Int {
    return history.size
  }
}

class OrderHistory {

  /** Map of orders of the submarine, key turn id and value list of orders */
  val orders = mutableMapOf<Int, MutableList<Order>>()

  /** Register a new order */
  fun add(turn: Int, order: Order) {
    // register order
    if (orders.containsKey(turn) && orders[turn] != null)
      orders[turn]!!.add(order)
    else {
      orders[turn] = mutableListOf()
      orders[turn]!!.add(order)
    }
  }

  fun add(turn: Int, orders: List<Order>) {
    orders.forEach { add(turn, it) }
  }

  fun get(turn: Int): List<Order> {
    if (orders.containsKey(turn))
      return orders[turn]!!
    return emptyList()
  }

}

class Graph<T>(private val bidirectional: Boolean = false) {
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

  fun getApplied(direction: Direction): Vector2D {
    val new = Vector2D(this)
    new.apply(direction)
    return new
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
