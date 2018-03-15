/**
* Auto-generated code below aims at helping you parse
* the standard input according to the problem statement.
**/

var M = parseInt(readline()); // the amount of motorbikes to control
var V = parseInt(readline()); // the minimum amount of motorbikes that must survive
var L0 = readline(); // L0 to L3 are lanes of the road. A dot character . represents a safe space, a zero 0 represents a hole in the road.
var L1 = readline();
var L2 = readline();
var L3 = readline();
var map = new Map();
map.fillLine( 0, L0 );
map.fillLine( 1, L1 );
map.fillLine( 2, L2 );
map.fillLine( 3, L3 );

// game loop
while (true) {

  // Fill Datas
  var turn = new Turn();
  var node = new Node();
  node._isfirst = true;
  node._turn = turn;
  node._turn._action = "START";

  var S = parseInt(readline()); // the motorbikes' speed
  for (var i = 0; i < M; i++) {
    var inputs = readline().split(' ');
    var X = parseInt(inputs[0]); // x coordinate of the motorbike
    var Y = parseInt(inputs[1]); // y coordinate of the motorbike
    var A = parseInt(inputs[2]); // indicates whether the motorbike is activated "1" or detroyed "0"

    var moto = new Moto();
    moto._x = X;
    moto._y = Y;
    moto._alive = A === 1;
    moto._speed = S;
    turn._motos[i] = moto;
  }

  // SIMULATOR
  var simulator = new Simulator();
  simulator.dfs( node, 0 );
  var next = simulator.getaction();

  // A single line containing one of 6 keywords: SPEED, SLOW, JUMP, WAIT, UP, DOWN.
  print( next );
}

function Simulator(){
  var MAX_DEPTH = 5;

  this._bestleaf = null;
  this._bestleafeval = 0;

  this.dfs = function( node, depth ){

    depth ++;
    if( depth < MAX_DEPTH ){

      var actions = [ "SPEED", "JUMP", "UP", "DOWN", "SLOW"];
      for( var i = 0; i < actions.length; i++) {
        //  printErr( " Depth : " + depth + " with Action : " + actions[i] );

        var nextturn = computenextturn( node._turn, actions[i], depth === 1 );
        var nextnode = new Node();
        node._next[ i ] = nextnode;
        nextnode._previous = node;
        nextnode._turn = nextturn;
        nextnode._depth = depth;

        // special stamp for imediate next moves
        if( depth == 1 ){
          nextnode._isnext = true;
        }

        // recursive with backtracking
        if( nextturn._valid ){
          this.dfs( nextnode, depth );
        }

      }
    } else if( depth === MAX_DEPTH ){

      // Terminal node
      var eval = evaluate( node._turn );


      if( eval > this._bestleafeval ){
        printErr(" EVALUATION IS : " + eval + " FOR : " + getactionlist( node ));
        this._bestleafeval = eval;
        this._bestleaf = node;
      }
    }

  };

  this.getaction = function(){

    if( this._bestleaf == null ){
      printErr(" No bestleaf has been found !")
      return "WAIT";
    } else{

      printErr(" Best Action Series is :")

      var node = this._bestleaf;
      printErr( getactionlist( node ) );

      while( ! node._isnext ){
        node = node._previous;
      }

      return node._turn._action;
    }

  };

  var evaluate = function( turn ){

    var COEFF_ALIVE = 1e6;
    var COEFF_SPEED = 1;

    if( ! turn._valid ){
      return -1;
    } else if( turn._motos[0]._speed === 0 ){
      return -1;
    }
    else{

      var eval = 0;

      for (var i = 0; i < turn._motos.length; i++) {
        var moto = turn._motos[i];

        // moto alive
        if( moto._alive ){
          eval += COEFF_ALIVE;
        }

        // moto speed
        eval += COEFF_SPEED * moto._x;

      }

      return eval;
    }

  };


  var computenextturn = function( turn, action, debug ){

    var nextturn = clone( turn ); // Copy
    nextturn._action = action;

    if( debug ){
      printErr( " Turn : " + JSON.stringify( nextturn ).toString() );
    }

    for ( var i = 0; i < nextturn._motos.length; i++) {

      var moto = nextturn._motos[ i ];
      if( moto._alive ){

        var previousmoto = clone( moto ); // Copy

        // back to the ground :
        moto._jump = false;

        // Move
        if( action === "SPEED" ){
          if( moto._speed < 50 ){
            moto._speed ++;
          } else{
            nextturn._valid = false;
          }
        } else if( action === "SLOW" ){
          if( moto._speed > 1 ){
            moto._speed --;
          } else{
            nextturn._valid = false;
          }
        } else if( action === "JUMP" ){
          moto._jump = true;
        } else if( action === "WAIT" ){

        } else if( action === "DOWN" ){
          if( moto._y < 3 ){
            moto._y ++;
          } else{
            nextturn._valid = false;
          }
        } else if( action === "UP" ){
          if( moto._y > 0 ){
            moto._y --;
          } else{
            nextturn._valid = false;
          }
        }

        if( nextturn._valid ){
          // default move
          moto._x += moto._speed;

          // is in a hole ?
          if( ! moto._jump ){

            // moto on the background

            var minx = previousmoto._x;
            var maxx = moto._x;

            var miny = Math.min( previousmoto._y, moto._y );
            var maxy = Math.max( previousmoto._y, moto._y );

            for (var ix = minx + 1; ix < maxx; ix++) {
              for (var iy = miny; iy <= maxy; iy++) {

                if( debug ){
                  printErr( " X : " + ix + ", Y : " + iy );
                  printErr( " HOLE  : " +  map._holes[iy][ix] );
                }

                if( map._holes[iy][ix] ){
                  moto._alive = false;
                }

              }
            }

          }

          // moto is in the air :
          if( map._holes[ moto._y ][ moto._x ] ){
            moto._alive = false;
          }

        }
      }
    }

    // Check validity of the turn
    if( nextturn._valid ){
      var nbalive = 0;
      for ( var i = 0; i < nextturn._motos.length; i++) {
        var moto = nextturn._motos[ i ];
        if( moto._alive ){
          nbalive ++;
        }
      }

      if( nbalive < V ){
        nextturn._valid = false;
      }
    }

    //printErr( " Next Turn : " + JSON.stringify( nextturn ).toString() );
    return nextturn;

  };

}

function Node(){
  this._turn;
  this._isfirst = false;
  this._isnext = false;
  this._previous = null;
  this._next = [];
  this._depth = 0;
}

function Turn(){
  this._motos = [];
  this._valid = true;
  this._action = null;
}

function Moto(){
  this._line = 0;
  this._x = 0;
  this._y = 0;
  this._speed = 0;
  this._alive = true;
  this._jump = false;

}

function Map(){
  this._holes = [];

  this.fillLine = function( y, line ){
    this._holes[ y ] = [];
    for( var i = 0; i < line.length; i++ ){
      this._holes[ y ][ i ] = line.charAt(i) == "0";
    }
  }

}

function getactionlist( node ){
  var out = "";
  if( node !== null && node != undefined
    && node._turn !== null && node._turn != undefined ){

      out+= node._turn._action + ", ";
      while( node._previous !== null
        && node._previous !== undefined ){
          node = node._previous;
          out+= node._turn._action + ", ";
        }

      }
      return out;
    }

    function clone(obj) {
      var copy;

      // Handle the 3 simple types, and null or undefined
      if (null == obj || "object" != typeof obj) return obj;

      // Handle Date
      if (obj instanceof Date) {
        copy = new Date();
        copy.setTime(obj.getTime());
        return copy;
      }

      // Handle Array
      if (obj instanceof Array) {
        copy = [];
        for (var i = 0, len = obj.length; i < len; i++) {
          copy[i] = clone(obj[i]);
        }
        return copy;
      }

      // Handle Object
      if (obj instanceof Object) {
        copy = {};
        for (var attr in obj) {
          if (obj.hasOwnProperty(attr)) copy[attr] = clone(obj[attr]);
        }
        return copy;
      }

      throw new Error("Unable to copy obj! Its type isn't supported.");
    }
