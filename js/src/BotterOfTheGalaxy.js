/**
 * Made with love by AntiSquid, Illedan and Wildum.
 * You can help children learn to code while you participate by donating to CoderDojo.
 **/

 //////////////////////////////////////
 ///// CONSTANTS
var MAP_LENGTH = 750;
var MAP_WIDTH = 1920;
var TRACE_LEVEL = 2;
var MY_HERO = "DEADPOOL";
var MY_TEAM_ID = parseInt(readline());
var ROUNT_TIME = 1000;


//////////////////////////////////////
///// GAME LOOP

var bushAndSpawnPointCount = parseInt(readline()); // usefrul from wood1, represents the number of bushes and the number of places where neutral units can spawn
for (var i = 0; i < bushAndSpawnPointCount; i++) {
    var inputs = readline().split(' ');
    var entityType = inputs[0]; // BUSH, from wood1 it can also be SPAWN
    var x = parseInt(inputs[1]);
    var y = parseInt(inputs[2]);
    var radius = parseInt(inputs[3]);
}
var itemCount = parseInt(readline()); // useful from wood2
for (var i = 0; i < itemCount; i++) {
    var inputs = readline().split(' ');
    var itemName = inputs[0]; // contains keywords such as BRONZE, SILVER and BLADE, BOOTS connected by "_" to help you sort easier
    var itemCost = parseInt(inputs[1]); // BRONZE items have lowest cost, the most expensive items are LEGENDARY
    var damage = parseInt(inputs[2]); // keyword BLADE is present if the most important item stat is damage
    var health = parseInt(inputs[3]);
    var maxHealth = parseInt(inputs[4]);
    var mana = parseInt(inputs[5]);
    var maxMana = parseInt(inputs[6]);
    var moveSpeed = parseInt(inputs[7]); // keyword BOOTS is present if the most important item stat is moveSpeed
    var manaRegeneration = parseInt(inputs[8]);
    var isPotion = parseInt(inputs[9]); // 0 if it's not instantly consumed
}

// game loop
var roundnumber = 0;
while (true) {

    var gold = parseInt(readline());
    var enemyGold = parseInt(readline());
    var roundType = parseInt(readline()); // a positive value will show the number of heroes that await a command
    var entityCount = parseInt(readline());

    var round = new Round();
    round._roundtype = roundType;
    round._friend = new TeamSnapshot();
    round._enemy = new TeamSnapshot();
    round._friend._gold = gold;
    round._enemy._gold = enemyGold;

    for (var i = 0; i < entityCount; i++) {

        // PARSE
        var inputs = readline().split(' ');
        var unitId = parseInt(inputs[0]);
        var team = parseInt(inputs[1]);
        var unitType = inputs[2]; // UNIT, HERO, TOWER, can also be GROOT from wood1
        var x = parseInt(inputs[3]);
        var y = parseInt(inputs[4]);
        var attackRange = parseInt(inputs[5]);
        var health = parseInt(inputs[6]);
        var maxHealth = parseInt(inputs[7]);
        var shield = parseInt(inputs[8]); // useful in bronze
        var attackDamage = parseInt(inputs[9]);
        var movementSpeed = parseInt(inputs[10]);
        var stunDuration = parseInt(inputs[11]); // useful in bronze
        var goldValue = parseInt(inputs[12]);
        var countDown1 = parseInt(inputs[13]); // all countDown and mana variables are useful starting in bronze
        var countDown2 = parseInt(inputs[14]);
        var countDown3 = parseInt(inputs[15]);
        var mana = parseInt(inputs[16]);
        var maxMana = parseInt(inputs[17]);
        var manaRegeneration = parseInt(inputs[18]);
        var heroType = inputs[19]; // DEADPOOL, VALKYRIE, DOCTOR_STRANGE, HULK, IRONMAN
        var isVisible = parseInt(inputs[20]); // 0 if it isn't
        var itemsOwned = parseInt(inputs[21]); // useful from wood1

        // BUILD
        var entity = buildEntity( unitType, heroType );
        entity._x = x;
        entity._y = y;
        entity._entityid = unitId;
        entity._entitytype = unitType;
        entity._teamid = team;
        entity._health = health;
        entity._maxhealth = maxHealth;
        entity._isvisible = isVisible;
        entity._damage = attackDamage;
        entity._movespeed = moveSpeed;
        entity._attackrange = attackRange;

        // Append to teams
        if( team === MY_TEAM_ID ){
          round._friend._entities[ round._friend._entities.length ] = entity;
        }
        else {
          round._enemy._entities[ round._enemy._entities.length ] = entity;
        }

    }

    // Write an action using print()
    // To debug: printErr('Debug messages...');

    // If roundType has a negative value then you need to output a Hero name, such as "DEADPOOL" or "VALKYRIE".
    // Else you need to output roundType number of any valid action, such as "WAIT" or "ATTACK unitId"

    // Compute the environment evaluation
    var environment = new Environment();
    var evaluation = environment.evaluate( round );
    trace( 1, "ENVIRONMENT EVALUATION : " + evaluation );


    // Compute next move
    var simulator = new Simulator();
    var thenextmove = simulator.nextmove( round );
    var nextround = simulator.nextmove( round, [ thenextmove ] );


    print( thenextmove.toStringValue() );
    roundnumber
}


//////////////////////////////////////
///// SIMULATOR

function Simulator(){

  /** Compute the next move */
  this.nextmove = function( round ){
    if( round._roundtype < 0 ){
      // First round
      return MY_HERO;
    } else {

      var action = null;
      // is close to bot ?
      var myHero = round._friend.getHeros()[0];
      var inDanger = false;
      for (var i = 0; i < round._enemy.getUnits().length; i++) {
        var unit = round._enemy.getUnits()[i];
        var distance = dist( myHero._x, myHero._y, unit._x, unit._y);
        trace( 4, "Comparing the unit " + unit._entityid + " and the distance is : " + distance );
        if(  distance < unit._attackrange + 20 )
          inDanger = true;
      }

      if( inDanger ){
         // back to base
        action = new Move();
        action._entity = myHero;
        action._x = round._friend.getTower()._x;
        action._y = round._friend.getTower()._y;

      } else {

        // Best option to kill ?
        var toKill = null;

        // Enemy Hero ?
        for (var i = 0; i < round._enemy.getHeros().length; i++) {
          var enemyHero = round._enemy.getHeros()[i];
          if( myHero.distTo( enemyHero._x, enemyHero._y ) < myHero._attackrange )
            toKill = enemyHero;
        }

        // Enemy Unit ?
        if( toKill === null ) {
          for (var i = 0; i < round._enemy.getUnits().length; i++) {
            var enemyUnit = round._enemy.getUnits()[i];
            if( myHero.distTo( enemyUnit._x, enemyUnit._y ) < myHero._attackrange )
              toKill = enemyUnit;
          }
        }

        if( toKill == null ){
          action = new AttackNearest();
          action._entity = myHero;
          action._entitytype = "UNIT";
        } else {
          action = new Attack();
          action._entity = myHero;
          action._entity = toKill._entityid;
        }
      }

      return action;
    }
  };

  /** Simulate the next round from an input round with an input move */
  this.nextround = function( round, actions /* array of actions for each entities */ ){

    // Copy the nextround
    var emptyround =  new Round();
    var nextround = Object.assign( nextround, round ); // Copy

    for( var action in actions ) {
      if (object.hasOwnProperty( action ) ) {
        nextround_action( nextround, action );
      }
    }
  };

  var nextround_action = function( nextround, currentaction ){

    if( Hero.prototype.isPrototypeOf( currentaction._entity ) ){
      nextround_hero( nextround, currentaction );
    }

  };

  /* Internal Function */
  var nextround_hero = function( nextround, action ){
    if( action != null && action != undefined ){

      // Move action
      if( Move.prototype.isPrototypeOf( action ) ){
        var traveldistance = dist( hero._x, hero._y, action._x, action._y );
        var coeff = 1;
        if( traveldistance > hero._movespeed ){
         coeff = hero._movespeed / traveldistance;
       }
       // update the new hero position :
       hero._x = hero_x + ( coeff ) * ( hero._x - action._x );
       hero._y = hero_y + ( coeff ) * ( hero._y - action._y );

      } else if( Attack.prototype.isPrototypeOf( action ) ){


      } else if( Wait().prototype.isPrototypeOf( action ) ){
        // eassssy
      }
    }
  };

}

//////////////////////////////////////
///// ENVIRONMENT

function Environment(){

  var COEFF_LIFE_HERO = 7;
  var COEFF_LIFE_UNIT = 1;
  var COEFF_LIFE_TOWER = 4;

  /** This function enable to evaluate a specific environment */
  this.evaluate = function( round ){

    // Hero Life
    var ratio_hero_friend = 0;
    for (var i = 0; i < round._friend.getHeros().length; i++) {
      ratio_hero_friend += round._friend.getHeros()[i]._health / round._friend.getHeros()[i]._maxhealth;
    }
    ratio_hero_friend /= round._friend.getHeros().length;

    var ratio_hero_enemies = 0;
    for (var i = 0; i < round._enemy.getHeros().length; i++) {
      ratio_hero_enemies += round._enemy.getHeros()[i]._health / round._enemy.getHeros()[i]._maxhealth;
    }
    ratio_hero_enemies /= round._enemy.getHeros().length;
    ratio_hero_enemies = 1 - ratio_hero_enemies;


    // Unit Life
    var ratio_unit_friend = 0;
    for (var i = 0; i < round._friend.getUnits().length; i++) {
      ratio_unit_friend += round._friend.getUnits()[i]._health / round._friend.getUnits()[i]._maxhealth;
    }
    ratio_unit_friend /= round._friend.getUnits().length;

    var ratio_unit_enemies = 0;
    for (var i = 0; i < round._friend.getUnits().length; i++) {
      ratio_unit_enemies += round._friend.getUnits()[i]._health / round._friend.getUnits()[i]._maxhealth;
    }
    ratio_unit_enemies /= round._friend.getUnits().length
    ratio_unit_enemies = 1 - ratio_unit_enemies;

    // Tower Life
    var ratio_tower_friend = round._friend.getTower()._health / round._friend.getTower()._maxhealth;
    var ratio_tower_enemies  = round._enemy.getTower()._health / round._enemy.getTower()._maxhealth;

    var objectif =
      ( ratio_hero_friend   * COEFF_LIFE_HERO +
      ratio_hero_enemies  * COEFF_LIFE_HERO +
      ratio_unit_friend   * COEFF_LIFE_UNIT +
      ratio_unit_enemies  * COEFF_LIFE_UNIT +
      ratio_tower_friend   * COEFF_LIFE_TOWER +
      ratio_tower_enemies  * COEFF_LIFE_TOWER ) / 6.0;
    return objectif*100;
  };

}


//////////////////////////////////////
///// ACTIONS
function Action(){
  this._entity = null;
}
function Move(){
  Action.call( this );
  this._x = null;
  this._y = null;
  this.toStringValue = function(){
    return "MOVE " + this._x + " " + this._y;
  };
}
Move.prototype = Object.create( Action.prototype );

function Attack(){
  Action.call( this );
  this._entityid = null;
  this.toStringValue = function(){
    return "ATTACK " + this._entityid;
  };
}
Attack.prototype = Object.create( Action.prototype );


function AttackNearest(){
  Action.call( this );
  this._entitytype = null; /* String */
  this.toStringValue= function(){
    return "ATTACK_NEAREST " + this._entitytype;
  };
}
Attack.prototype = Object.create( Action.prototype );

function MoveAttack(){
  Action.call( this );
  this._x = null;
  this._y = null;
  this._entityid = null;
  this.toStringValue = function(){
    return "MOVE_ATTACK " + this._x + " " + this._y + " " + this._entityid;
  };
}
MoveAttack.prototype = Object.create( Action.prototype );

function Wait(){
  Action.call( this );
  this.toStringValue = function(){
    return "WAIT";
  };
}
Wait.prototype = Object.create( Action.prototype );




//////////////////////////////////////
///// GAME ELEMENTS
function Round(){
  this._roundtype=null;
  this._friend=null;
  this._enemy=null;
}

function TeamSnapshot(){
  this._teamid=null;
  this._gold= null;
  this._entities = [];

  /** Return a specific entity from its id */
  this.getEntityById = function( theEntityid ){
    var entity = null;
    for (var i = 0; i < this._entities.length; i++) {
      if( this._entities[i]._entityid === theEntityid )
        entity = this._entities[i];
    }
    return entity;
  }

  /** Retrieve all heros in the team */
  this.getHeros = function(){
    var heros = [];
    index = 0;
    for (var i = 0; i < this._entities.length; i++) {
      if( this._entities[i]._entitytype === "HERO" ){
        heros[index] = this._entities[i];
        index ++;
      }
    }
    return heros;
  };

  /** Retrieve all units in the team */
  this.getUnits = function(){
    var units = [];
    index = 0;
    for (var i = 0; i < this._entities.length; i++) {
      if( this._entities[i]._entitytype === "UNIT" ){
        units[index] = this._entities[i];
        index ++;
      }
    }
    return units;
  };

  /* get the tower */
  this.getTower = function(){
    var tower = null;
    for (var i = 0; i < this._entities.length; i++) {
      if( this._entities[i]._entitytype === "TOWER" ){
        tower = this._entities[i];
      }
    }
    return tower;
  };
}

function Entity(){
  /*if( ! Entity.prototype.isPrototypeOf( this ) ){
    throw "Create an Entity from an input object that is not an Entity";
  }*/

  this._x=null;
  this._y=null;
  this._entityid = null;
  this._entitytype = null;
  this._teamid = null;
  this._health = null;
  this._maxhealth = null;
  this._isvisible = null
  this._damage = null;
  this._movespeed = null;
  this._attackrange = null;

  this.isranged = function(){
    return this._attackrange > 150;
  }

  this.timeTo = function( x, y ){
    return ( dist( x, y, this._x, this._y ) / this._movespeed );
  }
  this.distTo = function( x, y ){
    return dist( x, y, this._x, this._y );
  }

}


function Unit(){
  Entity.call( this );
  this.aggro = 0; /*  Unit aggro lasts for 3 rounds, including the initial round */
}
Unit.prototype = Object.create( Entity.prototype );

function Tower(){
  Entity.call( this );
}
Tower.prototype = Object.create( Entity.prototype );

function Hero(){
  Entity.call( this );
  this._name=null;
}
Hero.prototype = Object.create( Entity.prototype );

function Deadpool(){
  Hero.call( this );
}
Deadpool.prototype = Object.create( Hero.prototype );

function DoctorStrange(){
  Hero.call( this );
}
DoctorStrange.prototype = Object.create( Hero.prototype );

function Hulk(){
  Hero.call( this );
}
Hulk.prototype = Object.create( Hero.prototype );

function IronMan(){
  Hero.call( this );
}
IronMan.prototype = Object.create( Hero.prototype );

function Valkyrie(){
  Hero.call( this );
}
Valkyrie.prototype = Object.create( Hero.prototype );


// UTILS
function buildEntity( entityType, heroType ){
  var entity = null;
  if( entityType === "UNIT" ){
    entity = new Unit();
  } else if( entityType === "TOWER" ){
    entity = new Tower();
  } else if ( entityType === "HERO" ) {
    if( heroType === "DEADPOOL" ){
      entity = new Deadpool();
    } else if( heroType === "VALKYRIE" ){
      entity = new Valkyrie();
    } else if( heroType === "DOCTOR_STRANGE" ){
      entity = new DoctorStrange();
    } else if ( heroType === "HULK") {
      entity = new Hulk();
    } else if ( heroType === "IRONMAN") {
      entity = new IronMan();
    } else {
       trace( 1, "Can't create hero with " + entityType, heroType );
    }
  } else{
    trace( 1, "Can't create entity with " + entityType, heroType );
  }
  return entity;
}

//////////////////////////////////////
///// HELPERS

function trace( level, message ){
  if( level <= TRACE_LEVEL ){
    printErr( message );
  }
}

function dist( x1, y1, x2, y2 ){
  return Math.sqrt( ( x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
}
