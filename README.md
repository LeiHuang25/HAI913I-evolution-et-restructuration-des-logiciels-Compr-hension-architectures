# HAI913I — TP2 : Compréhension et architectures

Année 2026–2027 · Université de Montpellier · Enseignant : Bachar Rima

## 1. Objectif et chemin d'entrée

L'outil transforme des faits décrivant un projet Java en modules : chargement des classes, méthodes et arcs d'appel, calcul du couplage, graphe pondéré, clustering hiérarchique par lien moyen, dendrogramme et coupe sous contraintes.

**Chemin 2 du sujet : jeu canonique fourni.** Le programme charge les CSV de `resources/`. Il ne relit pas les sources Java et n'utilise ni JDT ni Spoon. Aucun adaptateur vers le modèle du TP1 n'est actuellement implémenté. Le sujet autorise cette entrée au même titre que l'adaptation des faits personnels du TP1.

L'entrée est centralisée dans `Facts.load(...)`. Les traitements travaillent ensuite sur les faits chargés ; il n'existe pas encore d'interface Java abstraite permettant de substituer un fournisseur TP1. Cette évolution reste possible.

## 2. Organisation

```text
racine-du-depot/
  README.md
  resources/             faits du projet de validation
  examples/              micro-jeu et démonstrations fournies
  starter/
    pom.xml
    src/main/java/hai913i/tp2/
    src/test/java/hai913i/tp2/
    src/test/resources/   micro-jeu utilisé par les tests
    output/               résultats générés, ignorés par Git
    target/               compilation et JAR, ignorés par Git
```

| Classe | Responsabilité |
|---|---|
| `Facts` | Charger les trois CSV et représenter les classes, méthodes et arcs |
| `Coupling` | Compter les arcs inter-classes et fournir C(A,B) |
| `CouplingGraph` | Construire le graphe, exporter DOT/CSV et recharger les CSV |
| `Cluster` | Représenter une feuille ou une fusion avec ses membres et ses fils |
| `AverageLinkage` | Calculer la similarité entre deux groupes |
| `HierarchicalClustering` | Construire l'arbre avec départage déterministe |
| `ClusteringReporter` | Afficher la liste chronologique des fusions |
| `DendrogramExporter` | Exporter l'arbre en DOT et en texte indenté |
| `ModuleCutter` | Calculer la moyenne interne et sélectionner les branches |
| `ModuleExporter` | Enregistrer les modules et l'état des contraintes |
| `Main` | Lire les arguments, coordonner les traitements et afficher les résultats |

Toutes ces classes appartiennent au paquetage `hai913i.tp2`. Le dossier `examples` contient du support pédagogique ; il ne constitue pas le programme principal.

## 3. Environnement

- JDK : Java 17 minimum ; compilation avec `maven.compiler.release=17`. Environnement précédemment vérifié : Oracle JDK 26, build 26+35-2893.
- Maven : 3.9.11.
- Graphviz : 16.1.0 pour convertir les DOT en images.
- Système : Windows 11, commandes PowerShell.
- IDE utilisé : Eclipse ; sa version exacte reste à renseigner depuis Help > About Eclipse IDE.
- Aucune bibliothèque d'exécution supplémentaire.
- Tests : JUnit Jupiter 5.14.4.
- Plugins Maven : compiler 3.14.1, surefire 3.5.4, jar 3.4.2.

Vérifier les versions sur la machine utilisée pour la remise :

```powershell
java -version
mvn -v
dot -V
```

Un accès aux dépôts Maven est nécessaire au premier téléchargement des dépendances et plugins. Graphviz n'est pas nécessaire au calcul Java, seulement à la conversion des DOT.

## 4. Construction depuis une copie fraîche

```powershell
git clone https://github.com/LeiHuang25/HAI913I-evolution-et-restructuration-des-logiciels-Compr-hension-architectures.git
cd HAI913I-evolution-et-restructuration-des-logiciels-Compr-hension-architectures/starter
mvn clean package
```

La commande compile les sources, exécute les tests et produit `target/hai913i-tp2.jar`. Les commandes suivantes sont toutes exécutées **depuis `starter`**. Conserver `resources` et `examples` à côté de ce dossier : un test lit `../resources`.

```powershell
mvn clean test
```

Cette dernière commande exécute les tests mais ne recrée pas le JAR après le nettoyage ; utiliser ensuite `mvn package` pour exécuter le programme.

## 5. Entrées et arguments

```text
java -jar target/hai913i-tp2.jar DOSSIER [--cp VALEUR] [--prefix PREFIXE]
```

Le dossier contient trois fichiers UTF-8, séparés par des points-virgules, avec en-tête :

```text
PREFIXE-classes.csv : class;simpleName;package
PREFIXE-methods.csv : method;owner
PREFIXE-calls.csv   : caller;callerClass;callee;calleeClass;sites
```

Le préfixe est découvert à partir de `*-classes.csv`. S'il existe plusieurs jeux dans le dossier, préciser `--prefix`. Les JSON fournis sont des références ; le programme actuel lit les CSV uniquement. Le fichier des classes est indispensable pour conserver les classes isolées.

```powershell
java -jar target/hai913i-tp2.jar ../resources --cp 0.025
java -jar target/hai913i-tp2.jar ../examples --cp 0.20
java -jar target/hai913i-tp2.jar ../resources --prefix facts-validation --cp 0.025
```

L'implémentation conserve la valeur par défaut 0.025 du squelette si `--cp` est absent. Cette valeur est un choix d'implémentation, pas une constante universelle prescrite par le sujet ; les expériences utilisent un CP explicite. Le séparateur décimal attendu est le point.

## 6. Conventions de calcul

- Une dépendance est un arc d'appel de méthode. Héritage, implémentation, types, instanciation et exceptions ne sont pas ajoutés.
- Un arc compte une fois, même si `sites` est supérieur à 1. Les arcs intra-classe sont exclus du couplage.
- `C(A,B) = r(A,B) / |E_inter|`, avec les deux sens agrégés ; C est symétrique et sa diagonale est nulle. Un couple absent vaut zéro. Sans arc inter-classe, les couplages valent zéro par convention.
- Le lien moyen inclut tous les couples entre deux groupes, même ceux de valeur nulle ; son dénominateur est le produit des tailles des groupes.
- Les classes initiales sont numérotées par nom qualifié trié. Les nouveaux groupes reçoivent les numéros suivants. Parmi les couples à moins de 1e-9 du maximum, le plus petit couple d'identifiants est retenu.
- La coupe part de la racine : un groupe non singleton est retenu si sa moyenne sur tous ses couples internes est strictement supérieure à CP ; sinon les deux fils sont explorés. Une feuille est toujours retenue.
- La moyenne interne du groupe est distincte de sa similarité de fusion. Après la coupe, le nombre de modules doit être au plus `M/2`, en division entière.

## 7. Expériences et résultats attendus

```powershell
java -jar target/hai913i-tp2.jar ../resources --cp 0.015
java -jar target/hai913i-tp2.jar ../resources --cp 0.025
java -jar target/hai913i-tp2.jar ../resources --cp 0.045
```

| CP | Modules | Maximum | Contrainte |
|---|---:|---:|---|
| 0.015 | 4 | 7 | respectée ; un module de 11 classes |
| 0.025 | 7 | 7 | respectée ; un module de 7 classes |
| 0.045 | 10 | 7 | violée ; coupe rejetée |

Le CP retenu pour l'analyse est 0.025 : compromis entre la coupe grossière à 0.015 et la coupe invalide à 0.045. Ce choix n'est pas présenté comme optimal parmi tous les seuils possibles.

Contrôles sur la validation : 15 classes, 59 méthodes, 44 arcs, dont 7 intra-classe et 37 inter-classes ; 19 couples non nuls ; somme des couplages sur les couples non orientés égale à 1 ; maximum 4/37. Graphe : 15 nœuds, 19 arêtes. Classes isolées : Dvd et LoanException. Clustering : 14 fusions, premières similarités 4/37, 3/37 et 9/148 ; deux dernières nulles. Arbre : 15 feuilles et 14 nœuds internes.

Micro-jeu : 3 classes, 5 méthodes, 4 arcs, dont 3 inter-classes ; C(A,B)=2/3, C(B,C)=1/3 ; fusions à 2/3 et 1/6. CP=0.20 donne un module acceptable ; CP=0.50 donne deux modules, au-delà du maximum de 1.

## 8. Sorties et visualisation

Le programme crée automatiquement `output/`, relatif au répertoire courant. Pour le préfixe `facts-validation`, il produit :

```text
facts-validation-coupling.dot
facts-validation-nodes.csv
facts-validation-edges.csv
facts-validation-dendrogram.dot
facts-validation-dendrogram.txt
facts-validation-modules-cp-0.025.txt
```

Le graphe CSV est rechargé pendant l'exécution ; `Graphe identique : true` vérifie les nœuds, extrémités et poids. Les CSV préservent la précision des poids ; les labels DOT sont arrondis à quatre décimales. Les exports du graphe et de l'arbre sont remplacés à chaque exécution du même jeu ; le nom du fichier des modules distingue les CP.

```powershell
dot -Tpng output/facts-validation-coupling.dot -o output/facts-validation-coupling.png
dot -Tpng output/facts-validation-dendrogram.dot -o output/facts-validation-dendrogram.png
dot -Tpdf output/facts-validation-dendrogram.dot -o output/facts-validation-dendrogram.pdf
dot -Tpng output/facts-mini-coupling.dot -o output/facts-mini-coupling.png
dot -Tpng output/facts-mini-dendrogram.dot -o output/facts-mini-dendrogram.png
```

Le dendrogramme porte les similarités réelles dans les labels. Les hauteurs du dessin ne sont pas proportionnelles aux similarités.

## 9. Tests et déterminisme

La dernière validation de développement a exécuté 25 tests sans échec : FactsTest (6), CouplingTest (5), CouplingValidationTest (1), CouplingGraphTest (3), ClusterTest (2), AverageLinkageTest (2), HierarchicalClusteringTest (3), ModuleCutterTest (3). Les tests couvrent notamment les valeurs à la main, la symétrie, la somme, les classes isolées, le rechargement CSV, le départage, la structure de l'arbre et la coupe stricte.

Les exporteurs de dendrogramme et de modules ont été vérifiés par exécution et lecture des sorties ; ils n'ont pas de classe de tests dédiée. Aucun test ne prouve à lui seul une architecture idéale.

Comparer deux exécutions, depuis le même répertoire :

```powershell
java -jar target/hai913i-tp2.jar ../resources --cp 0.025 > output/run1.txt
java -jar target/hai913i-tp2.jar ../resources --cp 0.025 > output/run2.txt
cmd /c fc /b output\run1.txt output\run2.txt
```

`fc /b` doit indiquer qu'aucune différence n'a été trouvée (code 0).

## 10. Dépannage et limites connues

- Accents incorrects dans PowerShell : exécuter les commandes suivantes, puis relancer Java.

```powershell
chcp 65001
$utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
```

- JAR introuvable : exécuter `mvn package` depuis `starter`.
- Dossier de faits introuvable : vérifier le répertoire courant et les trois CSV. Mettre entre guillemets les chemins contenant des espaces.
- `dot` introuvable : installer Graphviz et rendre son dossier `bin` accessible dans PATH ; les calculs Java restent disponibles.
- Les arguments absents/inconnus, CP non numérique ou négatif sont refusés avec le code 1. Une erreur de lecture/écriture traitée produit le code 2.
- Limite actuelle : `NaN` et `Infinity` sont refusés par ModuleCutter, mais ne sont pas interceptés proprement par Main ; une exception peut être affichée après les premières étapes. Ne pas présenter ce cas comme une validation propre déjà acquise.
- Une coupe dépassant M/2 est signalée et exportée comme rejetée, mais l'exécution termine actuellement avec le code 0. La violation n'est donc pas détectable uniquement par le code de sortie.
- Le chargeur canonique contrôle le format de base, mais ne constitue pas un validateur complet de tous les CSV arbitraires. Un jeu vide n'est pas traité proprement par le clustering.
- Pas d'adaptateur TP1, d'interface graphique, de relation enrichie ni de comparaison avec le lien simple dans cette version. Les groupes obtenus ne constituent pas automatiquement des composants déployables.

## 11. Remise, IA et liens

Le dossier évalué doit comporter le compte-rendu PDF, ce README en PDF, les sources avec données nécessaires, les exports et la vidéo (ou son lien accessible). Exclure `target/`, caches, JAR et journaux temporaires. Les résultats utiles sont à copier dans le dossier `sorties/` de l'archive finale ; leur exclusion de Git ne signifie pas leur exclusion du rendu.

L'assistance ChatGPT/Codex a été utilisée pour expliquer les consignes et formules, proposer du code et des tests, diagnostiquer les erreurs, accompagner les validations et préparer la documentation. Le compte-rendu doit déclarer ces usages, les modèles effectivement utilisés si connus, les vérifications réalisées et les décisions de l'étudiant. Aucun historique complet n'est requis.

Dépôt : https://github.com/LeiHuang25/HAI913I-evolution-et-restructuration-des-logiciels-Compr-hension-architectures

Vidéo : lien à ajouter après enregistrement et vérification de l'accès. Version exacte d'Eclipse : à compléter avant remise.
