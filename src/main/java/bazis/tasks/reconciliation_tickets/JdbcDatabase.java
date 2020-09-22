package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.text.CheckedText;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public final class JdbcDatabase implements Database {

    private final String url;

    public JdbcDatabase(String url) {
        this.url = url;
    }

    @Override
    public Result<Record> select(Text query) throws BazisException {
        try (
            final Connection connection = DriverManager.getConnection(this.url);
            final Statement statement = connection.createStatement()
        ) {
            return DSL.using(SQLDialect.DEFAULT).fetch(
                statement.executeQuery(new CheckedText(query).asString())
            );
        } catch (final SQLException ex) {
            throw new BazisException(ex);
        }
    }

    @Override
    public int execute(Text script) throws BazisException {
        System.out.println(new CheckedText(script).asString());
        try (
            final Connection connection = DriverManager.getConnection(this.url);
            final Statement statement = connection.createStatement()
        ) {
            return statement.executeUpdate(new CheckedText(script).asString());
        } catch (final SQLException ex) {
            throw new BazisException(ex);
        }
    }

}
