package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import org.jooq.Record;
import org.jooq.Result;

public interface Database {

    Result<Record> select(Text query) throws BazisException;

    int execute(Text script) throws BazisException;

}
