import java.util.*

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val numberMineSpots = input.nextInt()
    for (i in 0 until numberMineSpots) {
        val x = input.nextInt()
        val y = input.nextInt()
    }

    // game loop
    while (true) {
        val gold = input.nextInt()
        val income = input.nextInt()
        val opponentGold = input.nextInt()
        val opponentIncome = input.nextInt()
        for (i in 0 until 12) {
            val line = input.next()
        }
        val buildingCount = input.nextInt()
        for (i in 0 until buildingCount) {
            val owner = input.nextInt()
            val buildingType = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
        }
        val unitCount = input.nextInt()
        for (i in 0 until unitCount) {
            val owner = input.nextInt()
            val unitId = input.nextInt()
            val level = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
        }

        // Write an action using println()
        // To debug: System.err.println("Debug messages...");

        println("WAIT")
    }
}

class Snapshot {


}

class Map {
    val SIZE = 12
    val void = "#"
    val neutral = "."
    val ownedActive = "O"
    val ownedInactive = "o"
    val activeOpponent = "X"
    val inactiveOpponent = "x"

    var map = Array(SIZE) { Array(SIZE) { void } }

}



