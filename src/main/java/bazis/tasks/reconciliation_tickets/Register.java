package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.exception.BazisException;

public interface Register {

    Iterable<Check> check(Iterable<Citizen> citizens) throws BazisException;

}
