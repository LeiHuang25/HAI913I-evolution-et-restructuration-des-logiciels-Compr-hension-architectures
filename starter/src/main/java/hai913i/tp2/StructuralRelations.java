package hai913i.tp2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class StructuralRelations {
	public record Relation(String source, String target, String kind) {
		
	}
	
	private final List<Relation> internalRelations;
	private final int externalCount;
	
	private StructuralRelations(
            List<Relation> internalRelations,
            int externalCount) {
        this.internalRelations = List.copyOf(internalRelations);
        this.externalCount = externalCount;
    }
	
	public static StructuralRelations load(
            Path csvPath, List<String> classNames)
            throws IOException {

        List<String> lines = Files.readAllLines(
                csvPath, StandardCharsets.UTF_8);

        if (lines.isEmpty()
                || !"source;target;kind".equals(lines.get(0).strip())) {
            throw new IOException(
                    "En-tête structure CSV invalide : " + csvPath);
        }

        Set<String> knownClasses = new HashSet<>(classNames);
        Set<Relation> uniqueRelations = new LinkedHashSet<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).strip();

            if (line.isEmpty()) {
                continue;
            }

            String[] fields = line.split(";", -1);

            if (fields.length != 3) {
                throw new IOException(
                        "Structure CSV, ligne " + (i + 1)
                        + " : 3 colonnes attendues");
            }

            String source = fields[0].strip();
            String target = fields[1].strip();
            String kind = fields[2].strip();

            if (source.isEmpty() || target.isEmpty()
                    || (!"extends".equals(kind)
                    && !"implements".equals(kind))) {
                throw new IOException(
                        "Relation invalide, ligne " + (i + 1));
            }

            if (!knownClasses.contains(source)) {
                throw new IOException(
                        "Type source inconnu : " + source);
            }

            if (source.equals(target)) {
                throw new IOException(
                        "Auto-relation interdite : " + source);
            }

            uniqueRelations.add(
                    new Relation(source, target, kind));
        }

        List<Relation> internal = uniqueRelations.stream()
                .filter(relation ->
                        knownClasses.contains(relation.target()))
                .toList();

        int external = uniqueRelations.size() - internal.size();

        return new StructuralRelations(internal, external);
    }
	
	public List<Relation> internalRelations() {
        return internalRelations;
    }

    public int externalCount() {
        return externalCount;
    }
}
