package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Scalar;
import bazis.cactoos3.Text;
import bazis.cactoos3.scalar.UncheckedScalar;
import java.util.Collection;
import java.util.Comparator;

public final class Contains implements Scalar<Boolean> {

    private final UncheckedScalar<Boolean> scalar;

    public Contains(final String text, final String subtext) {
        this(
            new Scalar<Boolean>() {
                @Override
                public Boolean value() {
                    return text.contains(subtext);
                }
            }
        );
    }

    public Contains(final Text text, final Text subtext) {
        this(
            new Scalar<Boolean>() {
                @Override
                public Boolean value() throws Exception {
                    return text.asString().contains(subtext.asString());
                }
            }
        );
    }

    public <T> Contains(final Collection<T> collection, final T item) {
        this(
            new Scalar<Boolean>() {
                @Override
                public Boolean value() {
                    return collection.contains(item);
                }
            }
        );
    }

    public <T> Contains(final Comparator<T> comparator,
                        final Iterable<T> iterable, final T item) {
        this(
            new Scalar<Boolean>() {
                @Override
                public Boolean value() {
                    boolean result = false;
                    for (final T element : iterable)
                        if (comparator.compare(item, element) == 0) {
                            result = true;
                            break;
                        }
                    return result;
                }
            }
        );
    }

    private Contains(Scalar<Boolean> scalar) {
        this.scalar = new UncheckedScalar<>(scalar);
    }

    @Override
    public Boolean value() {
        return this.scalar.value();
    }

}
