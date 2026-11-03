package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HierarchicalClusteringTest {

    @Test
    void buildsExpectedMiniTree() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));

        HierarchicalClustering clustering =
                new HierarchicalClustering(new Coupling(facts));

        Cluster root = clustering.build(facts.classNames());

        assertEquals(4, root.id());
        assertEquals(List.of("demo.A", "demo.B", "demo.C"),
                root.members());
        assertEquals(1.0 / 6.0, root.similarity(), 1e-9);

        // Au dernier tour : C porte l'id 2, AB porte l'id 3.
        assertEquals(List.of("demo.C"), root.left().members());

        Cluster ab = root.right();

        assertEquals(3, ab.id());
        assertEquals(List.of("demo.A", "demo.B"), ab.members());
        assertEquals(2.0 / 3.0, ab.similarity(), 1e-9);

        assertEquals(List.of("demo.A"), ab.left().members());
        assertEquals(List.of("demo.B"), ab.right().members());
    }
    
    @Test
    void validatesStandardTree() throws Exception {
        Facts facts = Facts.load(Path.of("../resources"));

        HierarchicalClustering clustering =
                new HierarchicalClustering(new Coupling(facts));

        Cluster root = clustering.build(facts.classNames());
        List<Cluster> merges = ClusteringReporter.merges(root);

        assertEquals(14, merges.size());
        assertEquals(facts.classNames(), root.members());
        assertEquals(15, countLeaves(root));

        // Les trois premières fusions du point de contrôle B1.
        assertEquals(4, merges.get(0).left().id());
        assertEquals(10, merges.get(0).right().id());
        assertEquals(4.0 / 37.0,
                merges.get(0).similarity(), 1e-9);

        assertEquals(0, merges.get(1).left().id());
        assertEquals(7, merges.get(1).right().id());
        assertEquals(3.0 / 37.0,
                merges.get(1).similarity(), 1e-9);

        assertEquals(15, merges.get(2).left().id());
        assertEquals(16, merges.get(2).right().id());
        assertEquals(9.0 / 148.0,
                merges.get(2).similarity(), 1e-9);

        for (int i = 1; i < merges.size(); i++) {
            assertTrue(
                    merges.get(i).similarity()
                    <= merges.get(i - 1).similarity() + 1e-9,
                    "Les similarités ne doivent pas augmenter");
        }

        assertEquals(0.0, merges.get(12).similarity(), 1e-9);
        assertEquals(0.0, merges.get(13).similarity(), 1e-9);

        verifyMembers(root);
    }
    
    private int countLeaves(Cluster node) {
        if (node.isLeaf()) {
            return 1;
        }

        return countLeaves(node.left()) + countLeaves(node.right());
    }

    private void verifyMembers(Cluster node) {
        if (node.isLeaf()) {
            assertEquals(1, node.members().size());
            return;
        }

        List<String> left = node.left().members();
        List<String> right = node.right().members();

        assertTrue(left.stream().noneMatch(right::contains),
                "Les deux fils doivent être disjoints");

        List<String> expected = new java.util.ArrayList<>(left);
        expected.addAll(right);
        expected.sort(String::compareTo);

        assertEquals(expected, node.members());

        verifyMembers(node.left());
        verifyMembers(node.right());
    }
    
    @Test
    void breaksTiesBySmallestIdPair(@TempDir Path directory)
            throws Exception {

        Files.write(
                directory.resolve("tie-classes.csv"),
                List.of(
                        "class;simpleName;package",
                        "demo.A;A;demo",
                        "demo.B;B;demo",
                        "demo.C;C;demo",
                        "demo.D;D;demo"),
                StandardCharsets.UTF_8);

        Files.write(
                directory.resolve("tie-methods.csv"),
                List.of(
                        "method;owner",
                        "demo.A#f();demo.A",
                        "demo.B#f();demo.B",
                        "demo.C#f();demo.C",
                        "demo.D#f();demo.D"),
                StandardCharsets.UTF_8);

        Files.write(
                directory.resolve("tie-calls.csv"),
                List.of(
                        "caller;callerClass;callee;calleeClass;sites",
                        "demo.A#f();demo.A;demo.C#f();demo.C;1",
                        "demo.B#f();demo.B;demo.D#f();demo.D;1",
                        "demo.C#f();demo.C;demo.D#f();demo.D;1"),
                StandardCharsets.UTF_8);

        Facts facts = Facts.load(directory);

        HierarchicalClustering clustering =
                new HierarchicalClustering(new Coupling(facts));

        Cluster root = clustering.build(facts.classNames());
        Cluster firstMerge = ClusteringReporter.merges(root).get(0);

        assertEquals(0, firstMerge.left().id());
        assertEquals(2, firstMerge.right().id());

        assertEquals(List.of("demo.A", "demo.C"),
                firstMerge.members());

        assertEquals(1.0 / 3.0,
                firstMerge.similarity(), 1e-9);
    }
}