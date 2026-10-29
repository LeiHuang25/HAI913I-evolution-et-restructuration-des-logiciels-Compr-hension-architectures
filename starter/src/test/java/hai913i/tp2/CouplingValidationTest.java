package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CouplingValidationTest {

    private static final double EPSILON = 1e-9;

    @Test
    void matchesValidationCheckpoints() throws Exception {
        Facts facts = Facts.load(Path.of("../resources"));
        Coupling coupling = new Coupling(facts);

        assertEquals(15, facts.classes().size());
        assertEquals(59, facts.methodOwners().size());
        assertEquals(44, facts.calls().size());

        assertEquals(7, coupling.intraClassArcs());
        assertEquals(37, coupling.interClassArcs());
        assertEquals(19, coupling.nonZeroPairs());

        assertEquals(4, coupling.arcCount(
                "library.model.Item",
                "library.service.LoanService"));

        assertEquals(4.0 / 37.0, coupling.value(
                "library.model.Item",
                "library.service.LoanService"), EPSILON);

        var classes = facts.classNames();
        double sum = 0.0;
        double maximum = 0.0;

        for (String a : classes) {
            assertEquals(0.0, coupling.value(a, a), EPSILON);
        }

        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                String a = classes.get(i);
                String b = classes.get(j);
                double value = coupling.value(a, b);

                assertTrue(value >= 0.0 && value <= 1.0);
                assertEquals(value, coupling.value(b, a), EPSILON);

                sum += value;
                maximum = Math.max(maximum, value);
            }
        }

        assertEquals(1.0, sum, EPSILON);
        assertEquals(4.0 / 37.0, maximum, EPSILON);

        for (String other : classes) {
            assertEquals(0.0, coupling.value(
                    "library.model.Dvd", other), EPSILON);
            assertEquals(0.0, coupling.value(
                    "library.service.LoanException", other), EPSILON);
        }
    }
}