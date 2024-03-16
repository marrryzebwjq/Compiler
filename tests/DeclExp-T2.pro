programme deuxiemetest: { prog qui calcule le maximum entre 2 nombres saisis par l'utilisateur }

var ent n1, n2, max ;

debut
    lire(n1) ; lire(n2) ; max := n1 ;
    si n2 > max alors max := n2 fsi ;
    ecrire (max)
fin
