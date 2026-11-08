package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class EnrichedCouplingTest {

    private Coupling loadEnrichedCoupling() throws Exception {
        Facts facts = Facts.load(Path.of("../resources"));

        StructuralRelations structure = StructuralRelations.load(
                Path.of("../resources/facts-validation-structure.csv"),
                facts.classNames());

        return new Coupling(facts, structure);
    }

    @Test
    void denominatorIncludesInternalStructuralRelations()
            throws Exception {

        Coupling coupling = loadEnrichedCoupling();

        assertEquals(7, coupling.intraClassArcs());
        assertEquals(37, coupling.interClassArcs());
        assertEquals(4, coupling.structuralRelations());
        assertEquals(41, coupling.totalRelations());
    }

    @Test
    void addsStructureWithoutChangingCallCounts()
            throws Exception {

        Coupling coupling = loadEnrichedCoupling();

        String dvd = "library.model.Dvd";
        String item = "library.model.Item";
        String book = "library.model.Book";

        // Dvd -> Item : aucune relation d'appel,
        // mais une relation d'héritage.
        assertEquals(0, coupling.arcCount(dvd, item));
        assertEquals(1, coupling.relationCount(dvd, item));
        assertEquals(1.0 / 41.0,
                coupling.value(dvd, item), 1e-9);

        // Book -> Item : un appel et une relation d'héritage.
        assertEquals(1, coupling.arcCount(book, item));
        assertEquals(2, coupling.relationCount(book, item));
        assertEquals(2.0 / 41.0,
                coupling.value(book, item), 1e-9);
    }

    @Test
    void enrichedCouplingIsSymmetricAndNormalized()
            throws Exception {

        Facts facts = Facts.load(Path.of("../resources"));
        Coupling coupling = loadEnrichedCoupling();
        var names = facts.classNames();

        double sum = 0.0;

        for (int i = 0; i < names.size(); i++) {
            String a = names.get(i);

            assertEquals(0.0, coupling.value(a, a), 1e-9);

            for (int j = i + 1; j < names.size(); j++) {
                String b = names.get(j);
                double value = coupling.value(a, b);

                assertEquals(value,
                        coupling.value(b, a), 1e-9);

                assertTrue(value >= 0.0 && value <= 1.0);

                sum += value;
            }
        }

        assertEquals(1.0, sum, 1e-9);
    }

    @Test
    void defaultModeRemainsCallsOnly() throws Exception {
        Facts facts = Facts.load(Path.of("../resources"));
        Coupling coupling = new Coupling(facts);

        assertEquals(0, coupling.structuralRelations());
        assertEquals(37, coupling.totalRelations());

        assertEquals(0.0,
                coupling.value(
                        "library.model.Dvd",
                        "library.model.Item"),
                1e-9);

        assertEquals(4.0 / 37.0,
                coupling.value(
                        "library.model.Item",
                        "library.service.LoanService"),
                1e-9);
    }
}