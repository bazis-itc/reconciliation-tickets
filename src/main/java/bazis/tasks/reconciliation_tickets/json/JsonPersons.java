package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.tasks.reconciliation_tickets.Person;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public final class JsonPersons extends IterableEnvelope<Person>
    implements Jsonable {

    public JsonPersons(JsonElement json) {
        this(new JsonPersons.Parsed(json));
    }

    public JsonPersons(Iterable<Person> iterable) {
        super(iterable);
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonArray json = new JsonArray();
        for (final Person person : this)
            json.add(new JsonPerson(person).asJson());
        return json;
    }

    private static final class Parsed extends IterableEnvelope<Person> {

        private Parsed(final JsonElement json) {
            super(
                new Scalar<Iterable<Person>>() {
                    @Override
                    public Iterable<Person> value() {
                        return new MappedIterable<>(
                            json.getAsJsonArray(),
                            new Func<JsonElement, Person>() {
                                @Override
                                public Person apply(JsonElement person) {
                                    return new JsonPerson(person);
                                }
                            }
                        );
                    }
                }
            );
        }

    }

}
