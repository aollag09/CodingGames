import java.util.*;

class Logger {

    public static void error(String flag, String message) {
        System.err.println(flag + " >> " + message);
    }
}

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int numberMineSpots = in.nextInt();
        for (int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
        }

        // game loop
        while (true) {

            int gold = in.nextInt();
            int income = in.nextInt();
            Team friend = new Team(gold, income);

            int opponentGold = in.nextInt();
            int opponentIncome = in.nextInt();
            Team enemy = new Team(opponentGold, opponentIncome);

            Map map = new Map();
            for (int i = 0; i < 12; i++) {
                String line = in.next();
                map.init(i, line);
            }

            int buildingCount = in.nextInt();
            for (int i = 0; i < buildingCount; i++) {
                int owner = in.nextInt();
                int buildingType = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                Building building = new Building(x, y, buildingType);
                if (owner == Team.OWNED) friend.buildings.add(building);
                else enemy.buildings.add(building);
            }
            int unitCount = in.nextInt();
            for (int i = 0; i < unitCount; i++) {
                int owner = in.nextInt();
                int unitId = in.nextInt();
                int level = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                Unit unit = new Unit(x, y, unitId, level);
                if (owner == Team.OWNED) friend.units.add(unit);
                else enemy.units.add(unit);
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("WAIT");
        }
    }
}

class Map {
    static final int SIZE = 12;
    static final String VOID = "#";
    static final String NEUTRAL = ".";
    static final String OWNED_ACTIVE = "O";
    static final String OWNED_INACTIVE = "o";
    static final String OPPONENT_ACTIVE = "X";
    static final String OPPONENT_INACTIVE = "x";

    String[][] map = new String[SIZE][SIZE];

    void init(int i, String line) {
        Logger.error("MAP", line);
        for (int j = 0; j < line.length(); j++)
            map[i][j] = String.valueOf(line.charAt(j));
    }

}

class Team {
    static final int OWNED = 0;
    static final int ENEMY = 1;

    int gold;
    int income;
    List<Unit> units = new ArrayList<Unit>();
    List<Building> buildings = new ArrayList<Building>();

    public Team(int gold, int income) {
        this.gold = gold;
        this.income = income;
    }
}

class Unit {
    final int x, y, id, level;

    public Unit(int x, int y, int id, int level) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.level = level;
    }
}

class Building {
    static final int HQ = 0;

    final int x, y, type;

    public Building(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}

class Action {

    static final String WAIT = "WAIT";
    static final String MOVE = "MOVE";
    static final String TRAIN = "TRAIN";

    final String action, label;
    final int x, y;

    public Action(String action, String label, int x, int y) {
        this.action = action;
        this.label = label;
        this.x = x;
        this.y = y;
    }

    public String toAction() {
        if (action.equals(WAIT))
            return action;
        else
            return action + " " + label + " " + x + y;
    }
}
