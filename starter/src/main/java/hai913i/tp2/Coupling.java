package hai913i.tp2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class Coupling {

    private record ClassPair(String first, String second) {
        static ClassPair of(String a, String b) {
            return a.compareTo(b) <= 0
                    ? new ClassPair(a, b)
                    : new ClassPair(b, a);
        }
    }

    private final Map<ClassPair, Integer> pairCounts =
            new HashMap<>();

    private final Map<ClassPair, Integer> structuralCounts =
            new HashMap<>();

    private final int intraClassArcs;
    private final int interClassArcs;
    private final int structuralRelations;

    // Mode historique : appels uniquement.
    public Coupling(Facts facts) {
        this(facts, null);
    }

    // Mode enrichi : appels et relations structurelles internes.
    public Coupling(Facts facts, StructuralRelations structure) {
        int intra = 0;
        int inter = 0;

        for (Facts.CallArc arc : facts.calls()) {
            String a = arc.callerClass();
            String b = arc.calleeClass();

            if (a.equals(b)) {
                intra++;
            } else {
                inter++;
                pairCounts.merge(
                        ClassPair.of(a, b), 1, Integer::sum);
            }
        }

        int structural = 0;

        if (structure != null) {
            for (StructuralRelations.Relation relation
                    : structure.internalRelations()) {

                structuralCounts.merge(
                        ClassPair.of(
                                relation.source(),
                                relation.target()),
                        1,
                        Integer::sum);

                structural++;
            }
        }

        this.intraClassArcs = intra;
        this.interClassArcs = inter;
        this.structuralRelations = structural;
    }

    public int intraClassArcs() {
        return intraClassArcs;
    }

    public int interClassArcs() {
        return interClassArcs;
    }

    public int structuralRelations() {
        return structuralRelations;
    }

    public int totalRelations() {
        return interClassArcs + structuralRelations;
    }

    public int nonZeroPairs() {
        var pairs = new HashSet<>(pairCounts.keySet());
        pairs.addAll(structuralCounts.keySet());
        return pairs.size();
    }

    // Nombre d'arcs d'appel uniquement.
    public int arcCount(String a, String b) {
        return pairCounts.getOrDefault(ClassPair.of(a, b), 0);
    }

    // Nombre total de relations pour le couple.
    public int relationCount(String a, String b) {
        return arcCount(a, b)
                + structuralCounts.getOrDefault(
                        ClassPair.of(a, b), 0);
    }

    public double value(String a, String b) {
        if (a.equals(b) || totalRelations() == 0) {
            return 0.0;
        }

        return (double) relationCount(a, b) / totalRelations();
    }
}