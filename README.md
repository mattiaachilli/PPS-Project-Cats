# PPS-Project-Cats

## Che cos'è Cats e perché?

#### Scala è un linguaggio con un approccio ibrido che supporta sia la programmazione orientata agli oggetti che quella funzionale il ché non lo rende un linguaggio puramente funzionale. La libreria Cats si sforza di fornire astrazioni di programmazione puramente funzionale che siano soprattutto efficienti. L’obiettivo di Cats è fornire una base per un’ecosistema di librerie pure e tipizzate per supportare la programmazione funzionale in applicazioni Scala

## Obiettivo del progetto

#### Il progetto ha come obiettivo quello di studiare la libreria Cats, ciò che la contradistingue, ciò che fornisce, con una particolare attenzione a meccanismi relativi a concorrenza, I/O e di gestione delle risorse con il supporto di Cats-Effect. Sono stati realizzati esperimenti relativi a I/O, concorrenza e gestione delle risorse. Infine è stata realizzata una RestAPI con Http4s e Cats-Effect.

## Struttura del progetto

#### Il progetto è strutturato principalmente in package come segue:
* Snippets code: frammenti di codice relativi a fibers, IO, gestione di risorse, concorrenza e principali typeclasses di Cats-Effect. Sono presenti snippets anche su Semigruppi, Monoidi, Funtori e Monadi.
* Esperimenti: mini-applicativi sulla gestione di risorse, IO e concorrenza.
* RestAPI: applicazione finale realizzata con Http4s e Cats-Effect con lo scopo di mettere insieme i principali meccanismi di Cats-Effect e di scrivere in un paradigma puramente funzionale.
