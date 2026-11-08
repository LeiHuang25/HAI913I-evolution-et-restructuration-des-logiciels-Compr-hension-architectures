package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SingleLinkageTest {

	@Test
    void selectsMaximumInsteadOfAverage() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        Coupling coupling = new Coupling(facts);

        Cluster a = new Cluster(0, "demo.A");
        Cluster b = new Cluster(1, "demo.B");
        Cluster c = new Cluster(2, "demo.C");
        Cluster ab = new Cluster(3, a, b, 2.0 / 3.0);

        Linkage average = new AverageLinkage(coupling);
        Linkage single = new SingleLinkage(coupling);

        assertEquals(1.0 / 6.0,
                average.similarity(ab, c), 1e-9);

        assertEquals(1.0 / 3.0,
                single.similarity(ab, c), 1e-9);

        assertEquals(single.similarity(ab, c),
                single.similarity(c, ab), 1e-9);
    }

    @Test
    void clusteringUsesSelectedLinkage() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        Coupling coupling = new Coupling(facts);

        var names = java.util.List.of(
                "demo.A", "demo.B", "demo.C");

        Cluster averageRoot = new HierarchicalClustering(
                new AverageLinkage(coupling)).build(names);

        Cluster singleRoot = new HierarchicalClustering(
                new SingleLinkage(coupling)).build(names);

        assertEquals(1.0 / 6.0,
                averageRoot.similarity(), 1e-9);

        assertEquals(1.0 / 3.0,
                singleRoot.similarity(), 1e-9);
    }

}
