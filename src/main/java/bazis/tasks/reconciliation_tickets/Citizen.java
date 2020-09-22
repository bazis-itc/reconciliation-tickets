package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Opt;
import bazis.cactoos3.exception.BazisException;
import java.util.Date;

public interface Citizen {

    Fio fio();

    String snils() throws BazisException;

    Opt<Date> birthdate() throws BazisException;

    Doc passport();

    Doc certificate();

    String carrier() throws BazisException;

}
