package bazis.tasks.reconciliation_tickets;

import bazis.tasks.reconciliation_tickets.dbf.DbfCitizens;
import java.io.File;
import java.text.SimpleDateFormat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public final class DbfCitizensITCase {

    @Test
    public void test() throws Exception {
        for (
            final Citizen citizen : new DbfCitizens(
            new File(
                this.getClass()
                    .getResource("/SC190601.dbf")
                    .getFile()
            )
            )
        ) System.out.printf(
            "%s %s %s %s, паспорт: %s %s%n",
            citizen.fio().surname(),
            citizen.fio().name(),
            citizen.fio().patronymic(),
            new SimpleDateFormat("dd.MM.yyyy").format(
                citizen.birthdate().get()
            ),
            citizen.passport().series(), citizen.passport().number()
        );
    }

}