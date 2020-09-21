package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.exception.BazisException;
import bazis.tasks.reconciliation_tickets.Check;
import bazis.tasks.reconciliation_tickets.Person;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonCheck implements Check, Jsonable {

    private static final String
        PERSON = "person", SUCCESS = "success", MESSAGE = "message";

    private final Check origin;

    public JsonCheck(JsonElement json) {
        this(new JsonCheck.Parsed(json));
    }

    public JsonCheck(Check origin) {
        this.origin = origin;
    }

    @Override
    public Person person() {
        return this.origin.person();
    }

    @Override
    public boolean success() {
        return this.origin.success();
    }

    @Override
    public String message() {
        return this.origin.message();
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonObject result = new JsonObject();
        result.add(JsonCheck.PERSON, new JsonPerson(this.person()).asJson());
        result.addProperty(JsonCheck.SUCCESS, this.success());
        result.addProperty(JsonCheck.MESSAGE, this.message());
        return result;
    }

    private static final class Parsed implements Check {

        private final JsonElement json;

        private Parsed(JsonElement json) {
            this.json = json;
        }

        @Override
        public Person person() {
            return new JsonPerson(
                this.json.getAsJsonObject().get(JsonCheck.PERSON)
            );
        }

        @Override
        public boolean success() {
            return this.json.getAsJsonObject()
                .get(JsonCheck.SUCCESS).getAsBoolean();
        }

        @Override
        public String message() {
            return this.json.getAsJsonObject()
                .get(JsonCheck.MESSAGE).getAsString();
        }

    }

}
