package hai913i.tp2;

import java.util.ArrayList;
import java.util.List;

public final class Cluster {
	private final int id;
	private final List<String> members;
	private final Cluster left;
	private final Cluster right;
	private final double similarity;
	
	// Une feuille : une seule classe.
    public Cluster(int id, String className) {
        this.id = id;
        this.members = List.of(className);
        this.left = null;
        this.right = null;
        this.similarity = 0.0;
    }
    
 // Un nœud interne : fusion de deux groupes disjoints.
    public Cluster(int id, Cluster left, Cluster right,
                   double similarity) {
        if (left.members.stream().anyMatch(right.members::contains)) {
            throw new IllegalArgumentException(
                    "Les groupes à fusionner doivent être disjoints");
        }

        this.id = id;
        this.left = left;
        this.right = right;
        this.similarity = similarity;

        List<String> merged = new ArrayList<>(left.members);
        merged.addAll(right.members);
        merged.sort(String::compareTo);
        this.members = List.copyOf(merged);
    }
    
    public int id() {
        return id;
    }

    public List<String> members() {
        return members;
    }

    public Cluster left() {
        return left;
    }

    public Cluster right() {
        return right;
    }

    public double similarity() {
        return similarity;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}
