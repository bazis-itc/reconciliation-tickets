package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Func;
import bazis.cactoos3.collection.SetOf;
import bazis.cactoos3.iterable.EmptyIterable;
import bazis.cactoos3.iterable.IterableOf;
import bazis.cactoos3.iterable.MappedIterable;
import org.jooq.Record;

public final class Person {

    private final Record record;

    public Person(Record record) {
        this.record = record;
    }

    public int centralId() {
        return new NoNulls(this.record).number("centralId").get().intValue();
    }

    public boolean hasCategory(Number id) {
        final String categories = new NoNulls(this.record).string("categories");
        return new SetOf<>(
            categories.isEmpty()
            ? new EmptyIterable<Integer>()
            : new MappedIterable<>(
                new IterableOf<>(categories.split("[|]")),
                new Func<String, Integer>() {
                    @Override
                    public Integer apply(String category) {
                        return Integer.parseInt(category);
                    }
                }
            )
        ).contains(id.intValue());
    }

}
