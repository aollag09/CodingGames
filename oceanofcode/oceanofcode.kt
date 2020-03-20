import java.util.*
import java.io.*
import java.math.*

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main(args : Array<String>) {
   
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

class Map{
  companion object{
    var SIZE = 15;
  }


}
