package main.kotlin

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
  val map: Map = Map(width, height);
  for (j in 0 until height)
    map.parse(input.nextLine(), j);

  // Initialise environment
  val env: Env = Env(map);
  env.submarine.id = myId;

  // Write an action using println()
  // To debug: System.err.println("Debug messages...");

  println("7 7")

  // game loop
  while (true) {
    // Update environment
    env.submarine.position = Vector2D(input.nextInt(), input.nextInt());
    env.submarine.life = input.nextInt();
    env.opponent.life = input.nextInt();
    env.submarine.torpedoCoolDown = input.nextInt()
    env.submarine.sonarCoolDown = input.nextInt()
    env.submarine.silenceCoolDown = input.nextInt()
    env.submarine.mineCoolDown = input.nextInt()
    env.submarine.sonarResult = input.next()
    if (input.hasNextLine()) {
      input.nextLine()
    }
    val opponentOrders = input.nextLine()

    // Write an action using println()
    // To debug: System.err.println("Debug messages...");

    println("MOVE N TORPEDO")
  }


}

class Env(map: Map) {
  var submarine: Submarine = Submarine();
  var opponent: Opponent = Opponent();
}

class Map(width: Int, height: Int) {
  val size: Vector2D = Vector2D(width, height);
  var islands: MutableSet<Vector2D> = HashSet();

  fun isIsland(pos: Vector2D): Boolean {
    return islands.contains(pos);
  }

  fun isWater(pos: Vector2D): Boolean {
    return !isIsland(pos);
  }

  fun parse(line: String, j: Int) {
    for (i in line.indices)
      if (line.toCharArray()[i] != '.')
        this.islands.add(Vector2D(i, j))
  }

  fun getWaterSection(section: Int): Set<Vector2D> {
    if (section < 1 || section > 9)
      throw RuntimeException("Section should be between 1 and 9")
    val water: MutableSet<Vector2D> = HashSet();
    val x: Int = section % 3;
    val y: Int = section / 3;
    for (i in x - 1 until x + 5) {
      for (j in y - 1 until y + 5) {
        val pos: Vector2D = Vector2D(i, j);
        if (isWater(pos))
          water.add(pos);
      }
    }
    return water;
  }

  fun neigh(pos: Vector2D): Set<Vector2D> {
    val neigh: MutableSet<Vector2D> = mutableSetOf();
    for (dx in -1..1 step 1)
      for (dy in -1..1 step 1)
        if (abs(dx) + abs(dy) == 1)
          if (pos.x + dx >= 0 && pos.x + dx < this.size.x)
            if (pos.y + dy >= 0 && pos.y + dy < this.size.y)
              if (isWater(Vector2D(pos.x + dx, pos.y + dy)))
                neigh.add(Vector2D(pos.x + dx, pos.y + dy));
    return neigh;
  }

  fun directedGraph(start: Vector2D): Graph<Vector2D> {
    // Populate graph
    var graph: Graph<Vector2D> = Graph();
    val visited: MutableSet<Vector2D> = mutableSetOf();
    val toVisit: MutableList<Vector2D> = mutableListOf();
    toVisit.add(start);
    while (toVisit.isNotEmpty()) {
      val next: Vector2D = toVisit.removeAt(0);
      if (!visited.contains(next)) {
        for (neigh in this.neigh(next)) {
          if (!visited.contains(neigh)) {
            graph.addEdge(next, neigh);
            toVisit.add(neigh);
          }
        }
        visited.add(next);
      }
    }
    return graph;
  }
}

class Submarine() {
  var id: Int = 0;
  var position: Vector2D = Vector2D();
  var life: Int = 6;
  var torpedoCoolDown: Int = 0;
  var sonarCoolDown: Int = 0;
  var silenceCoolDown: Int = 0;
  var mineCoolDown: Int = 0;
  var sonarResult: String = "NA"; // Can be Y, N or NA

}

class Opponent {
  var life: Int = 6;
}

abstract class Order {
  abstract fun toOrderString(): String;
}

class Surface : Order() {
  override fun toOrderString(): String {
    return "SURFACE"
  }
}

class Torpedo : Order() {
  var target: Vector2D = Vector2D();
  override fun toOrderString(): String {
    return "TORPEDO " + target.getIX() + " " + target.getIY();
  }
}

class LoadTorpedo : Order() {
  override fun toOrderString(): String {
    return "TORPEDO"
  }
}



class BiggestGraphAlgo {

  fun solve(): Vector2D {
    return Vector2D();
  }

}


class Move : Order() {
  var target: Vector2D = Vector2D();
  override fun toOrderString(): String {
    return "MOVE " + target.getIX() + " " + target.getIY();
  }
}

class Graph<T> {
  private val adjacencyMap: HashMap<T, HashSet<T>> = HashMap()

  fun addEdge(sourceVertex: T, destinationVertex: T) {
    // Add edge to source vertex / node.
    adjacencyMap
        .computeIfAbsent(sourceVertex) { HashSet() }
        .add(destinationVertex)
  }

  fun size(): Int {
    var size = 0;
    adjacencyMap.forEach { (_, hashSet) -> size += hashSet.size }
    return size;
  }

  override fun toString(): String = StringBuffer().apply {
    for (key in adjacencyMap.keys) {
      append("$key -> ")
      append(adjacencyMap[key]?.joinToString(", ", "[", "]\n"))
    }
  }.toString()
}

class Vector2D(var x: Double, var y: Double) {

  constructor(vector2D: Vector2D) : this(vector2D.x, vector2D.y);
  constructor(ix: Int, iy: Int) : this(ix.toDouble(), iy.toDouble());
  constructor() : this(0, 0);

  fun set(vector2D: Vector2D) {
    this.x = vector2D.x;
    this.y = vector2D.y;
  }

  fun getIX(): Int {
    return x.toInt();
  }

  fun getIY(): Int {
    return y.toInt();
  }

  fun length(): Double {
    return sqrt((x * x + y * y).toDouble());
  }

  fun direction(v: Vector2D): String {
    if (x != v.x && y != v.y)
      throw RuntimeException("Impossible to compute diagonal direction");
    if (x == v.x && y == v.y)
      throw RuntimeException("No direction, stay on same position")
    if (v.x > x)
      return "E"
    if (v.x < x)
      return "W"
    if (v.y > y)
      return "S"
    if (v.y < y)
      return "N"
    return "NA"
  }

  fun distance(vx: Double, vy: Double): Double {
    var dx = vx
    var dy = vy
    dx -= x
    dy -= y
    return sqrt(dx * dx + dy * dy)
  }

  fun distance(v: Vector2D): Double {
    val vx = v.x - this.x;
    val vy = v.y - this.y;
    return sqrt((vx * vx + vy * vy).toDouble());
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
