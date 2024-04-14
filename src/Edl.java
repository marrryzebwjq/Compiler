import java.io.*;

/**
 * @author Louis-Quentin NOE, Marie POINT, Blaise JULES
 * @version 2024
 */


public class Edl {

	// nombre max de modules, taille max d'un code objet d'une unite
	static final int MAXMOD = 5, MAXOBJ = 1000;
	// nombres max de references externes (REF) et de points d'entree (DEF)
	// pour une unite
	private static final int MAXREF = 10, MAXDEF = 10;

	// typologie des erreurs
	private static final int FATALE = 0, NONFATALE = 1;

	// valeurs possibles du vecteur de translation
	private static final int TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc;

	// table des translations de vecteur
	static private class TransVecElt {
		int decDon = 0;
		int decCode = 0;
	}

	// tableau des translations
	static TransVecElt[] tabVec;

	// table des définitions de procédures
	static private class DicoDefElt {
		String nomProc;
		int adPo = -1;
		int nbParam = -1;
	}

	// tableau des translations
	static DicoDefElt[] tabDef;

	// table des références
	static private class TransDefElt {
		int nbAd = 0;
		int[] tabAdr;
	}

	// tableau des références
	static TransDefElt[] tabRef;

	// vecteur de translation
	static private class TransExtElt {
		int po;
		int type;
	}

	// Variables de finalisation
	static int ipo;   // Nombre code total
	static int nMod;  // Nombre modules
	static int nbErr; // Nombre erreurs
	static int nbDef;
	static String nomProg;
	static String[] nomUnites;

	// utilitaire de traitement des erreurs
	// ------------------------------------
	static void erreur(int te, String m) {
		System.out.println(m);
		if (te == FATALE) {
			System.out.println("ABANDON DE L'EDITION DE LIENS");
			System.exit(1);
		}
		nbErr = nbErr + 1;
	}

	// utilitaire de remplissage de la table des descripteurs tabDesc
	// --------------------------------------------------------------
	static void lireDescripteurs() {
		String s;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");
		nomProg = s;
		nomUnites[0] = nomProg;

		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1)
					+ " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				nMod = nMod + 1;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);
				nomUnites[nMod] = s;

				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
			}
		}
	}

	// utilitaire de récupération depuis la table des définitions
	static int presentDef(String ident) {
		for (int i = nbDef; i > 0; --i) {
			if (tabDef[i].nomProc.equals(ident)) return i;
		}
		return 0;
	}


	static void constMap() {
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomProg
					+ ".map impossible");
		// pour construire le code concatene de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];

		// récupération des objets
		for (int i = 0; i <= nMod; ++i) {
			// Copie des modules dans po
			InputStream f = Lecture.ouvrir(nomUnites[i] + ".obj");
			if (f == null) {
				erreur(FATALE, "Fichier \"" + nomUnites[i] + ".obj\" n'éxiste pas!!");
			}

			// Récupération des transExt
			Descripteur desc = tabDesc[i];
			TransExtElt[] transTab = new TransExtElt[desc.getNbTransExt()];

			// Lecture des translations
			int nbTrans = 0;
			while (!Lecture.finFichier(f) && nbTrans != desc.getNbTransExt()) {
				transTab[nbTrans] = new TransExtElt();
				transTab[nbTrans].po = Lecture.lireInt(f);
				transTab[nbTrans].type = Lecture.lireInt(f);
				++nbTrans;
			}

			// Fin de fichier non attendu
			if (nbTrans != desc.getNbTransExt()) {
				erreur(FATALE, "Il n'y a pas le même nombre de translations dans le descripteur que dans le " +
						"fichier \"" + nomUnites[i] + ".obj" + "\" !!");
			}

			// Lecture des po
			while (!Lecture.finFichier(f)) {
				po[ipo++] = Lecture.lireInt(f);
			}

			// Fermeture
			Lecture.fermer(f);

			// Application des translations
			for (int k = 0; k < desc.getNbTransExt(); ++k) {
				switch (transTab[k].type) {
					// Translation de donnée
					case TRANSDON: {
						po[transTab[k].po + tabVec[i].decCode] += tabVec[i].decDon;
						break;
					}

					// Translation de code
					case TRANSCODE: {
						po[transTab[k].po + tabVec[i].decCode] += tabVec[i].decCode;
						break;
					}

					// Référencement
					case REFEXT: {
						po[transTab[k].po + tabVec[i].decCode] = tabRef[i].tabAdr[po[transTab[k].po] - 1];
						break;
					}
				}
			}
		}

		// écriture dans le fichier objet
		for (int i = 1; i < ipo; ++i) {
			Ecriture.ecrireInt(f2, po[i]);
			Ecriture.ecrireStringln(f2);
		}

        // modification nombre de variables globales
        int numGlobals = 0;
        for (int i = 0; i <= nMod; ++i) {
            numGlobals += tabDesc[i].getTailleGlobaux();
        }
        po[2] = numGlobals;

		// fermeture
		Ecriture.fermer(f2);

		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(ipo - 1, po, nomProg + ".ima");
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");

		// Initialisation
		tabDesc = new Descripteur[MAXMOD + 1];
		tabVec = new TransVecElt[MAXMOD + 1];
		tabDef = new DicoDefElt[61];
		tabRef = new TransDefElt[MAXMOD + 1];
		ipo = 1;
		nMod = 0;
		nbErr = 0;
		nbDef = 0;
		nomUnites = new String[MAXMOD + 1];

		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs();

		// trans + dico
		tabVec[0] = new TransVecElt();
		tabVec[0].decDon = 0;
		tabVec[0].decCode = 0;
		for (int i = 0; i <= nMod; ++i) {
			// Récupération de la table des définitions
			Descripteur desc = tabDesc[i];

			// Vérifie si le nombre de références et dé définitions par unité n'excède pas MAXREF et MAXDEF
			if (desc.getNbDef() > MAXDEF) {
				erreur(NONFATALE, "L'unité \"" + nomUnites[i]
						+ "\" a plus de définitions que supporté : (" + desc.getNbDef() + " > " + MAXDEF + ") !!");
				nbErr++;
			}
			if (desc.getNbRef() > MAXREF) {
				erreur(NONFATALE, "L'unité \"" + nomUnites[i]
						+ "\" a plus de définitions que supporté : (" + desc.getNbRef() + " > " + MAXREF + ") !!");
				nbErr++;
			}

			// Remplissage de la table des translations
			if (i > 0) {
				tabVec[i] = new TransVecElt();
				tabVec[i].decDon = tabVec[i - 1].decDon + tabDesc[i - 1].getTailleGlobaux();
				tabVec[i].decCode = tabVec[i - 1].decCode + tabDesc[i - 1].getTailleCode();
			}

			// Remplissage de la table des définitions
			for (int k = 1; k <= desc.getNbDef(); ++k) {
				if (presentDef(desc.getDefNomProc(k)) != 0) {
					erreur(NONFATALE, "La procédure \"" + desc.getDefNomProc(k) + "\" a déjà été définie!");
					nbErr++;
				} else {
					tabDef[nbDef + 1] = new DicoDefElt();
					tabDef[nbDef + 1].nomProc = desc.getDefNomProc(k);
					tabDef[nbDef + 1].adPo = desc.getDefAdPo(k) + tabVec[i].decCode;
					tabDef[nbDef + 1].nbParam = desc.getDefNbParam(k);
					nbDef++;
				}
			}
		}

		// adFinale
		for (int i = 0; i <= nMod; ++i) {
			Descripteur desc = tabDesc[i];
			tabRef[i] = new TransDefElt();
			tabRef[i].nbAd = desc.getNbRef();
			tabRef[i].tabAdr = new int[tabRef[i].nbAd];

			// Association références
			for (int k = 1; k <= desc.getNbRef(); ++k) {
				String nomRef = desc.getRefNomProc(k);
				int idDef = presentDef(nomRef);
				if (idDef == 0) {
					erreur(NONFATALE, "La procédure \"" + nomRef + "\" n'est définie dans aucun module!");
					nbErr++;
				} else {
					if (desc.getRefNbParam(k) != tabDef[idDef].nbParam) {
						erreur(NONFATALE, "La référence \"" + nomRef + "\" n'a pas le même nombre de paramètres (" +
								desc.getRefNbParam(k) + " ) avec la définition trouvée (" + tabDef[idDef].nbParam +
								") !!");
						nbErr++;
					}
					tabRef[i].tabAdr[k - 1] = tabDef[idDef].adPo;
				}
			}

		}

		// Finalisation des erreurs
		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}

		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap();
		System.out.println("Edition de liens terminee");
	}
}
