package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.iterable.IterableEnvelope;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class IntRange extends IterableEnvelope<Number> {
    
    public IntRange(final Number min, final Number max) {
        super(
            new Iterable<Number>() {
                @Override
                public Iterator<Number> iterator() {
                    return new IntRangeIterator(min, max);
                }
            }
        );
    }

}

final class IntRangeIterator implements Iterator<Number> {
    
    private final Number max;
    
    private Number next;

    IntRangeIterator(Number min, Number max) {
        super();
        this.max = max;
        this.next = min;
    }

    @Override
    public boolean hasNext() {
        return this.next.intValue() <= this.max.intValue();
    }

    @Override
    public Number next() {
        if (!this.hasNext()) throw new NoSuchElementException(
            "The iterator doesn't have any more items"
        );
        final Number result = this.next;
        this.next = this.next.intValue() + 1;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Method not implemented");
    }

}
