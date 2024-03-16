programme cinquiemetest:    { Avec un cond plutôt }

const moinscinq=-5;
var ent i, n, x, s ;
    bool b, b1;

debut
    lire(n) ; i := n ; s := 0 ; b := faux;
    ttq i > 0 faire
        lire(x) ; s := s + x ;
        b1 := (x = moinscinq);
        cond b1 : 
            b := vrai
        fcond ;
        i := i - 1 ;
    fait ;
    ecrire (s, b)
fin


programme cinquiemetest:    { Avec un cond plutôt }

const moinscinq=-5;
var ent i, n, x, s ;
    bool b, b1;

debut
    lire(n) ; i := n ; s := 0 ; b := faux;
    ttq i > 0 faire
        ttq i > 0 faire
            lire(x) ; s := s + x ;
            b1 := (x = moinscinq);
            cond b1 : 
                b := vrai
            fcond ;
            i := i - 1 ;
        fait ;
        i := i - 1 ;
    fait ;
    ecrire (s, b)
fin