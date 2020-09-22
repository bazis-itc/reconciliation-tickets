package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sx.datastore.SXDsFactory;

public final class SxDatabase implements Database {

    @Override
    public Result<Record> select(Text query) throws BazisException {
        try {
            return DSL.using(SQLDialect.DEFAULT).fetch(
                SXDsFactory.getDs().getDb().executeQuery(query.asString())
            );
        } catch (final Exception ex) {
            throw new BazisException(ex);
        }
    }

    @Override
    public int execute(Text script) throws BazisException {
        try {
            return SXDsFactory.getDs().getDb().executeUpdate(script.asString());
        } catch (final Exception ex) {
            throw new BazisException(ex);
        }
    }

}
