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

  x : null,
  y : null,
  dead : false,
  states : [],
  fuel : 1200,
  target : "CONTROL",

  setpos : function( x, y ){
    this.x = x;
    this.y = y;
    if( this.target === "CONTROL" ){
      var control = map.control();
      printErr( "Control :  " + JSON.stringify( control ) );
      if(  control != undefined && control != null && control.x == x && control.y == y ){
        // go back home !
        printErr( "Let's go back home !! " );
        this.taget = "HOME";
      }
    }
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

  search: function( grid, start, target ){

    // Init node grid
    init( grid );

    // Init first node
    var startnode =  grid[ start.x][start.y];
    startnode.g = 0;
    startnode.h = heuristic( start, target );
    startnode.f = startnode.g + startnode.h;

    // Init set list
    var closeset = [];
    var openset = [];
    openset.push( start );

    while( openset.length > 0 ){

      // lower f node in openset
      var minscore = C*R;
      var currentnode = null;
      for (var i = 0; i < openset.length; i++) {
        if( openset[i].f < minscore ){
          minscore = openset[i];
          currentnode = openset[i];
        }
      }

      //  render case -- Reach destination
      if( currentnode.pos == target ){
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

    }


  },

  neighbors: function( grid, node ){

    var ret = [];
    var x = node.pos.x;
    var y = node.pos.y;

    if(grid[x-1] && grid[x-1][y]) {
      ret.push(grid[x-1][y]);
    }
    if(grid[x+1] && grid[x+1][y]) {
      ret.push(grid[x+1][y]);
    }
    if(grid[x][y-1] && grid[x][y-1]) {
      ret.push(grid[x][y-1]);
    }
    if(grid[x][y+1] && grid[x][y+1]) {
      ret.push(grid[x][y+1]);
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
      printErr( "Line : " + i + " : " + ROW );
  }

  var printmap = true;
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
  printErr("Kirk : " + JSON.stringify( kirk ).toString() );

  print('RIGHT'); // Kirk's next move (UP DOWN LEFT or RIGHT).
}
