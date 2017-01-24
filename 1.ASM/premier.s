/* -- premier.s */
/* Ceci est un commentaire */
.global main /* 'main' est un point d entree est doit etre global */
 
main:          /* ceci est l entree principale */
    mov r0, #2 /* Mettre le chiffre 2 dans le registre r0 */
    bx lr      /* retour du principal */
