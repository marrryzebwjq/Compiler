programme dixieme:    { Test cond avec un aut }

var ent n ; ent m;
    bool b;

debut
    lire(n) ; lire(b);

    cond
        b=vrai et n=1 : m:= 10
    aut
        m:=30
    fcond ;

    ecrire (n)
fin
