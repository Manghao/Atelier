# Atelier

> Atelier - LP CISIIE

## Etudiants
- BAIER Geoffrey
- DUBOIS Morgan
- MATMAT Myriam
- WILMOUTH Steven

## Prérequis
- npm
- maven
- docker, docker-compose

## Installation - Linux, Mac
Aller dans le dossier installation_linux, puis exécuter les commandes suivantes :

- installer l'api java
<pre>./installApi.sh</pre>

- démarrer l'api java
<pre>./startApi.sh</pre>

- stopper l'api java
<pre>./stopApi.sh</pre>

- démarrer la partie back office
<pre>./startBackOffice.sh</pre>

- démarrer la partie player
<pre>./startPlayer.sh</pre>

## Configurations
> Configurer l'application joueur
- Modification du lien vers l'API JEE
> Editer le fichier **config.js** qui se trouve dans le dossier **webAppPlayer/src/config**
> Editer la variable **url** par votre url d'API
[Docmentation application joueur](https://github.com/Manghao/Atelier/blob/master/vuejs/webAppPlayer/README.md)

> Configurer l'application back-office
- Modification du lien vers l'API JEE
> Editer le fichier **index.js** qui se trouve dans le dossier **webAppBackOffice/src/configApi**
> Editer la variable **baseURL** par votre url d'API
[Docmentation application back-office](https://github.com/Manghao/Atelier/blob/master/vuejs/webAppBackOffice/README.md)
