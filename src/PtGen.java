/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/


import org.antlr.runtime.Lexer;

import javax.rmi.CORBA.Util;
import java.io.*;

/**
 * classe de mise en oeuvre du compilateur
 * =======================================
 * (verifications semantiques + production du code objet)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class PtGen {
    

    // constantes manipulees par le compilateur
    // ----------------------------------------

	private static final int 
	
	// taille max de la table des symboles
	MAXSYMB=300,

	// codes MAPILE :
	RESERVER=1,EMPILER=2,CONTENUG=3,AFFECTERG=4,OU=5,ET=6,NON=7,INF=8,
	INFEG=9,SUP=10,SUPEG=11,EG=12,DIFF=13,ADD=14,SOUS=15,MUL=16,DIV=17,
	BSIFAUX=18,BINCOND=19,LIRENT=20,LIREBOOL=21,ECRENT=22,ECRBOOL=23,
	ARRET=24,EMPILERADG=25,EMPILERADL=26,CONTENUL=27,AFFECTERL=28,
	APPEL=29,RETOUR=30,

	// codes des valeurs vrai/faux
	VRAI=1, FAUX=0,

    // types permis :
	ENT=1,BOOL=2,NEUTRE=3,

	// categories possibles des identificateurs :
	CONSTANTE=1,VARGLOBALE=2,VARLOCALE=3,PARAMFIXE=4,PARAMMOD=5,PROC=6,
	DEF=7,REF=8,PRIVEE=9,

    //valeurs possible du vecteur de translation 
    TRANSDON=1,TRANSCODE=2,REFEXT=3;


    // utilitaires de controle de type
    // -------------------------------
    /**
     * verification du type entier de l'expression en cours de compilation 
     * (arret de la compilation sinon)
     */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}
	/**
	 * verification du type booleen de l'expression en cours de compilation 
	 * (arret de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

	/**
	 * verification du type à gauche est le même que le type à droite lors d'une opération égalité / différence
	 * (arret de la compilation sinon)
	 */
	private static void verifMemeTypeGauche() {
		// Assertion, mauvaise utilisation de la fonction verifMemeTypeGauche
		assert (tCourLeft != NEUTRE);

		// Vérification
		if (tCourLeft != tCour)
			UtilLex.messErr("types gauche et droite différents! gauche=\"" + tCourLeft + "\", droite=\"" + tCour + "\"");

		// Réinitialisation du type à gauche pour tests d'assertion
		tCourLeft = NEUTRE;
	}

    // pile pour gerer les chaines de reprise et les branchements en avant
    // -------------------------------------------------------------------

    private static TPileRep pileRep;  


    // production du code objet en memoire
    // -----------------------------------

    private static ProgObjet po;
    
    
    // COMPILATION SEPAREE 
    // -------------------
    //
    /** 
     * modification du vecteur de translation associe au code produit 
     * + incrementation attribut nbTransExt du descripteur
     *  NB: effectue uniquement si c'est une reference externe ou si on compile un module
     * @param valeur : TRANSDON, TRANSCODE ou REFEXT
	 */
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// descripteur associe a un programme objet (compilation separee)
	private static Descripteur desc;


	// autres variables fournies
	// -------------------------

	// MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE LETTRES
	public static String trinome = "MBassiNoePoint";

	private static int tCour; // type de l'expression compilee
	private static int tCourLeft; // type de l'expression à gauche lors d'une opération qui peut être entière-entière, ou booléenne-booléenne comme les tests d'égalité.
	private static int vCour; // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)

	private static int vAff = 0;
	private static int vFun = 0;
	private static int vAdr = 0;
	private static int nbrAdr = 0;
	private static boolean inLocalContext = false;

	// TABLE DES SYMBOLES
	// ------------------
	//
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

	// it = indice de remplissage de tabSymb
	// bc = bloc courant (=1 si le bloc courant est le programme principal)
	private static int it, bc;

	/**
	 * utilitaire de recherche de l'ident courant (ayant pour code UtilLex.numIdCourant) dans tabSymb
	 *
	 * @param borneInf : recherche de l'indice it vers borneInf (=1 si recherche dans tout tabSymb)
	 * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code : UtilLex.numIdCourant de l'ident
	 * @param cat : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type : ENT, BOOL ou NEUTRE
	 * @param info : valeur pour une constante, ad d'exécution pour une variable, etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 *  utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() { 
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}
    

	/**
	 * initialisations
	 */
	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;

		// Index pour l'affectation
		vAff = 0;
		vFun = 0;

		// Variable pour gérer les adresses des variables.
		vAdr = 0;
		nbrAdr = 0;
		inLocalContext = false;

		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep();
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();
		
		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();
	
		// initialisation du type de l'expression courante
		tCour = NEUTRE;

	} // initialisations

	/**
	 *  code des points de generation A COMPLETER
	 *  -----------------------------------------
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {
	
		switch (numGen) {
			case 0: // Initialisation
			{
				initialisations();
				break;
			}


			case 1: // A chaque ident (nom de variable lu, on l'ajoute à la table des idents si pas déjà présent.)
			{
				if (presentIdent(1) == 0) {
					if(inLocalContext)
						placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, vAdr++);
					else
						placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, vAdr++);
					nbrAdr++;
				} else {    // Si la variable a déjà été déclarée précédemment, message d'erreur !
					UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" a déjà déclaré précédemment !");
				}
				break;
			}

			case 2: // Réserver nbrAdr espaces mémoire.
			{
				po.produire(RESERVER);
				if( bc == 1 ) {
					po.produire(nbrAdr);        // Le nombre de variables lues en global.
					nbrAdr = 0;                 // On réinitialise pour la prochaine reconnaissance.
				}
				else {
					po.produire(it - bc + 2);        // Le nombre de variables lues en local.
				}
				break;
			}

			case 3: // Vérification si variable globale ou locale
			{
				int ind = presentIdent(1);
				if (ind == 0) {
					UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" n'a pas été déclaré !");
				} else {
					if (tabSymb[ind].categorie != VARGLOBALE && tabSymb[ind].categorie != VARLOCALE) {
						UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" n'est pas une variable !");
					}
				}
				vAff = ind;
				break;
			}

			case 4: // Création d'une constante
			{
				int ind = presentIdent(1);
				if (ind == 0) {
					placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);
				} else {
					UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" a déjà déclaré précédemment !");
				}
				break;
			}

			case 5: // Affectation
			{
				if (vAff == 0) {
					UtilLex.messErr("Attention !! La variable que vous essayez de réserver n'existe pas !"); // Cette erreur ne devrait pas apparaître
				} else {
					if (tabSymb[vAff].type != tCour) {
						UtilLex.messErr("Les 2 membres gauche et droite de l'affection ne sont pas du même type !");
					}

					if (tabSymb[vAff].categorie == VARGLOBALE) {
						po.produire(AFFECTERG);
						po.produire(tabSymb[vAff].info);
					} else if (tabSymb[vAff].categorie == VARLOCALE) {
						po.produire(AFFECTERL);
						po.produire(tabSymb[vAff].info);
						po.produire(0); // Variable locale
					} else if (tabSymb[vAff].categorie == PARAMMOD) {
						po.produire(AFFECTERL);
						po.produire(tabSymb[vAff].info);
						po.produire(1);
					} else {
						UtilLex.messErr("Catégorie non prévue !");
					}
				}
				vAff = 0;
				break;
			}

			case 6: // Déclarer une procédure
			{
				int ind = presentIdent(1);
				if (ind == 0) {
					placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, po.getIpo() + 1);
					placeIdent(-1, PRIVEE, NEUTRE, 0);
					bc = it + 1;
				} else {
					UtilLex.messErr("Attention !! procédure \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" a déjà déclaré précédemment !");
				}
				break;
			}

			case 7: // Ajout d'un paramètre fixe
			{
				int ind = presentIdent(bc);
				if (ind == 0) {
					placeIdent(UtilLex.numIdCourant, PARAMFIXE, tCour, it + 1 - bc);
				} else {
					UtilLex.messErr("Attention !! paramètre \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" a déjà déclaré précédemment !");
				}

				break;
			}

			case 8: // Ajout d'un paramètre mod
			{
				int ind = presentIdent(bc);
				if (ind == 0) {
					placeIdent(UtilLex.numIdCourant, PARAMMOD, tCour, it + 1 - bc);
				} else {
					UtilLex.messErr("Attention !! paramètre \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" a déjà déclaré précédemment !");
				}

				break;
			}

			case 9: // Fin de la déclaration des paramètres
			{
				// Modification de la table des symboles pour affecter le nombre de paramètres
				EltTabSymb elt = tabSymb[bc - 1];
				elt.info = it - bc;
				break;
			}

			case 10: // Vérification si type de paramètre fixe est correcte par rapport à la table des symboles
			{
				// Vérification du type de la variable avec le type de paramètre
				EltTabSymb elt = tabSymb[bc + nbrAdr];
				if (elt.type != tCour) {
					UtilLex.messErr("Attention !! le type du paramètre  \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" est différente avec la déclaration du type de paramètre !");
					break;
				}

				break;
			}

			case 11: // Passage en argument d'une variable et vérification du type avec la table des symboles
			{
				// Vérification de l'éxistence de la variable
				int ind = presentIdent(1);
				if (ind == 0) {
					UtilLex.messErr("Attention !! paramètre \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" non déclarée !");
					break;
				}

				EltTabSymb var = tabSymb[ind];
				tCour = var.type;

				// Vérification du type de la variable
				EltTabSymb arg = tabSymb[bc + nbrAdr];
				if (arg.type != tCour) {
					UtilLex.messErr("Attention !! le type du paramètre  \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" est différente avec la déclaration du type de paramètre !");
				}

				// Récupération du contenu de la variable
				switch (var.categorie) {
					case VARGLOBALE: {
						po.produire(EMPILERADG);
						po.produire(var.info);
						break;
					}

					case VARLOCALE: {
						po.produire(EMPILERADL);
						po.produire(var.info);
						po.produire(0);
						break;
					}

					case PARAMMOD: {
						po.produire(EMPILERADL);
						po.produire(var.info);
						po.produire(1);
						break;
					}

					default: {
						UtilLex.messErr("Attention !! la carégorie de l'identifiant  \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" non reconnue !");
						break;
					}
				}

				break;
			}

			case 12: // Fin de l'appel de la fonction
			{
				po.produire(APPEL);
				po.produire(tabSymb[vFun + 0].info);
				po.produire(tabSymb[vFun + 1].info);

				vFun = 0;
				nbrAdr = 0;
				break;
			}

			case 13: // Indexation de la procédure pour récupérer son ipo
			{
				vFun = presentIdent(1);
				if (vFun == 0) {
					UtilLex.messErr("Attention !! procédure \""UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" non déclarée !");
					break;
				}

				break;
			}

			case 47: // Début bincond du saut des déclarations des procédures vers les instructions principales
			{
				po.produire(BINCOND);
				po.produire(0);
				pileRep.empiler(po.getIpo());
				break;
			}

			case 48: // Fin bincond du saut des déclarations des procédures vers les instructions principales
			{
				int ipoBincond = pileRep.depiler();
				po.modifier(ipoBincond, po.getIpo() + 1);
			}

			case 49: // Type entier
			{
				tCour = ENT;
				break;
			}

			case 50: // Type booléen
			{
				tCour = BOOL;
				break;
			}

			case 51: // Lecture
			{

				int index_lect = presentIdent(1);
				if (index_lect == 0) {
					UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" n'a pas été déclaré !");
				} else {
                    EltTabSymb elt = tabSymb[index_lect];

                    // Erreur -> Type non reconnu
                    if (elt.type != ENT && elt.type != BOOL) {
                        UtilLex.messErr("Type de variable non reconnu !");
                        break;
                    }

                    // Erreur -> Lecture dans une constante
                    if (elt.categorie == CONSTANTE) {
                        UtilLex.messErr("Impossible d'affecter une valeur à une constante !");
                        break;
                    }

                    // Erreur -> Catégorie non reconnue
					if (elt.categorie != VARGLOBALE && elt.categorie != VARLOCALE && elt.categorie != PARAMMOD) {
						UtilLex.messErr("Catégorie de variable non reconnue !");
					}

                    // Lecture en fonction de la type de variable
                    if (elt.type == ENT) {
                        po.produire(LIRENT);
                    } else if (elt.type == BOOL) {
                        po.produire(LIREBOOL);
                    }

                    // Affectation en fonction de la catégorie de la variable
                    if (elt.categorie == VARGLOBALE) {
                        po.produire(AFFECTERG);
                        po.produire(elt.info);
					} else if (elt.categorie == VARLOCALE) {
						po.produire(AFFECTERL);
						po.produire(elt.info);
						po.produire(0);
					} else if (elt.categorie == PARAMMOD) {
						po.produire(AFFECTERL);
						po.produire(elt.info);
						po.produire(1);
					}

				}
				break;
			}


			case 52: // Ecriture
			{
				if (tCour == ENT) {
					po.produire(ECRENT);
				} else if (tCour == BOOL) {
					po.produire(ECRBOOL);
				}
				break;
			}

			case 91:  // Marqueur du début de chaînage
			{
				pileRep.empiler(0);
				break;
			}

			case 92:  // Début branchement condition du while
			{
				pileRep.empiler(po.getIpo() + 1);
				break;
			}

			case 93:  // Fin si
			{
				int IpoBinouBsi = pileRep.depiler();
				po.modifier(IpoBinouBsi, po.getIpo() + 1);
				break;
			}

			case 94:  // Fin tant que
			{
				po.produire(BINCOND);
				po.produire(0);
				int ipoBsifaux = pileRep.depiler();
				po.modifier(ipoBsifaux, po.getIpo() + 1);
				int ipoDebutWhile = pileRep.depiler();
				po.modifier(po.getIpo(), ipoDebutWhile);
				break;
			}

			case 95:  // Fin du chaînage
			{
				int ipoBincond = pileRep.depiler();
				while (ipoBincond != 0) { // Résolution des bincond en prenant po[bincondi] = ipo + 1
					int nextInd = po.getElt(ipoBincond);
					po.modifier(ipoBincond, po.getIpo() + 1);
					ipoBincond = nextInd;
				}
				break;
			}

			case 96:  // Chaînage
			{
				po.produire(BINCOND);
				po.produire(0);
				int ipoBsifaux = pileRep.depiler(); // Dépilement du bsifaux pour le modifier en po[bsifaux] = ipo + 1
				po.modifier(ipoBsifaux, po.getIpo() + 1);
				int ipoBincond = pileRep.depiler(); // Dépilement du bincond pour le chaînage en po[bincond(i)] = bincond(i-1)
				po.modifier(po.getIpo(), ipoBincond);
				pileRep.empiler(po.getIpo());
				break;
			}
			
			case 97:  // Mémorisation de tcour
			{
				tCourLeft = tCour;
				break;
			}


			case 98:  // Début d'un si
			{
				verifBool();
				po.produire(BSIFAUX);
				po.produire(0);
				pileRep.empiler(po.getIpo()); // Empilement de l'indice de l'argument de bsifaux
				break;
			}

			case 99:  // Vérification expression est booléen
			{
				verifBool();
				break;
			}

			case 100: // Vérification expression est entier
			{
				verifEnt();
				break;
			}

			case 101: // Primaire -> Récupération de ident dans une expression
			{
				int index = presentIdent(1);
				if (index == 0) {
					UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" n'a pas été déclaré !");
				}

				EltTabSymb e = tabSymb[index];

				switch (e.categorie) {
					case CONSTANTE: {
						tCour = e.type;
						po.produire(EMPILER);
						po.produire(e.info);
						break;
					}

					case VARGLOBALE: {
						tCour = e.type;
						po.produire(CONTENUG);
						po.produire(e.info);
						break;
					}

					case VARLOCALE: {
						tCour = e.type;
						po.produire(CONTENUL);
						po.produire(e.info);
						po.produire(0);
						break;
					}

					case PARAMFIXE: {
						tCour = e.type;
						po.produire(CONTENUL);
						po.produire(e.info);
						po.produire(1);
						break;
					}

					default: {
						UtilLex.messErr("Attention !! \"" + UtilLex.chaineIdent(UtilLex.numIdCourant) + "\" n'a pas une catégorie correcte!");
						break;
					}
				}

				break;
			}

			case 102: // Primaire -> Empilement d'une valeur écrite
			{
				po.produire(EMPILER);
				po.produire(vCour);
				break;
			}

			case 103: // exp5 -> 'div'
			{
				verifEnt();

				po.produire(DIV);
				break;
			}

			case 104: // exp5 -> '*'
			{
				verifEnt();

				po.produire(MUL);
				break;
			}

			case 105: // exp4 -> '-'
			{
				verifEnt();

				po.produire(SOUS);
				break;
			}

			case 106: // exp4 -> '+'
			{
				verifEnt();

				po.produire(ADD);
				break;
			}

			case 107: // exp3 -> '<='
			{
				verifEnt();

				po.produire(INFEG);
				break;
			}

			case 108: // exp3 -> '<'
			{
				verifEnt();

				po.produire(INF);
				break;
			}

			case 109: // exp3 -> '>='
			{
				verifEnt();

				po.produire(SUPEG);
				break;
			}

			case 110: // exp3 -> '>'
			{
				verifEnt();

				po.produire(SUP);
				break;
			}

			case 111: // exp3 -> '<>'
			{
				verifMemeTypeGauche();

				po.produire(DIFF);
				break;
			}

			case 112: // exp3 -> '='
			{
				verifMemeTypeGauche();

				po.produire(EG);
				break;
			}

			case 113: // exp2 -> 'non'
			{
				verifBool();

				po.produire(NON);
				break;
			}

			case 114: // exp2 -> 'et'
			{
				verifBool();

				po.produire(ET);
				break;
			}

			case 115: // exp2 -> 'ou'
			{
				verifBool();

				po.produire(OU);
				break;
			}

			case 116: // Affectation de la valeur courante
			{
				tCour = ENT;
				vCour = UtilLex.valEnt;
				break;
			}

			case 117: // Affectation de la valeur courante négative
			{
				tCour = ENT;
				vCour = -UtilLex.valEnt;
				break;
			}

			case 118: // Affectation de la valeur booléenne vraie
			{
				tCour = BOOL;
				vCour = VRAI;
				break;
			}

			case 119: // Affectation de la valeur booléenne fausse
			{
				tCour = BOOL;
				vCour = FAUX;
				break;
			}

			case 255: {
				po.produire(ARRET);
				afftabSymb();  // Affichage de la table des symboles en fin de compilation
				po.constGen(); // Ecriture du fichier de mnémoniques
				po.constObj(); // Ecriture du fichier objet
				break;
			}

			default:  // Point de génération incorrect
			{
				System.out.println("Point de generation non prevu dans votre liste");
				break;
			}

		}
	}
}
    
    
    
    
    
    
    
    
    
    
    
    
    
 
