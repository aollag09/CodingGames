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

var log = function( tag, message ){
  if( tag == "THROW_SNAFFLE" ){
    printErr( "[" + tag + "] " + message );
  }
}


var myteamid = parseInt(readline()); // if 0 you need to score on the right of the map, if 1 you need to score on the left
var enemyteamid = myteamid == 0 ? 1 : 0;
var MAX_TURN = 200;

var Game = {
  field     : new Vector( 16001, 7501 ),
  goalleft  : new Vector( 0, 3750 ),
  goalright : new Vector( 16000, 3750 ),
  goalsize  : 4000,
  poleradius: 300,
  turn      : 0,
  obliviate : 5,
  petrificus: 10,
  accio     : 15,
  flipendo  : 20
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

Vector.prototype.length = function () {
  return Math.sqrt(this.lengthSq());
};

Vector.prototype.lengthSq = function () {
  return this.x * this.x + this.y * this.y;
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

Vector.prototype.dot = function (vec2) {
  return this.x * vec2.x + this.y * vec2.y;
};

Vector.prototype.angle = function( vec ) {
  var cos = this.dot( vec )/ ( this.length() * vec.length() );
  return Math.acos( cos );
};

Vector.prototype.rotate = function (angle) {
  var nx = (this.x * Math.cos(angle)) - (this.y * Math.sin(angle));
  var ny = (this.x * Math.sin(angle)) + (this.y * Math.cos(angle));

  this.x = nx;
  this.y = ny;

  return this;
};

Vector.prototype.toString = function() {
  return "( " + this.x + ", "+ this.y + " )";
};


///////////////////////////////
/// #ENTITIES
function Entity(){};

Entity.prototype = {
  id: null,
  position: null,
  velocity : null, /* between -106 & 106 */
  state : null,
  radius: null,
  collisable: null,
  mass : null,
  friction : null,
};

function Snaffle( id, position, velocity, state ){
  Entity.call( this );
  this.id = id;
  this.position = position;
  this.velocity = velocity;
  this.state = state;
  this.radius = 150;
  this.thrustmax = 500;
  this.collisable = false;
  this.mass = 0.5;
  this.friction = 0.75;
};

Snaffle.prototype = Object.create( Entity.prototype );

Snaffle.prototype = {
};

function Bludgers( id, position, velocity, state ){
  Entity.call( this );
  this.id = id;
  this.position = position;
  this.velocity = velocity;
  this.state = state; /* entityid of victim or -1 */
  this.radius = 200;
  this.collisable = true;
  this.mass = 8;
  this.friction = 0.9;
}

Bludgers.prototype = Object.create( Entity.prototype );

Bludgers.prototype = {
};


function Wizard( id, position, velocity, state ){
  Entity.call( this );
  this.id = id;
  this.position = position;
  this.velocity = velocity; /* between -106 & 106 */
  this.acceleration = null; // usefull ?
  this.radius = 400;
  this.thrustmax = 150;
  this.collisable = true;
  this.state = state; /* 1 has a snaffle, 0 nope */
  this.action = null;
  this.teamid = null;
  this.mass = 1;
  this.friction = 0.75;
};

Wizard.prototype = Object.create( Entity.prototype );

Wizard.prototype = {
  action: null,
  teamid : null,

  move : function( target, power ){
    if( power != undefined && power != null ){
      this.action = new Action( "MOVE", target, power );
    } else {
      // Compute the best power regarding the new target
      var coeff = 1;
      var computedpower = this.thrustmax * coeff;
      this.action = new Action( "MOVE", target, computedpower );
    }
  },

  throw : function( target, power ){
    this.action = new Action( "THROW", target, power );
  },

  obliviate : function( target ){
    this.action = new Spell( "OBLIVIATE", target );
  },

  petrificus : function( target ){
    this.action = new Spell( "PETRIFICUS", target );
  },

  accio : function( target ){
    this.action = new Spell( "ACCIO", target );
  },

  flipendo : function( target ){
    this.action = new Spell( "FLIPENDO", target );
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

function Spell( spell, targetid ){
  this.spell = spell;
  this.target = targetid;
}

Spell.prototype = {
  spell : null,
  target: null,

  toString : function(){
    return this.spell + " " + this.target;
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
  this.bludgers = [];
  this.snaffles = [];
  this.friends = [];
  this.enemies = [];
};

Round.prototype = {
  friends : null,
  enemies : null,
  snaffles : null,
  bludgers : null,

  getentity : function( id ){
    var all = this.getentities();
    return all.find( function( entity ){
      return entity.id == id;
    });
  },

  getentities : function(){
    return this.bludgers.concat( this.snaffles.concat( this.friends.wizards.concat( this.enemies.wizards ) ) );
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

  /* Compute the next round */
  next_round : function(){
    // Copy the round
    var nextround = Object.assign( nextround, this.round );
  },

};


function WizardGenerator( round ){
  this.round = round;
}

WizardGenerator.prototype = {
  round : null,

  computeaction_attack_naive : function( wizard ){

    if( wizard.state == 1 ){
      this.throw_snaffle( wizard );
    } else {
      this.move_closest_snaffle_available( wizard );
      this.choose_spell( wizard );
    }

  },

  choose_spell : function( wizard ){
    //this.flipendo_spell( wizard );
    //this.petrificus_goal_spell( wizard );
    this.accio_spell( wizard );
  },

  accio_spell : function( wizard ){
    var manafactor = 1;

    if( this.round.friends.magic >=   Game.accio + Game.petrificus ){
      // Accio the snaffle the closest to the goal
      var goal = ( wizard.teamid == 1 ) ? Game.goalright : Game.goalleft;
      var mindist = Game.field.x / 3;
      var distwiztogoal = goal.distance( wizard.position );
      log( "ACCIO", "distance wizard with goal ===> " + distwiztogoal );
      var snaffle = undefined;
      for( var i = 0; i < this.round.snaffles.length; i++ ){
        var dist = goal.distance( this.round.snaffles[i].position );
        if( dist < mindist  /* */
          && dist < distwiztogoal /* wiz is between snaffle and goal -> no accio */ ){
            mindist = dist;
            snaffle = this.round.snaffles[i];
          }
        }
        if( snaffle != undefined ){

          var teammate = round.getteammate( wizard );

          // The closest wizard launch the accio spell
          var distTeammate = teammate.position.distance( snaffle.position );
          var distWizard   = wizard.position.distance( snaffle.position );
          log( "ACCIO", "distTeammate ===> " + distTeammate );
          log( "ACCIO", "distWizard ===> " + distWizard );
          if(  distWizard < distTeammate ){

            // If the snaffle is to close : no accio
            var snaffletooclose = 2000;
            // If the snaffle is too far
            var snaffletoofar = 9000;

            if( distWizard > snaffletooclose
              && distWizard < snaffletoofar ){
                log( "ACCIO", "Launch Accio Spell on ===> " +  snaffle.id );
                wizard.accio( snaffle.id );
              }
            }
          }
        }
      },

      flipendo_spell : function( wizard ){
        var manafactor = 1;
        if( this.round.friends.magic >= manafactor * Game.flipendo ){
          var goal = ( wizard.teamid == 0 ) ? Game.goalright : Game.goalleft;
          log( "FLIP", "wizard.position ===> " + wizard.position.toString() );
          log( "FLIP", "goal.position ===> " + goal.toString() );

          // Loop on all snaffles
          for( var i = 0; i < this.round.snaffles.length; i++ ){
            var snaffle = this.round.snaffles[ i ];
            log( "FLIP", "snaffle.id ===> " + snaffle.id );
            log( "FLIP", "snaffle.position ===> " + snaffle.position.toString() );

            // Compute the line equation between the snaffle and the wizard :
            // y=mx+p
            var m = ( snaffle.position.y - wizard.position.y ) / ( snaffle.position.x - wizard.position.x );
            var p = wizard.position.y - ( m * wizard.position.x );
            log( "FLIP", "m ===> " + m );
            log( "FLIP", "p ===> " + p );

            // direction should point to the goal
            var ytarget = goal.x * m + p;
            log( "FLIP", "ytarget ===> " + ytarget );
            if( Math.abs( ytarget - goal.y ) > ( Game.goalsize / 2.0 ) ){
              log( "FLIP", "Direction is not head to the goal" );
              continue;
            }

            // snaffle should be between goal & wizard
            var snaffleToGoal = snaffle.position.distance( goal );
            var zardToGoal    = wizard.position.distance( goal );
            log( "FLIP", "snaffleToGoal ===> " + snaffleToGoal );
            log( "FLIP", "zardToGoal ===> " + zardToGoal );
            if( snaffleToGoal > zardToGoal ){
              log( "FLIP", "Snaffle is not between goal & wizard" );
              continue;
            }

            // Snaffle should not be too close from the wizard
            var minDistWithZard = 3000;
            var distSnaffZard = wizard.position.distance( snaffle.position );
            log( "FLIP", "distSnaffZard ===> " + distSnaffZard );
            if( distSnaffZard < minDistWithZard ){
              log( "FLIP", "Snaffle is too close from the wizard" );
              continue;
            }

            // Snaffle should not be too close from the goal
            var minDistWithGoal = 1000;
            var distSnaffGoal = wizard.position.distance( goal );
            log( "FLIP", "distSnaffGoal ===> " + distSnaffGoal );
            if( distSnaffGoal < minDistWithGoal ){
              log( "FLIP", "Snaffle is too close from the goal" );
              continue;
            }

            // Check that no obstacle is on the way
            var enemyzard = this.round.getwizards( ( wizard.teamid == 0 ) ? 1 : 0 );
            var obstacles = this.round.bludgers.concat( enemyzard );
            var obstacleOntheWay = false;
            for (var i = 0; i < obstacles.length; i++) {
              var obstacle = obstacles[i];
              log( "FLIP", "obstacle.id ===> " + obstacle.id );

              var distObstSnaf = obstacle.position.distance( snaffle.position );
              var distObstGoal = obstacle.position.distance( goal );
              log( "FLIP", "distObstSnaf ===> " + distObstSnaf );
              log( "FLIP", "distObstGoal ===> " + distObstGoal );

              // current obstacle is on the way ?
              if( ( distObstGoal + distObstSnaf ) <= snaffleToGoal + obstacle.radius ){
                log( "FLIP", "This obstacle is on the way ===> " + obstacle.id );
                obstacleOntheWay = true;
              }
            }

            // launch spell
            if( obstacleOntheWay == false ){
              log( "FLIP", "Launch the flipendo spell !" );
              wizard.flipendo( snaffle.id );
              break;
            }
          }
        }
      },

      petrificus_goal_spell : function( wizard ){

        // Pertificus as a goal keeper
        var manafactor = 1;
        if( this.round.friends.magic >= manafactor * Game.petrificus ){
          var enemies = this.round.getwizards( enemyteamid );
          var teammate = round.getteammate( wizard );
          var safetyfactor = 2.5;
          log( "GOAL", "safetyfactor ===> " + safetyfactor );
          // Save snaffle going to fast to the goal
          for( var i = 0; i < this.round.snaffles.length; i++ ){
            var snaffle = this.round.snaffles[ i ];
            log( "GOAL", "snaffle.id ===> " + snaffle.id );
            log( "GOAL", "snaffle.position ===> " + snaffle.position.toString( ) );
            log( "GOAL", "snaffle.velocity ===> " + snaffle.velocity.toString( ) );
            var mygoal = ( wizard.teamid == 1 ) ? Game.goalright : Game.goalleft;
            var zardToSnaffle     = wizard.position.distance( snaffle.position );
            var teammateToSnaffle = teammate.position.distance( snaffle.position );

            // The wizard most far is calling the spell
            if( zardToSnaffle > teammateToSnaffle ){
              if ( wizard.teamid == 1 ){
                // My goal is at the right
                log( "GOAL", "My goal is at the right" );
                if( snaffle.position.x < Game.goalright.x &&
                  ( snaffle.position.x + safetyfactor * snaffle.velocity.x ) >= Game.goalright.x ){
                    log( "GOAL", "Launch the pretrificus spell ! ");
                    wizard.petrificus( snaffle.id );
                    break;
                  }
                } else {
                  // My goal is at the left
                  log( "GOAL", "My goal is at the left" );
                  if( snaffle.position.x > Game.goalleft.x &&
                    ( snaffle.position.x + safetyfactor * snaffle.velocity.x ) <= Game.goalleft.x  ){
                      wizard.petrificus( snaffle.id );
                      break;
                    }
                  }
                }
              }
            }
          },


          petrificus_spell : function( wizard ){

            // On bludger
            for( var i = 0; i < this.round.bludgers.length; i++ ){
              // Is bludger targeting the current wizard
              if( this.round.bludgers[i].state == wizard.id ) {
                var minDistance = 2000;
                if( wizard.position.distance( this.round.bludgers[i].position ) < minDistance ){
                  // Launch the spell !!
                  wizard.petrificus(  this.round.bludgers[i].id );
                }
              }
            }

            // On enemy
            var manafactor = 2;
            if( this.round.friends.magic >=  manafactor * Game.petrificus ){
              var enemies = this.round.getwizards( enemyteamid );
              if( enemies[ 0 ] != null ){
                wizard.petrificus( enemies[ 0 ].id );
                this.round.friends.magic -= Game.petrificus;
              }
            }
          },


          obliviate_spell : function( wizard ){
            // Obliviate Spell
            if( round.friends.magic >= Game.obliviate ){
              // Enough magic
              for( var i = 0; i < this.round.bludgers.length; i++ ){
                // Is bludger targeting the current wizard
                if( this.round.bludgers[i].state == wizard.id ) {
                  // Is not far
                  var minDistance = 4000;
                  if( wizard.position.distance( this.round.bludgers[i].position ) < minDistance ){
                    // Launch the spell !!
                    wizard.obliviate( this.round.bludgers[i].id );
                  }
                }
              }
            }
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
                  || teammate.action.target == undefined /* go if teammate taget no one */
                  || teammate.action.target != this.round.snaffles[i].position /* go if teammate taget another one */
                  /*|| teammate.position.distance( this.round.snaffles[i].position ) > 10  snaffle not next to temate*/
                  || this.round.snaffles.length <= 1  )/* go if only one remaining snaffle */{

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
                target =  ( wizard.teamid == 1 ) ? Game.goalright : Game.goalleft;
              }

              // move to closest snaffle
              wizard.move( target );
            },


            throw_snaffle : function( wizard ){

              if( wizard.state != 1 ){
                throw Exception( "State of the wizard should be 1" );
              }

              // throw the waffle in the goal
              var goal = ( wizard.teamid == 0 ) ? Game.goalright : Game.goalleft;
              var power = 500;
              log( "THROW" , " wizard.id ===> " + wizard.id );
              log( "THROW" , " wizard.position ===> " + wizard.position.x + "," + wizard.position.y );
              log( "THROW" , " goal.position ===> " + goal.x + "," + goal.y );

              var direction = new Vector( goal.x - wizard.position.x ,goal.y - wizard.position.y );
              log( "THROW" , " direction.position ===> " + direction.x + "," + direction.y );
              var angle = Math.atan2( direction.y, direction.x );
              log( "THROW" , " angle ===> " + angle );

              // Try to throw over a near snaffle to push two snaffles !
              for (var i = 0; i < this.round.snaffles.length; i++) {
                var snaffle = this.round.snaffles[i]
                var snaffToZard = snaffle.position.distance( wizard.position );
                log( "THROW_SNAFFLE" , " snaffle.id ===> " + snaffle.id );
                log( "THROW_SNAFFLE" , " snaffToZard ===> " + snaffToZard );

                // Extra limits
                var minDist = 600;
                if( snaffToZard < minDist ){
                  log( "THROW_SNAFFLE" , "The snaff is too close" );
                  continue;
                }
                var maxDist = 3000;
                if( snaffToZard > maxDist ){
                  log( "THROW_SNAFFLE" , "The snaff is too far" );
                  continue;
                }

                // Comute direction
                var snaffdirection = new Vector( snaffle.position.x - wizard.position.x, snaffle.position.y - wizard.position.y );
                var snaffangle = Math.atan2( snaffdirection.y, snaffdirection.x );
                log( "THROW_SNAFFLE" , " snaffdirection ===> " + snaffdirection );
                log( "THROW_SNAFFLE" , " snaffangle ===> " + snaffangle );
                log( "THROW_SNAFFLE" , " angletogoal ===> " + angle );
                log( "THROW_SNAFFLE" , " maxAngle ===> " + maxAngle );
                var maxAngle = Math.PI / 3;
                if( Math.abs( angle - snaffangle ) < maxAngle ){
                  // Target the other snaffle instead of the goal
                  log( "THROW_SNAFFLE" , " Target the other snaffle instead of the goal ===> " + snaffle.id );
                  angle = snaffangle;
                  break;
                }
              }


              // if no enenmy is in the path :)
              // create the obstacle angle list : [ angle_obstacle1, angle_range_obstacle1, angle, range angle ... ]
              log( "THROW" , " ### look for obstacles " );
              var enemyzard = this.round.getwizards( ( wizard.teamid == 0 ) ? 1 : 0 );
              var obstacles = this.round.bludgers.concat( enemyzard );
              var obstacleanglelist = [];
              var noobstacle = true;
              for( var i = 0; i < obstacles.length; i++ ) {
                var obstacle =  obstacles[ i ];
                log( "THROW" , " obstacle.id ===> " + obstacle.id );
                var obstacleradius = obstacle.radius;
                log( "THROW" , " obstacle.radius ===> " + obstacleradius );

                var maxdist = 5000;
                if( obstacle.position.distance( wizard ) > 5000 ){
                  pintErr( " This obstacle is too far ");
                  continue;
                }

                var obstacledirection = new Vector( obstacle.position.x - wizard.position.x, obstacle.position.y - wizard.position.y );
                log( "THROW" , " obstacledirection.position ===> " + obstacledirection.x + "," + obstacledirection.y );
                var obstacleangle = Math.atan2( obstacledirection.y, obstacledirection.x );
                log( "THROW" , " obstacleangle ===> " + obstacleangle );
                var radiusfactor = 2;
                var obstacleanglerange = Math.atan2( radiusfactor * obstacleradius, obstacle.position.distance( wizard.position ) );
                log( "THROW" , " obstacleanglerange ===> " + obstacleanglerange );

                if( Math.abs( angle - obstacleangle ) < obstacleanglerange ){
                  // An obstacle on the way has been found !
                  log( "THROW" , " !!! This obstacle is on the way to the goal ")
                  noobstacle = false;
                  obstacleanglelist.push( obstacleangle );
                  obstacleanglelist.push( obstacleanglerange );
                }
              }

              if( noobstacle == false ){
                log( "THROW" , " An obstacle has been found while throwing the snaffle ");
                // Compute the best angle to throw the boal regarding the different obstacles
                var newangle = undefined;
                var bestangle = Game.field.x;
                for( var j = 0; j < obstacleanglelist.length; j+=2 ) {
                  var obstacleangle = obstacleanglelist[j];
                  var obstacleanglerange = obstacleanglelist[ j+1 ];
                  var currentnewangle = obstacleangle;
                  if( angle < obstacleangle ){
                    currentnewangle -= obstacleanglerange;
                  } else {
                    currentnewangle += obstacleanglerange;
                  }
                  if( Math.abs( currentnewangle - angle ) < bestangle ){
                    bestangle = Math.abs( currentnewangle - angle );
                    newangle = currentnewangle;
                  }
                }
                if( newangle != undefined ){
                  angle = newangle;
                }
              }

              var distToGoal = Math.trunc( goal.distance( wizard.position ) );
              log( "THROW" , " power ===> " + power );
              log( "THROW" , " final angle is ===> " + angle );
              var nx = Math.trunc( wizard.position.x + distToGoal * Math.cos( angle ) );
              var ny = Math.trunc( wizard.position.y + distToGoal * Math.sin( angle ) );
              var target = new Vector(  nx, ny );
              log( "THROW" , " target.position ===> " + target.x + "," + target.y );

              // throw the snaffle
              wizard.throw( target, power );
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

            }

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
                var snaff = new Snaffle( entityId, new Vector( x, y ), new Vector( vx, vy ), state );
                round.snaffles.push( snaff );
              }
              else if( entityType == "BLUDGER" ){
                var blud = new Bludgers( entityId, new Vector( x, y ), new Vector( vx, vy ), state );
                round.bludgers.push( blud );
              }

            }

            var wizardgenerator = new WizardGenerator( round );
            round.friends.wizards.forEach( function( zard ){
              wizardgenerator.computeaction_attack_naive( zard );
              print( zard.action.toString() );
            });

          }
