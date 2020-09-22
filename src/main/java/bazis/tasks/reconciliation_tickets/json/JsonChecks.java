package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.tasks.reconciliation_tickets.Check;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public final class JsonChecks extends IterableEnvelope<Check>
    implements Jsonable {

    public JsonChecks(JsonElement json) {
        this(new JsonChecks.Parsed(json));
    }

    public JsonChecks(Iterable<Check> iterable) {
        super(iterable);
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonArray json = new JsonArray();
        for (final Check check : this)
            json.add(new JsonCheck(check).asJson());
        return json;
    }

    private static final class Parsed extends IterableEnvelope<Check> {

        private Parsed(final JsonElement json) {
            super(
                new Scalar<Iterable<Check>>() {
                    @Override
                    public Iterable<Check> value() {
                        return new MappedIterable<>(
                            json.getAsJsonArray(),
                            new Func<JsonElement, Check>() {
                                @Override
                                public Check apply(JsonElement check) {
                                    return new JsonCheck(check);
                                }
                            }
                        );
                    }
                }
            );
        }

    }

}
