
var M = parseInt(readline()); // the amount of motorbikes to control
var V = parseInt(readline()); // the minimum amount of motorbikes that must survive
var L0 = readline(); // L0 to L3 are lanes of the road. A dot character . represents a safe space, a zero 0 represents a hole in the road.
var L1 = readline();
var L2 = readline();
var L3 = readline();

var lanes = [L0, L1, L2, L3];

printErr('Road : ');
lanes.forEach(function(lane) {
  printErr(lane);
});

function point(x, y) {
  return lanes[y].charAt(x);
};

var Moto = function(id, x, y, speed) {
  this.id = id;
  this.x = x;
  this.y = y;
  this.s = speed;
  this.states = [];
};

Moto.prototype = {
  id : null,
  x : null,
  y : null,
  s : null,
  dead : false,
  jumping : false,
  switching : 0,

  states : null,

  save : function() {
    this.states.push({
      x : this.x,
      y : this.y,
      s : this.s,
      dead : this.dead
    });
  },

  point : function() {
    return point(this.x, this.y);
  },

  command : function(command) {
    this.save();

    if (!this.dead) {
      this[command.toLowerCase()]();
      this.move();
    }
  },

  move : function() {
    if (this.switching && this.y + this.switching < 0 || this.y + this.switching > 3) {
      this.dead = true;
      this.jumping = false;
      this.switching = 0;
      return;
    }

    if (!this.jumping) {
      var limit = this.s;
      if (this.switching) {
        limit -= 1;
      }
      for (var delta = 1; delta <= limit; ++delta) {
        if (point(this.x + delta, this.y) == '0') {
          this.dead = true;
          break;
        }
      }

      if (this.switching) {
        // Check the new lane too
        for (var delta = 1; delta <= this.s; ++delta) {
          if (point(this.x + delta, this.y + this.switching) == '0') {
            this.dead = true;
            break;
          }
        }
      }
    }

    this.x += this.s;
    this.y += this.switching;

    this.jumping = false;
    this.switching = 0;

    if (this.point() == '0') {
      this.dead = true;
    }
  },

  speed : function() {
    this.s = Math.min(50, this.s + 1);
  },

  slow : function() {
    this.s = Math.max(0, this.s - 1);
  },

  jump : function() {
    this.jumping = true;
  },

  wait : function() {

  },

  up : function() {
    this.switching = -1;
  },

  down : function() {
    this.switching = +1;
  },

  undo : function() {
    var state = this.states.pop();

    this.x = state.x;
    this.y = state.y;
    this.s = state.s;
    this.dead = state.dead;
  }
};

var motos = [];

function execute(command) {
  motos.forEach(function(moto) {
    moto.command(command);
  });
}

function undo() {
  motos.forEach(function(moto) {
    moto.undo();
  });
}

function alives() {
  return motos.filter(function(moto) {
    return !moto.dead;
  }).length;
}

function alive() {
  return motos.filter(function(moto) {
    return !moto.dead;
  })[0];
}

var EXPLORE_DEPTH = 6,
    COMMANDS = 'WAIT SPEED SLOW JUMP UP DOWN'.split(' ');

var exploreCount = 0,
    deadWays = 0;
function explore(depth, commands, X, previous) {
  if (!depth || alive().x > L0.length - 1) {
    ++exploreCount;
    return {
      count : alives(),
      x : alive().x,
      commands : commands
    };
  }

  var resultCount = 0,
      resultX = 0,
      resultCommand = null,
      resultCommands = null;

  for (var i = 0; i < COMMANDS.length; ++i) {
    var command = COMMANDS[i],
        firstAlive = alive();

    // Don 't test stupids cases
    if (previous == 'SLOW' && command == 'SPEED') {
      continue;
    }

    if (previous == 'SPEED' && command == 'SLOW') {
      continue;
    }

    if (firstAlive.s == 50 && command == 'SPEED') {
      continue;
    }

    if (firstAlive.s == 1 && command == 'SLOW') {
      continue;
    }

    var before = firstAlive.x;

    execute(command);

    var count = alives(),
        moto = alive();

    if (!count || before == moto.x) {
      // Dead way. No more moto or we won't move.
      ++deadWays;
      undo();
      continue;
    }

    var result = explore(depth - 1, (commands || '') + ' ' + command, X, previous);

    undo();

    if (result.count > resultCount || (result.count == resultCount && result.x > resultX)) {
      resultCount = result.count;
      resultX = result.x;
      resultCommand = command;
      resultCommands = result.commands;
    }
  }

  if (depth == EXPLORE_DEPTH) {
    printErr('Solution : ', resultCommands);
    printErr('count : ', resultCount);
    printErr('x : ', resultX);
  }

  return depth == EXPLORE_DEPTH ? resultCommand : {
    count : resultCount,
    x : resultX,
    commands : resultCommands
  };
}

// game loop
while (true) {
    var benchmark = new Date();
    motos = [];
    exploreCount = 0;
    deadWays = 0;

    var S = parseInt(readline()); // the motorbikes' speed
    var X;
    for (var i = 0; i < M; i++) {
      var inputs = readline().split(' ');
      X = parseInt(inputs[0]); // x coordinate of the motorbike
      var Y = parseInt(inputs[1]); // y coordinate of the motorbike
      var A = !!parseInt(inputs[2]); // indicates whether the motorbike is activated "1" or detroyed "0"

      if (A) {
        motos.push(new Moto(i, X, Y, S, A));
      }
    }

    var result = explore(EXPLORE_DEPTH, '', X);

    printErr('Tested solutions :', exploreCount);
    printErr('Dead ways :', deadWays);
    printErr('Time :', (new Date().getTime()) - benchmark.getTime(), 'ms');

    //printErr('X', X);

    print(result);
}
