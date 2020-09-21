package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.exception.BazisException;

public interface Fio {

    String surname() throws BazisException;

    String name() throws BazisException;

    String patronymic() throws BazisException;

}
