package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.collection.ListOf;
import bazis.cactoos3.exception.BazisException;
import bazis.tasks.reconciliation_tickets.Citizen;
import bazis.tasks.reconciliation_tickets.dbf.DbfCitizens;
import java.io.File;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public final class JsonCitizensITCase {

    @Test
    public void test() throws BazisException {
        final String printed = new JsonText(
            new JsonCitizens(
                new ListOf<>(
                    new DbfCitizens(
                        new File(
                            this.getClass()
                                .getResource("/SC190601.dbf")
                                .getFile()
                        )
                    )
                ).subList(0, 9)
            )
        ).asString();
        final Iterable<Citizen> parsed =
            new JsonCitizens(new JsonText(printed).asJson());
        System.out.println(new JsonText(new JsonCitizens(parsed)).asString());
    }

}