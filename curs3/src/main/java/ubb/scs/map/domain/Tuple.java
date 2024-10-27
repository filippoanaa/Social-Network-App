package ubb.scs.map.domain;

import java.util.Objects;

public class Tuple<E1,E2> {
    private final E1 e1;
    private final E2 e2;
    public Tuple(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    public E1 getE1() {
        return e1;
    }

    public E2 getE2() {
        return e2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(e1, tuple.e1) && Objects.equals(e2, tuple.e2) || Objects.equals(e1, tuple.e2) && Objects.equals(e2, tuple.e1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(e1, e2);
    }

    @Override
    public String toString() {
        return e1.toString() + " " + e2.toString();
    }
    public int size() {
        if(e1 == null || e2 == null)
            return 1;
        return 2;
    }

    public boolean contains(E2 id) {
        return e1.equals(id) || e2.equals(id);
    }
}
