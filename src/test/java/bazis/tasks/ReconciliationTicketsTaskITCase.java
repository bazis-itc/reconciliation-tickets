package bazis.tasks;

import bazis.cactoos3.Opt;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.IterableOf;
import bazis.cactoos3.map.Entry;
import bazis.cactoos3.map.MapOf;
import bazis.cactoos3.opt.OptOfNullable;
import bazis.dbf.DbfRecord;
import bazis.tasks.reconciliation_tickets.Check;
import bazis.tasks.reconciliation_tickets.Citizen;
import bazis.tasks.reconciliation_tickets.Database;
import bazis.tasks.reconciliation_tickets.FakeDatabase;
import bazis.tasks.reconciliation_tickets.FakeRegister;
import bazis.tasks.reconciliation_tickets.HttpRegister;
import bazis.tasks.reconciliation_tickets.JdbcRegister;
import bazis.tasks.reconciliation_tickets.Register;
import bazis.tasks.reconciliation_tickets.dbf.DbfCitizen;
import java.io.File;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import sx.datastore.impl.fs.SXFileObj;

@Ignore
public final class ReconciliationTicketsTaskITCase {

    @Test
    public void temp() throws BazisException {
        final DSLContext central = DSL.using(
            "jdbc:sqlserver://172.3.1.34:1425;databaseName=central",
            "sa", "S1tex2017"
        );
        final Iterable<Check> result =
            new JdbcRegister(
                central,
                new MapOf<>(
                    new Entry<Integer, Database>(
                        6, new FakeDatabase("http://172.3.1.34:8086/R07/")
                    )
                )
            ).check(
                new IterableOf<Citizen>(
                    new DbfCitizen(
                        new DbfRecord() {
                            private final Map<String, Object> fields = new MapOf<>(
                                new Entry<String, Object>("FM", "АНДРЕЕВА"),
                                new Entry<String, Object>("IM", "ЛЮДМИЛА"),
                                new Entry<String, Object>("OT", "АЛЕКСАНДРОВНА"),
                                new Entry<String, Object>("SNILS", "10531652312"),
                                new Entry<String, Object>("DTR", "1976/01/11"),
                                new Entry<String, Object>("SER_DOC", "1701"),
                                new Entry<String, Object>("NOM_DOC", "373383"),
                                new Entry<>("SER_LG", null),
                                new Entry<>("NOM_LG", null),
                                new Entry<>("KOD_ORG", null)
                            );

                            @Override
                            public Opt<?> field(String column) {
                                if (!this.fields.containsKey(column))
                                    throw new IllegalArgumentException();
                                return new OptOfNullable<>(this.fields.get(column));
                            }
                        }
                    )
                )
            );
        for (final Check check : result) {
            System.out.println(check.success());
            System.out.println(check.message());
        }
    }

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

    @Test
    public void fake() throws Exception {
        this.execute(new FakeRegister());
    }

    private void execute(Register register) throws Exception {
        final SXFileObj input = Mockito.mock(SXFileObj.class);
        Mockito.when(input.getFile()).thenReturn(
            new File(
                this.getClass()
                    .getResource("/SC300801.dbf")
                    .getFile()
            )
        );
        final SXFileObj output = Mockito.mock(SXFileObj.class);
        Mockito.when(output.getFile()).thenReturn(new File("target"));
        final ReconciliationTicketsTask task = new ReconciliationTicketsTask(
            DSL.using(
                "jdbc:sqlserver://172.3.1.34:1471;databaseName=rab",
                "sa", "S1tex2017"
            ),
            register
        );
        task.getLinkedData().put("importFile", input);
        task.getLinkedData().put("outputFolder", output);
        task.execute();
    }

}