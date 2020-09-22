package bazis.tasks.reconciliation_tickets.dbf;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.dbf.DbfRecord;
import bazis.dbf.JavaDbf;
import bazis.tasks.reconciliation_tickets.Citizen;
import java.io.File;
import java.nio.charset.Charset;

public final class DbfCitizens extends IterableEnvelope<Citizen> {

    public DbfCitizens(final File file) {
        super(
            new Scalar<Iterable<Citizen>>() {
                @Override
                public Iterable<Citizen> value() throws Exception {
                    return new MappedIterable<>(
                        new JavaDbf(file, Charset.forName("CP866")).records(),
                        new Func<DbfRecord, Citizen>() {
                            @Override
                            public Citizen apply(DbfRecord record) {
                                return new DbfCitizen(record);
                            }
                        }
                    );
                }
            }
        );
    }

}
