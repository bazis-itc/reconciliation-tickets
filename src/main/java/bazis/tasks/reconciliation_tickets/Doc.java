package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.exception.BazisException;

public interface Doc {

    String series() throws BazisException;

    String number() throws BazisException;

}
