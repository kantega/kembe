package kembe.util;

import fj.Ord;
import fj.Ordering;

import java.util.Comparator;

public class Order {

    public static <A> Comparator<A> toComparator(final Ord<A> ord) {
        return new Comparator<A>() {
            @Override public int compare(A o1, A o2) {
                Ordering o = ord.compare( o1, o2 );
                if (o.equals( Ordering.LT ))
                    return -1;
                else if (o.equals( Ordering.EQ ))
                    return 0;
                else
                    return 1;
            }
        };
    }
}
