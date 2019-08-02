import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.stream.Collectors;

interface Solver {

  Action solve(Env e);
}

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem
 * statement.
 */
class Player {

  public static void main(String args[]) {
    Scanner in = new Scanner(System.in);

    Env env = new Env();

    int numSites = in.nextInt();
    for (int i = 0; i < numSites; i++) {
      EmptySite site = new EmptySite();
      site.id = in.nextInt();
      site.location = new Vector2D(in.nextInt(), in.nextInt());
      site.radius = in.nextInt();
      env.sites.add(site);
    }

    Env simulationEnv = null;

    // game loop
    while (true) {
      env.friend.gold = in.nextInt();
      int touchedSite = in.nextInt();

      // update sites
      for (int i = 0; i < numSites; i++) {
        int siteId = in.nextInt();
        if (env.getSite(siteId).isPresent()) {
          Site previous = env.getSite(siteId).get();
          int gold =
              in.nextInt(); // The total number of gold remaining to be mined from this site (-1
          // if unknown)
          int maxMineSize =
              in.nextInt(); // The maximum rate that a mine can extract gold from this site (-1 if
          // unknown)
          int structureType = in.nextInt(); // -1 = No structure, 2 = Barracks
          int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
          int param1 = in.nextInt();
          int param2 = in.nextInt();
          env.sites.remove(previous);
          env.sites.add(
              Site.build(
                  siteId,
                  previous.location,
                  previous.radius,
                  gold,
                  maxMineSize,
                  structureType,
                  owner,
                  param1,
                  param2));
        } else {
          System.err.println("Unknown site with id " + siteId);
        }
      }

      // update units
      int numUnits = in.nextInt();
      env.friend.units = new ArrayList<>();
      env.enemy.units = new ArrayList<>();
      for (int i = 0; i < numUnits; i++) {
        int x = in.nextInt();
        int y = in.nextInt();
        int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
        int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
        int health = in.nextInt();
        Unit unit = Unit.build(x, y, unitType, health);
        if (owner == 0) env.friend.units.add(unit);
        else env.enemy.units.add(unit);
      }
      env.friend.getQeen().touchedSite = touchedSite;

      System.err.println(env.toString());
      if (simulationEnv != null) {
        // compare previous simulation with result ...
        simulationEnv.printDiff(env);
      }

      Solver solver = new StupidSolver();
      Action action = solver.solve(env);

      // update simulation
      Simulator simulator = new Simulator();
      simulationEnv = simulator.simulate(env, action, null);

      action.print();
      // First line: A valid queen action
      // Second line: A set of training instructions
    }
  }
}

class StupidSolver implements Solver {

  @Override
  public Action solve(Env e) {
    Action action = new Action();
    action.queen = queenAction(e);
    action.train = trainAction(e);
    return action;
  }

  private QueenAction queenAction(Env e) {
    QueenAction next = null;
    next = build(e);
    if (next == null) {
      next = moveToNextSite(e);
      if (next == null) {
        next = nothing();
      }
    }
    return next;
  }

  private Build build(Env e) {
    Build buildAction = null;
    int siteId = e.friend.getQeen().touchedSite;
    if (siteId != -1) {
      if (e.getSite(siteId).isPresent()) {
        Site site = e.getSite(siteId).get();

        long nbKnightBarracks =
            e.sites.stream()
                .filter(s -> s instanceof KnightBarracks)
                .filter(s -> ((Structure) s).isFriend)
                .count();
        long nbArcherBarracks =
            e.sites.stream()
                .filter(s -> s instanceof ArcherBarracks)
                .filter(s -> ((Structure) s).isFriend)
                .count();
        long nbGiantBarracks =
            e.sites.stream()
                .filter(s -> s instanceof GiantBarracks)
                .filter(s -> ((Structure) s).isFriend)
                .count();
        long totalIncome =
            e.sites.stream()
                .filter(s -> s instanceof Mine)
                .filter(s -> ((Structure) s).isFriend)
                .mapToInt(s -> ((Mine) s).income)
                .sum();
        long minIncome = 10;

        Structure building = null;
        if (site instanceof EmptySite
            || (site instanceof Structure && !((Structure) site).isFriend)) {

          if (totalIncome < minIncome && site.maxMineSize > 2 && site.gold > 100) {
            building = new Mine();
          } else if (nbKnightBarracks < 1) {
            building = new KnightBarracks();
          } else if (nbGiantBarracks < 1) {
            building = new GiantBarracks();
          } else building = new Tower();
        }

        if (totalIncome < minIncome
            && site instanceof Mine
            && ((Mine) site).income < ((Mine) site).maxMineSize
            && site.gold > 100) {
          building = ((Mine) site).clone();
          ((Mine) building).build();
        }
        if (site instanceof Tower && ((Tower) site).life < 500) {
          Tower tower = ((Tower) site).clone();
          tower.build();
        }

        if (building != null) {
          buildAction = new Build();

          // copy existing information on building
          Optional<Site> existing = e.getSite(siteId);
          if (existing.isPresent()) {
            building.id = existing.get().id;
            building.gold = existing.get().gold;
            building.maxMineSize = existing.get().maxMineSize;
            building.radius = existing.get().radius;
            building.location = existing.get().location.clone();
            building.isFriend = true;
          }
          buildAction.building = building;
          buildAction.id = siteId;
        }
      }
    }
    return buildAction;
  }

  private Move moveToNextSite(Env e) {
    Move move = null;
    Queen queen = e.friend.getQeen();
    Vector2D queenPos = queen.location;
    Optional<Site> next =
        e.sites.stream()
            .filter(s -> s instanceof EmptySite)
            .min(Comparator.comparing(site -> site.location.distance(queenPos)));

    if (next.isPresent()) {
      // move to next empty place
      move = new Move();
      move.to = queen.location.towards(next.get().location, queen.speed);
    } else {
      // move to smallest tower
      next =
          e.sites.stream()
              .filter(s -> s instanceof Tower)
              .filter(s -> ((Structure) s).isFriend)
              .min(Comparator.comparing(s -> ((Tower) s).life));
      if (next.isPresent()) {
        move = new Move();
        move.to = queen.location.towards(next.get().location, queen.speed);
      }
    }

    return move;
  }

  private TrainAction trainAction(Env e) {
    TrainAction trainAction = new TrainAction();
    List<Integer> trainSites = new ArrayList<>();
    long nbKnights = e.friend.units.stream().filter(unit -> unit instanceof Knight).count();
    long nbArcher = e.friend.units.stream().filter(unit -> unit instanceof Archer).count();
    long nbGiants = e.friend.units.stream().filter(unit -> unit instanceof Giant).count();

    if (nbKnights == 0 || nbGiants != 0) {
      // Train knights
      Optional<Site> knightBarrack =
          e.sites.stream()
              .filter(s -> s instanceof KnightBarracks)
              .filter(s -> ((Structure) s).isFriend)
              .filter(s -> ((Barracks) s).available())
              .filter(s -> ((Barracks) s).trainCost <= e.friend.gold)
              .min(
                  Comparator.comparing(site -> site.location.distance(e.enemy.getQeen().location)));
      knightBarrack.ifPresent(
          site -> {
            trainAction.trainIds.add(site.id);
            e.friend.gold -= ((Barracks) site).trainCost;
          });
    } else {
      // Train giants
      Optional<Site> giantBarracks =
          e.sites.stream()
              .filter(s -> s instanceof GiantBarracks)
              .filter(s -> ((Structure) s).isFriend)
              .filter(s -> ((Barracks) s).available())
              .filter(s -> ((Barracks) s).trainCost <= e.friend.gold)
              .min(
                  Comparator.comparing(
                      site -> site.location.distance(e.friend.getQeen().location)));
      giantBarracks.ifPresent(
          site -> {
            trainAction.trainIds.add(site.id);
            e.friend.gold -= ((Barracks) site).trainCost;
          });
    }

    return trainAction;
  }

  private Wait nothing() {
    return new Wait();
  }
}

class Evaluator {

  double queenDanger; // evaluate life lost by your queen
  double armyDanger; // evaluate total life lost by your units
  double attackQueen; // evaluate life lost by the enemy queen
  double attackArmy; // evaluate total life lost by enemy units
  double quickAction; // distance of move without doing anything
  double goldDiff; // gold diff at the end of all actions
  double goldIncomeDiff; // gold income diff at the end of all actions
  double buildings; // mandatory building are here ?
  double buildingsPosition; // does building are well situated regarding environment ?
  double discovery; // available gold has to be dicovered. Often more gold & income rate at center

  public Evaluator() {
    queenDanger = 0;
    armyDanger = 0;
    attackQueen = 0;
    attackArmy = 0;
  }

  double evaluate(Env env, List<Action> actions) {
    towerImpact(env, actions);
    return queenDanger;
  }

  private void towerImpact(Env env, List<Action> actions) {
    // compute the losing point of life of the queen if not moving
    int damage = 0;

    // towers
    List<Tower> attackTowers =
        env.sites.stream()
            .filter(site -> site instanceof Tower)
            .map(site -> (Tower) site)
            .filter(tower -> !tower.isFriend)
            .filter(tower -> tower.location.distance(env.friend.getQeen().location) <= tower.range)
            .collect(Collectors.toList());
    for (Tower tower : attackTowers) {
      int hp = tower.life;
      int range = Tower.computeRange(tower, hp);

      // has a defend creep ?
      boolean defendCreep =
          env.friend.creeps().stream()
              .anyMatch(creep -> creep.location.distance(tower.location) < tower.range);

      if (!defendCreep) {
        while (range > tower.location.distance(env.friend.getQeen().location)) {
          damage += 3;
          hp -= 4;
          range = Tower.computeRange(tower, hp);
        }
      }
    }
  }
}

class Simulator {

  Env simulate(Env env, Action friendAction, Action enemyAction) {
    Env next = env.clone();
    processTrainAction(next, friendAction.train, true);
    processQueenAction(next, friendAction.queen, false);
    processEndOfTurnUpdates(next);
    return next;
  }

  private void processTrainAction(Env env, TrainAction trainAction, boolean friend) {
    for (int trainSite : trainAction.trainIds) {
      env.getSite(trainSite)
          .ifPresent(
              site -> {
                if (site instanceof Barracks) {
                  Barracks barracks = (Barracks) site;
                  if (friend) env.friend.gold -= barracks.trainCost;
                  else env.enemy.gold -= barracks.trainCost;
                  barracks.working = barracks.trainingTime;
                }
              });
    }
  }

  private void processQueenAction(Env env, QueenAction queenAction, boolean friend) {
    Queen queen = friend ? env.friend.getQeen() : env.enemy.getQeen();
    if (queenAction instanceof Move) {
      queen.location = ((Move) queenAction).to;
    } else if (queenAction instanceof Build) {
      Build build = (Build) queenAction;
      // remove existing building
      Optional<Site> existing = env.getSite(build.id);
      existing.ifPresent(site -> env.sites.remove(site));
      env.sites.add(build.building);
    } else if (queenAction instanceof Wait) {
      // nothing to do ...
    }
  }

  private void processEndOfTurnUpdates(Env env) {
    // working time reduce of 1.
    env.sites.stream()
        .filter(site -> site instanceof Barracks)
        .map(site -> (Barracks) site)
        .filter(barracks -> barracks.working > 0)
        .forEach(barracks -> barracks.working--);

    // available gold is reduced.
    env.sites.stream()
        .filter(site -> site instanceof Mine)
        .map(site -> (Mine) site)
        .forEach(Mine::collect);

    // tower melting
    env.sites.stream()
        .filter(site -> site instanceof Tower)
        .map(site -> (Tower) site)
        .forEach(Tower::melt);

    // Remove dead creeps

  }
}

class Action {
  QueenAction queen;
  TrainAction train;

  Action() {}

  void print() {
    queen.print();
    train.print();
  }
}

class TrainAction {
  List<Integer> trainIds;

  TrainAction() {
    trainIds = new ArrayList<>();
  }

  void print() {
    StringBuilder sb = new StringBuilder("TRAIN");
    for (Integer trainId : trainIds) sb.append(" ").append(trainId);
    System.out.println(sb.toString());
  }
}

abstract class QueenAction {
  abstract void print();
}

class Wait extends QueenAction {

  @Override
  void print() {
    System.out.println("WAIT");
  }
}

class Move extends QueenAction {
  Vector2D to;

  @Override
  void print() {
    System.out.println("MOVE " + to.getIntX() + " " + to.getIntY());
  }
}

class Build extends QueenAction {
  int id;
  Structure building;

  @Override
  void print() {
    System.out.println("BUILD " + id + " " + building.name);
  }
}

class Env implements Cloneable {
  Team friend;
  Team enemy;
  TreeSet<Site> sites;
  private Map map;

  Env() {
    map = new Map();
    friend = new Team();
    enemy = new Team();
    sites = new TreeSet<>();
  }

  @Override
  protected Env clone() {
    Env env = null;
    try {
      env = (Env) super.clone();
      env.friend = friend.clone();
      env.enemy = enemy.clone();
      env.sites = new TreeSet<>();
      for (Site site : sites) env.sites.add(site.clone());
      env.map = map.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return env;
  }

  Optional<Site> getSite(int id) {
    return sites.stream().filter(s -> s.id == id).findAny();
  }

  @Override
  public String toString() {
    return "Env{"
        + "map="
        + map
        + ", friend="
        + friend
        + ", enemy="
        + enemy
        + ", sites="
        + sites
        + '}';
  }

  void printDiff(Env other) {
    friend.printDiff(other.friend);
    // enemy.printDiff(other.enemy);
    Iterator<Site> lefts = sites.iterator();
    Iterator<Site> rights = other.sites.iterator();
    while (lefts.hasNext() || rights.hasNext()) {
      Site left = lefts.hasNext() ? lefts.next() : null;
      Site right = rights.hasNext() ? rights.next() : null;
      if (left == null || !left.equals(right)) {
        System.err.println("Site diff from : " + left + " to : " + right);
      }
    }
  }
}

class Map implements Cloneable {
  private Vector2D dimension;

  Map() {
    dimension = new Vector2D(1920, 1000);
  }

  @Override
  protected Map clone() throws CloneNotSupportedException {
    Map map = (Map) super.clone();
    map.dimension = dimension.clone();
    return map;
  }

  @Override
  public String toString() {
    return "Map{" + "dimension=" + dimension + '}';
  }
}

class Team implements Cloneable {
  int gold;
  List<Unit> units;

  Team() {
    this.gold = 100;
    this.units = new ArrayList<>();
  }

  @Override
  protected Team clone() throws CloneNotSupportedException {
    Team team = (Team) super.clone();
    team.gold = gold;
    team.units = new ArrayList<>();
    for (Unit unit : units) team.units.add(unit.clone());
    return team;
  }

  Queen getQeen() {
    return (Queen) this.units.stream().filter(u -> u instanceof Queen).findAny().get();
  }

  List<Creep> creeps() {
    return this.units.stream()
        .filter(unit -> unit instanceof Creep)
        .map(unit -> (Creep) unit)
        .collect(Collectors.toList());
  }

  void printDiff(Team other) {
    if (!this.equals(other)) {
      if (gold != other.gold) System.err.println("Team gold diff from " + gold + " to : " + gold);
      for (int i = 0; i < Math.max(units.size(), other.units.size()); i++) {
        Unit left = i < units.size() ? units.get(i) : null;
        Unit right = i < other.units.size() ? other.units.get(i) : null;
        if (left == null || !left.equals(right)) {
          System.err.println("Team unit diff from : " + left + " to : " + right);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "Team{" + "gold=" + gold + ", units=" + units + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Team team = (Team) o;
    return gold == team.gold && Objects.equals(units, team.units);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gold, units);
  }
}

abstract class FieldObject {
  Vector2D location;
  int radius;
  int mass;
}

abstract class Unit extends FieldObject implements Cloneable {
  int health;
  int speed;

  Unit() {}

  static Unit build(int x, int y, int unitType, int health) {
    Unit unit = null;
    if (unitType == 0) {
      unit = new Knight();
    } else if (unitType == 1) {
      unit = new Archer();
    } else if (unitType == 2) {
      unit = new Giant();
    } else if (unitType == -1) {
      unit = new Queen();
    } else {
      throw new IllegalArgumentException(" unit type invalid " + unitType);
    }

    unit.location = new Vector2D(x, y);
    unit.health = health;
    return unit;
  }

  @Override
  protected Unit clone() throws CloneNotSupportedException {
    Unit unit = (Unit) super.clone();
    unit.location = location.clone();
    unit.health = health;
    unit.speed = speed;
    unit.radius = radius;
    return unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Unit unit = (Unit) o;
    return health == unit.health
        && speed == unit.speed
        && radius == unit.radius
        && Objects.equals(location, unit.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, health, speed, radius);
  }
}

class Queen extends Unit {
  int touchedSite;

  public Queen() {
    super();
    this.radius = 30;
    this.health = 200;
    this.speed = 60;
    this.mass = 100;
  }

  @Override
  protected Queen clone() throws CloneNotSupportedException {
    Queen queen = (Queen) super.clone();
    queen.touchedSite = touchedSite;
    return queen;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Queen queen = (Queen) o;
    return touchedSite == queen.touchedSite;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), touchedSite);
  }

  @Override
  public String toString() {
    return "Queen{"
        + "location="
        + location
        + ", health="
        + health
        + ", speed="
        + speed
        + ", radius="
        + radius
        + ", touchedSite="
        + touchedSite
        + '}';
  }
}

abstract class Creep extends Unit {
  int range;

  Creep() {}

  @Override
  protected Creep clone() throws CloneNotSupportedException {
    Creep creep = (Creep) super.clone();
    creep.range = range;
    return creep;
  }

  abstract int damage(Unit unit);

  abstract int damage(Site site);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Creep creep = (Creep) o;
    return range == creep.range;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), range);
  }
}

class Knight extends Creep {

  Knight() {
    this.speed = 100;
    this.health = 25;
    this.radius = 20;
    this.range = 0;
    this.mass = 4;
  }

  @Override
  protected Knight clone() throws CloneNotSupportedException {
    return (Knight) super.clone();
  }

  @Override
  public String toString() {
    return "Knight{"
        + "location="
        + location
        + ", health="
        + health
        + ", speed="
        + speed
        + ", radius="
        + radius
        + ", range="
        + range
        + '}';
  }

  @Override
  int damage(Unit unit) {
    if (unit instanceof Queen) return 1;
    else return 0;
  }

  @Override
  int damage(Site site) {
    return 0;
  }
}

class Archer extends Creep {
  Archer() {
    this.speed = 75;
    this.health = 45;
    this.radius = 25;
    this.range = 200;
    this.mass = 9;
  }

  @Override
  protected Archer clone() throws CloneNotSupportedException {
    return (Archer) super.clone();
  }

  @Override
  int damage(Unit unit) {
    if (unit instanceof Queen) return 0;
    else if (unit instanceof Giant) return 10;
    else return 2;
  }

  @Override
  int damage(Site site) {
    return 0;
  }
}

class Giant extends Creep {

  public Giant() {
    this.speed = 50;
    this.health = 200;
    this.radius = 40;
    this.range = 0;
    this.mass = 20;
  }

  @Override
  protected Giant clone() throws CloneNotSupportedException {
    return (Giant) super.clone();
  }

  @Override
  int damage(Unit unit) {
    return 0;
  }

  @Override
  int damage(Site site) {
    if (site instanceof Tower) return 80;
    else return 0;
  }
}

abstract class Site extends FieldObject implements Cloneable, Comparable {
  int id;
  int gold;
  int maxMineSize;

  public Site() {
    mass = Integer.MAX_VALUE;
  }

  static Site build(
      int siteId,
      Vector2D location,
      int radius,
      int gold,
      int maxMineSize,
      int structureType,
      int owner,
      int param1,
      int param2) {
    Site site = null;
    if (structureType == 2) {
      // barrack
      if (param2 == 0) site = new KnightBarracks();
      else if (param2 == 1) site = new ArcherBarracks();
      else if (param2 == 2) site = new GiantBarracks();
      else {
        System.err.println("Wrong barrackType " + param2);
      }
      assert site != null;
      ((Barracks) site).working = param1;
    } else if (structureType == 1) {
      // tower
      site = new Tower();
      ((Tower) site).life = param1;
      ((Tower) site).range = param2;
    } else if (structureType == 0) {
      // tower
      site = new Mine();
      ((Mine) site).income = param1;
    } else {
      // empty
      site = new EmptySite();
    }

    site.id = siteId;
    site.location = location;
    site.radius = radius;
    site.gold = gold;
    site.maxMineSize = maxMineSize;
    if (site instanceof Structure) {
      ((Structure) site).isFriend = owner == 0;
    }

    return site;
  }

  @Override
  protected Site clone() throws CloneNotSupportedException {
    Site site = (Site) super.clone();
    site.id = id;
    site.location = location.clone();
    site.radius = radius;
    site.gold = gold;
    site.maxMineSize = maxMineSize;
    return site;
  }

  double area() {
    return Math.PI * radius * radius;
  }

  @Override
  public int compareTo(Object o) {
    if (o instanceof Site) return id - ((Site) o).id;
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Site site = (Site) o;
    return id == site.id
        && radius == site.radius
        // && gold == site.gold, can compare gold as sometimes it not yet discovered ...
        // && maxMineSize == site.maxMineSize
        && Objects.equals(location, site.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, location, radius, gold, maxMineSize);
  }
}

class EmptySite extends Site {

  public EmptySite() {}

  @Override
  protected EmptySite clone() throws CloneNotSupportedException {
    return (EmptySite) super.clone();
  }

  @Override
  public String toString() {
    return "EmptySite{"
        + "id="
        + id
        + ", location="
        + location
        + ", radius="
        + radius
        + ", gold="
        + gold
        + ", "
        + "maxMineSize="
        + maxMineSize
        + '}';
  }
}

abstract class Structure extends Site {
  boolean isFriend;
  String name;

  public Structure() {}

  @Override
  protected Structure clone() throws CloneNotSupportedException {
    Structure structure = (Structure) super.clone();
    structure.isFriend = isFriend;
    structure.name = name;
    return structure;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Structure structure = (Structure) o;
    return isFriend == structure.isFriend && Objects.equals(name, structure.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isFriend, name);
  }
}

class Mine extends Structure {
  int income;

  public Mine() {
    super();
    name = "MINE";
    income = 1;
  }

  @Override
  protected Mine clone() {
    Mine mine = null;
    try {
      mine = (Mine) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    mine.income = income;
    return mine;
  }

  void build() {
    income++;
  }

  int collect() {
    int collect = 0;
    collect = Math.min(income, gold);
    gold -= collect;
    return collect;
  }

  @Override
  public String toString() {
    return "Mine{"
        + "id="
        + id
        + ", location="
        + location
        + ", radius="
        + radius
        + ", gold="
        + gold
        + ", maxMineSize"
        + "="
        + maxMineSize
        + ", isFriend="
        + isFriend
        + ", income="
        + income
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Mine mine = (Mine) o;
    return income == mine.income;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), income);
  }
}

class Tower extends Structure {

  static final int TOWER_HP_INITIAL = 200;
  static final int TOWER_HP_INCREMENT = 100;
  static final int TOWER_HP_MAXIMUM = 800;
  static final int TOWER_CREEP_DAMAGE_MIN = 3;
  static final int TOWER_CREEP_DAMAGE_CLIMB_DISTANCE = 200;
  static final int TOWER_QUEEN_DAMAGE_MIN = 1;
  static final int TOWER_QUEEN_DAMAGE_CLIMB_DISTANCE = 200;
  static final int TOWER_MELT_RATE = 4;
  static final int TOWER_COVERAGE_PER_HP = 1000;

  int life;
  int range;

  public Tower() {
    super();
    name = "TOWER";
    life = TOWER_HP_INITIAL;
    range = computeRange(this, life);
  }

  public static int computeRange(Tower tower, int hp) {
    return (int) Math.sqrt((tower.life * 1000 + tower.area()) / Math.PI);
  }

  void build() {
    if (this.life <= 0) this.life += Tower.TOWER_HP_INITIAL;
    else this.life += Tower.TOWER_HP_INCREMENT;
    this.life = Math.min(this.life, Tower.TOWER_HP_MAXIMUM);
    this.range = computeRange(this, life);
  }

  void melt() {
    this.life -= Tower.TOWER_MELT_RATE;
    this.range = computeRange(this, life);
  }

  @Override
  protected Tower clone() {
    Tower tower = null;
    try {
      tower = (Tower) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    tower.life = life;
    tower.range = range;
    return tower;
  }

  @Override
  public String toString() {
    return "Tower{"
        + "id="
        + id
        + ", location="
        + location
        + ", radius="
        + radius
        + ", gold="
        + gold
        + ", "
        + "maxMineSize="
        + maxMineSize
        + ", isFriend="
        + isFriend
        + ", life="
        + life
        + ", range="
        + range
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Tower tower = (Tower) o;
    return life == tower.life && range == tower.range;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), life, range);
  }
}

abstract class Barracks extends Structure {

  int working;
  int trainCost;
  int nbUnitTrained;
  int trainingTime;

  public Barracks() {}

  @Override
  protected Barracks clone() throws CloneNotSupportedException {
    Barracks barracks = (Barracks) super.clone();
    barracks.working = working;
    barracks.trainCost = trainCost;
    barracks.nbUnitTrained = nbUnitTrained;
    barracks.trainingTime = trainingTime;
    return barracks;
  }

  boolean available() {
    return working <= 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Barracks barracks = (Barracks) o;
    return working == barracks.working
        && trainCost == barracks.trainCost
        && nbUnitTrained == barracks.nbUnitTrained
        && trainingTime == barracks.trainingTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), working, trainCost, nbUnitTrained, trainingTime);
  }

  @Override
  public String toString() {
    return "Barracks{"
        + "id="
        + id
        + ", location="
        + location
        + ", radius="
        + radius
        + ", gold="
        + gold
        + ", "
        + "maxMineSize="
        + maxMineSize
        + ", isFriend="
        + isFriend
        + ", name='"
        + name
        + '\''
        + ", working="
        + working
        + ", trainCost="
        + trainCost
        + ", nbUnitTrained="
        + nbUnitTrained
        + ", trainingTime="
        + trainingTime
        + '}';
  }
}

class KnightBarracks extends Barracks {

  KnightBarracks() {
    super();
    this.trainCost = 80;
    this.nbUnitTrained = 4;
    this.trainingTime = 5;
    this.name = "BARRACKS-KNIGHT";
  }

  @Override
  protected KnightBarracks clone() throws CloneNotSupportedException {
    return (KnightBarracks) super.clone();
  }
}

class ArcherBarracks extends Barracks {

  ArcherBarracks() {
    super();
    this.trainCost = 100;
    this.nbUnitTrained = 2;
    this.trainingTime = 8;
    this.name = "BARRACKS-ARCHER";
  }

  @Override
  protected ArcherBarracks clone() throws CloneNotSupportedException {
    return (ArcherBarracks) super.clone();
  }

  @Override
  public String toString() {
    return "ArcherBarracks{"
        + "id="
        + id
        + ", location="
        + location
        + ", radius="
        + radius
        + ", isFriend="
        + isFriend
        + ", trainCost="
        + trainCost
        + ", nbUnitTrained="
        + nbUnitTrained
        + ", trainingTime="
        + trainingTime
        + '}';
  }
}

class GiantBarracks extends Barracks {

  public GiantBarracks() {
    super();
    this.trainCost = 140;
    this.nbUnitTrained = 1;
    this.trainingTime = 10;
    this.name = "BARRACKS-GIANT";
  }

  @Override
  protected GiantBarracks clone() throws CloneNotSupportedException {
    return (GiantBarracks) super.clone();
  }
}

class Vector2D {

  double x;
  double y;

  public Vector2D() {}

  Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Vector2D(Vector2D v) {
    this.x = v.x;
    this.y = v.y;
  }

  private static Vector2D toCartesian(double magnitude, double angle) {
    return new Vector2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
  }

  public static Vector2D add(Vector2D v1, Vector2D v2) {
    return new Vector2D(v1.x + v2.x, v1.y + v2.y);
  }

  public static Vector2D subtract(Vector2D v1, Vector2D v2) {
    return new Vector2D(v1.x - v2.x, v1.y - v2.y);
  }

  private static double dot(Vector2D v1, Vector2D v2) {
    return v1.x * v2.x + v1.y * v2.y;
  }

  public static double cross(Vector2D v1, Vector2D v2) {
    return (v1.x * v2.y - v1.y * v2.x);
  }

  public static double project(Vector2D v1, Vector2D v2) {
    return (dot(v1, v2) / v1.getLength());
  }

  public static Vector2D getProjectedVector(Vector2D v1, Vector2D v2) {
    return v1.getNormalized().getMultiplied(Vector2D.dot(v1, v2) / v1.getLength());
  }

  public int getIntX() {
    Double x = this.x;
    return x.intValue();
  }

  public int getIntY() {
    Double y = this.y;
    return y.intValue();
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  private void set(Vector2D v) {
    this.x = v.x;
    this.y = v.y;
  }

  public void setZero() {
    x = 0;
    y = 0;
  }

  public double[] getComponents() {
    return new double[] {x, y};
  }

  public double getLength() {
    return Math.sqrt(x * x + y * y);
  }

  public double getLengthSq() {
    return (x * x + y * y);
  }

  public double distanceSq(double vx, double vy) {
    vx -= x;
    vy -= y;
    return (vx * vx + vy * vy);
  }

  public double distanceSq(Vector2D v) {
    double vx = v.x - this.x;
    double vy = v.y - this.y;
    return (vx * vx + vy * vy);
  }

  public double distance(double vx, double vy) {
    vx -= x;
    vy -= y;
    return Math.sqrt(vx * vx + vy * vy);
  }

  public double distance(Vector2D v) {
    double vx = v.x - this.x;
    double vy = v.y - this.y;
    return Math.sqrt(vx * vx + vy * vy);
  }

  public double getAngle() {
    return Math.atan2(y, x);
  }

  public void normalize() {
    double magnitude = getLength();
    x /= magnitude;
    y /= magnitude;
  }

  private Vector2D getNormalized() {
    double magnitude = getLength();
    return new Vector2D(x / magnitude, y / magnitude);
  }

  public void add(Vector2D v) {
    this.x += v.x;
    this.y += v.y;
  }

  public void add(double vx, double vy) {
    this.x += vx;
    this.y += vy;
  }

  public Vector2D getAdded(Vector2D v) {
    return new Vector2D(this.x + v.x, this.y + v.y);
  }

  public void subtract(Vector2D v) {
    this.x -= v.x;
    this.y -= v.y;
  }

  public void subtract(double vx, double vy) {
    this.x -= vx;
    this.y -= vy;
  }

  public Vector2D getSubtracted(Vector2D v) {
    return new Vector2D(this.x - v.x, this.y - v.y);
  }

  public void multiply(double scalar) {
    x *= scalar;
    y *= scalar;
  }

  private Vector2D getMultiplied(double scalar) {
    return new Vector2D(x * scalar, y * scalar);
  }

  public void divide(double scalar) {
    x /= scalar;
    y /= scalar;
  }

  public Vector2D getDivided(double scalar) {
    return new Vector2D(x / scalar, y / scalar);
  }

  public Vector2D getPerp() {
    return new Vector2D(-y, x);
  }

  private double dot(Vector2D v) {
    return (this.x * v.x + this.y * v.y);
  }

  private double dot(double vx, double vy) {
    return (this.x * vx + this.y * vy);
  }

  public double cross(Vector2D v) {
    return (this.x * v.y - this.y * v.x);
  }

  public double cross(double vx, double vy) {
    return (this.x * vy - this.y * vx);
  }

  public double project(Vector2D v) {
    return (this.dot(v) / this.getLength());
  }

  public double project(double vx, double vy) {
    return (this.dot(vx, vy) / this.getLength());
  }

  public Vector2D getProjectedVector(Vector2D v) {
    return this.getNormalized().getMultiplied(this.dot(v) / this.getLength());
  }

  public Vector2D getProjectedVector(double vx, double vy) {
    return this.getNormalized().getMultiplied(this.dot(vx, vy) / this.getLength());
  }

  public void rotateBy(double angle) {
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    double rx = x * cos - y * sin;
    y = x * sin + y * cos;
    x = rx;
  }

  public Vector2D getRotatedBy(double angle) {
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    return new Vector2D(x * cos - y * sin, x * sin + y * cos);
  }

  public Vector2D towards(Vector2D target, double max) {
    if (this.distance(target) < max) return target;
    else {
      return Vector2D.add(this, Vector2D.subtract(target, this).resize(max));
    }
  }

  public Vector2D resize(double newSize) {
    return this.getNormalized().getMultiplied(newSize);
  }

  public void rotateTo(double angle) {
    set(toCartesian(getLength(), angle));
  }

  public Vector2D getRotatedTo(double angle) {
    return toCartesian(getLength(), angle);
  }

  public void reverse() {
    x = -x;
    y = -y;
  }

  public Vector2D getReversed() {
    return new Vector2D(-x, -y);
  }

  @Override
  public Vector2D clone() {
    return new Vector2D(x, y);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Vector2D) {
      Vector2D v = (Vector2D) obj;
      return (x == v.x) && (y == v.y);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Vector2d[" + x + ", " + y + "]";
  }
}
