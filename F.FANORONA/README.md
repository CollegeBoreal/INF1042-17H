# Fanorona

![alt tag](docs/Fanorona.png)

## Faire tourner le programme

Assurez vous que vous pointez sur le répertoire src pour utiliser les images
![alt tag](docs/Run-Acme.Main.png)

## Guide du Développeur

Fanorona is written in Java. Its UI Component is an Applet and mostly uses AWT;
- A StackLayout is a LayoutManager that arranges components in a vertical (or horizontal)
- A 64 BitSet allowing Data Representation and Manipulation of the board.
- A Tree Game Search using the [Alpha-Beta](https://chessprogramming.wikispaces.com/Alpha-Beta) Pruning search implementing: 
    * a mini-max evaluation
    * a Transposition Table
    * an Aspiration Search
    * an Internal Iterative Deepening [IID]
    * an alpha-beta enhancement called Principal Variation
    * a - Memory Test Driver reducing the calculation time. MTD(f)    

Some other Tree Search Documentation


 Strategy and board game programming  - David Eppstein's Courses at UCI - Lecture notes April 22, 1997
 Alpha-Beta Search  - David Eppstein's Courses at UCI

 Recherche arborescente (In French) Bruno Bouzy - Universite Rene Descartes
, 16 Novembre 2005

## Fanorona Bits Explanation

* An Excell Spreadsheet trying to explain the bit manipulation used in the game. 
![alt tag](docs/Fanorona Bits Explanation.xls)

## Tasks

- UI and Search Engine Separation



