package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CouplingGraphTest {
	@TempDir
	Path temporaryDirectory;
	
	@Test
    void buildsMiniGraph() throws Exception {
        Facts facts = Facts.load(Path.of("src/test/resources"));
        CouplingGraph graph = new CouplingGraph(
                facts.classNames(), new Coupling(facts));

        assertEquals(3, graph.nodes().size());
        assertEquals(2, graph.edges().size());
        assertEquals(List.of(), graph.isolatedClasses());
    }
	
	@Test
    void csvRoundTripPreservesValidationGraph() throws Exception {
        Facts facts = Facts.load(Path.of("../resources"));
        CouplingGraph original = new CouplingGraph(
                facts.classNames(), new Coupling(facts));

        original.exportCsv(temporaryDirectory, "validation");

        CouplingGraph restored = CouplingGraph.loadCsv(
                temporaryDirectory, "validation");

        assertEquals(15, restored.nodes().size());
        assertEquals(19, restored.edges().size());

        assertEquals(original.nodes(), restored.nodes());
        assertEquals(original.edges(), restored.edges());

        assertEquals(List.of(
                "library.model.Dvd",
                "library.service.LoanException"),
                restored.isolatedClasses());
    }

    @Test
    void rejectsEdgeWithUnknownEndpoint() throws Exception {
        Files.write(
                temporaryDirectory.resolve("invalid-nodes.csv"),
                List.of("class", "demo.A"),
                StandardCharsets.UTF_8);

        Files.write(
                temporaryDirectory.resolve("invalid-edges.csv"),
                List.of(
                        "first;second;weight",
                        "demo.A;demo.Unknown;0.5"),
                StandardCharsets.UTF_8);

        assertThrows(IOException.class,
                () -> CouplingGraph.loadCsv(
                        temporaryDirectory, "invalid"));
    }
	
}
