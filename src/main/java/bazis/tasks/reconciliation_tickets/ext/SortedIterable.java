package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.scalar.UncheckedScalar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class SortedIterable<T> extends IterableEnvelope<T> {

    public SortedIterable(
        final Iterable<T> origin, final Comparator<T> comparator) {
        super(
            new Scalar<Iterable<T>>() {
                @Override
                public Iterable<T> value() {
                    final List<T> list = new LinkedList<>();
                    for (final T item : origin) list.add(item);
                    Collections.sort(list, comparator);
                    return Collections.unmodifiableCollection(list);
                }
            }
        );
    }

    public <K extends Comparable<K>> SortedIterable(
        final Iterable<T> origin, final Func<T, K> keys) {
        this(
            origin, keys,
            new Comparator<K>() {
                @Override
                public int compare(K left, K right) {
                    return left.compareTo(right);
                }
            }
        );
    }

    public <K> SortedIterable(final Iterable<T> origin,
        final Func<T, K> keys, final Comparator<K> comparator) {
        this(
            origin,
            new Comparator<T>() {
                @Override
                public int compare(final T left, final T right) {
                    return new UncheckedScalar<>(
                        new Scalar<Integer>() {
                            @Override
                            public Integer value() throws Exception {
                                return comparator.compare(
                                    keys.apply(left), keys.apply(right)
                                );
                            }
                        }
                    ).value();
                }
            }
        );
    }

}

