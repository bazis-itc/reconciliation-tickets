package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Opt;
import bazis.cactoos3.opt.OptOfNullable;
import java.util.Date;
import org.jooq.Record;

public final class NoNulls {

    private final Record record;

    public NoNulls(Record record) {
        this.record = record;
    }

    public boolean bool(String field) {
        final Boolean value = this.record.getValue(field, Boolean.class);
        return value != null && value;
    }

    public String string(String field) {
        final String value = this.record.getValue(field, String.class);
        return value == null ? "" : value;
    }

    public Opt<Number> number(String field) {
        return new OptOfNullable<>(this.record.getValue(field, Number.class));
    }

    public Number number(String field, Number def) {
        final Number number = this.record.getValue(field, Number.class);
        return number == null ? def : number;
    }

    public Opt<Date> date(String field) {
        return new OptOfNullable<>(this.record.getValue(field, Date.class));
    }

}
