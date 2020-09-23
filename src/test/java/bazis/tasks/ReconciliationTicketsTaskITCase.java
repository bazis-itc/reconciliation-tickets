package bazis.tasks;

import bazis.cactoos3.map.Entry;
import bazis.cactoos3.map.MapOf;
import bazis.tasks.reconciliation_tickets.Database;
import bazis.tasks.reconciliation_tickets.FakeDatabase;
import bazis.tasks.reconciliation_tickets.HttpRegister;
import bazis.tasks.reconciliation_tickets.JdbcRegister;
import bazis.tasks.reconciliation_tickets.Register;
import java.io.File;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import sx.datastore.impl.fs.SXFileObj;

@Ignore
public final class ReconciliationTicketsTaskITCase {

    @Test
    public void http() throws Exception {
        this.execute(new HttpRegister("http://172.3.1.34:8025/central/"));
    }

    @Test
    public void jdbc() throws Exception {
        final DSLContext central = DSL.using(
            "jdbc:sqlserver://172.3.1.34:1425;databaseName=central",
            "sa", "S1tex2017"
        );
        this.execute(
            new JdbcRegister(
                central,
                new MapOf<>(
                    new Entry<Integer, Database>(
                        1, new FakeDatabase("http://172.3.1.34:8067/rab/")
                    ),
                    new Entry<Integer, Database>(
                        2, new FakeDatabase("http://172.3.1.34:8068/rab/")
                    ),
                    new Entry<Integer, Database>(
                        3, new FakeDatabase("http://172.3.1.34:8069/rab/")
                    )
                )
            )
        );

    }

    private void execute(Register register) throws Exception {
        final SXFileObj input = Mockito.mock(SXFileObj.class);
        Mockito.when(input.getFile()).thenReturn(
            new File(
                this.getClass()
                    .getResource("/SC190601.dbf")
                    .getFile()
            )
        );
        final SXFileObj output = Mockito.mock(SXFileObj.class);
        Mockito.when(output.getFile()).thenReturn(new File("target"));
        final ReconciliationTicketsTask task =
            new ReconciliationTicketsTask(register);
        task.getLinkedData().put("importFile", input);
        task.getLinkedData().put("outputFolder", output);
        task.execute();
    }

}