import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/** @author Momo && Dim Solveur de l'algorithme g�n�tique */
interface IDarwin {

  /**
   * Fait tourner l'algorithme g�n�tique jusqu'� ce que la condition d'arr�t finale soit remplie
   *
   * @return La population finale
   */
  IPopulation solve();

  /** @return La condition d'arret actuellement utilis�e */
  IConditionArret getConditionArret();

  /**
   * Fixe la condition d'arret
   *
   * @param condition
   */
  void setConditionArret(IConditionArret condition);

  /** @return La selection naturelle sur laquelle on itere */
  ISelectionNaturelle getSelectionNaturelle();

  /** Modifie la selection Naturelle */
  void setSelectionNaturelle(ISelectionNaturelle newSelectionNaturelle);
}

/**
 * @author Momo && Dim R�pr�sente une caract�ristique cod�e par un unique g�ne, lors d'une
 *     instanciation avec un �tat particulier, on obtient un all�le de ce g�ne. Une caract�ristique
 *     est clonable afin de pouvoir la modifier sans perdre l'originale.
 */
interface ICaracteristique extends Cloneable {

  /** @return le nom de la caract�ristique */
  String getName();

  /** @return Le Bitset codant la caract�ristique */
  BitSet getBitSet();

  /** @return le nombre de bits sur lesquels peut �tre cod�e la caract�ristique */
  int getTailleBitSet();

  /** Met � jour la caract�ristique lors d'une modification du g�ne */
  void update();

  /** @return Une copie de la caract�ristique (et non une r�ference) */
  ICaracteristique clone();
}

/**
 * @author Momo && Dim D�fini une condition d'arr�t lors de la r�solution d'un probl�me avec
 *     l'algorithme g�n�tique
 */
interface IConditionArret {

  /**
   * @param population
   * @return true si la condition d'arr�t sur la population donn�e en param�tre est satisfaite
   */
  boolean isSatisfied(IPopulation population);

  /** @return La condition d'arr�t suivante ou null si il n'y en a pas */
  IConditionArret nextConditionArret();

  /** @return Le nouvel environnement � appliquer, ou null si on garde le m�me */
  IEnvironnement nextEnvironnement();

  /** @return Le nombre d'it�rations effectu�es (ie le nombre d'appels � isSatisfied */
  int getNombreIteration();
}

/** @author Dim && Momo Permet de croiser deux individus */
interface ICrossOver {

  /**
   * @param individu1
   * @param individu2
   * @return effectue un crossOver � partir des deux individus pass�s en param�tre et renvoie les
   *     individus obtenus sous forme de liste
   */
  List<IIndividu> crossOver(IIndividu individu1, IIndividu individu2);
}

/**
 * @author Dim && Momo Repr�sente un environnement, capable d'�valuer un individu. Il est essentiel
 *     de bien d�finir l'environnement associ� � un probl�me afin de pouvoir le r�soudre au mieux.
 */
interface IEnvironnement {

  /**
   * @param individu
   * @return un double correspondant � l'�valuation de l'individu @ Si l'individu ne peut pas �tre
   *     �valu�
   */
  double evaluerIndividu(IIndividu individu);

  /**
   * @param individu
   * @return true si l'individu est valide et �valuable dans cet environnement
   */
  boolean isValid(IIndividu individu);
}

/**
 * @author Dim && Momo Repr�sente un individu, compos� de diff�rentes caract�ristiques, tout comme
 *     une caract�ristique, un individu est clonable.
 */
interface IIndividu extends Cloneable {

  /** @return Un Integer caract�risant le type de l'individu */
  Integer getType();

  /** @return Le nom de l'individu */
  String getName();

  /**
   * Permet de changer le nom d'un individu
   *
   * @param name
   */
  void setName(String name);

  /** @return L'ensemble des caract�ristiques propres � l'individu */
  List<ICaracteristique> getListCaracteristique();

  /** D�finit la liste de caract�ristique donn�e en param�tre comme �tant le g�nome de l'individu */
  void setListCaracteristiques(List<ICaracteristique> caracs);

  /**
   * @param index
   * @return La caract�ristique plac�e � l'index donn�e en param�tre
   */
  ICaracteristique getCaracteristique(int index);

  /** @return Le nombre de caract�ristiques d�finissant l'individu */
  int getNombreCaracteristiques();

  /** @return Une copie de l'individu (et non une r�ference) */
  IIndividu clone();
}

/**
 * @author Momo && Dim Permet de muter une ou plusieurs caract�ristiques d'un individu en agissant
 *     directement sur le bitSet
 */
interface IMutation {

  /**
   * @param individu
   * @return mute un individu et le retourne
   */
  IIndividu muter(IIndividu individu);

  /**
   * @param individu
   * @return true si il est possible de muter l'individu pass� en param�tre
   */
  boolean mutationIndividuPossible(IIndividu individu);

  /**
   * @param caracteristique
   * @return true si il est possible de muter la caract�ristique pass�e en param�tre
   */
  boolean mutationCaracteristiquePossible(ICaracteristique caracteristique);
}

/**
 * @author Momo && Dim Repr�sente un ensemble d'individu (de taille d�finie) plong�s dans un
 *     environnement particulier. Les individus tout comme la population dont ils font partie
 *     peuvent �tre �valu�s relativement � cet environnement.
 */
interface IPopulation {

  /** @return Le nombre d'individu que la population doit en th�orie contenir. */
  int getTailleSouhaitee();

  /**
   * @return Le nombre d'individu que la population contient en r�alit�, ce nombre ne doit diff�rer
   *     du nombre th�orique que temporairement.
   */
  int getTailleEffective();

  /** @return L'ensemble des individus de la population sous forme de liste. */
  List<IIndividu> getListIndividus();

  /** @param individus L'ensemble des individus � placer dans la population. */
  void setListIndividus(List<IIndividu> individus);

  /**
   * @param i L'index de l'individu � retourner
   * @return L'individu � l'index i dans la population
   */
  IIndividu getIndividu(int i);

  /**
   * Ajoute un individu � une population d�j� existante
   *
   * @param individu L'individu � ajouter
   */
  void ajouterIndividu(IIndividu individu);

  /** @return L'environnement auquel est li�e la population */
  IEnvironnement getEnvironnement();

  /**
   * Associe un environnement � la population
   *
   * @param environnement L'environnement � associer � la population
   */
  void setEnvironnement(IEnvironnement environnement);

  /**
   * @param individu L'individu � evaluer
   * @return Un double correspondant � l'�valuation de l'individu dans l'environnement auquel est il
   *     est soumis @ Si l'individu n'est pas �valuable dans l'environnement actuel
   */
  double evaluerIndividu(IIndividu individu);

  /**
   * @param index L'index de l'individu � �valuer
   * @return L'�valuation de l'individu plac� � l'index donn� dans la population @ Si l'individu
   *     n'est pas �valuable dans l'environnement actuel
   */
  double evaluerIndividu(int index);

  /**
   * @return L'�valuation de la population, qui en g�n�ral est �gale au cumul des �valuations des
   *     individus de la population
   */
  double evaluerPopulation();

  /** @return L'individu de la population qui a la meilleure �valuation */
  IIndividu getBestIndividu();

  /** G�n�re une population au hasard */
  void generer();
}

/**
 * @author Momo && Dim Permet de selectionner un �chantillon d'individu parmis une population, et
 *     selon des crit�res d�finis.
 */
interface ISelection {

  /**
   * @param population
   * @return renvoie un �chantillon d'individus selectionn�s parmis la population pass�e en
   *     param�tre
   */
  List<IIndividu> selectionner(IPopulation population);

  /**
   * @param population
   * @return true si il est possible d'effectuer une selection sur la population
   */
  boolean selectionPossible(IPopulation population);
}

/** @author Dim && Momo Permet d'effectuer une selection naturelle sur une g�n�ration */
interface ISelectionNaturelle {

  /** @return le type de selection utilis� en d�but de s�lection naturelle */
  ISelection getSelectionInitiale();

  /** @return le type de selection utilis� en fin de selection naturelle */
  ISelection getSelectionFinale();

  /** @return le type de mutation utilis�e */
  IMutation getMutation();

  /** @return le type de crossOver utilis� */
  ICrossOver getCrossOver();

  /** @return la population sur laquelle on effectue la selection */
  IPopulation getPopulation();

  /** Met � jour la population gr�ce � une selection "naturelle" */
  void nextGeneration();
}

class Darwin implements IDarwin {

  // VARIABLES D'INSTANCE
  /** La selection naturelle utilis�e pour les it�ration */
  protected ISelectionNaturelle selectionNaturelle;

  // VARIABLES D'INSTANCES D'AFFICHAGE
  protected boolean afficherStartingInfos,
      afficherEndingInfos,
      afficherChaqueGeneration,
      afficherChaqueScore,
      afficherIterations;

  /** La condition d'arr�t */
  protected IConditionArret conditionArret;

  public Darwin(ISelectionNaturelle selectionNaturelle, IConditionArret conditionArret) {
    this.selectionNaturelle = selectionNaturelle;
    this.setConditionArret(conditionArret);
    this.afficherStartingInfos = false;
    this.afficherEndingInfos = true;
    this.afficherChaqueGeneration(false);
    this.afficherChaqueScore(true);
    this.afficherIterations(true);
  }

  /** Constructeur Vide */
  public Darwin() {
    this.afficherChaqueGeneration(false);
    this.afficherChaqueScore(true);
    this.afficherIterations(true);
  }

  @Override
  public IConditionArret getConditionArret() {
    return this.conditionArret;
  }

  @Override
  public void setConditionArret(IConditionArret condition) {
    this.conditionArret = condition;
  }

  @Override
  public ISelectionNaturelle getSelectionNaturelle() {
    return this.selectionNaturelle;
  }

  @Override
  public void setSelectionNaturelle(ISelectionNaturelle newSelectionNaturelle) {
    this.selectionNaturelle = newSelectionNaturelle;
  }

  @Override
  public IPopulation solve() {

    /* Affichage de la population initiale */
    if (afficherStartingInfos) {
      System.err.println("-----------------------------------------------------");
      System.err.println("*****     DARWIN Genetic Solver version 1.0     *****");
      System.err.println("*****             By Momo and Dim'              *****");
      System.err.println("*****          Left-Handed power team           *****");
      System.err.println("-----------------------------------------------------" + "\n" + "\n");

      System.err.println("-----------------------------------------------------");
      System.err.println("*****          Population initiale :            *****");
      System.err.println("-----------------------------------------------------" + "\n");

      System.err.println(
          this.getSelectionNaturelle().getPopulation()
              + "\n"
              + "Score : "
              + this.getSelectionNaturelle().getPopulation().evaluerPopulation()
              + "\n"
              + "\n");

      System.err.println("-----------------------------------------------------");
      System.err.println("*****   Demarrage de l'algorithme g�n�tique     *****");
      System.err.println("-----------------------------------------------------" + "\n");
    }
    int iterations = 0;

    while (this.getConditionArret() != null) {

      /* Nouvelle g�n�ration */
      this.getSelectionNaturelle().nextGeneration();

      /* Verification de la condition d'arr�t */
      if (this.getConditionArret().isSatisfied(this.getSelectionNaturelle().getPopulation())) {

        /* Application du nouvel environnement si besoin */
        if (this.getConditionArret().nextEnvironnement() != null) {
          this.getSelectionNaturelle()
              .getPopulation()
              .setEnvironnement(this.getConditionArret().nextEnvironnement());
        }
        iterations = this.getConditionArret().getNombreIteration();
        this.setConditionArret(this.getConditionArret().nextConditionArret());
      }

      /* Affichages */
      if (this.afficherChaqueGeneration) {
        System.err.println(this.getSelectionNaturelle().getPopulation());
      }
      if (this.afficherChaqueScore) {
        System.err.println(
            "Score : " + this.getSelectionNaturelle().getPopulation().evaluerPopulation());
      }
      //      if(this.afficherIterations){
      //        System.err.println("It�rations :" + iterations);
      //      }
      //      iterations ++;
    }

    IPopulation popFinale = this.getSelectionNaturelle().getPopulation();
    if (afficherEndingInfos) {
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

  public void afficherChaqueGeneration(boolean b) {
    this.afficherChaqueGeneration = b;
  }

  public void afficherChaqueScore(boolean b) {
    this.afficherChaqueScore = b;
  }

  public void afficherIterations(boolean b) {
    this.afficherIterations = b;
  }
}

/**
 * Selection naturelle standard : On selectionne m(<=taille de la population) individus, qu'on
 * croise puis mute, on ins�re les nouveaux individus obtenus au sein de la population et on
 * r�effectue une selection afin de se ramener � la taille de population souhait�e.
 *
 * @author Dim
 */
class SelectionNaturelleSimple extends SelectionNaturelle {

  // VARIABLES D'INSTANCE

  /**
   * Le nombre de couple sur lesquel effectuer un crossOver d'une g�n�ration � l'autre Ce nombre
   * peut �tre
   */
  protected int nombreCouples;

  /**
   * La probabit� d'apparition d'individus completement nouveaux, doit �tre comprise entre 0 et 1
   */
  protected double probImmigration;

  /**
   * Constructeur avec types de selection, crossOver et Mutation pr�d�finis
   *
   * @param pop La population sur laquelle effectuer la selection Naturelle
   * @param nbIndividusSelInit Le nombre d'individus � choisir lors de la premiere selection
   * @param nbCaracCross Le de caract�res � interchanger lors du crossOver
   * @param pCross La probabilit� de crossOver
   * @param nbBitMut Le nombre de bits � muter lors de la mutation
   * @param pMut La probabilit� de mutation
   * @param nbCouples Le nombre de couples � former lors du crossOver
   * @param probIm La probabilit� d'immigration @
   */
  public SelectionNaturelleSimple(
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

  /**
   * Constructeur o� on d�finit manuellement toutes les �tapes de la selection, et le nombre de
   * couples pour le crossOver
   *
   * @param selInit La selection initiale
   * @param selFin La selection finale
   * @param cross Le crossOver
   * @param mut La mutation
   * @param pop La population
   * @param nbCouples Le nombre de couples
   * @param probImmigration La probabilit� d'immigration
   */
  public SelectionNaturelleSimple(
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
  public void nextGeneration() {

    /* IMMIGRATION */

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
        this.getPopulation().ajouterIndividu(i);
      }
    }

    /* SELECTION */
    List<IIndividu> selectionnes =
        new ArrayList<IIndividu>(this.getSelectionInitiale().selectionner(this.getPopulation()));
    List<IIndividu> nouveaux = new ArrayList<IIndividu>();

    /* CROSSOVER */
    if (this.crossOverPossible()) {

      ISelection selectionCrossOver;
      try {
        selectionCrossOver = new SelectionTirageAleatoire(2);

        /* On effectue autant de crossOver qu'il y a de couple (on peut avoir plusieurs fois les m�mes couples, sachant qu'ils ne
        donneront pas forc�ment les m�me individus enfants */
        for (int i = 0; i < this.getNombreCouples(); i++) {

          /* Selection des individu pour le crossOver */
          List<IIndividu> selCross = selectionCrossOver.selectionner(this.getPopulation());
          List<IIndividu> croises = this.getCrossOver().crossOver(selCross.get(0), selCross.get(1));

          /* Ajout aux individus selectionn�s */
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
    }

    /* AJOUT DANS LA POPULATION */

    for (IIndividu i : nouveaux) {
      if (!this.getPopulation().getListIndividus().contains(i)) {
        this.getPopulation().ajouterIndividu(i);
      }
    }
    /* SELECTION */
    this.getPopulation().setListIndividus(this.selectionFinale.selectionner(this.getPopulation()));
  }
}

class SelectionTirageAleatoire extends Selection {

  public SelectionTirageAleatoire(int nbIndividus) {
    super(nbIndividus);
  }

  @Override
  public List<IIndividu> selectionner(IPopulation population) {
    /* On s'assure qu'il est possible d'effectuer la selection (si ce n'est pas le cas on renvoie la population inchang�e */
    if (this.selectionPossible(population)) {

      /* Initialisation de la liste des individus selectionn�s */
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
          "Selection impossible, plus d'individus � selectionner que d'individus pr�sents dans la population");
      return population.getListIndividus();
    }
  }

  /**
   * Selectionne un individu au hasard dans la population
   *
   * @param population
   * @return IIndividu l'individu selectionn� au hasard
   */
  protected IIndividu selectionUnique(IPopulation population) {
    int i = (int) (Math.random() * population.getTailleEffective());
    return population.getIndividu(i);
  }
}

/**
 *
 *
 * <h2>La classe Selection par tournoi</h2>
 *
 * Cette selection organise des rencontres entre pairs d'individus, et la gagnant est celui qui a
 * une meilleur �valuation que son adversaire. Cette m�thode de selection permet de garder des
 * individus parfois avec des �valuations m�diocre tout en assurant une bonne convergence
 *
 * <p>On r�sultat du tournoi, on obtient donc un nombre nbIndivdu de vainceur apr�s avoir organis�
 * une s�rie de rencontre sur plusieurs �tapes (ex: 1/16�me, 1/8�me, quart de final...)
 *
 * @author Amaury
 */
class SelectionTournoi extends Selection {

  /** La liste des individus � retourner apr�s une selection */
  private ArrayList<IIndividu> individusGagnant;

  /**
   * Constructeur de base
   *
   * @param nbIndividus, le nombre d'individus � selectionner @
   */
  public SelectionTournoi(int nbIndividus) {
    super(nbIndividus);
    individusGagnant = new ArrayList<IIndividu>();
  }

  @Override
  public List<IIndividu> selectionner(IPopulation population) {
    List<IIndividu> selectionnes = new ArrayList<IIndividu>(population.getListIndividus());
    tournoi(selectionnes, population);
    return individusGagnant;
  }

  /**
   * C'est dans cette m�thode que nous organisons une des �tapes du tournoi en fonction du nombre
   * d'individus souhait�s � l'arriv�e.
   */
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

/**
 * Selection d'invidus sur le mod�le �litiste simple ! On selectionne simplement un nom pr�determin�
 * d'individu en fonction de leur rang apr�s �valuation par l'environement ! On ne prend donc ici
 * que les meilleurs individus
 *
 * @author Amaury
 */
class SelectionElitiste extends Selection {

  public SelectionElitiste(int nbIndividus) {
    super(nbIndividus);
  }

  @Override
  public List<IIndividu> selectionner(IPopulation population) {
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
          "Selection impossible, plus d'individus � selectionner que d'individus pr�sents dans la population");
      return population.getListIndividus();
    }
  }
}

/**
 * Permet de r�aliser un crossOver simple : avec une probabilit� p, on interchange n caract�res pris
 * au hasard entre les individus concern�s.
 *
 * @author Dim
 */
class CrossOverSimple extends CrossOver {

  private static final long serialVersionUID = 1649476761942148889L;

  // VARIABLES D'INSTANCE
  /** Le nombre de caract�res � interchanger */
  protected int nbCaracteres;

  /**
   * @param n nombre de caract�res � interchanger
   * @param p probabilit� de crossOver @
   */
  public CrossOverSimple(int n, double p) {
    super(p);
    this.nbCaracteres = n;
  }

  @Override
  public List<IIndividu> crossOver(IIndividu individu1, IIndividu individu2) {

    // On initialise la liste � retourner avec les individus inchang�s
    ArrayList<IIndividu> retour =
        new ArrayList<IIndividu>(Arrays.asList(new IIndividu[] {individu1, individu2}));

    // Si le hasard en d�cide ainsi (et que les individus sont du m�me type, on effectue le
    // crossOver
    if (this.doCrossOver() && individu1.getType().equals(individu2.getType())) {

      int nbTotalCaracteres = individu1.getNombreCaracteristiques();
      /* On s'assure que les individus contiennent au moins n+1 caract�res */
      int nbCaracteresEffectif =
          (this.nbCaracteres < nbTotalCaracteres) ? this.nbCaracteres : nbTotalCaracteres;
      IIndividu n1 = individu1.clone();
      IIndividu n2 = individu2.clone();

      /* On choisi au hasard les caract�res � interchanger et on effectue le crossOver*/
      for (int i = 0; i < nbCaracteresEffectif; i++) {
        int j = (int) (Math.random() * nbTotalCaracteres);
        ICaracteristique c1 = n1.getListCaracteristique().get(j).clone();
        ICaracteristique c2 = n2.getListCaracteristique().get(j).clone();
        n1.getListCaracteristique().remove(j);
        n1.getListCaracteristique().add(j, c2);
        n2.getListCaracteristique().remove(j);
        n2.getListCaracteristique().add(j, c1);
      }
      n1.setName(n1.getName() + "-j");
      n2.setName(n2.getName() + "-j");
      retour = new ArrayList<IIndividu>(Arrays.asList(new IIndividu[] {n1, n2}));
    }
    return retour;
  }
}

/**
 * Permet de r�aliser une mutation simple : Pour toutes les caract�ristiques de l'individu, on
 * change l'�tat d'un nombre d�fini de bits du bitSet avec une probabilit� p.
 *
 * @author Dim
 */
class MutationSimple extends Mutation {

  // VARIABLES D'INSTANCE
  /** Le nombre de bits � muter par d�faut */
  protected static final int NOMBRE_BIT_A_MUTER_DEFAUT = 1;

  // CONSTANTES
  /** La probabilit� de mutation par d�faut */
  protected static final double PROBABILITE_DEFAUT = 0.1;
  /** le nombre de bits � muter dans une caract�ristique */
  protected int nbBitAMuter;

  /** Constructeur par d�faut @ */
  public MutationSimple() {
    super(PROBABILITE_DEFAUT);
    this.nbBitAMuter = NOMBRE_BIT_A_MUTER_DEFAUT;
  }

  /**
   * Constructeur h�rit�
   *
   * @param nbBitAMuter
   * @param prob @
   */
  public MutationSimple(int nbBitAMuter, double prob) {
    super(prob);
    this.nbBitAMuter = nbBitAMuter;
  }

  @Override
  public IIndividu muter(IIndividu individu) {
    boolean mutant = false;
    IIndividu nIndividu = individu.clone();
    if (this.mutationIndividuPossible(nIndividu)) {
      for (ICaracteristique c : nIndividu.getListCaracteristique()) {
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

  /**
   * @param caracteristique
   * @return true si la caract�ristique a �t� mut�e
   */
  protected boolean muterCaracteristique(ICaracteristique caracteristique) {
    boolean retour = false;
    if (this.mutationCaracteristiquePossible(caracteristique) && this.doMutation()) {
      for (int i = 0; i < this.nbBitAMuter; i++) {
        int bit = (int) (Math.random() * caracteristique.getTailleBitSet());
        caracteristique.getBitSet().flip(bit);
        caracteristique.update();
      }
      retour = true;
    }
    return retour;
  }

  public boolean mutationCaracteristiquePossible(ICaracteristique caracteristique) {
    return (caracteristique.getTailleBitSet() > nbBitAMuter);
  }

  @Override
  public boolean mutationIndividuPossible(IIndividu individu) {
    return (individu.getNombreCaracteristiques() != 0);
  }
}

///// INTERFACES ////

/** @author Dim && Momo Impl�mentation partielle et abstraite de ICaracteristique */
abstract class Caracteristique implements ICaracteristique {

  // VARIABLES D'INSTANCES

  /** Le BitSet qui code la caract�ristique de l'individu */
  protected BitSet bitSet;

  /** La taille de ce BitSet */
  protected int tailleBitSet;

  /** Le nom de cette caract�ristique */
  protected String nom;

  /** Constructeur par d�faut */
  protected Caracteristique() {}

  /**
   * Constructeur basique
   *
   * @param nom
   * @param bitSet
   * @param tailleBitSet
   */
  protected Caracteristique(String nom, BitSet bitSet, int tailleBitSet) {
    this.tailleBitSet = tailleBitSet;
    this.bitSet = bitSet;
    this.nom = nom;
  }

  /**
   * Constructeur par recopie (pour le clone() )
   *
   * @param c
   */
  protected Caracteristique(Caracteristique c) {
    this.nom = new String(c.getName());
    this.bitSet = (BitSet) c.getBitSet().clone();
    this.tailleBitSet = c.tailleBitSet;
  }

  @Override
  public BitSet getBitSet() {
    return this.bitSet;
  }

  @Override
  public String getName() {
    return this.nom;
  }

  @Override
  public int getTailleBitSet() {
    return this.tailleBitSet;
  }

  @Override
  public abstract void update();

  public abstract Caracteristique clone();

  /** Repr�sentation sous forme de chaine de caract�res */
  public String toString() {
    return "Caracteristique " + nom + " [bitSet = " + bitSet + "]";
  }
}

class ConditionArretSimpleIterations extends ConditionArret {

  private static final long serialVersionUID = -4791732884654986092L;

  protected int limiteIterations;

  public ConditionArretSimpleIterations(int limite) {
    this.limiteIterations = limite;
  }

  @Override
  public boolean isSatisfied(IPopulation population) {
    this.iterations++;
    return this.getNombreIteration() >= this.limiteIterations;
  }

  @Override
  public IConditionArret nextConditionArret() {
    return null;
  }

  @Override
  public IEnvironnement nextEnvironnement() {
    return null;
  }
}

/** @author Dim && Momo Impl�mentation partielle et abstraite de IConditionArret */
abstract class ConditionArret implements IConditionArret {

  // VARIABLES D'INSTANCE
  /** Le nombre d'it�rations effectu�e */
  protected int iterations;

  /** Constructeur � appeler lorsque la condition est la premi�re utilis�e */
  protected ConditionArret() {
    this.iterations = 0;
  }

  /**
   * Constructeur � appeler lorsque la condition suit une autre (on transmet le nombre d'it�rations)
   *
   * @param iterations
   */
  protected ConditionArret(int iterations) {
    this.iterations = iterations;
  }

  @Override
  public abstract boolean isSatisfied(IPopulation population);

  @Override
  public abstract IConditionArret nextConditionArret();

  @Override
  public abstract IEnvironnement nextEnvironnement();

  @Override
  public int getNombreIteration() {
    return this.iterations;
  }
}

/** @author Momo && Dim Impl�mentation partielle et abstraite de ICrossOver */
abstract class CrossOver implements ICrossOver {

  // VARIABLES D'INSTANCE
  /** La probabilit� que le crossOver ait lieu (doit �tre comprise entre 0 et 1) */
  protected double probabilite;

  /** Constructeur par d�faut */
  protected CrossOver() {
    probabilite = 0.7;
  }

  /**
   * Constructeur basique
   *
   * @param probabilite La probabilit� pour que le crossOver ait lieu (entre 0 et 1) @ si la
   *     probabilit� est en dehors de [0,1]
   */
  protected CrossOver(double probabilite) {
    if (probabilite < 0 || probabilite > 1)
      System.err.println("Probabilit� de crossOver non comprise entre 0 et 1");
    else {
      this.probabilite = probabilite;
    }
  }

  @Override
  public abstract List<IIndividu> crossOver(IIndividu individu1, IIndividu individu2);

  /** @return true avec une probabilit� p */
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

/** @author Momo && Dim Impl�mentation partielle et abstraite de IEnvironnement */
abstract class Environnement implements IEnvironnement {

  // VARIABLES D'INSTANCES :

  /** Le nom de l'environement */
  protected String name;

  /** Constructeur par d�faut */
  protected Environnement() {}

  /**
   * Constructeur basique
   *
   * @param name
   */
  protected Environnement(String name) {
    this.name = name;
  }

  public abstract double evaluerIndividu(IIndividu individu);

  public abstract boolean isValid(IIndividu individu);

  public String toString() {
    return "Environnement [name=" + name + "]";
  }
}

/** @author Momo && Dim Impl�mentation partielle et abstraite de IIndividu */
abstract class Individu implements IIndividu {

  // VARIABLES D'INSTANCES :

  /** L'identifiant correspondant au type de l'individu */
  protected int type;

  /** Le nom de l'individu */
  protected String name;

  /** La liste des caract�ristiques de l'individu */
  protected List<ICaracteristique> caracteristiques;

  /** Constructeur par d�faut */
  protected Individu() {
    caracteristiques = new ArrayList<>();
  }

  /**
   * Construteur basique
   *
   * @param name
   * @param caracteristiques
   */
  protected Individu(String name, List<ICaracteristique> caracteristiques) {
    this.type = -1;
    this.name = name;
    this.caracteristiques = caracteristiques;
  }

  /**
   * Constructeur par recopie (pour le clone() ).
   *
   * @param i
   */
  protected Individu(Individu i) {
    this.type = i.getType();
    this.name = new String(i.getName());
    this.caracteristiques = new ArrayList<ICaracteristique>();
    for (ICaracteristique c : i.getListCaracteristique()) {
      this.caracteristiques.add(c.clone());
    }
  }

  @Override
  public List<ICaracteristique> getListCaracteristique() {
    return this.caracteristiques;
  }

  @Override
  public void setListCaracteristiques(List<ICaracteristique> caracs) {
    this.caracteristiques = caracs;
  }

  @Override
  public ICaracteristique getCaracteristique(int index) {
    return this.getListCaracteristique().get(index);
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

  public abstract Individu clone();

  /*public boolean equals(Object o){
    boolean retour = false;
    if(o instanceof Individu){
      retour = true;
      Individu i = (Individu) o;
      for(int j=0; j<this.getNombreCaracteristiques(); j++){
        retour = retour && this.getCaracteristique(j).equals(i.getCaracteristique(j));
      }
    }
    return retour;
  }*/

  public String toString() {
    String retour = "Individu " + name + ", caracteristiques = ";
    for (ICaracteristique c : this.getListCaracteristique()) {
      retour += "\n" + "     " + c;
    }
    return retour;
  }
}

/** @author Dim && Momo Impl�mentation partielle et abstraite de IMutation */
abstract class Mutation implements IMutation {

  // VARIABLES D'INSTANCE

  /** La probablit� que la mutation ait lieu (comprit entre 0 et 1) */
  protected double probabilite;

  /** Constructeur par d�faut */
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

  /**
   * @param caracteristique
   * @return true si la caract�ristique contient plus de bits que de bits � muter
   */
  public abstract boolean mutationCaracteristiquePossible(ICaracteristique caracteristique);

  /** @return true avec une probabilit� p */
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

/** @author Dim && Momo Impl�mentation partielle et abstraite de IPopulation */
abstract class Population implements IPopulation {

  // VARIABLES D'INSTANCES

  /** La liste de tous les individus */
  protected List<IIndividu> individus;

  /** Le nombre d'individus souhait�s */
  protected int nombreIndividusSouhaite;

  /** L'environement dans lequel �volue la population */
  protected IEnvironnement environnement;

  /** Constructeur par d�faut */
  protected Population() {
    individus = new ArrayList<>();
  }

  /**
   * Constructeur basique
   *
   * @param nombreIndividusSouhaites
   * @param environnement
   */
  protected Population(int nombreIndividusSouhaites, IEnvironnement environnement) {
    this.nombreIndividusSouhaite = nombreIndividusSouhaites;
    this.environnement = environnement;
    individus = new ArrayList<>();
    this.generer();
  }

  @Override
  public void ajouterIndividu(IIndividu individu) {
    this.getListIndividus().add(individu);
  }

  @Override
  public double evaluerIndividu(IIndividu individu) {
    double retour = this.getEnvironnement().evaluerIndividu(individu);
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
  public IEnvironnement getEnvironnement() {
    return this.environnement;
  }

  @Override
  public void setEnvironnement(IEnvironnement environnement) {
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

/** @author Dim && Momo Impl�mentation partielle et abstraite de ISelection */
abstract class Selection implements ISelection {

  // VARIABLES D'INSTANCE
  /** Le nombre d'individus � garder lors d'une selection (>=1) */
  protected int nbIndivus;

  /** Constructeur par d�faut */
  protected Selection() {}

  /**
   * Constructeur basique
   *
   * @param nbIndividus Le nombre d'individus � selectionner @ Si ce nombre < 1
   */
  protected Selection(int nbIndividus) {
    if (nbIndividus < 1) System.err.println("Nombre d'individus � selectionner < 1");
    else this.nbIndivus = nbIndividus;
  }

  @Override
  public boolean selectionPossible(IPopulation population) {
    return (this.nbIndivus <= population.getTailleEffective());
  }

  @Override
  public abstract List<IIndividu> selectionner(IPopulation population);
}

/** @author Momo && Dim Impl�mentation partielle et abstraite de ISelectionNaturelle */
abstract class SelectionNaturelle implements ISelectionNaturelle {

  /** La selection qui selectionne les individus � croiser et � muter */
  protected ISelection selectionInitiale;

  /**
   * La selection qui selectionne les individus � garder dans la population d'une g�n�ration �
   * l'autre
   */
  protected ISelection selectionFinale;

  /** L'objet CrossOver qui permet de croiser certains individus */
  protected ICrossOver crossOver;

  /**
   * L'objet de Mutation qui permet de g�n�rer de tout nouveaux individus en modifiant leur code
   * g�n�tique
   */
  protected IMutation mutation;

  /** La population sur laquelle la Selection Naturelle va s'effectuer */
  protected IPopulation population;

  protected SelectionNaturelle(
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

  /**
   * @return true si il y a au moins deux individus dans la population, ie que le crossOver peut se
   *     faire
   */
  protected boolean crossOverPossible() {
    return this.getPopulation().getTailleEffective() >= 2;
  }
}
