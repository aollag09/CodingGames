/**
* Grab Snaffles and try to throw them through the opponent's goal!
* Move towards a Snaffle and use your team id to determine where you need to throw it.
**//*
var readline = function(){
return "12 34 32";
}
var printErr = function( string ){
console.log( string );
}*/


var myteamid = parseInt(readline()); // if 0 you need to score on the right of the map, if 1 you need to score on the left
var enemyteamid = myteamid == 0 ? 1 : 0;
var MAX_TURN = 200;

var Game = {
  field     : new Vector( 16001, 7501 ),
  goalleft  : new Vector( 0, 3750 ),
  goalright : new Vector( 16000, 3750 ),
  goalsize  : 4000,
  poleradius: 300,
  turn : 0
};

///////////////////////////////
/// GEOMETRY

var collision_circle = function( pos1, radius1, pos2, radius2 ){
  var dist = Math.sqrt( Math.pow( pos1.x - pos2.x, 2 ) + Math.pow( pos1.y - pos2.y, 2 ) );
  return dist < ( radius1 + radius2 );
}

var collision_rectangle = function( recta1, recta2, rectb1, rectb2 ){
  return recta1.x < rectb1.x + ( rectb2.x - rectb1.x )  &&
  recta1.x + ( recta2.x - recta1.x ) > rectb1.x &&
  recta1.y < rectb1.y + ( rectb2.y - rectb1.y ) &&
  ( recta2.y - recta1.y ) + recta1.y > rectb1.y;
}


function Vector( x, y ) {
  if (!(this instanceof Vector)) {
    return new Vector( x, y );
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

Vector.prototype.distance = function (vec) {
  return Math.sqrt(this.distanceSq(vec));
};

Vector.prototype.distanceSq = function (vec) {
  var dx = this.distanceX(vec),
  dy = this.distanceY(vec);

  return dx * dx + dy * dy;
};

Vector.prototype.distanceX = function (vec) {
  return this.x - vec.x;
};

Vector.prototype.distanceY = function (vec) {
  return this.y - vec.y;
};

Vector.prototype.rotate = function (angle) {
  var nx = (this.x * Math.cos(angle)) - (this.y * Math.sin(angle));
  var ny = (this.x * Math.sin(angle)) + (this.y * Math.cos(angle));

  this.x = nx;
  this.y = ny;

  return this;
};


///////////////////////////////
/// #ENTITIES
function Entity(){};

Entity.prototype = {
  id: null,
  position: null,
  state : null,
  radius: null,
  collisable: null,
};

function Snaffle( id, position, state ){
  Entity.call( this );
  this.id = id;
  this.position = position;
  this.state = state;
  this.radius = 150;
  this.collisable = false;
};

Snaffle.prototype = Object.create( Entity.prototype );

Snaffle.prototype = {

};

function Wizard( id, position, velocity, state ){
  Entity.call( this );
  this.id = id;
  this.position = position;
  this.velocity = velocity;
  this.acceleration = null; // usefull ?
  this.radius = 400;
  this.collisable = true;
  this.state = state; /* 1 has a snaffle, 0 nope */
  this.action = null;
  this.teamid = null;
};

Wizard.prototype = Object.create( Entity.prototype );

Wizard.prototype = {
  velocity : null, /* between -106 & 106 */
  action: null,
  teamid : null,

  move : function( target, power ){
    this.action = new Action( "MOVE", target, power );
  },

  throw : function( target, power ){
    this.action = new Action( "THROW", target, power );
  }
};

function Action( action, target, power ){
  this.action = action;
  this.target = target;
  this.power = power;
};

Action.prototype = {
  action : null, /* MOVE THROW */
  target : null,
  power : null, /* move max : 150, throw max : 500 */

  toString : function(){
    return this.action + " " + this.target.x + " " + this.target.y + " " + this.power;
  }
};

function Team(){
  this.wizards = [];
};

Team.prototype = {
  teamid : null,
  score : 0,
  magic : 0,
  wizards : null,
};

function Round(){
  this.snaffles = [];
  this.friends = [];
  this.enemies = [];
};

Round.prototype = {
  friends : null,
  enemies : null,
  snaffles : null,

  getentity : function( id ){
    var all = this.snaffles.concat( this.friends.wizards.concat( this.enemies.wizards ) );
    return all.find( function( entity ){
      return entity.id == id;
    });
  },

  getwizards : function( teamid ){
    var all = this.friends.wizards.concat( this.enemies.wizards );
    return all.filter( function( zard ){
      return zard.teamid == teamid;
    } );
  },

  getteammate : function( wizard ){
    var all = this.friends.wizards.concat( this.enemies.wizards );
    return all.find( function( zard ){
      return zard.teamid == wizard.teamid && zard.id != wizard.id;
    } );
  }


};


///////////////////////////////
/// #SIMU
function Simulation( round ){
  this.round = round;
}

Simulation.prototype = {
  round : null,

  computeaction_attack_naive : function( wizard ){

    if( wizard.state == 1 ){
      throw_snaffle( wizard );
    } else {
      move_closest_snaffle_available( wizard );
    }

  },

  goal_keeper : function( wizard ){

    // the goal to defend :
    var goal = ( wizard.teamid == 1 ) ? Game.goalright : Game.goalleft;

    // find the most dangerous enemy
    var enemy = null;
    var distancemin = Game.field.x;
    var enemies = round.getwizards( enemyteamid );
    for (var i = 0; i < enemies.length; i++) {
      var dist = enemies[ i ].distance( goal );
      if( dist < distancemin ){
        distancemin = dist;
        enemy = enemies[ i ];
      }
    }

    // go in between most dangerous enemy and goal :
    target = new Vector( ( enemy.position.x - goal.x ) / 2, ( enemy.position.y - goal.y ) / 2 );
    wizard.move( target, 100 );
    
  },

  move_closest_snaffle_available : function( wizard ){

    // find the closest snaffles available
    var teammate = round.getteammate( wizard );
    var snaffle = null;
    var mindist = Game.field.x;

    for( var i = 0; i < this.round.snaffles.length; i++ ){

      // distance to snaffle
      var dist = wizard.position.distance( this.round.snaffles[i].position );
      if( dist < mindist ){

        // is it available ?
        if( teammate.action == null
          || teammate.action.target == undefined
          || teammate.action.target.distance( this.round.snaffles[i].position ) > 10 ){

            // yes ! go target
            mindist = dist;
            snaffle = this.round.snaffles[i];

          }
        }
      }

      // go the closest snaffles
      var target = null;
      if( snaffle != null ){
        target = snaffle.position;
      } else{
        // back to base if no snaffle available : close to the end of the game
        target =  ( wizard.teamid == 0 ) ? Game.goalright : Game.goalleft;
      }

      // move to closest snaffle
      wizard.move( target, 100 );
    },


    throw_snaffle : function( wizard ){
      if( wizard.state != 1 ){
        throw Exception( "State of the wizard should be 1" );
      }

      // throw the waffle in the goal
      var goal = ( wizard.teamid == 0 ) ? Game.goalright : Game.goalleft;
      var target = goal;
      var power = 500;

      // if no enenmy is in the path :)
      var defense = null;
      var enemies = this.round.getwizards( ( wizard.teamid == 0 ) ? 1 : 0 );
      enemies.forEach(
        function( zard ){
          var distzard = ( zard.position.distance( wizard.position ) + zard.position.distance( target ) );
          var maxdist  = ( wizard.position.distance( target ) + zard.radius + round.snaffles[0].radius );
          if( distzard < maxdist ){
            defense = wizard;
          }
        }
      );

      if( defense != null ){
        power = 150;
      }

      // throw the snaffle
      wizard.throw( target, power );
    },

  }



  // game loop
  while( true ){

    var round = new Round();
    round.friends = new Team();
    round.enemies = new Team();
    round.friends.teamid = myteamid;
    round.enemies.teamid = enemyteamid;

    var inputs = readline().split(' ');
    var myScore = parseInt(inputs[0]);
    var myMagic = parseInt(inputs[1]);
    round.friends.score = myScore;
    round.friends.magic = myMagic;

    var inputs = readline().split(' ');
    var opponentScore = parseInt(inputs[0]);
    var opponentMagic = parseInt(inputs[1]);
    round.enemies.score = opponentScore;
    round.enemies.magic = opponentMagic;

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

      if( entityType == "WIZARD" || entityType == "OPPONENT_WIZARD" ){
        var zard = new Wizard( entityId, new Vector( x, y ), new Vector( vx, vy ), state );

        if( entityType == "WIZARD" ){
          zard.teamid = myteamid;
          round.friends.wizards.push( zard );
        } else {
          zard.teamid = enemyteamid;
          round.enemies.wizards.push( zard );
        }
      }
      else if( entityType == "SNAFFLE" ){
        var snaff = new Snaffle( entityId, new Vector( x, y ), state );
        round.snaffles.push( snaff );
      }

    }

    var simulator = new Simulation( round );
    round.friends.wizards.forEach( function( zard ){
      simulator.computeaction_attack_naive( zard );
      print( zard.action.toString() );
    });

  }
