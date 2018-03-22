/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 **/

var myTeamId = parseInt(readline()); // if 0 you need to score on the right of the map, if 1 you need to score on the left

var MAX_TURN = 200;

var game = {
  field     : new Vector( 16001, 7501 ),
  goalleft  : new Vector( 0, 3750 ),
  goalright : new Vector( 16000, 3750 ),
  goalsize  : 4000,
  poleradius: 300,
  turn : 0
};

///////////////////////////////
/// Snaffles

Snaffle.prototype = {
  vector : null,
  radius : 150
};


///////////////////////////////
/// WIZARDS

Wizard.prototype = {
  position : null,
  speed : null,
  radius : 400
}
///////////////////////////////
/// VECTOR

function Vector( x, y ) {
	if (!(this instanceof Vector)) {
		return new Vector(x, y);
	}
	/** The X axis */
	this.x = x || 0;
	/** The Y axis */
	this.y = y || 0;
};

Vector.fromObject = function (obj) {
	return new Vector(obj.x || 0, obj.y || 0);
};

Vector.prototype.add = function (vec) {
	this.x += vec.x;
	this.y += vec.y;
	return this;
};




// game loop
while (true) {
    var inputs = readline().split(' ');
    var myScore = parseInt(inputs[0]);
    var myMagic = parseInt(inputs[1]);
    var inputs = readline().split(' ');
    var opponentScore = parseInt(inputs[0]);
    var opponentMagic = parseInt(inputs[1]);
    var entities = parseInt(readline()); // number of entities still in game
    for (var i = 0; i < entities; i++) {
        var inputs = readline().split(' ');
        var entityId = parseInt(inputs[0]); // entity identifier
        var entityType = inputs[1]; // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
        var x = parseInt(inputs[2]); // position
        var y = parseInt(inputs[3]); // position
        var vx = parseInt(inputs[4]); // velocity
        var vy = parseInt(inputs[5]); // velocity
        var state = parseInt(inputs[6]); // 1 if the wizard is holding a Snaffle, 0 otherwise
    }
    for (var i = 0; i < 2; i++) {

        // Write an action using print()
        // To debug: printErr('Debug messages...');


        // Edit this line to indicate the action for each wizard (0 ≤ thrust ≤ 150, 0 ≤ power ≤ 500)
        // i.e.: "MOVE x y thrust" or "THROW x y power"
        print('MOVE 8000 3750 100');
    }
}
