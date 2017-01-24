# Assembleur

Avec un Raspberry Pi, ouvrez une fenetre "Terminal"

Créer un répertoire de travail
```
$ mkdir asm
```

Ouvrir un fichier avec l'éditeur nano
```
$ nano premier.s
```

copier le contenu ci-dessous
```
/* -- premier.s */
/* Ceci est un commentaire */
.global main /* 'main' est un point d entree est doit etre global */
 
main:          /* ceci est l entree principale */
    mov r0, #2 /* Mettre le chiffre 2 dans le registre r0 */
    bx lr      /* retour du principal */
```

compiler le code source premier avec as
```
$ as -o premier.o premier.s
```

créer l'éxécutable avec gcc
```
$ gcc -o premier premier.o
```

Éxécuter le resultat de la compilation (sans aucun retour d'afficher)
```
$ ./premier
```

Éxécuter le resultat de la compilation avec un retour
```
$ ./premier; echo $?
```


http://thinkingeek.com/2013/01/09/arm-assembler-raspberry-pi-chapter-1/

