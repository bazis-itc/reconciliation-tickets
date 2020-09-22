package bazis.tasks;

import bazis.cactoos3.Func;
import bazis.cactoos3.Opt;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.IterableOf;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.dbf.DbfType;
import bazis.dbf.JavaDbf;
import bazis.dbf.RecordData;
import bazis.tasks.reconciliation_tickets.Check;
import bazis.tasks.reconciliation_tickets.HttpRegister;
import bazis.tasks.reconciliation_tickets.Person;
import bazis.tasks.reconciliation_tickets.Register;
import bazis.tasks.reconciliation_tickets.dbf.DbfPersons;
import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import sx.common.MsgBean;
import sx.datastore.impl.fs.SXFileObj;
import sx.scheduler.SXTask;

//bazis.tasks.ReconciliationTicketsTask
public class ReconciliationTicketsTask extends SXTask {

    private final Register register;

    public ReconciliationTicketsTask() {
        this(new HttpRegister("http://172.3.1.34:8025/central/"));
    }

    ReconciliationTicketsTask(Register register) {
        this.register = register;
    }

    @Override
    protected MsgBean execute() throws Exception {
        final File input =
            SXFileObj.class.cast(super.getLinkedObj("importFile")).getFile();
        new JavaDbf(
            new File(
                String.format(
                    "%s%sSC2%s",
                    SXFileObj.class.cast(super.getLinkedObj("outputFolder"))
                        .getFile().getAbsolutePath(),
                    File.separator, input.getName().substring(3)
                )
            ),
            Charset.forName("CP866")
        ).create(
            new IterableOf<>(
                DbfType.CHARACTER.column("SNILS", 11),
                DbfType.CHARACTER.column("FM", 40),
                DbfType.CHARACTER.column("IM", 40),
                DbfType.CHARACTER.column("OT", 40),
                DbfType.CHARACTER.column("DTR", 10),
                DbfType.CHARACTER.column("SER_DOC", 10),
                DbfType.CHARACTER.column("NOM_DOC", 10),
                DbfType.CHARACTER.column("SER_LG", 10),
                DbfType.CHARACTER.column("NOM_LG", 10),
                DbfType.CHARACTER.column("KOD_ORG", 15),
                DbfType.NUMERIC.column("PR_PROV", 1),
                DbfType.CHARACTER.column("COMM", 60)
            ),
            new MappedIterable<>(
                this.register.check(new DbfPersons(input)),
                new Func<Check, Object[]>() {
                    @Override
                    public Object[] apply(Check check) throws BazisException {
                        final Person person = check.person();
                        final Opt<Date> birthdate = person.birthdate();
                        return new RecordData()
                            .withString(person.snils())
                            .withString(person.fio().surname())
                            .withString(person.fio().name())
                            .withString(person.fio().patronymic())
                            .withString(
                                birthdate.has()
                                    ? new SimpleDateFormat("yyyy/MM/dd")
                                        .format(birthdate.get())
                                    : ""
                            )
                            .withString(person.passport().series())
                            .withString(person.passport().number())
                            .withString(person.certificate().series())
                            .withString(person.certificate().number())
                            .withString(person.carrier())
                            .withInt(check.success() ? 1 : 0)
                            .withString(check.message())
                            .toArray();
                    }
                }
            )
        );
        return null;
    }

}
