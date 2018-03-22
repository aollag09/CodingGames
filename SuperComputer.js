/**
* Auto-generated code below aims at helping you parse
* the standard input according to the problem statement.
**/

var N = parseInt(readline());
printErr( "N : " + N );
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

printErr( "End Sort" );


var mintime = N;
var maxtime = -N;
for (var i = 0; i < tasks.length; i++) {

  if( tasks[i].day < mintime ){
    mintime = tasks[i].day;
  }

  if( tasks[ i ].day + tasks[ i ].duration > maxtime ){
    maxtime = tasks[ i ].day + tasks[ i ].duration;
  }

}

printErr( " mintime : " + mintime );
printErr( " maxtime : " + maxtime );
var maxtime = maxtime - mintime;


var nbtasks = 0;

var timeline = {
  planning : [],
  nbhoursplanned : 0,

  schedule : function( task ){



    var available = true;

    // optimization
    /*if( this.nbhoursplanned >= maxtime ){
    available = false */


    if( available ){

      for( var i = task.day; i < ( task.day + task.duration ); i ++ ){

        // more than capacity ?
        if( this.planning[ i ] === true ){
          available = false;
          break;
        }

      }
    }

    if( available ){

      // increment the number of tasks
      nbtasks ++;
        printErr( "Schedule Task : " + JSON.stringify( task ) );
      // append to schedule
      for( var j = task.day; j < ( task.day + task.duration ); j ++ ){
        this.planning[ j ] = true;
        this.nbhoursplanned ++;
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


// init
for (var i = mintime; i < maxtime; i++) {
  timeline.planning[ i ] = false;
}



// add tasks to timeline
for (var i = 0; i < sortedtasks.length; i++) {
  timeline.schedule( sortedtasks[i] );
}

print( nbtasks );
