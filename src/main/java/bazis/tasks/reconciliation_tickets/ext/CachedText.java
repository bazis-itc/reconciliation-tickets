package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Scalar;
import bazis.cactoos3.Text;
import bazis.cactoos3.scalar.CachedScalar;

public final class CachedText implements Text {
    
    private final Scalar<String> scalar;

    public CachedText(final Text text) {
        this.scalar = new CachedScalar<>(
            new Scalar<String>() {
                @Override
                public String value() throws Exception {
                    return text.asString();
                }
            }
        );
    }

    @Override
    public String asString() throws Exception {
        return this.scalar.value();
    }

}
