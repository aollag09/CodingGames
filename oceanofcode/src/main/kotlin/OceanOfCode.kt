@file:Suppress("MemberVisibilityCanBePrivate", "LocalVariableName")

import java.util.*
import kotlin.collections.HashSet
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
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
    println(Order.toString(env.terrible.orders.get(env.turn)))

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
    trackerKasakta.updateFireReach(turn, terrible, kasakta)
    trackerTerrible.updateFireReach(turn, kasakta, terrible)
  }

  fun endTurn() {
    // Update trackers
    trackerTerrible.update(terrible.orders.get(turn))
    trackerKasakta.testPrintMap(true)
  }

}

class Map(width: Int, height: Int) {

  /** Size of the map */
  val size: Vector2D = Vector2D(width, height)

  /** 2D boolean map for islands */
  private val islands = Array(size.getIX()) { _ -> Array(size.getIY()) { _ -> false } }

  fun isIsland(pos: Vector2D): Boolean {
    return islands[pos.getIX()][pos.getIY()]
  }

  fun isWater(pos: Vector2D): Boolean {
    return if (pos.x >= 0 && pos.x < size.getIX() && pos.y >= 0 && pos.y < size.getIY())
      !isIsland(pos)
    else
      false
  }

  fun parse(line: String, j: Int) {
    for (i in line.indices)
      if (line.toCharArray()[i] != '.')
        this.islands[i][j] = true
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
    val neigh = mutableSetOf<Vector2D>()
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
  val mines = mutableSetOf<Vector2D>()

  /** Cooldowns */
  var torpedoCoolDown = -1
  var sonarCoolDown = -1
  var silenceCoolDown = -1
  var mineCoolDown = -1
  var sonarResult = "NA" // Can be Y, N or NA

  /** Available neigh for next move*/
  fun neigh(map: Map): List<Vector2D> {
    return neigh(position, map)
  }

  /** Available neigh for next move from a starting position */
  fun neigh(start: Vector2D, map: Map): List<Vector2D> {
    val neigh = mutableListOf<Vector2D>()
    for (n in map.neigh(start))
      if (!trail.contains(n))
        neigh.add(n)
    return neigh
  }

  /** Register an order for a specific turn */
  fun register(turn: Int, order: Order) {
    if (order !is Empty) {
      // add to trail move position
      if (order is Move) {
        trail.add(Vector2D(position))
        position.apply(order.direction)
      }
      if (order is Surface || order is SurfaceSector)
        trail.clear()
      if (order is Mine) {
        val mine = position.getApplied(order.direction)
        mines.add(mine)
      }
      if( order is Trigger ){
        val mine = order.target
        mines.remove(mine)
      }
      orders.add(turn, order)
    }
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

  /** Return true if direction lead to less than limit number of moves */
  fun isTrapDirection(map: Map, direction: Direction, limit: Int = 17): Boolean {
    val start = position.getApplied(direction)
    val moves = mutableSetOf(start, position)
    val open = LinkedList<Vector2D>()
    open.push(start)
    while (moves.size < limit && open.isNotEmpty()) {
      val current = open.poll()
      for (neigh in neigh(current, map))
        if (!moves.contains(neigh)) {
          open.add(neigh)
          moves.add(neigh)
        }
    }
    return moves.size < limit
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
  fun evaluate(direction: Direction, targets: List<Vector2D> = targets()): Int {
    var evaluation = 0
    for (target in targets)
      if (!map.isWater(target.getApplied(direction)))
        evaluation++
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

  fun updateFireReach(turn: Int, from: Submarine, to: Submarine) {
    // Check torpedo impact in previous turn
    var torpedo: Torpedo? = null
    from.orders.get(turn - 1).forEach { if (it is Torpedo) torpedo = it }

    // Check trigger impact in previous turn
    var trigger: Trigger? = null
    from.orders.get(turn - 1).forEach { if (it is Trigger) trigger = it }

    // Compute delta live and target
    val deltaLife = deltaLife(to, turn)
    val targetTorpedo: Vector2D? = if (torpedo != null) torpedo!!.target else null
    val targetTrigger: Vector2D? = if (trigger != null) trigger!!.target else null

    if (targetTorpedo != null || targetTrigger != null) {
      // Register impact of tracker
      val candidate = targetMap()
      when (deltaLife) {
        0 -> {
          // A L'EAU, remove all candidates in the zone
          if (targetTrigger != null) {
            outdate(candidate[targetTrigger])
            map.neighDiagonal(targetTrigger).forEach { outdate(candidate[it]) }
          }
          if (targetTorpedo != null) {
            outdate(candidate[targetTorpedo])
            map.neighDiagonal(targetTorpedo).forEach { outdate(candidate[it]) }
          }
        }
        1, 3 -> {
          // TOUCHE, keep only candidates in the area
          val currentCandidates = mutableListOf<Vector2D>()
          if (targetTorpedo != null) {
            outdate(candidate[targetTorpedo])
            map.neighDiagonal(targetTorpedo).forEach {
              if (candidate[it] != null)
                currentCandidates.add(candidate.getValue(it))
            }
          }
          if (targetTrigger != null) {
            outdate(candidate[targetTrigger])
            map.neighDiagonal(targetTrigger).forEach {
              if (candidate[it] != null)
                currentCandidates.add(candidate.getValue(it))
            }
          }
          outdateAllExcept(currentCandidates)
        }
        2, 4 -> {
          // TOUCHE COULE, NICE ! keep only target candidate !
          if (targetTorpedo != null)
            outdateAllExcept(listOf(candidate.getValue(targetTorpedo)))
          if (targetTrigger != null)
            outdateAllExcept(listOf(candidate.getValue(targetTrigger)))
        }
      }
    }
  }

  private fun deltaLife(to: Submarine, turn: Int): Int {
    // Look if enemy has surfaced
    var surfaced = false
    to.orders.get(turn - 1).forEach { if (it is SurfaceSector || it is SurfaceSector) surfaced = true }

    // Compute delta of life
    var deltaLife = to.life.get(turn - 1) - to.life.get(turn)
    if (surfaced)
      deltaLife -= 1
    return deltaLife
  }

  fun testPrintMap(prod: Boolean) {
    if (prod)
      System.err.println("Candidates : " + candidates.size + ", Outdated : " + outdated.size)
    else
      println("Candidates : " + candidates.size + ", Outdated : " + outdated.size)

    val targets = targets()
    if ((prod && candidates.size < 100) || !prod) {
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
}

abstract class AbstractStrategy {

  abstract fun apply(): Order

  abstract fun name(): String

  fun applyTimer(): Order {
    val start = System.currentTimeMillis()
    val order: Order = apply()
    val time = System.currentTimeMillis() - start
    System.err.println("Strategy " + name() + " in " + time + " ms")
    return order
  }

}

class Strategy(val env: Env) {

  fun next() {
    // Fire
    val fire = FireStrategy(env.terrible, env.trackerKasakta).apply()
    env.terrible.register(env.turn, fire)

    // Surface action
    val surface = TrapStrategy(env.terrible, env.map).apply()
    if (surface !is Empty)
      env.terrible.register(env.turn, surface)
    else {
      // Compute move action
      var move: Order
      move = AggressiveApproach(env.terrible, env.trackerKasakta).apply()
      if (move is Empty)
        move = InvisibleStrategy(env.terrible, env.trackerTerrible, env.turn).apply()
      if (move is Empty)
        move = SurfaceStrategy().apply()

      // Load weapon on move action
      if (move is Move)
        LoadStrategy(env.terrible).load(move)

      // Add move action
      env.terrible.register(env.turn, move)
    }

    // Defense strategy
    val defense = DefenseStrategy(env.terrible).apply()
    env.terrible.register(env.turn, defense)

    // Mine strategy
    val mine = MineStrategy(env.terrible, env.trackerKasakta).apply()
    env.terrible.register(env.turn, mine)

    // Trigger strategy
    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    env.terrible.register(env.turn, trigger)

    // Message strategy
    val message = MessageStrategy(env.trackerTerrible, env.trackerKasakta).apply()
    env.terrible.register(env.turn, message)
  }
}

class LoadStrategy(val submarine: Submarine) {

  fun load(order: Move) {
    if (!submarine.isTorpedoReady()) {
      order.weapon = Weapon.TORPEDO
      submarine.torpedoCoolDown--
    } else if (!submarine.isSilenceReady()) {
      order.weapon = Weapon.SILENCE
      submarine.silenceCoolDown--
    } else if (!submarine.isMineReady()) {
      order.weapon = Weapon.MINE
      submarine.mineCoolDown--
    }
  }

}

class DefenseStrategy(val submarine: Submarine) : AbstractStrategy() {

  override fun apply(): Order {
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

  override fun name(): String {
    return "Defense Strategy"
  }

  private fun silence(): Silence {
    // Fake move
    return Silence(Direction.N, 0)
  }

}

class FireStrategy(val submarine: Submarine, val tracker: Tracker) : AbstractStrategy() {

  companion object {
    const val MINIMUM_TARGET_FOR_FIRE_STRATEGY = 15
  }

  /** Compute the most aggressive next move*/
  override fun apply(): Order {
    var order: Order = Empty()
    if (tracker.candidates.size < MINIMUM_TARGET_FOR_FIRE_STRATEGY)
      order = fire()
    return order
  }

  override fun name(): String {
    return "Fire Strategy"
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

class AggressiveApproach(val submarine: Submarine, val opponent: Tracker) : AbstractStrategy() {

  companion object {
    const val MINIMUM_TARGET_FOR_AGGRESSIVE_STRATEGY = 25
  }

  /** Try an approach on the best target */
  override fun apply(): Order {
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

  override fun name(): String {
    return "Aggressive Approach"
  }
}

class InvisibleStrategy(val submarine: Submarine, val tracker: Tracker, val turn: Int) : AbstractStrategy() {

  /** Compute the most silent next move */
  override fun apply(): Order {
    var best = Vector2D()
    var silence = Int.MAX_VALUE
    val targets = tracker.targets()
    for (neigh in submarine.neigh(tracker.map)) {
      val direction = submarine.position.direction(neigh)
      val evaluation = tracker.evaluate(direction, targets)
      if (evaluation < silence) {
        // PCS, remove is trap direction computation for the first n turn
        val minTurn = 5
        if (turn <= minTurn || !submarine.isTrapDirection(tracker.map, direction)) {
          silence = evaluation
          best = neigh
        }
      }
    }
    System.err.println("Silent strategy, move to $best")
    return Move(submarine.position.direction(best))
  }

  override fun name(): String {
    return "Invisible Strategy"
  }
}

class SurfaceStrategy : AbstractStrategy() {
  override fun apply(): Order {
    return Surface()
  }

  override fun name(): String {
    return "Surface Strategy"
  }
}

class TrapStrategy(val submarine: Submarine, val map: Map) : AbstractStrategy() {

  override fun apply(): Order {
    var order: Order = Empty()
    if (submarine.trail.size > 8) // optimisation
      if (submarine.neigh(map).isEmpty())
        order = Surface()
    return order
  }

  override fun name(): String {
    return "Trap Strategy"
  }
}

class MineStrategy(val submarine: Submarine, val tracker: Tracker) : AbstractStrategy() {

  override fun apply(): Order {
    var order: Order = Empty()
    if (submarine.isMineReady()) {
      var best = 0
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
    return order
  }

  override fun name(): String {
    return "Mine Strategy"
  }

}

class TriggerStrategy(val submarine: Submarine, val tracker: Tracker) : AbstractStrategy() {

  companion object {
    const val MINIMUM_TARGET_FOR_TRIGGER_STRATEGY = 16
  }

  override fun apply(): Order {
    var order: Order = Empty()
    if (tracker.candidates.size <= MINIMUM_TARGET_FOR_TRIGGER_STRATEGY)
      order = trigger()
    return order
  }

  override fun name(): String {
    return "Trigger Strategy"
  }

  private fun trigger(): Order {
    var order: Order = Empty()

    // Look for best target : showing the most information ...
    var best = 0.45 // min evaluation to trigger bomb
    var bestMine: Vector2D? = null
    val targets = tracker.targets()
    val nbTotalTargets = targets.size
    for (mine in submarine.mines) {

      // Count percentage of targets will be touch
      var nbTarget = 0
      tracker.map.neighDiagonal(mine).forEach { if (targets.contains(it)) nbTarget++ }
      var evaluation = nbTarget.toDouble() / nbTotalTargets

      // Evaluate distance
      val quickDistance = submarine.position.distance(mine)
      if (quickDistance == 0.0)
        continue // will not fire on me please
      if (quickDistance < 1.5)
        evaluation -= 0.5 // accept to fire next to me but with penalty

      System.err.println("==>>> Mine " + mine.toString() + " evaluation = " + evaluation)

      if (evaluation > best) {
        best = evaluation
        bestMine = mine
      }
    }

    // Target found
    if (bestMine != null) {
      System.err.println("Trigger Mine : $bestMine")
      order = Trigger(bestMine)
    }

    return order
  }

}

class MessageStrategy(val tracker1: Tracker, val tracker2: Tracker) : AbstractStrategy() {

  override fun apply(): Message {
    val message = tracker1.candidates.size.toString() + " " + tracker2.candidates.size.toString()
    return Message(message)
  }

  override fun name(): String {
    return "Message Strategy"
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
        var direction: Direction = Direction.NA
        if (params.size > 1)
          direction = Direction.valueOf(params[1])
        return Mine(direction)
      }
      if (order.contains("TRIGGER")) {
        val params = order.split(" ")
        val target = Vector2D(params[1].toInt(), params[2].toInt())
        return Trigger(target)
      }
      if (order.contains("MSG")) {
        return Message(order.substringAfter(" "))
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

class Mine(val direction: Direction = Direction.NA) : Order() {
  override fun toOrderString(): String {
    return if (direction == Direction.NA) "MINE" else "MINE " + direction.name
  }
}

class Trigger(val target: Vector2D) : Order() {
  override fun toOrderString(): String {
    return "TRIGGER " + target.getIX() + " " + target.getIY()
  }
}

class Message(val message: String) : Order() {
  override fun toOrderString(): String {
    return "MSG $message"
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
    return if (turn <= 0 || turn > history.size)
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

  fun distance(v: Vector2D): Double {
    val vx = v.x - this.x
    val vy = v.y - this.y
    return sqrt((vx * vx + vy * vy))
  }

  fun getAdded(v: Vector2D): Vector2D {
    return Vector2D(x + v.x, y + v.y)
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
