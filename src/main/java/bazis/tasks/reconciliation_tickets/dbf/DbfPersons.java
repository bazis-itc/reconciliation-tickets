package bazis.tasks.reconciliation_tickets.dbf;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.dbf.DbfRecord;
import bazis.dbf.JavaDbf;
import bazis.tasks.reconciliation_tickets.Person;
import java.io.File;
import java.nio.charset.Charset;

public final class DbfPersons extends IterableEnvelope<Person> {

    public DbfPersons(final File file) {
        super(
            new Scalar<Iterable<Person>>() {
                @Override
                public Iterable<Person> value() throws Exception {
                    return new MappedIterable<>(
                        new JavaDbf(file, Charset.forName("CP866")).records(),
                        new Func<DbfRecord, Person>() {
                            @Override
                            public Person apply(DbfRecord record) {
                                return new DbfPerson(record);
                            }
                        }
                    );
                }
            }
        );
    }

}
