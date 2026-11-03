package hai913i.tp2;

public final class AverageLinkage {
	private final Coupling coupling;
	
	public AverageLinkage(Coupling coupling) {
		this.coupling = coupling;
	}
	
	public double similarity(Cluster first, Cluster second) {
		double sum = 0.0;
		
		for (String a : first.members()) {
            for (String b : second.members()) {
                sum += coupling.value(a, b);
            }
        }
		
		double pairCount = (double) first.members().size() * second.members().size();
		
		return sum/pairCount;
	}
}
