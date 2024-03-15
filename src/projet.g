// Grammaire du langage PROJET
// CMPL L3info 
// Nathalie Girard, Veronique Masson, Laurent Perraudeau
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog {PtGen.pt(255);} EOF
      |    unitmodule  EOF
  ;
  
unitprog
  : 'programme' ident ':'  
     declarations  
     corps { System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' ident ':' 
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident  (',' ident )* ptvg
  ;
  
partieref: 'ref'  specif (',' specif)* ptvg
  ;
  
specif  : ident  ( 'fixe' '(' type  ( ',' type  )* ')' )? 
                 ( 'mod'  '(' type  ( ',' type  )* ')' )? 
  ;
  
consts  : 'const' ( ident '=' valeur {PtGen.pt(4);} ptvg )+
  ;
  
vars  : 'var' ( type ident {PtGen.pt(1);} ( ','  ident {PtGen.pt(1);} )* ptvg {PtGen.pt(2);} )+
  ;
  
type  : 'ent' {PtGen.pt(49);}
  |     'bool' {PtGen.pt(50);}
  ;
  
decprocs: (decproc ptvg)+
  ;
  
decproc :  'proc'  ident  parfixe? parmod? consts? vars? corps 
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin' {PtGen.pt(120);}
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident  ( ',' ident  )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident  ( ',' ident  )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  |
  ;
  
inssi : 'si' expression {PtGen.pt(4);} 'alors' instructions ('sinon'  instructions)? 'fsi' 
  ;
  
inscond : 'cond' expression {PtGen.pt(98)} ':' instructions {PtGen.pt(97);}
          (','  expression {PtGen.pt(98)} ':' instructions {PtGen.pt(96);})*
          ('aut' instructions |  )
          'fcond' {PtGen.pt(95);}
  ;
  
boucle  : 'ttq' {PtGen.pt(53);} expression 'faire' instructions 'fait' 
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(51);} ( ',' ident {PtGen.pt(51);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(52);} ( ',' expression {PtGen.pt(52);} )* ')'
   ;
  
affouappel
  : ident ( {PtGen.pt(3); }   ':=' expression {PtGen.pt(5);}
            |   (effixes (effmods)?)?  
           )
  ;
  
effixes : '(' (expression  (',' expression  )*)? ')'
  ;
  
effmods :'(' (ident  (',' ident  )*)? ')'
  ; 
  
expression: (exp1) ({PtGen.pt(99);} 'ou'  exp1 {PtGen.pt(115);} )*
  ;
  
exp1  : exp2 ({PtGen.pt(99);} 'et' exp2 {PtGen.pt(114);} )*
  ;
  
exp2  : 'non' exp2 {PtGen.pt(113); PtGen.pt(50);}
  | exp3
  ;
  
exp3  : exp4 
  ( {PtGen.pt(100);} '='  exp4 {PtGen.pt(112); PtGen.pt(50);}
  | {PtGen.pt(100);} '<>' exp4 {PtGen.pt(111); PtGen.pt(50);}
  | {PtGen.pt(100);} '>'  exp4 {PtGen.pt(110); PtGen.pt(50);}
  | {PtGen.pt(100);} '>=' exp4 {PtGen.pt(109); PtGen.pt(50);}
  | {PtGen.pt(100);} '<'  exp4 {PtGen.pt(108); PtGen.pt(50);}
  | {PtGen.pt(100);} '<=' exp4 {PtGen.pt(107); PtGen.pt(50);}
  ) ?
  ;
  
exp4  : exp5 
        ('+' {PtGen.pt(100);} exp5 {PtGen.pt(106); PtGen.pt(49);}
        |'-' {PtGen.pt(100);} exp5 {PtGen.pt(105); PtGen.pt(49);}
        )*
  ;
  
exp5  : primaire 
        (    '*'  {PtGen.pt(100);} primaire {PtGen.pt(104); PtGen.pt(49);}
          | 'div' {PtGen.pt(100);} primaire {PtGen.pt(103); PtGen.pt(49);}
        )*
  ;
  
primaire: valeur {PtGen.pt(102);}
  | ident {PtGen.pt(101);}
  | '(' expression ')'
  ;
  
valeur  : nbentier {PtGen.pt(116);}
  | '+' nbentier {PtGen.pt(116);}
  | '-' nbentier {PtGen.pt(117);}
  | 'vrai' {PtGen.pt(118);}
  | 'faux' {PtGen.pt(119);}
  ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	   



	   
