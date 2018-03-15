/**
* Auto-generated code below aims at helping you parse
* the standard input according to the problem statement.
**/

var N = parseInt(readline());
var MAX = 1;
//printErr( "N : " + N );
var tasks = [];

for( var i = 0; i < N; i++ ){
  var inputs = readline().split(' ');
  var J = parseInt(inputs[0]);
  var D = parseInt(inputs[1]);

  var task = {
    day : J,
    duration : D
  };

  tasks[ i ] = task;
}

// sort the tasks by duration
var sortedtasks = tasks.sort( function( task1, task2 ) {
  return ( task1.duration - task2.duration );
});

var nbtasks = 0;

var timeline = {
  planning : [],

  schedule : function( task ){

    //printErr( "Schedule Task : " + JSON.stringify( task ) );
    
    var available = true;
    for( var i = task.day; i < ( task.day + task.duration ); i ++ ){

      // init if first time
      if( this.planning[ i ] === undefined || this.planning[ i ] === null ){
        this.planning[ i ] = 0;
      }

      // more than capacity ?
      if( (  this.planning[ i ] + 1 ) > MAX ){
        available = false;
      }

    }

    if( available ){

      // increment the number of tasks
      nbtasks ++;

      // append to schedule
      for( var i = task.day; i < ( task.day + task.duration ); i ++ ){
        this.planning[ i ] += 1;
      }
    }

    //printErr( this.toDebugString( ) );
  },

  toDebugString : function(){
    var ret = "[";
    for (var i = 0; i < this.planning.length; i++) {
      ret += this.planning[i] +", "
    }
    ret += "]";
    return ret;
  }

};

// add tasks to timeline
for (var i = 0; i < tasks.length; i++) {
  timeline.schedule( tasks[i] );
}


print( nbtasks );
