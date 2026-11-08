package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StructuralRelationsTest {

	@TempDir
    Path tempDir;
	
	@Test
    void loadsInternalRelationsAndFiltersExternalTypes()
            throws Exception {

        Facts facts = Facts.load(Path.of("../resources"));

        StructuralRelations relations = StructuralRelations.load(
                Path.of("../resources/facts-validation-structure.csv"),
                facts.classNames());

        var internal = relations.internalRelations();

        assertEquals(4, internal.size());
        assertEquals(3, relations.externalCount());

        assertTrue(internal.contains(
                new StructuralRelations.Relation(
                        "library.model.Book",
                        "library.model.Item",
                        "extends")));

        assertTrue(internal.contains(
                new StructuralRelations.Relation(
                        "library.model.Dvd",
                        "library.model.Item",
                        "extends")));

        assertTrue(internal.contains(
                new StructuralRelations.Relation(
                        "library.model.Item",
                        "library.model.Loanable",
                        "implements")));

        assertTrue(internal.contains(
                new StructuralRelations.Relation(
                        "library.service.ConsoleNotifier",
                        "library.service.Notifier",
                        "implements")));
    }
	
	@Test
    void duplicateRowsAreCountedOnce() throws Exception {
        Path csv = tempDir.resolve("structure.csv");

        Files.writeString(csv, """
                source;target;kind
                demo.A;demo.B;extends
                demo.A;demo.B;extends
                demo.A;external.Base;implements
                demo.A;external.Base;implements
                """, StandardCharsets.UTF_8);

        StructuralRelations relations = StructuralRelations.load(
                csv, List.of("demo.A", "demo.B"));

        assertEquals(1, relations.internalRelations().size());
        assertEquals(1, relations.externalCount());
    }
	
	@Test
    void rejectsUnknownSource() throws Exception {
        Path csv = tempDir.resolve("unknown-source.csv");

        Files.writeString(csv, """
                source;target;kind
                demo.Unknown;demo.B;extends
                """, StandardCharsets.UTF_8);

        assertThrows(java.io.IOException.class,
                () -> StructuralRelations.load(
                        csv, List.of("demo.A", "demo.B")));
    }

}
