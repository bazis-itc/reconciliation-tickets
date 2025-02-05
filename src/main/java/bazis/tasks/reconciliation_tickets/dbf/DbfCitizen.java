package bazis.tasks.reconciliation_tickets.dbf;

import bazis.cactoos3.Opt;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.opt.OptOf;
import bazis.dbf.DbfRecord;
import bazis.tasks.reconciliation_tickets.Citizen;
import bazis.tasks.reconciliation_tickets.Doc;
import bazis.tasks.reconciliation_tickets.Fio;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DbfCitizen implements Citizen {

    private final DbfRecord record;

    public DbfCitizen(DbfRecord record) {
        this.record = record;
    }

    @Override
    public Fio fio() {
        return new Fio() {
            @Override
            public String surname() throws BazisException {
                return DbfCitizen.this.string("FM");
            }
            @Override
            public String name() throws BazisException {
                return DbfCitizen.this.string("IM");
            }
            @Override
            public String patronymic() throws BazisException {
                return DbfCitizen.this.string("OT");
            }
        };
    }

    @Override
    public String snils() throws BazisException {
        return this.string("SNILS");
    }

    @Override
    public Opt<Date> birthdate() throws BazisException {
        try {
            return new OptOf<>(
                new SimpleDateFormat("dd.MM.yyyy").parse(
                    this.string("DTR")
                )
            );
        } catch (final ParseException ex) {
            throw new BazisException(ex);
        }
    }

    @Override
    public Doc passport() {
        return new DbfDoc(this.record, "SER_DOC", "NOM_DOC");
    }

    @Override
    public Doc certificate() {
        return new DbfDoc(this.record, "SER_LG", "NOM_LG");
    }

    @Override
    public String carrier() throws BazisException {
        return this.string("KOD_ORG");
    }

    private String string(String column) throws BazisException {
        return new DbfRecord.Smart(this.record).string(column);
    }

}
