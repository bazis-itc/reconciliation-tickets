package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.collection.ListOf;
import bazis.cactoos3.exception.BazisException;
import bazis.tasks.reconciliation_tickets.Person;
import bazis.tasks.reconciliation_tickets.dbf.DbfPersons;
import java.io.File;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public final class JsonPersonsITCase {

    @Test
    public void test() throws BazisException {
        final String printed = new JsonText(
            new JsonPersons(
                new ListOf<>(
                    new DbfPersons(
                        new File("D:\\Exchange\\SC190601.dbf")
                    )
                ).subList(0, 9)
            )
        ).asString();
        final Iterable<Person> parsed =
            new JsonPersons(new JsonText(printed).asJson());
        System.out.println(new JsonText(new JsonPersons(parsed)).asString());
    }

}