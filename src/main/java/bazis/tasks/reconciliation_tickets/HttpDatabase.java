package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sx.bazis.uninfoobmen.sys.sql.ExecSelectRayon;

public final class HttpDatabase implements Database {

    private final String url;

    public HttpDatabase(String url) {
        this.url = url;
    }

    @Override
    public Result<Record> select(Text query) throws BazisException {
        try {
            return DSL.using(SQLDialect.DEFAULT).fetch(
                ExecSelectRayon.exec(query.asString(), this.url)
            );
        } catch (final Exception ex) {
            throw new BazisException(
                String.format("Сервер не опрошен: %s", this.url), ex
            );
        }
    }

    @Override
    public int execute(Text script) throws BazisException {
        throw new UnsupportedOperationException("Method not implemented");
    }

}
