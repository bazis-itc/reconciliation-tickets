package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.iterable.IterableOf;
import bazis.cactoos3.map.Entries;
import bazis.cactoos3.map.MapEnvelope;
import bazis.cactoos3.map.MapOf;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

public final class BoroughMap extends MapEnvelope<Integer, Database> {

    public BoroughMap(final DSLContext central) {
        super(
            new MapOf<>(
                new Entries<>(
                    new IterableOf<>(
                        new Scalar<Iterable<Record>>() {
                            @Override
                            public Iterable<Record> value() {
                                return central.selectFrom(
                                    DSL.table("REFERENCE_INF")
                                ).fetch();
                            }
                        }
                    ),
                    new Func<Record, Integer>() {
                        @Override
                        public Integer apply(Record record) {
                            return new NoNulls(record).number("A_OUID")
                                .get().intValue();
                        }
                    },
                    new Func<Record, Database>() {
                        @Override
                        public Database apply(Record record) {
                            return new HttpDatabase(
                                new NoNulls(record).string("A_IP_ADRESS_RAION")
                            );
                        }
                    }
                )
            )
        );
    }

}
