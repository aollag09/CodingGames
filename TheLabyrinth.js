/* eslint */

/*function readline(){
return "10 10 &fdklsfk lkdqfmdl fdlqkfm";
}
function printErr( string ){
console.log( string );
}*/

var inputs = readline().split(' ');
var R = parseInt(inputs[0]); // number of rows.
var C = parseInt(inputs[1]); // number of columns.
var A = parseInt(inputs[2]); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.

// Kirk
var kirk = {

  fuel : 1200,
  start : null,
  target : null,
  backhome : false,

  // Update the target
  setpos : function( x, y ){
    this.start = map.grid[x][y];
    this.updatetarget();
  },

  updatetarget : function(){
    var control = map.control();
    var start = map.start();

    if( control == null ){
      // Explore
      this.target = null;

    } else {
      if( this.backhome ){
        this.target = start;
      }
      else if(  control != null && control.char == this.start.char ){
        this.backhome = true;
        this.target = start;
      } else{
        this.target = control;
      }
    }
  },

  next : function(){

    // compute shortest path :
    var resultpath = astar.search( map.grid, this.start, this.target, true );
    if( resultpath == null || resultpath.length == 0 ){
      resultpath = astar.search( map.grid, this.start, this.target, false );
    }
    // debug
    /*printErr( "RESULT PATH :")
    printErr( JSON.stringify( resultpath ).toString() );*/

    // next move is the first element :
    var next = resultpath[ 0 ];

    if( next.pos.x > this.start.pos.x ){
      return "RIGHT";
    }
    if( next.pos.x < this.start.pos.x ){
      return "LEFT";
    }
    if( next.pos.y < this.start.pos.y ){
      return "UP";
    }
    if( next.pos.y > this.start.pos.y ){
      return "DOWN";
    }
    return "NOPE"
  },

  save : function() {
    this.states.push({
      x : this.x,
      y : this.y,
      fuel : this.fuel,
      dead : this.dead
    });
  },

  undo : function() {
    var state = this.states.pop();
    this.x = state.x;
    this.y = state.y;
    this.dead = state.dead;
    this.fuel = fuel;
  },

  up : function() {
    this.x += 1;
    this.fuel --;
  },

  down : function() {
    this.x -= 1;
    this.fuel --;
  },

  left : function() {
    this.y -= 1;
    this.fuel --;
  },

  right : function() {
    this.y += 1;
    this.fuel --;
  }

};



// The Map
var map = {

  // Initialize the grid
  grid : new Array( C ),

  init : function( ){
    for( var i = 0; i < this.grid.length; i++ ){
      this.grid[i] = new Array( R );

      for( var j = 0; j < this.grid[i].length; j++ ){
        this.grid[i][j] = {}; // init
        this.grid[i][j].pos = {
          x: i,
          y: j
        };
      }

    }
  },

  update : function( y, row ){
    for (var x = 0; x < row.length; x++) {
      this.grid[x][y].char = row.charAt( x );
    }
  },

  point : function( x, y){
    return this.grid[x][y];
  },

  start : function(){
    return this.lookup( 'T' );
  },

  control : function(){
    return this.lookup( 'C' );
  },

  lookup : function( char ){
    for( var ix = 0; ix < this.grid.length; ix++) {
      for (var iy = 0; iy < this.grid[ix].length; iy++) {
        if( this.grid[ix][iy].char == char ){
          return this.grid[ix][iy];
        }
      }
    }
    return null;
  }

};


// A* algorithm
var astar = {

  init: function( grid ){

    // Init Grid
    for( var lx = 0; lx < C; lx++ ) {
      for( var ly = 0; ly < R; ly++ ) {
        var node = grid[lx][ly];
        node.f = 0;
        node.g = 0;
        node.h = 0;
        node.cost = 1;
        node.closed = false;
        node.parent = null;
      }
    }

  },

  search: function( grid, start, target, force ){

    // Init node grid
    this.init( grid );

    printErr( "START NODE : " + JSON.stringify( start ) );
    printErr( "TARGET NODE : " + JSON.stringify( target ) );

    // Init first node
    start.g = 0;
    if( target != null ){
      start.h = this.heuristic( start.pos, target.pos );
    }
    start.f = start.g + start.h;

    // Init set list
    var closeset = [];
    var openset = [];
    openset.push( start );
    var nbiteration = 0;

    while( openset.length > 0 ){
      nbiteration ++;

      // lower f node in openset
      var minscore = C*R;
      var currentnode = null;
      for (var i = 0; i < openset.length; i++) {
        if( openset[i].f < minscore ){
          minscore = openset[i];
          currentnode = openset[i];
        }
      }

  //    printErr( "CURRENT NODE : " + JSON.stringify( currentnode.pos ) + " " + currentnode.char );

      //  render case --
      var stop = false;
      if( target == null ){
        stop = currentnode.char == "?";  // closest unknwon places
      }
      if( target != null && ! stop ){
        stop = currentnode.char == target.char; // reach target
      }
      if( stop ){
        printErr( " NB ITERATION : " + nbiteration );
    //    printErr( "END LOOP WITH : " + JSON.stringify( currentnode ) );
        var curr = currentnode;
        var ret = [];
        while(curr.parent) {
          ret.push(curr);
          curr = curr.parent;
        }
        return ret.reverse();
      }


      // Normal case -- move currentNode from open to closed, process each of its neighbors
      openset.splice( openset.indexOf( currentnode ), 1 );
      closeset.push( currentnode );

      // loop on all neighbors
      var next = this.neighbors( grid, currentnode );
      for( var i = 0; i < next.length; i++ ){
        neighbor = next[i];

        if( this.isWall( neighbor, target, force  ) || closeset.includes( neighbor ) ) {
          continue; // skip this node
        }

        var gscore = currentnode.g + 1;
        var gisbest = false;

        if( ! openset.includes( neighbor ) ){
          // This the the first time we have arrived at this node, it must be the best
          // Also, we need to take the h (heuristic) score since we haven't done so yet
          gisbest = true;
          neighbor.h = 0;
          if( target != null ){
            neighbor.h = this.heuristic( neighbor.pos, target.pos );
          }
          openset.push( neighbor );
        } else if( gscore < neighbor.g ){
          // We have already seen the node, but last time it had a worse g (distance from start)
          gisbest = true;
        }

        if( gisbest ){
          // Found an optimal (so far) path to this node.   Store info on how we got here and
          //  just how good it really is...
          neighbor.parent = currentnode;
          neighbor.g = gscore;
          neighbor.f = neighbor.g + neighbor.h;
        }
      }
    }

    // No result was found -- empty array signifies failure to find path
    return [];

  },

  contains : function( node, array ){
    return array.includes( node );
  },

  isWall : function( node, target, force ){
    var iswall = node.char == "#";
    if( force && target != null ){
      iswall = iswall || node.char == "?";
    }

    return iswall
  },

  neighbors: function( grid, node ){

    var ret = [];
    var x = node.pos.x;
    var y = node.pos.y;

    if(grid[x][y-1] && grid[x][y-1]) {
      ret.push(grid[x][y-1]);
    }
    if(grid[x][y+1] && grid[x][y+1]) {
      ret.push(grid[x][y+1]);
    }
    if(grid[x-1] && grid[x-1][y]) {
      ret.push(grid[x-1][y]);
    }
    if(grid[x+1] && grid[x+1][y]) {
      ret.push(grid[x+1][y]);
    }


    return ret;
  },

  heuristic: function( pos0, pos1 ) {
    // This is the Manhattan distance
    var d1 = Math.abs(pos1.x - pos0.x);
    var d2 = Math.abs(pos1.y - pos0.y);
    return d1 + d2;
  }

};



// Game Loop
map.init();

while (true) {
  var inputs = readline().split(' ');
  var KR = parseInt(inputs[0]); // row where Kirk is located.
  var KC = parseInt(inputs[1]); // column where Kirk is located.

  // update map
  for (var i = 0; i < R; i++) {
    var ROW = readline(); // C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
    map.update( i, ROW );
  }

  var printmap = false;
  if( printmap ){
    printErr( " The Map : " );
    for (var i = 0; i < R; i++) {
      line = "";
      for (var j = 0; j < C; j++) {
        line += map.grid[j][i].char;
      }
      printErr( line );
    }
  }

  // update kirk
  kirk.setpos( KC, KR );
  var next = kirk.next();
  printErr("Kirk : " + JSON.stringify( kirk ).toString() );

  print( next ); // Kirk's next move (UP DOWN LEFT or RIGHT).
}
