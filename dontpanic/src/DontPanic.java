import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Env env = new Env();

        int nbFloors = in.nextInt(); // number of floors
        int width = in.nextInt(); // width of the area
        env.map.dimension = new Vector2D(width, nbFloors);
        env.nbRounds = in.nextInt(); // maximum number of rounds
        int exitFloor = in.nextInt(); // floor on which the exit is found
        int exitPos = in.nextInt(); // position of the exit on its floor
        env.map.exit = new Vector2D(exitPos, exitFloor);
        env.nbTotalClones = in.nextInt(); // number of generated clones
        env.nbAdditionalElevators = in.nextInt(); // number of additional elevators that you can build

        int nbElevators = in.nextInt(); // number of elevators
        for (int i = 0; i < nbElevators; i++) {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor
            env.map.elevators.add(new Vector2D(elevatorPos, elevatorFloor));
        }

        System.err.println(env.toString());
        // game loop
        while (true) {
            Clone clone = new Clone();
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            clone.pos = new Vector2D(clonePos, cloneFloor);
            clone.action = Action.fromString(in.next()); // direction of the leading clone: LEFT or RIGHT
            env.leader = clone;
            if (env.round == 0) {
                env.map.generator = new Vector2D(clone.pos);
            }
            if (cloneFloor == -1 && clonePos == -1 && clone.action == Action.NONE) {
                System.out.println("WAIT"); // no leading clone wait for a new one
                continue;
            }

            Action next = Solver.solve(env);
            System.out.println(next.toString());

            // update env
            if (next.equals(Action.ELEVATOR)) {
                env.map.elevators.add(new Vector2D(env.leader.pos));
                env.nbAdditionalElevators--;
            }
            env.round++;
        }
    }

    enum Action {
        LEFT, RIGHT, NONE, WAIT, BLOCK, ELEVATOR;

        static Action fromString(String s) {
            if (s.equals("LEFT"))
                return LEFT;
            else if (s.equals("RIGHT"))
                return RIGHT;
            else if (s.equals("NONE")) {
                return NONE;
            } else if (s.equals("WAIT")) {
                return WAIT;
            } else if (s.equals("BLOCK")) {
                return BLOCK;
            } else if (s.equals("ELEVATOR")) {
                return ELEVATOR;
            } else
                return null;
        }
    }

    static class Vector2D {

        public double x;
        public double y;

        public Vector2D() {
        }

        public Vector2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vector2D(Vector2D v) {
            set(v);
        }

        public static Vector2D toCartesian(double magnitude, double angle) {
            return new Vector2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
        }

        public static Vector2D add(Vector2D v1, Vector2D v2) {
            return new Vector2D(v1.x + v2.x, v1.y + v2.y);
        }

        public static Vector2D subtract(Vector2D v1, Vector2D v2) {
            return new Vector2D(v1.x - v2.x, v1.y - v2.y);
        }

        public static double dot(Vector2D v1, Vector2D v2) {
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

        public void set(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void set(Vector2D v) {
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

        public Vector2D getNormalized() {
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

        public Vector2D getMultiplied(double scalar) {
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

        public double dot(Vector2D v) {
            return (this.x * v.x + this.y * v.y);
        }

        public double dot(double vx, double vy) {
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

    static class Solver {

        public static Action solve(final Env env) {
            Vector2D target = getTarget(env);
            return getAction(env, target);
        }

        private static Action getAction(final Env env, final Vector2D target) {
            Action nextMove = Action.WAIT;
            if (target != null) {
                System.err.println("Target : " + target);
                // block if going wrong direction
                if (Math.abs(target.x - env.leader.pos.x) > 10 && env.nbAdditionalElevators > 0) {
                    nextMove = Action.ELEVATOR;
                } else if (env.leader.pos.x < target.x && env.leader.action == Action.LEFT) {
                    nextMove = Action.BLOCK;
                } else if (env.leader.pos.x > target.x && env.leader.action == Action.RIGHT) {
                    nextMove = Action.BLOCK;
                } else if (env.leader.pos.x == target.x) {
                    nextMove = Action.WAIT;
                }
            } else {
                if (env.nbAdditionalElevators > 0) {
                    // no target : build elevator
                    nextMove = Action.ELEVATOR;
                }
            }
            return nextMove;
        }

        private static Vector2D getTarget(final Env env) {
            Vector2D target = null;
            if (env.leader.pos.y == env.map.exit.y) {
                // target is exit
                target = env.map.exit;
            } else {
                // target is next elevator
                Optional<Vector2D> nextElevator = env.map.nextElevator(env.leader);
                if (nextElevator.isPresent()) {
                    target = nextElevator.get();
                }
            }
            return target;
        }
    }

    static class Env {
        Clone leader;
        List<Clone> dead;
        int nbRounds;
        int nbTotalClones;
        int nbAdditionalElevators;
        int round = 0;
        Map map;

        public Env() {
            map = new Map();
            dead = new ArrayList<>();
        }

        public boolean hasNewClone() {
            return round % 3 == 0;
        }

        @Override
        public String toString() {
            return "Env{" + "leader=" + leader + ", dead=" + dead + ", nbRounds=" + nbRounds + ", nbTotalClones=" + nbTotalClones + ", nbAdditionalElevators=" + nbAdditionalElevators + ", round=" + round + ", map=" + map + '}';
        }
    }

    static class Map {
        Vector2D dimension;
        Vector2D exit;
        List<Vector2D> elevators;
        Vector2D generator;

        public Map() {
            elevators = new ArrayList<>();
        }

        public Optional<Vector2D> nextElevator(Clone clone) {
            // look for the best quickest next elevator
            return elevators.stream().filter(elevator -> {
                return elevator.y == clone.pos.y;
            }).min(Comparator.comparing(v -> {
                double value = Math.abs(v.x - clone.pos.x);
                if (clone.pos.x < v.x && clone.action == Action.LEFT) {
                    value += 2;
                } else if (clone.pos.x > v.x && clone.action == Action.RIGHT) {
                    value += 2;
                }
                System.err.println(v.toString() + " val : " + value);
                return value;
            }));
        }

        @Override
        public String toString() {
            return "Map{" + "dimension=" + dimension + ", exit=" + exit + ", elevators=" + elevators + ", generator=" + generator + '}';
        }
    }

    static class Clone {
        Vector2D pos;
        Action action;

        public Clone() {
        }
    }
}
