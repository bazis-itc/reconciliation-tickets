package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Text;

public final class SyncText implements Text {

    private final Text origin;

    public SyncText(Text origin) {
        this.origin = origin;
    }

    @Override
    public String asString() throws Exception {
        synchronized (this) {
            return this.origin.asString();
        }
    }

}
