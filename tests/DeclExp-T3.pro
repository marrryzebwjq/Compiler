programme troisiemetest: { prog qui fait la somme de n nombres saisis par l'utilisateur }

var ent i, n, x, s ;
    bool b;

debut
    lire(n) ; i := n ; s := 0 ; b := faux;
    ttq i > 0 faire
        lire(x) ; s := s + x ;
        i := i - 1 ;
    fait ;
    ecrire (s)
fin
