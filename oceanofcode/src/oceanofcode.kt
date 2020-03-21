import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun main(args: Array<String>) {

    val input = Scanner(System.`in`)
    val width = input.nextInt()
    val height = input.nextInt()
    val myId = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    for (i in 0 until height) {
        val line = input.nextLine()
    }

    // Write an action using println()
    // To debug: System.err.println("Debug messages...");

    println("7 7")

    // game loop
    while (true) {
        val x = input.nextInt()
        val y = input.nextInt()
        val myLife = input.nextInt()
        val oppLife = input.nextInt()
        val torpedoCooldown = input.nextInt()
        val sonarCooldown = input.nextInt()
        val silenceCooldown = input.nextInt()
        val mineCooldown = input.nextInt()
        val sonarResult = input.next()
        if (input.hasNextLine()) {
            input.nextLine()
        }
        val opponentOrders = input.nextLine()

        // Write an action using println()
        // To debug: System.err.println("Debug messages...");

        println("MOVE N TORPEDO")
    }


}

class Map(size: Vector2D) {
    var size: Vector2D = Vector2D(15, 15);
    var width = 15;


}

class Submarine {

}

class Vector2D(x: Double, y: Double) {

    var x: Double = 0.0;
    var y: Double = 0.0;

    constructor(vector2D: Vector2D) : this(vector2D.x, vector2D.y)
    constructor(ix: Int, iy: Int) : this(ix + 0.0, iy + 0.0);

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

    fun distance(vx: Double, vy: Double): Double {
        var dx = vx
        var dy = vy
        dx -= x
        dy -= y
        return sqrt(dx * dx + dy * dy)
    }

    fun distance(v: Vector2D): Double {
        var vx = v.x - this.x;
        var vy = v.y - this.y;
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
        return getNormalized()!!.getMultiplied(this.dot(v!!) / this.length())
    }

    fun getProjectedVector(vx: Double, vy: Double): Vector2D {
        return getNormalized()!!.getMultiplied(this.dot(vx, vy) / this.length())
    }

    fun getProjectedVector(v1: Vector2D, v2: Vector2D): Vector2D {
        return v1.getNormalized()!!.getMultiplied(dot(v1, v2) / v1.length())
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

}
