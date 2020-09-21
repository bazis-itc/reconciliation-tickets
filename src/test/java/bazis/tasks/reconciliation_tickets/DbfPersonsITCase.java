package bazis.tasks.reconciliation_tickets;

import bazis.tasks.reconciliation_tickets.dbf.DbfPersons;
import java.io.File;
import java.text.SimpleDateFormat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public final class DbfPersonsITCase {

    @Test
    public void test() throws Exception {
        for (
            final Person person : new DbfPersons(
                new File("D:\\Exchange\\SC190601.dbf")
            )
        ) System.out.printf(
            "%s %s %s %s, паспорт: %s %s%n",
            person.fio().surname(),
            person.fio().name(),
            person.fio().patronymic(),
            new SimpleDateFormat("dd.MM.yyyy").format(
                person.birthdate().get()
            ),
            person.passport().series(), person.passport().number()
        );
    }

}