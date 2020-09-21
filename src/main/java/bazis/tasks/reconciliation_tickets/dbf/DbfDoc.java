package bazis.tasks.reconciliation_tickets.dbf;

import bazis.cactoos3.exception.BazisException;
import bazis.dbf.DbfRecord;
import bazis.tasks.reconciliation_tickets.Doc;

final class DbfDoc implements Doc {

    private final DbfRecord record;

    private final String series, number;

    DbfDoc(DbfRecord record, String series, String number) {
        this.record = record;
        this.series = series;
        this.number = number;
    }

    @Override
    public String series() throws BazisException {
        return new DbfRecord.Smart(this.record).string(this.series);
    }

    @Override
    public String number() throws BazisException {
        return new DbfRecord.Smart(this.record).string(this.number);
    }

}
