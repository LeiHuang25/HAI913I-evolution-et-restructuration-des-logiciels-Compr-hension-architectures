package hai913i.tp2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class ModuleExporter {

    private ModuleExporter() {
    }

    public static void exportText(
            List<Cluster> modules,
            ModuleCutter cutter,
            int classCount,
            double cp,
            Path path) throws IOException {

        Files.createDirectories(path.toAbsolutePath().getParent());

        StringBuilder text = new StringBuilder();

        text.append("CP = ").append(Double.toString(cp)).append("\n");
        text.append("Classes : ").append(classCount).append("\n");
        text.append("Nombre de modules : ")
                .append(modules.size()).append("\n");
        text.append("Maximum autorisé M/2 : ")
                .append(classCount / 2).append("\n");

        boolean acceptable = modules.size() <= classCount / 2;

        text.append("Contrainte M/2 : ")
                .append(acceptable
                        ? "RESPECTÉE"
                        : "VIOLÉE — coupe rejetée")
                .append("\n\n");

        int number = 1;

        for (Cluster module : modules) {
            text.append(String.format(Locale.ROOT,
                    "Module %d | groupe = %d | classes = %d%n",
                    number++, module.id(), module.members().size()));

            text.append("Moyenne interne : ");

            if (module.isLeaf()) {
                text.append("N/A (classe unique)");
            } else {
                text.append(Double.toString(
                        cutter.internalAverage(module)));
            }

            text.append("\n");
            text.append(module.members()).append("\n\n");
        }

        Files.writeString(
                path, text.toString(), StandardCharsets.UTF_8);
    }
}