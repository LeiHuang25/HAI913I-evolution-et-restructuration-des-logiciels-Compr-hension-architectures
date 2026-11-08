# HAI913I — TP2 : Compréhension et architectures

Université de Montpellier · 2026–2027 · Lei Huang

Ce projet regroupe les classes d'un programme Java à partir de ses faits d'entrée. Il calcule le couplage, construit un graphe pondéré et un dendrogramme, puis sélectionne des modules avec un seuil CP.

Les parties A et B utilisent les CSV fournis : c'est le chemin 2 du sujet. Les bonus permettent de comparer deux critères de liaison, d'ajouter l'héritage et les interfaces, de parcourir l'arbre dans une fenêtre Swing et d'analyser un second projet avec les faits reconstruits par le TP1.

## 1. Prérequis

Versions utilisées pour les dernières vérifications :

| Outil | Version |
|---|---|
| Java | Oracle JDK 26, build 26+35-2893 ; compilation ciblant Java 17 |
| Maven | 3.9.11 |
| Graphviz | 16.1.0 |
| Eclipse | Eclipse IDE for Java Developers, plateforme 4.41.0 |
| Système | Windows 11, PowerShell |
| Tests | JUnit Jupiter 5.14.4 |

Le projet demande Java 17 au minimum. Il n'a aucune dépendance d'exécution supplémentaire ; Swing fait partie du JDK. Graphviz sert seulement à convertir les DOT en images. Maven utilise les plugins compiler 3.14.1, surefire 3.5.4 et jar 3.4.2. Le premier build nécessite un accès aux dépôts Maven.

Pour vérifier l'environnement :

```powershell
java -version
mvn -v
dot -V
```

## 2. Organisation du projet

```text
tp2/
  README.md
  resources/             faits de validation et relations structurelles
  examples/              micro-jeu et exemples fournis
  second-project/        faits du programme TP2 extraits avec le TP1
  starter/
    pom.xml
    src/main/java/hai913i/tp2/
    src/test/java/hai913i/tp2/
    src/test/resources/
    output/              résultats générés, ignorés par Git
    target/              compilation et JAR, ignorés par Git
```

Le projet Maven à importer dans Eclipse est **starter**, car il contient le fichier `pom.xml`.

| Classes | Rôle |
|---|---|
| `Facts`, `StructuralRelations` | Charger les faits et les relations structurelles |
| `Coupling`, `CouplingGraph` | Calculer les poids et construire le graphe |
| `Cluster` | Conserver les membres, les fils et la similarité d'une fusion |
| `Linkage`, `AverageLinkage`, `SingleLinkage` | Choisir le calcul de similarité entre groupes |
| `HierarchicalClustering` | Construire l'arbre et départager les égalités |
| `ModuleCutter` | Couper l'arbre et calculer les moyennes internes |
| `ClusteringReporter`, `DendrogramExporter`, `ModuleExporter` | Afficher et exporter les résultats |
| `Main`, `ClusterExplorer` | Lancer la chaîne en console ou explorer l'arbre |

L'entrée du TP2 reste centralisée dans `Facts.load`. Le TP1 fournit des CSV pour C4 ; ses classes et JDT ne sont pas chargés par le programme TP2.

## 3. Construction

Depuis une copie fraîche :

```powershell
$repo = "https://github.com/LeiHuang25/" +
  "HAI913I-evolution-et-restructuration-des-logiciels-Compr-hension-architectures.git"
git clone $repo tp2
cd tp2/starter
mvn clean package
```

La commande compile le projet, exécute les tests et crée `target/hai913i-tp2.jar`. Les commandes suivantes se lancent **depuis starter**, sauf indication contraire. Garder `resources`, `examples` et `second-project` à côté de ce dossier : certains tests lisent les faits de `../resources`.

## 4. Entrées et options

```text
java -jar target/hai913i-tp2.jar DOSSIER
    [--cp VALEUR] [--prefix PREFIXE]
    [--linkage average|single] [--relations calls|enriched]
```

Cette présentation décrit la syntaxe ; saisir la commande sur une ligne ou utiliser les continuations PowerShell montrées plus bas.

| Option | Fonction |
|---|---|
| `DOSSIER` | Dossier contenant les CSV du jeu de faits |
| `--cp` | Seuil de moyenne interne ; 0.025 si absent |
| `--prefix` | Préfixe des fichiers ; découvert automatiquement si absent |
| `--linkage` | `average` par défaut, ou `single` pour C1 |
| `--relations` | `calls` par défaut, ou `enriched` pour C2 |

Le séparateur décimal est le point. La valeur par défaut de CP vient du squelette ; ce n'est pas une valeur adaptée à tous les projets.

Chaque jeu contient les trois fichiers UTF-8 suivants, séparés par des points-virgules :

```text
PREFIXE-classes.csv : class;simpleName;package
PREFIXE-methods.csv : method;owner
PREFIXE-calls.csv   : caller;callerClass;callee;calleeClass;sites
```

Le mode enrichi demande aussi `PREFIXE-structure.csv`, avec l'en-tête `source;target;kind`. Les valeurs de `kind` sont `extends` et `implements`. Le fichier fourni a été obtenu à partir du bloc `extra.inheritance` de `resources/facts-validation.json`. Il contient les sept relations d'origine ; le chargeur conserve les quatre relations internes et écarte les trois cibles externes. Il supprime les doublons et refuse une source absente du projet.

## 5. Exécution des parties A et B

```powershell
java -jar target/hai913i-tp2.jar ../examples --cp 0.20
java -jar target/hai913i-tp2.jar ../resources --cp 0.015
java -jar target/hai913i-tp2.jar ../resources --cp 0.025
java -jar target/hai913i-tp2.jar ../resources --cp 0.045
```

Sur la validation, les compteurs attendus sont : 15 classes, 59 méthodes, 44 arcs, dont 7 internes et 37 inter-classes. Le graphe contient 15 nœuds et 19 arêtes. `Dvd` et `LoanException` sont isolés. La somme des couplages vaut 1 et le maximum vaut 4/37. L'arbre a 14 fusions ; les premières similarités sont 4/37, 3/37 et 9/148.

| CP | Modules | Plus grand module | Limite de 7 |
|---|---:|---:|---|
| 0.015 | 4 | 11 classes | respectée |
| 0.025 | 7 | 7 classes | respectée |
| 0.045 | 10 | 5 classes | dépassée, coupe rejetée |

Le seuil retenu pour le modèle de base est 0.025. Sur le micro-jeu, les compteurs sont 3 classes, 5 méthodes et 4 arcs. Les deux fusions ont les similarités 2/3 puis 1/6. La moyenne interne de la racine vaut 1/3 : avec CP=0.20, elle est retenue ; avec CP=0.50, la coupe donne deux modules et dépasse le maximum de un.

## 6. Bonus C1 : lien simple

```powershell
java -jar target/hai913i-tp2.jar ../resources --linkage average --cp 0.025
java -jar target/hai913i-tp2.jar ../resources --linkage single --cp 0.025
```

Le lien moyen prend la moyenne sur tous les couples inter-groupes, y compris ceux de couplage nul. Le lien simple prend leur maximum. Les deux méthodes utilisent le même couplage et la même règle de départage.

Les choix divergent à la deuxième fusion : le lien moyen réunit `LibraryApp` et `Catalog` à 0.0811, alors que le lien simple ajoute `LoanService.Loan` à `{Item, LoanService}` à 0.1081. Sur le micro-jeu, les groupes fusionnés sont les mêmes, mais la dernière similarité passe de 1/6 à 1/3.

## 7. Bonus C2 : relations enrichies

```powershell
java -jar target/hai913i-tp2.jar ../resources `
  --relations enriched --linkage average --cp 0.020
```

Chaque relation structurelle interne compte une fois, avec le même poids qu'un arc d'appel. Le couplage enrichi additionne les deux nombres par paire, puis divise par le total des relations internes : 37 + 4 = 41 sur la validation. `arcCount` continue à compter les seuls appels ; `relationCount` compte les deux catégories.

Le graphe enrichi contient 15 nœuds et 22 arêtes. Seul `LoanException` reste isolé ; `Dvd` est connecté à `Item`. `Book–Item` avait déjà un appel, donc la relation d'héritage augmente son poids sans créer une nouvelle arête.

| CP enrichi | Modules | Limite de 7 |
|---|---:|---|
| 0.015 | 4 | respectée |
| 0.020 | 7 | respectée |
| 0.024 | 7 | respectée |
| 0.025 | 8 | dépassée |

Les valeurs 0.020 et 0.024 donnent la même partition. Le seuil retenu pour cette variante est 0.020. À 0.025, la paire `Member–ConsoleNotifier` est séparée car son poids 1/41 est inférieur au seuil. Il faut donc distinguer l'effet des nouvelles relations de celui du changement d'échelle des poids.

## 8. Bonus C3 : interface graphique

```powershell
java -cp target/hai913i-tp2.jar hai913i.tp2.ClusterExplorer ../resources
```

La fenêtre permet de développer ou replier les branches, puis de sélectionner un groupe ou une classe. Le panneau de droite affiche les membres et, pour un groupe, la similarité de fusion et la moyenne interne.

L'explorateur utilise les appels seuls et le lien moyen. Il n'accepte que le dossier des faits, sans les options de `Main`. Le changement de CP et les modes bonus se font en console.

## 9. Bonus C4 : second projet

Les fichiers de `second-project` proviennent des sources du programme TP2, version `0511a68`. Ils sont déjà fournis : le TP1 n'est pas nécessaire pour les charger.

```powershell
java -jar target/hai913i-tp2.jar ../second-project --cp 0.025
java -jar target/hai913i-tp2.jar ../second-project --cp 0.015
java -jar target/hai913i-tp2.jar ../second-project --cp 0.010
```

L'extraction donne 21 types et 93 méthodes pour 15 fichiers Java. Les 124 arcs comprennent 100 arcs inter-classes et 24 internes. Le graphe de couplage contient 21 nœuds et 36 arêtes, sans type isolé ; l'arbre comporte 20 fusions.

| CP | Modules | Limite de 10 |
|---|---:|---|
| 0.025 | 13 | dépassée |
| 0.015 | 10 | respectée |
| 0.010 | 7 | respectée |

Le seuil retenu est 0.015 : il respecte le plafond en conservant une partition plus détaillée que 0.010.

Pour reconstruire les faits, utiliser le TP1 au commit `9092503`, qui contient le correctif des accesseurs de records et l'exporteur CSV. Depuis la **racine du dépôt TP2** :

```powershell
$repoTp1 = "https://github.com/LeiHuang25/" +
  "HAI913I-evolution-et-restructuration-des-logiciels-Analyse-Statique.git"
git clone $repoTp1 ../tp1-extractor
git -C ../tp1-extractor checkout 9092503
mvn -f ../tp1-extractor/starter/pom.xml clean package
cd starter
java -cp ../../tp1-extractor/starter/target/hai913i-tp1-analyzer.jar `
  hai913i.tp1.Tp2FactsExportMain . ../second-project facts-tp2
```

Les statistiques 21/93/124 correspondent aux sources TP2 de `0511a68`. Une modification des sources peut changer l'extraction. L'exporteur écrit une ligne par arc et place son nombre de sites dans la dernière colonne ; le TP2 compte les arcs, pas les sites. Le graphe reste fondé sur les cibles statiques de JDT, sans expansion des implémentations possibles d'un appel d'interface.

## 10. Sorties et images

Le programme crée `output/` dans le répertoire courant. Pour le jeu de validation en mode de base, il produit :

```text
facts-validation-coupling.dot
facts-validation-nodes.csv
facts-validation-edges.csv
facts-validation-dendrogram.dot
facts-validation-dendrogram.txt
facts-validation-modules-cp-0.025.txt
```

Le mode enrichi utilise le préfixe `facts-validation-enriched`. Le lien simple ajoute `-single` au préfixe des arbres et des modules. Le graphe dépend du modèle de relations, pas du critère de liaison. Les exports d'un même mode sont remplacés à chaque exécution ; les noms des fichiers de modules distinguent les CP.

Pour générer les images :

```powershell
dot -Tpng output/facts-validation-coupling.dot `
  -o output/facts-validation-coupling.png
dot -Tpng output/facts-validation-dendrogram.dot `
  -o output/facts-validation-dendrogram.png
dot -Tpng output/facts-validation-single-dendrogram.dot `
  -o output/facts-validation-single-dendrogram.png
dot -Tpng output/facts-validation-enriched-coupling.dot `
  -o output/facts-validation-enriched-coupling.png
dot -Tpng output/facts-tp2-dendrogram.dot `
  -o output/facts-tp2-dendrogram.png
```

Les labels donnent les similarités réelles. La hauteur du dessin n'est pas une échelle proportionnelle. Les deux CSV du graphe conservent les poids sans l'arrondi des labels. Pendant l'exécution, `Graphe identique : true` confirme le rechargement des nœuds et des arêtes.

## 11. Tests et conventions

```powershell
mvn test
```

La dernière construction a exécuté **34 tests sans échec** : 25 pour le fonctionnement initial, 2 pour le lien simple et 7 pour C2. Ils contrôlent notamment les valeurs du micro-jeu, la symétrie, la somme, le graphe, le départage, l'arbre et la coupe stricte. Les exports ont aussi été examinés dans les TXT et les images. L'interface Swing a été vérifiée manuellement.

Les principales conventions sont :

- Les arcs internes sont exclus du couplage. Les deux sens d'appel sont agrégés dans une paire non orientée et la diagonale vaut zéro.
- Les classes initiales sont numérotées par nom qualifié trié ; chaque fusion reçoit l'identifiant suivant. Parmi les candidats à moins de 1e-9 du maximum, le plus petit couple d'identifiants est choisi.
- Un groupe est retenu si sa moyenne sur tous ses couples internes est **strictement supérieure** à CP. Une feuille est toujours retenue. Le plafond global est M/2 en division entière.
- Une coupe rejetée n'est pas corrigée par des fusions arbitraires.

Pour comparer deux sorties, depuis le même répertoire :

```powershell
java -jar target/hai913i-tp2.jar ../resources --linkage single > output/run1.txt
java -jar target/hai913i-tp2.jar ../resources --linkage single > output/run2.txt
(Get-FileHash output/run1.txt).Hash -eq (Get-FileHash output/run2.txt).Hash
```

Le résultat attendu est `True`. Cette vérification a été effectuée sur le jeu de validation. Les trois CSV de C4 ont également été comparés à une nouvelle extraction et sont identiques.

## 12. Dépannage et limites

- **JAR introuvable** : lancer `mvn package` depuis `starter`. Après `mvn clean test`, les tests sont exécutés, mais le JAR n'est pas recréé.
- **Faits introuvables** : vérifier le dossier courant et les trois CSV. Mettre les chemins contenant des espaces entre guillemets. En présence de plusieurs jeux, utiliser `--prefix`.
- **Structure CSV introuvable** : `--relations enriched` demande aussi le fichier `PREFIXE-structure.csv`. Les autres jeux peuvent être analysés avec `--relations calls`.
- **Accents incorrects dans PowerShell** : régler l'encodage, puis relancer Java :

```powershell
chcp 65001
$utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
```

- **dot introuvable** : installer Graphviz et ajouter son dossier `bin` au PATH. Les calculs Java peuvent être utilisés sans Graphviz.
- Les arguments absents ou inconnus, les modes invalides et les CP non numériques ou négatifs sont refusés avec le code 1. Les erreurs de lecture ou d'écriture traitées donnent le code 2.
- `NaN` et `Infinity` ne sont pas interceptés proprement par `Main` et peuvent déclencher une exception dans la coupe. Un jeu vide n'est pas traité proprement par le clustering.
- Une coupe dépassant M/2 est signalée et exportée comme rejetée, mais le programme termine actuellement avec le code 0.
- Le chargeur ne valide pas toutes les incohérences possibles de CSV arbitraires. Le modèle d'entrée reste la classe concrète `Facts`.
- En C4, le correctif du TP1 ajoute les accesseurs implicites de records. Il ne représente pas toutes les méthodes générées par le compilateur. Les appels réfléchis et toutes les cibles dynamiques ne sont pas reconstruits.

## 13. Remise et utilisation de l'IA

Le dossier de remise comprend le rapport PDF, ce README en PDF, les sources et données, les exports utiles et la vidéo ou son lien. Ne pas inclure `target/`, les caches et les JAR. Copier les résultats choisis de `output/` dans le dossier `sorties/` de l'archive. Les faits de `second-project/` sont des entrées nécessaires à C4 et restent versionnés.

ChatGPT/Codex a été utilisé pour expliquer les calculs, proposer du code et des tests, diagnostiquer des erreurs, accompagner Git et préparer la documentation. Les propositions ont été vérifiées par compilation et exécution. Le rapport détaille ces usages, notamment le problème des accesseurs de records. La vidéo reste à enregistrer ; aucun lien de démonstration n'est annoncé comme disponible.

## 14. Liens et références

- [Dépôt TP2](https://github.com/LeiHuang25/HAI913I-evolution-et-restructuration-des-logiciels-Compr-hension-architectures)
- [Dépôt TP1 utilisé pour C4](https://github.com/LeiHuang25/HAI913I-evolution-et-restructuration-des-logiciels-Analyse-Statique)
- Bachar Rima, *TP2 — Annexe 1 : Regrouper des classes, implémentation, validation et restitution*, 2026–2027 : organisation des traitements, invariants et règle de coupe.
- [Maven : cycle de construction](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [Graphviz : ligne de commande](https://graphviz.org/doc/info/command.html)
- [Swing : utilisation de JTree](https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html) — tutoriel JDK 8, utilisé pour les principes de navigation et de sélection.
- [Java SE 17 : records et accesseurs implicites](https://docs.oracle.com/en/java/javase/17/language/records.html)
- [Eclipse JDT : ASTParser](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/ASTParser.html)

Documentation externe consultée le 8 novembre 2026.

## 15. Commandes pour la vidéo de démonstration

La démonstration principale couvre les parties A et B. Elle montre une construction propre, les faits chargés, le couplage et son graphe, les fusions et le dendrogramme, puis deux coupes dont une rejetée. Les commandes ci-dessous se lancent dans PowerShell **depuis `starter`**, une par une. Les journaux servent à retrouver les résultats produits pendant la vidéo ; ils ne remplacent pas l'exécution réelle.

**Construction et exécution de référence :**

```powershell
mvn clean package
New-Item -ItemType Directory -Force output | Out-Null
java -jar target/hai913i-tp2.jar ../resources `
  --relations calls --linkage average --cp 0.025 |
  Tee-Object -FilePath output/demo-validation.txt
```

Attendre `BUILD SUCCESS` avant de lancer le JAR. Le build actuel exécute 34 tests. Le dossier `output` est créé avant `Tee-Object`, pour que cette commande fonctionne aussi dans une copie fraîche.

**A0 et A1 — compteurs et couplage :**

```powershell
Get-Content -Encoding UTF8 output/demo-validation.txt -TotalCount 16
Get-Content -Encoding UTF8 output/demo-validation.txt |
  Select-String -Pattern @(
    'library.model.Item <-> library.service.LoanService',
    'Somme des couplages'
  )
```

Montrer 15 classes, 59 méthodes, 44 arcs, puis 7 arcs internes, 37 inter-classes et 19 paires non nulles. Le couple Item–LoanService vaut 0.1081 et la somme vaut 1.0000.

**A2 — graphe pondéré :**

```powershell
Get-Content -Encoding UTF8 output/demo-validation.txt |
  Select-String -Pattern '^Graphe de couplage :' -Context 0,5
Get-Content -Encoding UTF8 output/demo-validation.txt |
  Select-String -Pattern '^Graphe identique :'
dot -Tpng output/facts-validation-coupling.dot `
  -o output/facts-validation-coupling.png
Start-Process output/facts-validation-coupling.png
```

Montrer les 15 nœuds, 19 arêtes, les deux classes isolées et le contrôle de rechargement. Fermer ou réduire l'image avant de poursuivre dans le terminal.

**B1 et B2 — fusions et dendrogramme :**

```powershell
Get-Content -Encoding UTF8 output/demo-validation.txt |
  Select-String -Pattern '^Fusions du clustering :' -Context 0,15
Get-Content -Encoding UTF8 output/facts-validation-dendrogram.txt -TotalCount 9
dot -Tpng output/facts-validation-dendrogram.dot `
  -o output/facts-validation-dendrogram.png
Start-Process output/facts-validation-dendrogram.png
```

Montrer les 14 fusions, les trois premières similarités 0.1081, 0.0811 et 0.0608, puis les deux dernières à zéro. L'arbre comporte 15 feuilles et 14 nœuds internes. La hauteur du dessin n'est pas une échelle proportionnelle de similarité.

**B3 — une coupe acceptée et une coupe rejetée :**

```powershell
Get-Content -Encoding UTF8 output/demo-validation.txt |
  Select-String -Pattern '^Modules pour CP' -Context 0,17
java -jar target/hai913i-tp2.jar ../resources `
  --relations calls --linkage average --cp 0.045 |
  Tee-Object -FilePath output/demo-rejet.txt
Get-Content -Encoding UTF8 output/demo-rejet.txt |
  Select-String -Pattern '^Modules pour CP' -Context 0,23
```

À CP=0.025, les sept modules respectent la limite de sept. À CP=0.045, les dix modules dépassent cette limite et la coupe est rejetée. Le programme signale ce rejet dans le texte ; le code de sortie n'est pas utilisé comme preuve, car il reste actuellement à zéro.

**Compléments pour montrer les bonus réalisés :**

```powershell
# C1 : première différence avec le lien moyen, à la deuxième fusion.
java -jar target/hai913i-tp2.jar ../resources `
  --relations calls --linkage single --cp 0.025 |
  Tee-Object -FilePath output/demo-single.txt
Get-Content -Encoding UTF8 output/demo-single.txt |
  Select-String -Pattern '^Fusions du clustering :' -Context 0,3

# C2 : couplage enrichi, avec un seuil adapté à cette mesure.
java -jar target/hai913i-tp2.jar ../resources `
  --relations enriched --linkage average --cp 0.020 |
  Tee-Object -FilePath output/demo-enriched.txt
Get-Content -Encoding UTF8 output/demo-enriched.txt |
  Select-String -Pattern @(
    '^Relations structurelles', '^Relations externes',
    '^Dénominateur', '^Arêtes :',
    '^Nombre de modules', '^Contrainte M/2'
  )

# C3 : développer une branche, puis sélectionner un groupe.
java -cp target/hai913i-tp2.jar hai913i.tp2.ClusterExplorer ../resources

# C4 : analyser les faits reconstruits pour le second projet.
java -jar target/hai913i-tp2.jar ../second-project `
  --relations calls --linkage average --cp 0.015 |
  Tee-Object -FilePath output/demo-second.txt
Get-Content -Encoding UTF8 output/demo-second.txt -TotalCount 16
Get-Content -Encoding UTF8 output/demo-second.txt |
  Select-String -Pattern '^Nombre de modules|^Maximum autorisé|^Contrainte M/2'
```

Fermer la fenêtre Swing pour revenir au terminal. Pour C4, les fichiers fournis donnent 21 types, 93 méthodes, 124 arcs et dix modules à CP=0.015. La reconstruction de ces faits avec le TP1 est documentée en section 9 ; elle n'est pas remplacée par une extraction dans le TP2.

Le commentaire oral peut être remplacé par des sous-titres. Une fois la vidéo enregistrée, ajouter son lien au README et au rapport si elle est hébergée en dehors de l'archive, puis vérifier qu'il est accessible.
