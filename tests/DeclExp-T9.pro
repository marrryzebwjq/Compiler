programme neuvieme:    { Test cond }

const moinscinq=-5;
var ent n ; ent m;
    bool b;

debut
    lire(n) ; lire(b);

    cond
        b=vrai et n=1 : m:= 10,
        b=vrai et n=2 : m:=20,
        b=faux         : m:=500000
    aut
        m:=30; ecrire(m)
    fcond ;

    ecrire (n)
fin


debut
    lire(n) ; lire(b);

    cond
        b=vrai et n=1 : m:= 10
    fcond ;

    ecrire (n)
fin

debut
    lire(n) ; lire(b);

    cond
        b=vrai et n=1 : m:= 10
    aut
        m:=30
    fcond ;

    ecrire (n)
fin
