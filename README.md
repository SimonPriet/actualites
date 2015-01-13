# À propos de l'application Actualités

* licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt)
* financeur : Région Picardie, Conseil général  91, Région Poitou Charente
* description : Application de diffusion d'actualités organisées en fil

# Documentation technique
## Construction

<pre>
		gradle copyMod
</pre>

## Déploier dans ent-core


## Configuration

Dans le fichier `/ent-core.json.template` :


Déclarer l'application dans la liste :
<pre>
	{
      "name": "net.atos~actualites~0.2-SNAPSHOT",
      "config": {
        "main" : "net.atos.entng.actualites.Actualites",
        "port" : 8022,
        "app-name" : "Actualites",
        "app-address" : "/actualites",
        "app-icon" : "actualites-large",
        "host": "http://localhost:8090",
        "ssl" : false,
        "auto-redeploy": false,
        "userbook-host": "http://localhost:8090",
        "integration-mode" : "HTTP",
        "app-registry.port" : 8012,
        "mode" : "dev",
        "entcore.port" : 8009
      }
    }
</pre>


Associer une route d'entée à la configuration du module proxy intégré (`"name": "net.atos~actualites~0.2-SNAPSHOT"`) :
<pre>
	{
		"location": "/actualites",
		"proxy_pass": "http://localhost:8022"
	}
</pre>


# Présentation du module

## Fonctionnalités

Actualités est une application de création, de publication et de consultation d'actualités.
Les actualités sont organisées en fils d'actualités (ou Catégories). 
Les actualités sont soumises à un workflow simple de publication. Elles ont donc un état ("Brouillon", "En attende de validation", "Publiée") qui conditionne leur visibilité finale dans le fil. Des dates de publication et d'expiration peuvent de plus être définies.
Des permissions sur les différentes actions possibles sur les actualités, dont la publication, sont configurées dans ces fils (via des partages Ent-core). Le droit de lecture, correspondant à qui peut consulter les actualités d'un fil est également configuré de cette manière.


## Modèle de persistance

Les données du module sont stockées dans une collection MongoDb.
La collection comporte des objets `Thread` correspondant à chaque Fil d'actualités, avec métadonnées et partages.
Chaque `Thread` comprend une liste (tableau) d'objets `Info` représentant chacun une actualité et ses métadonnées, dont son état.


## Modèle serveur

Le module serveur utilise 1 contrôleur de déclaration :
 * `ActualitesController` : Routage des vues, déclaration des APIs et sécurité globale
Et 3 contrôleurs de traitement :
 * `ThreadControllerHelper` : APIs de manipulation des Fils d'actualités (Thread)
 * `InfoControllerHelper` : APIs de manipulation des Actualités (Info)
 * `StateControllerHelper` : APIs du workflow de publication des Actualités

Les contrôleurs étendent les classes du framework Ent-core exploitant les CrudServices de base.
Pour manipulations spécifiques, des classes de Service sont utilisées :
 * `ThreadService` : concernant les Fils d'actualités
 * `InfoService` : concernant les Actualités

Des classes de modèle décrivent entres autres les Etats de actualités dans le workflow de publication.

## Modèle front-end

Le modèle Front-end manipule 2 objets model :
 * `Thread` comprenant une Collection d'objets `Info` correspondant aux Actualités de ce Fil.
 * `Info` correspondant à une Actualité. Comprenant un champ `status` représentant l'état de publication.

Il y a 2 Collections globales :
 * `model.threads` : objets `Thread` synchronisée depuis le serveur.
 * `model.infos` : objets `Info` synchronisée depuis le serveur.
 
