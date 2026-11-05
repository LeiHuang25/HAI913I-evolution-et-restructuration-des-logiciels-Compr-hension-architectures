package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ModuleCutterTest {

	@Test
    void internalAverageDiffersFromMergeSimilarity() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        Coupling coupling = new Coupling(facts);

        Cluster root = new HierarchicalClustering(coupling)
                .build(facts.classNames());

        ModuleCutter cutter = new ModuleCutter(coupling);

        assertEquals(1.0 / 3.0,
                cutter.internalAverage(root), 1e-9);

        assertEquals(1.0 / 6.0,
                root.similarity(), 1e-9);

        // Le fils droit est le groupe {A,B}.
        assertEquals(2.0 / 3.0,
                cutter.internalAverage(root.right()), 1e-9);

        // Le fils gauche est la feuille C.
        assertEquals(0.0,
                cutter.internalAverage(root.left()), 1e-9);
    }
	
	@Test
	void splitsWhenAverageEqualsThreshold() throws Exception {
	    Facts facts = Facts.load(Path.of("src/test/resources"));
	    Coupling coupling = new Coupling(facts);

	    Cluster root = new HierarchicalClustering(coupling)
	            .build(facts.classNames());

	    ModuleCutter cutter = new ModuleCutter(coupling);

	    // La moyenne de {A,B,C} vaut exactement 1/3.
	    var modules = cutter.cut(root, 1.0 / 3.0);

	    assertEquals(2, modules.size());
	    assertEquals(List.of("demo.C"), modules.get(0).members());
	    assertEquals(List.of("demo.A", "demo.B"),
	            modules.get(1).members());
	}
	
	@Test
	void matchesStandardCutsAndPreservesPartition() throws Exception {
	    Facts facts = Facts.load(Path.of("../resources"));
	    Coupling coupling = new Coupling(facts);

	    Cluster root = new HierarchicalClustering(coupling)
	            .build(facts.classNames());

	    ModuleCutter cutter = new ModuleCutter(coupling);

	    double[] thresholds = {0.015, 0.025, 0.045};
	    int[] expectedCounts = {4, 7, 10};

	    for (int i = 0; i < thresholds.length; i++) {
	        double cp = thresholds[i];
	        var modules = cutter.cut(root, cp);

	        assertEquals(expectedCounts[i], modules.size());

	        Set<String> seen = new HashSet<>();

	        for (Cluster module : modules) {
	            assertTrue(isSubtree(root, module),
	                    "Un module doit être un sous-arbre");

	            for (String name : module.members()) {
	                assertTrue(seen.add(name),
	                        "Classe présente plusieurs fois : " + name);
	            }

	            if (!module.isLeaf()) {
	                assertTrue(cutter.internalAverage(module) > cp,
	                        "La moyenne doit dépasser strictement CP");
	            }
	        }

	        assertEquals(new HashSet<>(facts.classNames()), seen);

	        boolean acceptable =
	                modules.size() <= facts.classes().size() / 2;

	        assertEquals(i < 2, acceptable);
	    }
	}
	
	private boolean isSubtree(Cluster node, Cluster candidate) {
	    if (node == candidate) {
	        return true;
	    }

	    if (node.isLeaf()) {
	        return false;
	    }

	    return isSubtree(node.left(), candidate)
	            || isSubtree(node.right(), candidate);
	}
	
}
