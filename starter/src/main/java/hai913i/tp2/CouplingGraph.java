package hai913i.tp2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CouplingGraph {
	public record Edge(String first, String second, double weight) {}
	
	private final List<String> nodes;
	private final List<Edge> edges;
 
	public CouplingGraph(List<String> classNames, Coupling coupling) {
        this.nodes = classNames.stream().sorted().toList();

        List<Edge> result = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                String a = nodes.get(i);
                String b = nodes.get(j);
                double weight = coupling.value(a, b);

                if (weight > 0.0) {
                    result.add(new Edge(a, b, weight));
                }
            }
        }

        this.edges = List.copyOf(result);
	}
	
	private CouplingGraph(List<String> nodes, List<Edge> edges) {
	    this.nodes = List.copyOf(nodes);
	    this.edges = List.copyOf(edges);
	}
 
	public List<String> nodes() {
        return nodes;
	}
	 
    public List<Edge> edges() {
        return edges;
    }

    public List<String> isolatedClasses() {
        Set<String> connected = new HashSet<>();

        for (Edge edge : edges) {
            connected.add(edge.first());
            connected.add(edge.second());
        }

        return nodes.stream()
                .filter(name -> !connected.contains(name))
                .toList();
    }
    
    public void exportDot(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);

        StringBuilder dot = new StringBuilder();
        dot.append("graph Coupling {\n");
        dot.append("  graph [charset=\"UTF-8\", overlap=false];\n");
        dot.append("  node [shape=box];\n");

        // Déclarer tous les nœuds, même les classes isolées.
        for (String name : nodes) {
            dot.append("  ")
                    .append(quote(name))
                    .append(";\n");
        }

        for (Edge edge : edges) {
            dot.append("  ")
                    .append(quote(edge.first()))
                    .append(" -- ")
                    .append(quote(edge.second()))
                    .append(" [label=\"")
                    .append(String.format(Locale.ROOT, "%.4f", edge.weight()))
                    .append("\"];\n");
        }

        dot.append("}\n");

        Files.writeString(path, dot.toString(), StandardCharsets.UTF_8);
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"";
    }
    
    public void exportCsv(Path directory, String prefix) throws IOException {
        Files.createDirectories(directory);

        List<String> nodeLines = new ArrayList<>();
        nodeLines.add("class");
        nodeLines.addAll(nodes);

        List<String> edgeLines = new ArrayList<>();
        edgeLines.add("first;second;weight");

        for (Edge edge : edges) {
            edgeLines.add(
                    edge.first() + ";"
                    + edge.second() + ";"
                    + Double.toString(edge.weight()));
        }

        Files.write(
                directory.resolve(prefix + "-nodes.csv"),
                nodeLines,
                StandardCharsets.UTF_8);

        Files.write(
                directory.resolve(prefix + "-edges.csv"),
                edgeLines,
                StandardCharsets.UTF_8);
    }
    
    public static CouplingGraph loadCsv(Path directory, String prefix)
            throws IOException {

        List<String> nodeLines = Files.readAllLines(
                directory.resolve(prefix + "-nodes.csv"),
                StandardCharsets.UTF_8);

        List<String> edgeLines = Files.readAllLines(
                directory.resolve(prefix + "-edges.csv"),
                StandardCharsets.UTF_8);

        if (nodeLines.isEmpty() || !"class".equals(nodeLines.get(0))) {
            throw new IOException("En-tête du fichier des nœuds invalide");
        }

        if (edgeLines.isEmpty()
                || !"first;second;weight".equals(edgeLines.get(0))) {
            throw new IOException("En-tête du fichier des arêtes invalide");
        }

        List<String> loadedNodes = new ArrayList<>();
        Set<String> knownNodes = new HashSet<>();

        for (int i = 1; i < nodeLines.size(); i++) {
            String name = nodeLines.get(i);

            if (name.isBlank() || !knownNodes.add(name)) {
                throw new IOException("Nœud vide ou dupliqué : " + name);
            }

            loadedNodes.add(name);
        }

        List<Edge> loadedEdges = new ArrayList<>();
        Set<List<String>> knownPairs = new HashSet<>();

        for (int i = 1; i < edgeLines.size(); i++) {
            String[] fields = edgeLines.get(i).split(";", -1);

            if (fields.length != 3) {
                throw new IOException("Arête mal formée, ligne " + (i + 1));
            }

            String a = fields[0];
            String b = fields[1];

            if (!knownNodes.contains(a) || !knownNodes.contains(b)
                    || a.equals(b)) {
                throw new IOException("Extrémités invalides : " + a + ", " + b);
            }

            if (a.compareTo(b) > 0) {
                String temp = a;
                a = b;
                b = temp;
            }

            if (!knownPairs.add(List.of(a, b))) {
                throw new IOException("Arête dupliquée : " + a + ", " + b);
            }

            double weight;
            try {
                weight = Double.parseDouble(fields[2]);
            } catch (NumberFormatException e) {
                throw new IOException("Poids invalide, ligne " + (i + 1), e);
            }

            if (!Double.isFinite(weight) || weight <= 0.0 || weight > 1.0) {
                throw new IOException("Poids hors limites : " + weight);
            }

            loadedEdges.add(new Edge(a, b, weight));
        }

        loadedNodes.sort(String::compareTo);
        loadedEdges.sort(java.util.Comparator
                .comparing(Edge::first)
                .thenComparing(Edge::second));

        return new CouplingGraph(loadedNodes, loadedEdges);
    }
}
