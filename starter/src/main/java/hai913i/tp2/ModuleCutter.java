package hai913i.tp2;

import java.util.ArrayList;
import java.util.List;

public final class ModuleCutter {
	private final Coupling coupling;
	
	public ModuleCutter(Coupling coupling) {
        this.coupling = coupling;
    }
	
	public double internalAverage(Cluster group) {
        var members = group.members();

        if (members.size() < 2) {
            return 0.0;
        }

        double sum = 0.0;
        long pairCount = 0;

        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {
                sum += coupling.value(
                        members.get(i), members.get(j));
                pairCount++;
            }
        }

        return sum / pairCount;
    }
	
	public List<Cluster> cut(Cluster root, double cp) {
	    if (!Double.isFinite(cp) || cp < 0.0) {
	        throw new IllegalArgumentException(
	                "CP doit être fini et positif ou nul");
	    }

	    List<Cluster> modules = new ArrayList<>();
	    collectModules(root, cp, modules);

	    return List.copyOf(modules);
	}

	private void collectModules(
	        Cluster node, double cp, List<Cluster> modules) {

	    if (node.isLeaf()) {
	        modules.add(node);
	        return;
	    }

	    if (internalAverage(node) > cp) {
	        modules.add(node);
	        return;
	    }

	    collectModules(node.left(), cp, modules);
	    collectModules(node.right(), cp, modules);
	}
}
