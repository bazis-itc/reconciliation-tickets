package bazis.tasks;

import bazis.cactoos3.Func;
import bazis.cactoos3.Opt;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.IterableOf;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.cactoos3.scalar.UncheckedScalar;
import bazis.cactoos3.text.CheckedText;
import bazis.cactoos3.text.Lines;
import bazis.cactoos3.text.TextOf;
import bazis.dbf.DbfType;
import bazis.dbf.JavaDbf;
import bazis.dbf.RecordData;
import bazis.tasks.reconciliation_tickets.Check;
import bazis.tasks.reconciliation_tickets.Citizen;
import bazis.tasks.reconciliation_tickets.HttpRegister;
import bazis.tasks.reconciliation_tickets.Register;
import bazis.tasks.reconciliation_tickets.dbf.DbfCitizens;
import bazis.tasks.reconciliation_tickets.ext.TextWithParams;
import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sx.common.MsgBean;
import sx.datastore.SXDsFactory;
import sx.datastore.impl.fs.SXFileObj;
import sx.scheduler.SXTask;

//bazis.tasks.ReconciliationTicketsTask
public class ReconciliationTicketsTask extends SXTask {

    private final DSLContext context;

    private final Register register;

    public ReconciliationTicketsTask() {
        this(
            DSL.using(
                new UncheckedScalar<>(
                    new Scalar<DataSource>() {
                        @Override
                        public DataSource value() throws Exception {
                            return SXDsFactory.getDs().getDb().getDataSource();
                        }
                    }
                ).value(),
                SQLDialect.DEFAULT
            ),
            new HttpRegister("http://192.168.10.21:8080/central/")
        );
    }

    ReconciliationTicketsTask(DSLContext context, Register register) {
        this.context = context;
        this.register = register;
    }

    @Override
    protected MsgBean execute() throws Exception {
        final File input =
            SXFileObj.class.cast(super.getLinkedObj("importFile")).getFile();
        final Iterable<Check> checks =
            this.register.check(new DbfCitizens(input));
        this.saveIntoFile(
            checks,
            new File(
                String.format(
                    "%s%sSC2%s",
                    SXFileObj.class.cast(super.getLinkedObj("outputFolder"))
                        .getFile().getAbsolutePath(),
                    File.separator, input.getName().substring(3)
                )
            )
        );
        this.saveIntoDb(checks);
        return null;
    }

    private void saveIntoFile(Iterable<Check> checks, File file) throws BazisException {
        new JavaDbf(file, Charset.forName("CP866")).create(
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
                checks,
                new Func<Check, Object[]>() {
                    @Override
                    public Object[] apply(Check check) throws BazisException {
                        final Citizen citizen = check.citizen();
                        final Opt<Date> birthdate = citizen.birthdate();
                        return new RecordData()
                            .withString(citizen.snils())
                            .withString(citizen.fio().surname())
                            .withString(citizen.fio().name())
                            .withString(citizen.fio().patronymic())
                            .withString(
                                birthdate.has()
                                    ? new SimpleDateFormat("yyyy/MM/dd")
                                        .format(birthdate.get())
                                    : ""
                            )
                            .withString(citizen.passport().series())
                            .withString(citizen.passport().number())
                            .withString(citizen.certificate().series())
                            .withString(citizen.certificate().number())
                            .withString(citizen.carrier())
                            .withInt(check.success() ? 1 : 0)
                            .withString(check.message())
                            .toArray();
                    }
                }
            )
        );
    }

    private void saveIntoDb(Iterable<Check> checks) throws BazisException {
        final String format = new CheckedText(
            new Lines(
                "INSERT RECONCILIATION_TICKETS_VIEW (",
                "  A_SURNAME, A_NAME, A_PATRONYMIC, A_SNILS, A_BIRTHDATE, ",
                "  A_PASSPORT_SERIES, A_PASSPORT_NUMBER, ",
                "  A_DOC_SERIES, A_DOC_NUMBER, ",
                "  A_CARRIER_CODE, A_CHECK_STATUS, A_COMMENT",
                ") VALUES (",
                "  '{0}', '{1}', '{2}', '{3}', {4}, '{5}', ",
                "  '{6}', '{7}', '{8}', '{9}', '{10}', '{11}'",
                ")"
            )
        ).asString();
        final Text inserts = new Lines(
            new MappedIterable<>(
                checks,
                new Func<Check, Text>() {
                    @Override
                    public Text apply(Check check) throws BazisException {
                        final Citizen citizen = check.citizen();
                        final Opt<Date> birthdate = citizen.birthdate();
                        return new TextWithParams(
                            format,
                            citizen.fio().surname(),
                            citizen.fio().name(),
                            citizen.fio().patronymic(),
                            citizen.snils(),
                            birthdate.has()
                                ? String.format(
                                    "'%s'",
                                    new SimpleDateFormat("yyyy-MM-dd")
                                        .format(birthdate.get())
                                )
                                : "NULL",
                            citizen.passport().series(),
                            citizen.passport().number(),
                            citizen.certificate().series(),
                            citizen.certificate().number(),
                            citizen.carrier(),
                            check.success() ? "1" : "0",
                            check.message()
                        );
                    }
                }
            )
        );
        this.context.execute(
            new CheckedText(
                new Lines(
                    new TextOf("TRUNCATE TABLE RECONCILIATION_TICKETS_VIEW"),
                    new TextOf("SET DATEFORMAT ymd"),
                    inserts
                )
            ).asString()
        );
    }

}
