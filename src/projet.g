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


unite : unitprog {PtGen.pt(255);} EOF
      | unitmodule {PtGen.pt(255);} EOF
  ;
  
unitprog
  : 'programme' {PtGen.pt(14);} ident ':'
     declarations  
     corps { System.out.println("succes, arret de la compilation"); }
  ;
  
unitmodule
  : 'module' {PtGen.pt(14);} ident  ':'
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident {PtGen.pt(16);} (',' ident {PtGen.pt(16);})* ptvg
  ;
  
partieref: 'ref'  specif (',' specif)* ptvg
  ;
  
specif  : ident {PtGen.pt(17);} ( 'fixe' '(' type {PtGen.pt(18);}  ( ',' type {PtGen.pt(18);}  )* ')' )?
                 ( 'mod'  '(' type {PtGen.pt(19);}   ( ',' type {PtGen.pt(19);}  )* ')' )?
  ;
  
consts  : 'const' ( ident '=' valeur {PtGen.pt(4);} ptvg )+
  ;
  
vars  : 'var' ( type ident {PtGen.pt(1);} ( ','  ident {PtGen.pt(1);} )* ptvg )+ {PtGen.pt(2);}
  ;
  
type  : 'ent' {PtGen.pt(49);}
  |     'bool' {PtGen.pt(50);}
  ;
  
decprocs: ({PtGen.pt(47);} decproc ptvg)+ {PtGen.pt(48);}
  ;
  
decproc : 'proc' ident {PtGen.pt(6);} parfixe? parmod? {PtGen.pt(9);} consts? vars? corps {PtGen.pt(46);}
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin'
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident {PtGen.pt(7);} ( ',' ident {PtGen.pt(7);} )*
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident {PtGen.pt(8);} ( ',' ident {PtGen.pt(8);} )*
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
  
inssi : 'si' expression {PtGen.pt(98);} 'alors' instructions ('sinon' {PtGen.pt(90);} instructions)? 'fsi' {PtGen.pt(93);}
  ;
  
inscond : 'cond' expression {PtGen.pt(91); PtGen.pt(98);} ':' instructions
          ({PtGen.pt(96);} ','  expression {PtGen.pt(98);} ':' instructions)*
          ({PtGen.pt(96);} 'aut' instructions | {PtGen.pt(93);})
          'fcond' {PtGen.pt(95);}
  ;
  
boucle  : 'ttq' {PtGen.pt(92);} expression {PtGen.pt(98);} 'faire' instructions 'fait' {PtGen.pt(94);}
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(51);} ( ',' ident {PtGen.pt(51);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(52);} ( ',' expression {PtGen.pt(52);} )* ')'
   ;
  
affouappel
  : ident {PtGen.pt(13);} ( {PtGen.pt(3);}   ':=' expression {PtGen.pt(5);}
            |   (effixes (effmods)?)? {PtGen.pt(12);}
           )
  ;
  
effixes : '(' (expression {PtGen.pt(10);}  (',' expression {PtGen.pt(10);} )*)? ')'
  ;
  
effmods :'(' (ident {PtGen.pt(11);}  (',' ident {PtGen.pt(11);} )*)? ')'
  ; 
  
expression: (exp1) ({PtGen.pt(99);} 'ou'  exp1 {PtGen.pt(115);} )*
  ;
  
exp1  : exp2 ({PtGen.pt(99);} 'et' exp2 {PtGen.pt(114);} )*
  ;
  
exp2  : 'non' exp2 {PtGen.pt(113);}
  | exp3
  ;
  
exp3  : exp4 
  ( {PtGen.pt(97); } '='  exp4 {PtGen.pt(112); PtGen.pt(50);}
  | {PtGen.pt(97); } '<>' exp4 {PtGen.pt(111); PtGen.pt(50);}
  | {PtGen.pt(100);} '>'  exp4 {PtGen.pt(110); PtGen.pt(50);}
  | {PtGen.pt(100);} '>=' exp4 {PtGen.pt(109); PtGen.pt(50);}
  | {PtGen.pt(100);} '<'  exp4 {PtGen.pt(108); PtGen.pt(50);}
  | {PtGen.pt(100);} '<=' exp4 {PtGen.pt(107); PtGen.pt(50);}
  ) ?
  ;
  
exp4  : exp5 
        ('+' {PtGen.pt(100);} exp5 {PtGen.pt(106);}
        |'-' {PtGen.pt(100);} exp5 {PtGen.pt(105);}
        )*
  ;
  
exp5  : primaire 
        (    '*'  {PtGen.pt(100);} primaire {PtGen.pt(104);}
          | 'div' {PtGen.pt(100);} primaire {PtGen.pt(103);}
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



	   
