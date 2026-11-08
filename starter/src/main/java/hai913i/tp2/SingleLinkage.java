package hai913i.tp2;

public final class SingleLinkage implements Linkage{
	private final Coupling coupling;
	
	public SingleLinkage(Coupling coupling) {
		this.coupling = coupling;
	}
	
	@Override
    public double similarity(Cluster first, Cluster second) {
        double maximum = 0.0;

        for (String a : first.members()) {
            for (String b : second.members()) {
                maximum = Math.max(
                        maximum,
                        coupling.value(a, b));
            }
        }

        return maximum;
    }
}
