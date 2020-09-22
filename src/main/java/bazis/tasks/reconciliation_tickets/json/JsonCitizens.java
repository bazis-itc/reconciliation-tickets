package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.tasks.reconciliation_tickets.Citizen;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public final class JsonCitizens extends IterableEnvelope<Citizen>
    implements Jsonable {

    public JsonCitizens(JsonElement json) {
        this(new JsonCitizens.Parsed(json));
    }

    public JsonCitizens(Iterable<Citizen> iterable) {
        super(iterable);
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonArray json = new JsonArray();
        for (final Citizen citizen : this)
            json.add(new JsonCitizen(citizen).asJson());
        return json;
    }

    private static final class Parsed extends IterableEnvelope<Citizen> {

        private Parsed(final JsonElement json) {
            super(
                new Scalar<Iterable<Citizen>>() {
                    @Override
                    public Iterable<Citizen> value() {
                        return new MappedIterable<>(
                            json.getAsJsonArray(),
                            new Func<JsonElement, Citizen>() {
                                @Override
                                public Citizen apply(JsonElement citizen) {
                                    return new JsonCitizen(citizen);
                                }
                            }
                        );
                    }
                }
            );
        }

    }

}
