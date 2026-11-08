package hai913i.tp2;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public final class ClusterExplorer {

    private ClusterExplorer() {
    }

    // Conserver le Cluster réel dans chaque nœud affiché.
    private record TreeEntry(Cluster cluster) {
        @Override
        public String toString() {
            if (cluster.isLeaf()) {
                return cluster.members().get(0);
            }

            return String.format(Locale.ROOT,
                    "Groupe %d | %d classes | S = %.4f",
                    cluster.id(),
                    cluster.members().size(),
                    cluster.similarity());
        }
    }

    private static DefaultMutableTreeNode toTreeNode(
            Cluster cluster) {

        DefaultMutableTreeNode node =
                new DefaultMutableTreeNode(
                        new TreeEntry(cluster));

        if (!cluster.isLeaf()) {
            node.add(toTreeNode(cluster.left()));
            node.add(toTreeNode(cluster.right()));
        }

        return node;
    }

    private static String describe(
            Cluster cluster, ModuleCutter cutter) {

        StringBuilder text = new StringBuilder();

        text.append("Identifiant : ")
                .append(cluster.id()).append("\n");

        text.append("Type : ")
                .append(cluster.isLeaf() ? "Classe" : "Groupe")
                .append("\n");

        text.append("Nombre de classes : ")
                .append(cluster.members().size()).append("\n\n");

        if (!cluster.isLeaf()) {
            text.append(String.format(Locale.ROOT,
                    "Similarité de fusion : %.6f%n",
                    cluster.similarity()));

            text.append(String.format(Locale.ROOT,
                    "Couplage interne moyen : %.6f%n",
                    cutter.internalAverage(cluster)));
        }

        text.append("\nMembres :\n");

        for (String member : cluster.members()) {
            text.append("  ").append(member).append("\n");
        }

        return text.toString();
    }

    private static void show(Cluster root, Coupling coupling) {
        JFrame frame = new JFrame(
                "TP2 — Exploration du dendrogramme");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTree tree = new JTree(toTreeNode(root));
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setMargin(new java.awt.Insets(12, 12, 12, 12));

        ModuleCutter cutter = new ModuleCutter(coupling);

        tree.addTreeSelectionListener(event -> {
            DefaultMutableTreeNode selected =
                    (DefaultMutableTreeNode)
                            tree.getLastSelectedPathComponent();

            if (selected != null) {
                TreeEntry entry =
                        (TreeEntry) selected.getUserObject();

                details.setText(
                        describe(entry.cluster(), cutter));
                details.setCaretPosition(0);
            }
        });

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(details));

        split.setResizeWeight(0.5);

        frame.add(split, BorderLayout.CENTER);
        frame.setSize(1100, 650);
        frame.setLocationRelativeTo(null);

        tree.setSelectionRow(0);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                    "Usage : java -cp target/hai913i-tp2.jar "
                    + "hai913i.tp2.ClusterExplorer "
                    + "<dossier-des-faits>");
            System.exit(1);
            return;
        }

        try {
            Facts facts = Facts.load(Path.of(args[0]));
            Coupling coupling = new Coupling(facts);

            Cluster root = new HierarchicalClustering(coupling)
                    .build(facts.classNames());

            SwingUtilities.invokeLater(
                    () -> show(root, coupling));

        } catch (Exception e) {
            System.err.println(
                    "Exploration impossible : " + e.getMessage());

            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            e.getMessage(),
                            "Erreur de chargement",
                            JOptionPane.ERROR_MESSAGE));
        }
    }
}