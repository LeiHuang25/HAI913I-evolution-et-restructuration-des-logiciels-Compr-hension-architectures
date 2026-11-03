package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AverageLinkageTest {

	@Test
    void singletonSimilarityEqualsCoupling() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        AverageLinkage linkage = new AverageLinkage(
                new Coupling(facts));

        Cluster a = new Cluster(0, "demo.A");
        Cluster b = new Cluster(1, "demo.B");

        assertEquals(2.0 / 3.0,
                linkage.similarity(a, b), 1e-9);
    }

    @Test
    void includesZeroCouplingsInAverage() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        AverageLinkage linkage = new AverageLinkage(
                new Coupling(facts));

        Cluster a = new Cluster(0, "demo.A");
        Cluster b = new Cluster(1, "demo.B");
        Cluster c = new Cluster(2, "demo.C");

        Cluster ab = new Cluster(3, a, b, 2.0 / 3.0);

        assertEquals(1.0 / 6.0,
                linkage.similarity(ab, c), 1e-9);

        assertEquals(linkage.similarity(ab, c),
                linkage.similarity(c, ab), 1e-9);
    }

}
