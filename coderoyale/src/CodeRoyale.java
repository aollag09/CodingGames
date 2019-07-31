import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

interface Solver {

    void solve(Env e);
}

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
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

        // game loop
        while (true) {
            env.friend.gold = in.nextInt();
            int touchedSite = in.nextInt();

            // update sites
            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                if (env.getSite(siteId).isPresent()) {
                    Site previous = env.getSite(siteId).get();
                    int gold = in.nextInt(); // The total number of gold remaining to be mined from this site (-1 if unknown)
                    int maxMineSize = in.nextInt(); // The maximum rate that a mine can extract gold from this site (-1 if unknown)
                    int structureType = in.nextInt(); // -1 = No structure, 2 = Barracks
                    int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
                    int param1 = in.nextInt();
                    int param2 = in.nextInt();
                    env.sites.remove(previous);
                    env.sites.add(Site.build(siteId, previous.location, previous.radius, gold, maxMineSize, structureType, owner, param1, param2));
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
                if (owner == 0)
                    env.friend.units.add(unit);
                else
                    env.enemy.units.add(unit);
            }
            env.friend.getQeen().touchedSite = touchedSite;

            // Write an action using System.out.println()
            System.err.println(env.toString());

            Solver solver = new StupidSolver();
            solver.solve(env);
            // First line: A valid queen action
            // Second line: A set of training instructions
        }
    }


}

class StupidSolver implements Solver {

    @Override
    public void solve(Env e) {
        queenAction(e);
        trainAction(e);
    }

    private void queenAction(Env e) {
        boolean nextStrategy;
        nextStrategy = build(e);
        if (nextStrategy) {
            nextStrategy = moveToNextSite(e);
            if (nextStrategy) {
                nothing();
            }
        }
    }

    private boolean moveToNextSite(Env e) {
        boolean nextStrategy = true;

        Vector2D queenPos = e.friend.getQeen().position;
        Optional<Site> next = e.sites.stream().filter(s -> s instanceof EmptySite).min(Comparator.comparing(site -> site.location.distance(queenPos)));

        if (next.isPresent()) {
            // move to next empty place
            System.out.println("MOVE " + next.get().location.getIntX() + " " + next.get().location.getIntY());
            nextStrategy = false;
        } else {
            // move to smallest tower
            next = e.sites.stream().filter(s -> s instanceof Tower).filter(s -> ((Structure) s).isFriend).min(Comparator.comparing(s -> ((Tower) s).life));
            if (next.isPresent()) {
                System.out.println("MOVE " + next.get().location.getIntX() + " " + next.get().location.getIntY());
                nextStrategy = false;
            }
        }

        return nextStrategy;
    }

    public void trainAction(Env e) {

        List<Integer> trainSites = new ArrayList<>();
        long nbKnights = e.friend.units.stream().filter(unit -> unit instanceof Knight).count();
        long nbArcher = e.friend.units.stream().filter(unit -> unit instanceof Archer).count();

        if (nbKnights == 0 || nbArcher > 2) {
            // Train knights
            if (e.friend.gold > KnightBarracks.trainCost) {
                Optional<Site> knightBarrack = e.sites.stream().filter(s -> s instanceof KnightBarracks).filter(s -> ((Structure) s).isFriend).filter(s -> ((Barracks) s).available()).min(Comparator.comparing(site -> site.location.distance(e.enemy.getQeen().position)));
                knightBarrack.ifPresent(site -> {
                    trainSites.add(site.id);
                    e.friend.gold -= KnightBarracks.trainCost;
                });

            }
        } else {
            // Train archers
            if (e.friend.gold > ArcherBarracks.trainCost) {
                Optional<Site> archerBarrack = e.sites.stream().filter(s -> s instanceof ArcherBarracks).filter(s -> ((Structure) s).isFriend).filter(s -> ((Barracks) s).available()).min(Comparator.comparing(site -> site.location.distance(e.friend.getQeen().position)));
                archerBarrack.ifPresent(site -> {
                    trainSites.add(site.id);
                    e.friend.gold -= ArcherBarracks.trainCost;
                });

            }
        }

        StringBuilder s = new StringBuilder("TRAIN");
        for (Integer id : trainSites) {
            s.append(" ").append(id);
        }
        System.out.println(s);
    }

    private void nothing() {
        System.out.println("WAIT");
    }

    private boolean build(Env e) {
        boolean nextStrategy = true;
        int siteId = e.friend.getQeen().touchedSite;
        if (siteId != -1) {
            if (e.getSite(siteId).isPresent()) {
                Site site = e.getSite(siteId).get();

                long nbKnightBarracks = e.sites.stream().filter(s -> s instanceof KnightBarracks).filter(s -> ((Structure) s).isFriend).count();
                long nbArcherBarracks = e.sites.stream().filter(s -> s instanceof ArcherBarracks).filter(s -> ((Structure) s).isFriend).count();
                long totalIncome = e.sites.stream().filter(s -> s instanceof Mine).filter(s -> ((Structure) s).isFriend).mapToInt(s -> ((Mine) s).income).sum();

                String type = "";
                if (site instanceof EmptySite || (site instanceof Structure && !((Structure) site).isFriend)) {

                    if (totalIncome < 5 && site.maxMineSize > 2) {
                        type += "MINE";
                    } else if (nbKnightBarracks < 1) {
                        type += "BARRACKS-KNIGHT";
                    } else if (nbArcherBarracks < 1) {
                        type += "BARRACKS-ARCHER";
                    } else
                        type += "TOWER";
                }

                if (totalIncome < 5 && site instanceof Mine && ((Mine) site).income < ((Mine) site).maxMineSize) {
                    type += "MINE";
                }
                if (site instanceof Tower && ((Tower) site).life < 500) {
                    type += "TOWER";
                }

                if (!type.isEmpty()) {
                    System.out.println("BUILD " + siteId + " " + type);
                    nextStrategy = false;
                }
            }
        }
        return nextStrategy;
    }
}

class Env {
    Team friend;
    Team enemy;
    Set<Site> sites;
    private Map map;

    Env() {
        map = new Map();
        friend = new Team();
        enemy = new Team();
        sites = new HashSet<>();
    }

    Optional<Site> getSite(int id) {
        return sites.stream().filter(s -> s.id == id).findAny();
    }


    @Override
    public String toString() {
        return "Env{" + "map=" + map + ", friend=" + friend + ", enemy=" + enemy + ", sites=" + sites + '}';
    }
}

class Map {
    private Vector2D dimension;

    Map() {
        dimension = new Vector2D(1920, 1000);
    }
}

class Team {
    int gold;
    List<Unit> units;

    Team() {
        this.gold = 100;
        this.units = new ArrayList<>();
    }

    Queen getQeen() {
        return (Queen) this.units.stream().filter(u -> u instanceof Queen).findAny().get();
    }

    @Override
    public String toString() {
        return "Team{" + "gold=" + gold + ", units=" + units + '}';
    }
}

abstract class Unit {
    Vector2D position;
    int health;
    int speed;

    Unit() {
    }

    static Unit build(int x, int y, int unitType, int health) {
        Unit unit = null;
        if (unitType == 0) {
            unit = new Knight();
        } else if (unitType == 1) {
            unit = new Archer();
        } else {
            unit = new Queen();
        }

        unit.position = new Vector2D(x, y);
        unit.health = health;
        return unit;
    }

}

class Queen extends Unit {
    int touchedSite;
    private int radius;

    public Queen() {
        super();
        this.radius = 30;
        this.health = 100;
        this.speed = 60;
    }

    boolean hasTouchedSite() {
        return touchedSite != -1;
    }

    @Override
    public String toString() {
        return "Queen{" + "position=" + position + ", health=" + health + ", speed=" + speed + ", radius=" + radius + ", touchedSite=" + touchedSite + '}';
    }
}

abstract class Creep extends Unit {
    boolean attackQueen;
}

class Knight extends Creep {
    Knight() {
        this.attackQueen = true;
        this.speed = Integer.MIN_VALUE; // TODO compute
    }

    @Override
    public String toString() {
        return "Knight{" + "position=" + position + ", health=" + health + ", speed=" + speed + ", attackQueen=" + attackQueen + '}';
    }
}

class Archer extends Creep {
    Archer() {
        this.attackQueen = false;
        this.speed = Integer.MIN_VALUE; // TODO compute
    }

    @Override
    public String toString() {
        return "Archer{" + "position=" + position + ", health=" + health + ", speed=" + speed + ", attackQueen=" + attackQueen + '}';
    }
}

abstract class Site {
    int id;
    Vector2D location;
    int radius;
    int gold;
    int maxMineSize;

    static Site build(int siteId, Vector2D location, int radius, int gold, int maxMineSize, int structureType, int owner, int param1, int param2) {
        Site site = null;
        if (structureType == 2) {
            // barrack
            if (param2 == 0)
                site = new KnightBarracks();
            else if (param2 == 1)
                site = new ArcherBarracks();
            else if (param2 == 2)
                site = new GiantBarracks();
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Site site = (Site) o;
        return id == site.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

class EmptySite extends Site {
    @Override
    public String toString() {
        return "EmptySite{" + "id=" + id + ", location=" + location + ", radius=" + radius + '}';
    }
}

abstract class Structure extends Site {
    boolean isFriend;
}

class Mine extends Structure {
    int income;
}

class Tower extends Structure {
    int life;
    int range;

    @Override
    public String toString() {
        return "Tower{" + "id=" + id + ", location=" + location + ", radius=" + radius + ", isFriend=" + isFriend + ", life=" + life + ", range=" + range + '}';
    }
}

abstract class Barracks extends Structure {

    int working;

    public boolean available() {
        return working <= 0;
    }

}

class KnightBarracks extends Barracks {

    static int trainCost = 80;
    static int nbUnitTrained = 4;
    static int trainingTime = Integer.MAX_VALUE;  // TODO compute

    KnightBarracks() {
        super();
    }

}

class ArcherBarracks extends Barracks {

    static int trainCost = 100;
    static int nbUnitTrained = 2;
    static int trainingTime = Integer.MAX_VALUE;  // TODO compute

    ArcherBarracks() {
        super();
    }

    @Override
    public String toString() {
        return "ArcherBarracks{" + "id=" + id + ", location=" + location + ", radius=" + radius + ", isFriend=" + isFriend + ", trainCost=" + trainCost + ", nbUnitTrained=" + nbUnitTrained + ", trainingTime=" + trainingTime + '}';
    }
}

class GiantBarracks extends Barracks {

    static int trainCost = 140;
    static int nbUnitTrained = 1;
    static int trainingTime = Integer.MAX_VALUE;  // TODO compute

}

class Vector2D {

    double x;
    double y;

    public Vector2D() {
    }

    Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v) {
        set(v);
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
        return new double[]{x, y};
    }

    private double getLength() {
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
