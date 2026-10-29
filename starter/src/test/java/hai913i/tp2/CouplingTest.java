package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CouplingTest {

    private static final double EPSILON = 1e-9;

    private Coupling coupling;
    private List<String> classes;

    @BeforeEach
    void setUp() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        coupling = new Coupling(facts);
        classes = facts.classNames();
    }

    @Test
    void countsArcsAndPairs() {
        assertEquals(1, coupling.intraClassArcs());
        assertEquals(3, coupling.interClassArcs());
        assertEquals(2, coupling.nonZeroPairs());
    }

    @Test
    void matchesHandCalculatedValues() {
        assertEquals(2, coupling.arcCount("demo.A", "demo.B"));
        assertEquals(1, coupling.arcCount("demo.B", "demo.C"));

        assertEquals(2.0 / 3.0,
                coupling.value("demo.A", "demo.B"), EPSILON);
        assertEquals(1.0 / 3.0,
                coupling.value("demo.B", "demo.C"), EPSILON);
    }

    @Test
    void isSymmetric() {
        for (String a : classes) {
            for (String b : classes) {
                assertEquals(
                        coupling.value(a, b),
                        coupling.value(b, a),
                        EPSILON);
            }
        }
    }

    @Test
    void diagonalAndAbsentPairsAreZero() {
        for (String a : classes) {
            assertEquals(0.0, coupling.value(a, a), EPSILON);
        }

        assertEquals(0.0,
                coupling.value("demo.A", "demo.C"), EPSILON);
    }

    @Test
    void unorderedCouplingsSumToOne() {
        double sum = 0.0;

        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                sum += coupling.value(classes.get(i), classes.get(j));
            }
        }

        assertEquals(1.0, sum, EPSILON);
    }
}