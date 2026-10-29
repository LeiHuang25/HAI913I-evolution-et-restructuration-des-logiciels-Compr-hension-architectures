package hai913i.tp2;

import java.util.HashMap;
import java.util.Map;

public final class Coupling {
	private record ClassPair(String first, String second) {
		
		static ClassPair of(String a, String b) {
			if (a.compareTo(b) <= 0) {
                return new ClassPair(a, b);
            }
            return new ClassPair(b, a);
		}
	}
	
	private final Map<ClassPair, Integer> pairCounts = new HashMap<>();
    private final int intraClassArcs;
    private final int interClassArcs;
    
    public Coupling(Facts facts) {
        int intra = 0;
        int inter = 0;

        for (Facts.CallArc arc : facts.calls()) {
            String a = arc.callerClass();
            String b = arc.calleeClass();

            if (a.equals(b)) {
                intra++;
            } else {
                inter++;
                pairCounts.merge(ClassPair.of(a, b), 1, Integer::sum);
            }
        }

        this.intraClassArcs = intra;
        this.interClassArcs = inter;
    }
    
    public int intraClassArcs() {
        return intraClassArcs;
    }

    public int interClassArcs() {
        return interClassArcs;
    }

    public int nonZeroPairs() {
        return pairCounts.size();
    }

    public int arcCount(String a, String b) {
        return pairCounts.getOrDefault(ClassPair.of(a, b), 0);
    }

    public double value(String a, String b) {
        if (a.equals(b) || interClassArcs == 0) {
            return 0.0;
        }
        return (double) arcCount(a, b) / interClassArcs;
    }
}
