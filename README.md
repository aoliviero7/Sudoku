# Sudoku P2P

Progetto accademico di [**Alessandro Oliviero**](https://github.com/aoliviero7)

Corso di Laurea Magistrale in Informatica - Curriculum Cloud Computing: **Architetture distribuite per il Cloud**

Professori: **Alberto Negro**, **Carmine Spagnuolo**, **Gennaro Cordasco**

[Dipartimento di Informatica](http://www.di.unisa.it) - [Università degli Studi di Salerno](https://www.unisa.it/) (Italia)

## Definizione del problema

In questo progetto è stata creata un'implementazione del gioco del sudoku, utilzzando una rete P2P. Ogni utente può piazzare un numero all'interno del sudoku; se non è già piazzato prende 1 punto, se è già piazzato ma è comunque corretto prende 0 punti, in altri casi riceve -1 punto. I sudoku sono basati su una matrice 9 x 9. Tutti gli utenti che giocano ad un sudoku vengono automaticamente informati quando un utente aumenta il proprio punteggio e quando il gioco è terminato. Il sistema consente agli utenti di generare (automaticamente) una nuova sfida Sudoku identificata da un nome, partecipare a una sfida utilizzando un nickname, ottenere la matrice intera che descrive il Sudoku e inserire un numero.

![](images/sudoku.png)

## Soluzione del progetto

Il perno principale su cui si basa il progetto è l'uso di una DHT (distributed hash table) che viene usata per poter salvare coppie chiave-valore contenenti il nome della
