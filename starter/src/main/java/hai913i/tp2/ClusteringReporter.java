package hai913i.tp2;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ClusteringReporter {
	private ClusteringReporter() {}
	
	public static List<Cluster> merges(Cluster root) {
		List<Cluster> result = new ArrayList<>();
        collect(root, result);

        // Les identifiants suivent l'ordre de création.
        result.sort(Comparator.comparingInt(Cluster::id));
        return List.copyOf(result);
	}
	
	private static void collect(Cluster node, List<Cluster> result) {
        if (node.isLeaf()) {
            return;
        }

        collect(node.left(), result);
        collect(node.right(), result);
        result.add(node);
    }
	
	public static void print(Cluster root, PrintStream out) {
        List<Cluster> merges = merges(root);

        out.println();
        out.println("Fusions du clustering :");

        int step = 1;

        for (Cluster node : merges) {
            out.printf(Locale.ROOT,
                    "%d | groupes (%d, %d) | %s + %s | %.4f%n",
                    step++,
                    node.left().id(),
                    node.right().id(),
                    node.left().members(),
                    node.right().members(),
                    node.similarity());
        }

        out.printf(Locale.ROOT,
                "Nombre de fusions : %d%n", merges.size());
    }
}
