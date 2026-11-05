package hai913i.tp2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class DendrogramExporter {
	private DendrogramExporter() {
    }
	
	public static void exportDot(Cluster root, Path path)
            throws IOException {

        Files.createDirectories(path.toAbsolutePath().getParent());

        StringBuilder dot = new StringBuilder();
        dot.append("digraph Dendrogram {\n");
        dot.append("  graph [charset=\"UTF-8\", rankdir=TB];\n");
        dot.append("  node [shape=box];\n");

        int classCount = root.members().size();
        appendNode(root, classCount, dot);

        dot.append("}\n");

        Files.writeString(
                path, dot.toString(), StandardCharsets.UTF_8);
    }
	
	private static void appendNode(
            Cluster node, int classCount, StringBuilder dot) {

        String label;

        if (node.isLeaf()) {
            label = quote(node.members().get(0));
        } else {
            int step = node.id() - classCount + 1;

            label = quote(String.format(Locale.ROOT,
                    "Fusion %d | S = %.4f",
                    step, node.similarity()));
        }

        dot.append("  n").append(node.id())
                .append(" [label=").append(label)
                .append("];\n");

        if (!node.isLeaf()) {
            appendNode(node.left(), classCount, dot);
            appendNode(node.right(), classCount, dot);

            dot.append("  n").append(node.id())
                    .append(" -> n").append(node.left().id())
                    .append(" [arrowhead=none];\n");

            dot.append("  n").append(node.id())
                    .append(" -> n").append(node.right().id())
                    .append(" [arrowhead=none];\n");
        }
    }
	
	public static void exportText(Cluster root, Path path)
	        throws IOException {

	    Files.createDirectories(path.toAbsolutePath().getParent());

	    StringBuilder text = new StringBuilder();
	    appendText(root, 0, text);

	    Files.writeString(
	            path, text.toString(), StandardCharsets.UTF_8);
	}

	private static void appendText(
	        Cluster node, int depth, StringBuilder text) {

	    text.append("  ".repeat(depth));

	    if (node.isLeaf()) {
	        text.append("Classe ")
	                .append(node.id())
	                .append(" : ")
	                .append(node.members().get(0))
	                .append("\n");
	    } else {
	        text.append(String.format(Locale.ROOT,
	                "Groupe %d | S = %.4f | membres = %s%n",
	                node.id(),
	                node.similarity(),
	                node.members()));

	        appendText(node.left(), depth + 1, text);
	        appendText(node.right(), depth + 1, text);
	    }
	}

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"";
    }
}
