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

            InFPopulation population = new InFPopulation(friend, map);
            population.generer();

            IIndividu best = population.getIndividu(0);

            StringJoiner output = new StringJoiner(";");
            for (ICaracteristic car : best.getListCaracteristics()) {
                output.add(((InFCaracteristic) car).actions.get(0).toAction());
            }
            System.out.println(output.toString());
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

    List<Vector> getOwnedAreas() {
        List<Vector> owned = new ArrayList<Vector>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String caze = map[i][j];
                if (caze == OWNED_ACTIVE)
                    owned.add(new Vector(i, j));
            }
        }
        return owned;
    }

    List<Vector> getSpawnAreas(boolean onlyNewPlaces) {
        List<Vector> spawn = new ArrayList<Vector>();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                String caze = map[i][j];
                if (caze == VOID)
                    continue;
                if (!onlyNewPlaces && caze.equals(OWNED_ACTIVE)) {
                    spawn.add(new Vector(i, j));
                } else {
                    // can spawn here if neighboor is owned
                    boolean neighboor = false;
                    if (i > 0 && map[i - 1][j].equals(OWNED_ACTIVE))
                        neighboor = true;
                    if (j < 0 && map[i][j - 1].equals(OWNED_ACTIVE))
                        neighboor = true;
                    if (i < SIZE - 1 && map[i + 1][j].equals(OWNED_ACTIVE))
                        neighboor = true;
                    if (j < SIZE - 1 && map[i][j + 1].equals(OWNED_ACTIVE))
                        neighboor = true;

                    if (neighboor)
                        spawn.add(new Vector(i, j));
                }
            }
        }
        return spawn;
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

    public static int cost(int level) {
        if (level == 1)
            return 10;
        else
            throw new RuntimeException("wrong level " + level);
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
            return action + " " + label + " " + x + " " + y;
    }
}

class Vector {

    int x, y;

    public Vector(int i, int j) {
        x = i;
        y = j;
    }
}


///////////////////////////
/////// GENETIC ICE AND FIRE

class InFGenetic {
    final static int CARACTERISTIC_SIZE = 10;
    final static int NB_INDIVIDUS = 5;
}

/**
 * Caracteristic is the path of actions for a single unit
 */
class InFCaracteristic implements ICaracteristic {

    int id; // id of the unit
    List<Action> actions;

    public InFCaracteristic(int id, List<Action> actions) {
        this.id = id;
        this.actions = actions;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public BitSet getBitSet() {
        return null;
    }

    @Override
    public int getSize() {
        return actions.size();
    }

    @Override
    public void update() {

    }

    @Override
    public ICaracteristic clone() {
        return new InFCaracteristic(this.id, new ArrayList<>(this.actions));
    }
}

class InFIndividu extends Individu {

}

class InFPopulation extends Population {

    final static Random rnd = new Random(1234);

    Team team;
    Map map;

    public InFPopulation(Team iteam, Map imap) {
        team = iteam;
        map = imap;
    }

    @Override
    public void generer() {
        for (int i = 0; i < InFGenetic.NB_INDIVIDUS; i++) {
            this.addIndividu(generateIndividu(i));
        }
    }

    public Individu generateIndividu(int index) {
        InFIndividu individu = new InFIndividu();

        List<ICaracteristic> cars = new ArrayList<>();

        for (Unit unit : team.units) {
            cars.add(generateCaracteristic(unit, false));
        }
        for (Unit unit : createUnits()) {
            cars.add(generateCaracteristic(unit, true));
        }

        individu.setListCaracteristiques(cars);
        individu.setName("Individu nb " + index);
        return individu;
    }

    public List<Unit> createUnits() {

        List<Unit> units = new ArrayList<Unit>();

        int income = team.income;
        int army = team.units.size();
        int armySafety = 3;
        if (army - armySafety >= income) {
            return units;
        }

        int money = team.gold;
        int safety = 5;

        while (money > safety) {

            // randomly create new unit
            int level = rnd.nextInt(1) + 1;
            int cost = Unit.cost(level);
            if (money - safety >= cost) {

                // choose new place
                List<Vector> places = map.getSpawnAreas(true);
                Vector spawn = places.get(rnd.nextInt(places.size()));
                units.add(new Unit(spawn.x, spawn.y, 12345, level));
            }
            money -= cost;
        }

        return units;
    }

    public ICaracteristic generateCaracteristic(Unit unit, boolean isNew) {
        // on car per unit
        Action action = new Action(Action.MOVE, String.valueOf(unit.id), unit.x, unit.y);
        List<Action> actions = new ArrayList<>();

        if (isNew) {
            // new action is to spawn
            actions.add(new Action(Action.TRAIN, String.valueOf(unit.level), unit.x, unit.y));
        }

        while (actions.size() < InFGenetic.CARACTERISTIC_SIZE) {
            action = generateAction(action);
            actions.add(action);
        }

        return new InFCaracteristic(unit.id, actions);
    }

    private Action generateAction(Action previous) {

        String action = Action.MOVE;
        String label = previous.label;

        int x = previous.x;
        int y = previous.y;

        boolean valid = false;
        while (!valid) {
            x += rnd.nextInt(3) - 1;
            y += rnd.nextInt(3) - 1;

            valid = validPosition(x, y);
            valid &= x != previous.x || y != previous.y;
            if (!valid) {
                // retry
                x = previous.x;
                y = previous.y;
            }
        }

        return new Action(action, label, x, y);
    }

    private boolean validPosition(int x, int y) {
        if (x < 0 || y < 0)
            return false;
        if (x >= Map.SIZE || y >= Map.SIZE)
            return false;
        if (map.map[x][y].equals(Map.VOID))
            return false;
        return true;
    }

}


///////////////////////////
/////// GENETIC COMMONS

interface IDarwin {
    IPopulation solve();

    IEndingCondition getEndingCondition();

    void setEndingCondition(IEndingCondition condition);

    INaturalSelection getNaturalSelection();

    void setNaturalSelection(INaturalSelection newSelectionNaturelle);
}

interface ICaracteristic extends Cloneable {
    String getName();

    BitSet getBitSet();

    int getSize();

    void update();

    ICaracteristic clone();
}

interface IEndingCondition {
    boolean isSatisfied(IPopulation population);

    IEndingCondition nextConditionArret();

    IEnvironment nextEnvironnement();

    int getNbIeration();
}

interface ICrossOver {
    List<IIndividu> crossOver(IIndividu individu1, IIndividu individu2);
}

interface IEnvironment {
    double evaluate(IIndividu individu);

    boolean isValid(IIndividu individu);
}

interface IIndividu extends Cloneable {
    Integer getType();

    String getName();

    void setName(String name);

    List<ICaracteristic> getListCaracteristics();

    void setListCaracteristiques(List<ICaracteristic> caracs);

    ICaracteristic getCaracteristique(int index);

    int getNombreCaracteristiques();

    IIndividu clone();
}

interface IMutation {
    IIndividu muter(IIndividu individu);

    boolean mutationIndividuPossible(IIndividu individu);

    boolean mutationCaracteristiquePossible(ICaracteristic caracteristique);
}

interface IPopulation {
    int getTailleSouhaitee();

    int getTailleEffective();

    List<IIndividu> getListIndividus();

    void setListIndividus(List<IIndividu> individus);

    IIndividu getIndividu(int i);

    void addIndividu(IIndividu individu);

    IEnvironment getEnvironnement();

    void setEnvironnement(IEnvironment environnement);

    double evaluerIndividu(IIndividu individu);

    double evaluerIndividu(int index);

    double evaluerPopulation();

    IIndividu getBestIndividu();

    void generer();
}

interface ISelection {
    List<IIndividu> select(IPopulation population);

    boolean selectionPossible(IPopulation population);
}

interface INaturalSelection {
    ISelection getSelectionInitiale();

    ISelection getSelectionFinale();

    IMutation getMutation();

    ICrossOver getCrossOver();

    IPopulation getPopulation();

    void nextGeneration();
}

class Darwin implements IDarwin {
    protected INaturalSelection selectionNaturelle;
    protected boolean printStartingInfos,
            printEndingInfos,
            printChaqueGeneration,
            printChaqueScore,
            printIterations;
    protected IEndingCondition conditionArret;

    public Darwin(INaturalSelection selectionNaturelle, IEndingCondition conditionArret) {
        this.selectionNaturelle = selectionNaturelle;
        this.setEndingCondition(conditionArret);
        this.printStartingInfos = false;
        this.printEndingInfos = true;
        this.printChaqueGeneration(false);
        this.printChaqueScore(true);
        this.printIterations(true);
    }

    public Darwin() {
        this.printChaqueGeneration(false);
        this.printChaqueScore(true);
        this.printIterations(true);
    }

    @Override
    public IEndingCondition getEndingCondition() {
        return this.conditionArret;
    }

    @Override
    public void setEndingCondition(IEndingCondition condition) {
        this.conditionArret = condition;
    }

    @Override
    public INaturalSelection getNaturalSelection() {
        return this.selectionNaturelle;
    }

    @Override
    public void setNaturalSelection(INaturalSelection newSelectionNaturelle) {
        this.selectionNaturelle = newSelectionNaturelle;
    }

    @Override
    public IPopulation solve() {/* Affichage de la population initiale */
        if (printStartingInfos) {
            System.err.println("-----------------------------------------------------");
            System.err.println("*****     DARWIN Genetic Solver version 1.0     *****");
            System.err.println("*****             By Momo and Dim'              *****");
            System.err.println("*****          Left-Handed power team           *****");
            System.err.println("-----------------------------------------------------" + "\n" + "\n");
            System.err.println("-----------------------------------------------------");
            System.err.println("*****          Population initiale :            *****");
            System.err.println("-----------------------------------------------------" + "\n");
            System.err.println(
                    this.getNaturalSelection().getPopulation()
                            + "\n"
                            + "Score : "
                            + this.getNaturalSelection().getPopulation().evaluerPopulation()
                            + "\n"
                            + "\n");
            System.err.println("-----------------------------------------------------");
            System.err.println("*****   Demarrage de l'algorithme génétique     *****");
            System.err.println("-----------------------------------------------------" + "\n");
        }
        int iterations = 0;
        while (this.getEndingCondition() != null) {/* Nouvelle g�n�ration */
            this.getNaturalSelection().nextGeneration();/* Verification de la condition d'arr�t */
            if (this.getEndingCondition().isSatisfied(this.getNaturalSelection().getPopulation())) {/* Application du nouvel environnement si besoin */
                if (this.getEndingCondition().nextEnvironnement() != null) {
                    this.getNaturalSelection()
                            .getPopulation()
                            .setEnvironnement(this.getEndingCondition().nextEnvironnement());
                }
                iterations = this.getEndingCondition().getNbIeration();
                this.setEndingCondition(this.getEndingCondition().nextConditionArret());
            }/* Affichages */
            if (this.printChaqueGeneration) {
                System.err.println(this.getNaturalSelection().getPopulation());
            }
            if (this.printChaqueScore) {
                System.err.println(
                        "Score : " + this.getNaturalSelection().getPopulation().evaluerPopulation());
            }
        }
        IPopulation popFinale = this.getNaturalSelection().getPopulation();
        if (printEndingInfos) {
            System.err.println("-----------------------------------------------------");
            System.err.println("*****           Resolution termin�e             *****");
            System.err.println("*****           Population finale :             *****");
            System.err.println("-----------------------------------------------------" + "\n");
            IIndividu best = popFinale.getBestIndividu();
            System.err.println(" Best Individu : " + best.toString());
            System.err.println("\n" + "Score : " + popFinale.evaluerIndividu(best));
            System.err.println("Apr�s : " + iterations + " g�n�rations");
        }
        return popFinale;
    }

    public void printChaqueGeneration(boolean b) {
        this.printChaqueGeneration = b;
    }

    public void printChaqueScore(boolean b) {
        this.printChaqueScore = b;
    }

    public void printIterations(boolean b) {
        this.printIterations = b;
    }
}

class NaturalSelectionSimple extends NaturalSelection {
    protected int nombreCouples;
    protected double probImmigration;

    public NaturalSelectionSimple(
            IPopulation pop,
            int nbIndividusSelInit,
            int nbCaracCross,
            double pCross,
            int nbBitMut,
            double pMut,
            int nbCouples,
            double probIm) {
        this(
                new SelectionTournoi(nbIndividusSelInit),
                new SelectionElitiste(pop.getTailleSouhaitee()),
                new CrossOverSimple(nbCaracCross, pCross),
                new MutationSimple(nbBitMut, pMut),
                pop,
                nbCouples,
                probIm);
    }

    public NaturalSelectionSimple(
            ISelection selInit,
            ISelection selFin,
            ICrossOver cross,
            IMutation mut,
            IPopulation pop,
            int nbCouples,
            double probImmigration) {
        super(selInit, selFin, cross, mut, pop);
        if (nbCouples < 0) {
            System.err.println("Nombre de couples indiqu� < 0");
        } else {
            this.nombreCouples = nbCouples;
        }
        if (probImmigration < 0 || probImmigration > 1) {
            System.err.println("Probabilit� d'immigration non comprise entre 0 et 1");
        } else {
            this.probImmigration = probImmigration;
        }
    }

    public int getNombreCouples() {
        return nombreCouples;
    }

    @Override
    public void nextGeneration() {/* IMMIGRATION */
        boolean doImmigration = false;
        if (this.probImmigration == 1) {
            doImmigration = true;
        } else {
            double d = Math.random();
            if (d < probImmigration) {
                doImmigration = true;
            }
        }
        if (doImmigration) {
            List<IIndividu> actuels = new ArrayList<IIndividu>();
            for (IIndividu i : this.getPopulation().getListIndividus()) {
                actuels.add(i.clone());
            }
            this.getPopulation().generer();
            for (IIndividu i : this.getPopulation().getListIndividus()) {
                i.setName(i.getName() + "-Immigr�");
            }
            for (IIndividu i : actuels) {
                this.getPopulation().addIndividu(i);
            }
        }/* SELECTION */
        List<IIndividu> selectionnes =
                new ArrayList<IIndividu>(this.getSelectionInitiale().select(this.getPopulation()));
        List<IIndividu> nouveaux = new ArrayList<IIndividu>();/* CROSSOVER */
        if (this.crossOverPossible()) {
            ISelection selectionCrossOver;
            try {
                selectionCrossOver = new SelectionTirageAleatoire(2);/* On effectue autant de crossOver qu'il y a de couple (on peut avoir plusieurs fois les m�mes couples, sachant qu'ils ne
            donneront pas forc�ment les m�me individus enfants */
                for (int i = 0; i < this.getNombreCouples(); i++) {/* Selection des individu pour le crossOver */
                    List<IIndividu> selCross = selectionCrossOver.select(this.getPopulation());
                    List<IIndividu> croises = this.getCrossOver().crossOver(selCross.get(0), selCross.get(1));/* Ajout aux individus selectionn�s */
                    nouveaux.addAll(croises);
                }
            } catch (Exception e) {
                System.err.println(
                        "Erreur dans l'initialisation de la selection des couples pour le crossOver");
                e.printStackTrace();
            }
        }
        /* MUTATION (Tous les selectionn�s y sont soumis)*/
        for (IIndividu i : selectionnes) {
            nouveaux.add(this.getMutation().muter(i));
        }/* AJOUT DANS LA POPULATION */
        for (IIndividu i : nouveaux) {
            if (!this.getPopulation().getListIndividus().contains(i)) {
                this.getPopulation().addIndividu(i);
            }
        }
        /* SELECTION */
        this.getPopulation().setListIndividus(this.selectionFinale.select(this.getPopulation()));
    }
}

class SelectionTirageAleatoire extends Selection {
    public SelectionTirageAleatoire(int nbIndividus) {
        super(nbIndividus);
    }

    @Override
    public List<IIndividu> select(IPopulation population) {
        /* On s'assure qu'il est possible d'effectuer la selection (si ce n'est pas le cas on renvoie la population inchang�e */
        if (this.selectionPossible(population)) {/* Initialisation de la liste des individus selectionn�s */
            List<IIndividu> selectionnes = new ArrayList<IIndividu>();
            for (int i = 0; i < this.nbIndivus; i++) {
                IIndividu individu;
                do {
                    individu = this.selectionUnique(population);
                } while (selectionnes.contains(individu));
                selectionnes.add(individu);
            }
            return selectionnes;
        } else {
            System.err.println(
                    "Selection impossible, plus d'individus � select que d'individus pr�sents dans la population");
            return population.getListIndividus();
        }
    }

    protected IIndividu selectionUnique(IPopulation population) {
        int i = (int) (Math.random() * population.getTailleEffective());
        return population.getIndividu(i);
    }
}

class SelectionTournoi extends Selection {
    private ArrayList<IIndividu> individusGagnant;

    public SelectionTournoi(int nbIndividus) {
        super(nbIndividus);
        individusGagnant = new ArrayList<IIndividu>();
    }

    @Override
    public List<IIndividu> select(IPopulation population) {
        List<IIndividu> selectionnes = new ArrayList<IIndividu>(population.getListIndividus());
        tournoi(selectionnes, population);
        return individusGagnant;
    }

    private void tournoi(List<IIndividu> individus, IPopulation population) {
        try {
            /* D�claration de la liste des individus s�lection poue la  manche du tournoi */
            ArrayList<IIndividu> individusCourant = new ArrayList<IIndividu>();
            if (individus.size() > this.nbIndivus * 2) {
                /* Dans ce cas l� tous les individus se rencontrent deux � deux ! */
                /* On shuffle la liste */
                Collections.shuffle(individus);
                /* On v�rifie que la liste est paire ou on g�re le cas impaire*/
                int size =
                        (individus.size() % 2 == 0) ? (int) (individus.size()) : (int) (individus.size() - 1.0);
                /* On confronte les individus deux � deux de suite dans la liste m�lang�e */
                for (int i = 0; i < size; i += 2) {
                    IIndividu id1 = individus.get(i);
                    IIndividu id2 = individus.get(i + 1);
                    IIndividu gagnant =
                            population.evaluerIndividu(id1) > population.evaluerIndividu(id2) ? id1 : id2;
                    individusCourant.add(gagnant);
                }
                if (size
                        == individus.size() - 1) /* Cas ou le liste est impaire on ajoute le dernier �l�ment*/
                    individusCourant.add(individus.get(individus.size() - 1));
                /* Appel recursif de la m�thode */
                tournoi(individusCourant, population);
            } else {
                /* C'est l'�tape finale du tournoi !*/
                /* Le but ici est de se faire s'affronter suffisament d'individus pour optenir
                 * le nombre souhait� d'individus finalement
                 */
                int nombreDAffrontements = (individus.size() - this.nbIndivus);
                /* On shuffle la liste */
                Collections.shuffle(individus);
                for (int i = 0; i < 2 * nombreDAffrontements - 1; i += 2) {
                    /* On se fait affronter le bon nombre d'individus */
                    IIndividu id1 = individus.get(i);
                    IIndividu id2 = individus.get(i + 1);
                    IIndividu gagnant =
                            population.evaluerIndividu(id1) > population.evaluerIndividu(id2) ? id1 : id2;
                    individusCourant.add(gagnant);
                }
                for (int i = 2 * nombreDAffrontements; i < individus.size(); i++) {
                    /* Tous les autres individus n'ont pas besoin d'�tre affronter, on les rajoute directement
                     * � la liste des individus s�l�ctionn�s */
                    individusCourant.add(individus.get(i));
                }
                /* On met a jour la liste des selection a retourner */
                individusGagnant.clear();
                individusGagnant.addAll(individusCourant);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SelectionElitiste extends Selection {
    public SelectionElitiste(int nbIndividus) {
        super(nbIndividus);
    }

    @Override
    public List<IIndividu> select(IPopulation population) {
        /* On s'assure qu'il est possible d'effectuer la selection (si ce n'est pas le cas on renvoie la population inchang�e */
        if (this.selectionPossible(population)
                || !(this.nbIndivus == population.getTailleSouhaitee())) {
            /* Initialisation de la liste des individus selectionn�s
             * On garde ce tableau de taille fixe en mettant � jour � chaque fois les
             * this.nbIndivi meilleurs individus en complexit� donc direment de O(n) */
            List<IIndividu> selectionnes = new ArrayList<IIndividu>();
            for (int i = 0; i < this.nbIndivus; i++) {
                selectionnes.add(null);
            }
            /* On traite les tous les autres �l�ments */
            for (int i = 0; i < population.getTailleEffective(); i++) {
                try {
                    double evaluationCourante = population.evaluerIndividu(population.getIndividu(i));
                    int indexBest = 0;
                    while (indexBest < this.nbIndivus) {
                        if (selectionnes.get(indexBest) == null
                                || evaluationCourante > population.evaluerIndividu(selectionnes.get(indexBest))) {
                            /* On ajoute l'individu courant � la liste des selectionn�s au bon index*/
                            /* 2 �tapes :*/
                            /* 1�) D�caler les indices sup�rieurs d'un rang de 1 car on ins�re un nouvel �l�ment*/
                            if (indexBest < this.nbIndivus - 1) {
                                /* Si celui d�j� selectionn� n'est pas le dernier �l�ment */
                                for (int j = this.nbIndivus - 2; j >= indexBest; j--) {
                                    selectionnes.set(j + 1, selectionnes.get(j));
                                }
                            }
                            /* 2�) Ajouter le nouvelle individu au bon rang des meilleurs */
                            selectionnes.set(indexBest, population.getIndividu(i));
                            /* Toutes les modifications �tant apport�es, on sort de la boucle */
                            indexBest = this.nbIndivus + 1;
                        } else indexBest++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return selectionnes;
        } else {
            System.err.println(
                    "Selection impossible, plus d'individus � select que d'individus pr�sents dans la population");
            return population.getListIndividus();
        }
    }
}

class CrossOverSimple extends CrossOver {
    private static final long serialVersionUID = 1649476761942148889L;
    protected int nbCaracteres;

    public CrossOverSimple(int n, double p) {
        super(p);
        this.nbCaracteres = n;
    }

    @Override
    public List<IIndividu> crossOver(IIndividu individu1, IIndividu individu2) {
        ArrayList<IIndividu> retour =
                new ArrayList<IIndividu>(Arrays.asList(new IIndividu[]{individu1, individu2}));
        if (this.doCrossOver() && individu1.getType().equals(individu2.getType())) {
            int nbTotalCaracteres = individu1.getNombreCaracteristiques();
            /* On s'assure que les individus contiennent au moins n+1 caract�res */
            int nbCaracteresEffectif =
                    (this.nbCaracteres < nbTotalCaracteres) ? this.nbCaracteres : nbTotalCaracteres;
            IIndividu n1 = individu1.clone();
            IIndividu n2 = individu2.clone();/* On choisi au hasard les caract�res � interchanger et on effectue le crossOver*/
            for (int i = 0; i < nbCaracteresEffectif; i++) {
                int j = (int) (Math.random() * nbTotalCaracteres);
                ICaracteristic c1 = n1.getListCaracteristics().get(j).clone();
                ICaracteristic c2 = n2.getListCaracteristics().get(j).clone();
                n1.getListCaracteristics().remove(j);
                n1.getListCaracteristics().add(j, c2);
                n2.getListCaracteristics().remove(j);
                n2.getListCaracteristics().add(j, c1);
            }
            n1.setName(n1.getName() + "-j");
            n2.setName(n2.getName() + "-j");
            retour = new ArrayList<IIndividu>(Arrays.asList(new IIndividu[]{n1, n2}));
        }
        return retour;
    }
}

class MutationSimple extends Mutation {
    protected static final int NOMBRE_BIT_A_MUTER_DEFAUT = 1;
    protected static final double PROBABILITE_DEFAUT = 0.1;
    protected int nbBitAMuter;

    public MutationSimple() {
        super(PROBABILITE_DEFAUT);
        this.nbBitAMuter = NOMBRE_BIT_A_MUTER_DEFAUT;
    }

    public MutationSimple(int nbBitAMuter, double prob) {
        super(prob);
        this.nbBitAMuter = nbBitAMuter;
    }

    @Override
    public IIndividu muter(IIndividu individu) {
        boolean mutant = false;
        IIndividu nIndividu = individu.clone();
        if (this.mutationIndividuPossible(nIndividu)) {
            for (ICaracteristic c : nIndividu.getListCaracteristics()) {
                boolean b = this.muterCaracteristique(c);
                mutant = mutant || b;
            }
        }
        if (mutant) {
            nIndividu.setName(nIndividu.getName() + "-mutant");
            return nIndividu;
        } else {
            return individu;
        }
    }

    protected boolean muterCaracteristique(ICaracteristic caracteristique) {
        boolean retour = false;
        if (this.mutationCaracteristiquePossible(caracteristique) && this.doMutation()) {
            for (int i = 0; i < this.nbBitAMuter; i++) {
                int bit = (int) (Math.random() * caracteristique.getSize());
                caracteristique.getBitSet().flip(bit);
                caracteristique.update();
            }
            retour = true;
        }
        return retour;
    }

    public boolean mutationCaracteristiquePossible(ICaracteristic caracteristique) {
        return (caracteristique.getSize() > nbBitAMuter);
    }

    @Override
    public boolean mutationIndividuPossible(IIndividu individu) {
        return (individu.getNombreCaracteristiques() != 0);
    }
}

class EndingConditionSimpleIterations extends EndingCondition {
    private static final long serialVersionUID = -4791732884654986092L;
    protected int limiteIterations;

    public EndingConditionSimpleIterations(int limite) {
        this.limiteIterations = limite;
    }

    @Override
    public boolean isSatisfied(IPopulation population) {
        this.iterations++;
        return this.getNbIeration() >= this.limiteIterations;
    }

    @Override
    public IEndingCondition nextConditionArret() {
        return null;
    }

    @Override
    public IEnvironment nextEnvironnement() {
        return null;
    }
}

abstract class EndingCondition implements IEndingCondition {
    protected int iterations;

    protected EndingCondition() {
        this.iterations = 0;
    }

    protected EndingCondition(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public abstract boolean isSatisfied(IPopulation population);

    @Override
    public abstract IEndingCondition nextConditionArret();

    @Override
    public abstract IEnvironment nextEnvironnement();

    @Override
    public int getNbIeration() {
        return this.iterations;
    }
}

abstract class CrossOver implements ICrossOver {
    protected double probabilite;

    protected CrossOver() {
        probabilite = 0.7;
    }

    protected CrossOver(double probabilite) {
        if (probabilite < 0 || probabilite > 1)
            System.err.println("Probabilit� de crossOver non comprise entre 0 et 1");
        else {
            this.probabilite = probabilite;
        }
    }

    @Override
    public abstract List<IIndividu> crossOver(IIndividu individu1, IIndividu individu2);

    protected boolean doCrossOver() {
        if (this.probabilite == 1d) {
            return true;
        } else {
            double d = Math.random();
            boolean retour = (d < this.probabilite) ? true : false;
            return retour;
        }
    }
}

abstract class Environment implements IEnvironment {
    protected String name;

    protected Environment() {
    }

    protected Environment(String name) {
        this.name = name;
    }

    public abstract double evaluate(IIndividu individu);

    public abstract boolean isValid(IIndividu individu);

    public String toString() {
        return "Environment [name=" + name + "]";
    }
}

class Individu implements IIndividu {
    protected int type;
    protected String name;
    protected List<ICaracteristic> caracteristiques;

    protected Individu() {
        caracteristiques = new ArrayList<>();
    }

    protected Individu(String name, List<ICaracteristic> caracteristiques) {
        this.type = -1;
        this.name = name;
        this.caracteristiques = caracteristiques;
    }

    protected Individu(Individu i) {
        this.type = i.getType();
        this.name = new String(i.getName());
        this.caracteristiques = new ArrayList<ICaracteristic>();
        for (ICaracteristic c : i.getListCaracteristics()) {
            this.caracteristiques.add(c.clone());
        }
    }

    @Override
    public IIndividu clone() {
        return new Individu(this);
    }

    @Override
    public List<ICaracteristic> getListCaracteristics() {
        return this.caracteristiques;
    }

    @Override
    public void setListCaracteristiques(List<ICaracteristic> caracs) {
        this.caracteristiques = caracs;
    }

    @Override
    public ICaracteristic getCaracteristique(int index) {
        return this.getListCaracteristics().get(index);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getNombreCaracteristiques() {
        return this.caracteristiques.size();
    }

    @Override
    public Integer getType() {
        return this.type;
    }


    public String toString() {
        String retour = "Individu " + name + ", caracteristiques = ";
        for (ICaracteristic c : this.getListCaracteristics()) {
            retour += "\n" + "     " + c;
        }
        return retour;
    }
}

abstract class Mutation implements IMutation {
    protected double probabilite;

    protected Mutation() {
        this.probabilite = 0.01;
    }

    protected Mutation(double prob) {
        if (prob < 0 || prob > 1)
            System.err.println("Probabilit� de mutation non comprise entre 0 et 1");
        else this.probabilite = prob;
    }

    public abstract IIndividu muter(IIndividu individu);

    public abstract boolean mutationIndividuPossible(IIndividu individu);

    public abstract boolean mutationCaracteristiquePossible(ICaracteristic caracteristique);

    protected boolean doMutation() {
        if (this.probabilite == 1d) {
            return true;
        } else {
            double d = Math.random();
            boolean retour = (d < this.probabilite) ? true : false;
            return retour;
        }
    }
}

abstract class Population implements IPopulation {
    protected List<IIndividu> individus;
    protected int nombreIndividusSouhaite;
    protected IEnvironment environnement;

    protected Population() {
        individus = new ArrayList<>();
    }

    protected Population(int nombreIndividusSouhaites, IEnvironment environnement) {
        this.nombreIndividusSouhaite = nombreIndividusSouhaites;
        this.environnement = environnement;
        individus = new ArrayList<>();
        this.generer();
    }

    @Override
    public void addIndividu(IIndividu individu) {
        this.getListIndividus().add(individu);
    }

    @Override
    public double evaluerIndividu(IIndividu individu) {
        double retour = this.getEnvironnement().evaluate(individu);
        return retour;
    }

    @Override
    public double evaluerIndividu(int index) {
        return this.evaluerIndividu(this.getIndividu(index));
    }

    @Override
    public double evaluerPopulation() {
        double retour = 0;
        for (IIndividu i : this.getListIndividus()) {
            try {
                retour += this.evaluerIndividu(i);
            } catch (Exception e) {
                System.err.println("L'individu " + i + "n'est pas �valuable");
                e.printStackTrace();
            }
        }
        return retour;
    }

    @Override
    public abstract void generer();

    @Override
    public IEnvironment getEnvironnement() {
        return this.environnement;
    }

    @Override
    public void setEnvironnement(IEnvironment environnement) {
        this.environnement = environnement;
    }

    @Override
    public IIndividu getIndividu(int i) {
        return this.getListIndividus().get(i);
    }

    @Override
    public List<IIndividu> getListIndividus() {
        return this.individus;
    }

    @Override
    public void setListIndividus(List<IIndividu> individus) {
        this.individus = individus;
    }

    @Override
    public int getTailleEffective() {
        return this.getListIndividus().size();
    }

    @Override
    public int getTailleSouhaitee() {
        return this.nombreIndividusSouhaite;
    }

    @Override
    public IIndividu getBestIndividu() {
        IIndividu retour = this.getIndividu(0);
        for (int i = 1; i < this.getTailleEffective(); i++) {
            try {
                if (this.evaluerIndividu(retour) < this.evaluerIndividu(this.getIndividu(i))) {
                    retour = this.getIndividu(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retour;
    }

    public String toString() {
        return "Population [individus=" + individus + ", environnement=" + environnement + "]";
    }
}

abstract class Selection implements ISelection {
    protected int nbIndivus;

    protected Selection() {
    }

    protected Selection(int nbIndividus) {
        if (nbIndividus < 1) System.err.println("Nombre d'individus � select < 1");
        else this.nbIndivus = nbIndividus;
    }

    @Override
    public boolean selectionPossible(IPopulation population) {
        return (this.nbIndivus <= population.getTailleEffective());
    }

    @Override
    public abstract List<IIndividu> select(IPopulation population);
}

abstract class NaturalSelection implements INaturalSelection {
    protected ISelection selectionInitiale;
    protected ISelection selectionFinale;
    protected ICrossOver crossOver;
    protected IMutation mutation;
    protected IPopulation population;

    protected NaturalSelection(
            ISelection selInit, ISelection selFin, ICrossOver cross, IMutation mut, IPopulation pop) {
        this.selectionInitiale = selInit;
        this.selectionFinale = selFin;
        this.crossOver = cross;
        this.mutation = mut;
        this.population = pop;
    }

    @Override
    public ICrossOver getCrossOver() {
        return this.crossOver;
    }

    @Override
    public IMutation getMutation() {
        return this.mutation;
    }

    @Override
    public IPopulation getPopulation() {
        return this.population;
    }

    @Override
    public ISelection getSelectionFinale() {
        return this.selectionFinale;
    }

    @Override
    public ISelection getSelectionInitiale() {
        return this.selectionInitiale;
    }

    @Override
    public abstract void nextGeneration();

    protected boolean crossOverPossible() {
        return this.getPopulation().getTailleEffective() >= 2;
    }
}
