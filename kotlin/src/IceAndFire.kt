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

	var map: Map
	var ice : Team
	var fire: Team

}

class Map {
    val SIZE = 12
    val VOID = "#"
    val NEUTRAL = "."
    val OWNED_ACTIVE = "O"
    val OWNED_INACTIVE = "o"
    val OPPONENT_ACTIVE = "X"
    val OPPONENT_INACTIVE = "x"

    var map = Array(SIZE) { Array(SIZE) { VOID } }

	fun init(index: Int, line: String){
        line.
		for( i in 0 until SIZE ){
			map.get( index ).set( i, line.get(i) );
		}
	}
}

class Team{
	var gold: Int
	var income: Int
	var units: Array<Unit>
	var building: Array
}

class Unit{
	var x: Int
	var y: Int
	var id: Int
	var level: Int
}

class Building{
	var x: Int
	var y: Int
	var type: Int
}

fun dist(x1: Int, y1: Int, x2:Int, y2: Int):Double{
	return (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)
}

