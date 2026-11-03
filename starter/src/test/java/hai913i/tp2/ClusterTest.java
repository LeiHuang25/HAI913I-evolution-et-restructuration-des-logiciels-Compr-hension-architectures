package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ClusterTest {
	@Test
    void mergePreservesChildrenAndSortedMembers() {
        Cluster a = new Cluster(0, "demo.A");
        Cluster b = new Cluster(1, "demo.B");

        Cluster merged = new Cluster(3, b, a, 2.0 / 3.0);

        assertTrue(a.isLeaf());
        assertFalse(merged.isLeaf());

        assertEquals(3, merged.id());
        assertEquals(List.of("demo.A", "demo.B"), merged.members());

        assertSame(b, merged.left());
        assertSame(a, merged.right());

        assertEquals(2.0 / 3.0, merged.similarity(), 1e-9);
    }
	
	@Test
    void rejectsOverlappingGroups() {
        Cluster a = new Cluster(0, "demo.A");
        Cluster anotherA = new Cluster(1, "demo.A");

        assertThrows(IllegalArgumentException.class,
                () -> new Cluster(2, a, anotherA, 0.5));
    }
}
