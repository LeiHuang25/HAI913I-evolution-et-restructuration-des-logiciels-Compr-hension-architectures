package hai913i.tp2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HierarchicalClustering {
	private static final double EPSILON = 1e-9;
	
	private final Linkage linkage;
	
	public HierarchicalClustering(Coupling coupling) {
        this(new AverageLinkage(coupling));
    }
	
	public HierarchicalClustering(Linkage linkage) {
		this.linkage = linkage;
	}

    public Cluster build(List<String> classNames) {
        if (classNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "La liste des classes est vide");
        }

        List<String> sortedNames = classNames.stream()
                .sorted()
                .toList();

        List<Cluster> active = new ArrayList<>();

        int nextId = 0;
        for (String name : sortedNames) {
            active.add(new Cluster(nextId++, name));
        }

        while (active.size() > 1) {
            active.sort(Comparator.comparingInt(Cluster::id));

            // Première passe : trouver le maximum réel.
            double maximum = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < active.size(); i++) {
                for (int j = i + 1; j < active.size(); j++) {
                    maximum = Math.max(maximum,
                            linkage.similarity(
                                    active.get(i), active.get(j)));
                }
            }

            // Deuxième passe : premier couple à égalité du maximum.
            int selectedI = -1;
            int selectedJ = -1;
            double selectedSimilarity = 0.0;

            search:
            for (int i = 0; i < active.size(); i++) {
                for (int j = i + 1; j < active.size(); j++) {
                    double similarity = linkage.similarity(
                            active.get(i), active.get(j));

                    if (Math.abs(similarity - maximum) <= EPSILON) {
                        selectedI = i;
                        selectedJ = j;
                        selectedSimilarity = similarity;
                        break search;
                    }
                }
            }

            Cluster left = active.get(selectedI);
            Cluster right = active.get(selectedJ);

            Cluster merged = new Cluster(
                    nextId++, left, right, selectedSimilarity);

            // Retirer le plus grand indice en premier.
            active.remove(selectedJ);
            active.remove(selectedI);
            active.add(merged);
        }

        return active.get(0);
    }											
	
}
