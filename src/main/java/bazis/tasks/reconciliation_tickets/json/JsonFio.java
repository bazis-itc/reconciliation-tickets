package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.exception.BazisException;
import bazis.tasks.reconciliation_tickets.Fio;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class JsonFio implements Fio, Jsonable {

    private static final String
        SURNAME = "surname",  NAME = "name", PATRONYMIC = "patronymic";

    private final Fio origin;

    JsonFio(JsonElement json) {
        this(new JsonFio.Parsed(json));
    }

    JsonFio(Fio origin) {
        this.origin = origin;
    }

    @Override
    public String surname() throws BazisException {
        return this.origin.surname();
    }

    @Override
    public String name() throws BazisException {
        return this.origin.name();
    }

    @Override
    public String patronymic() throws BazisException {
        return this.origin.patronymic();
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonObject result = new JsonObject();
        result.addProperty(JsonFio.SURNAME, this.surname());
        result.addProperty(JsonFio.NAME, this.name());
        result.addProperty(JsonFio.PATRONYMIC, this.patronymic());
        return result;
    }

    private static final class Parsed implements Fio {

        private final JsonElement json;

        private Parsed(JsonElement json) {
            this.json = json;
        }

        @Override
        public String surname() {
            return this.string(JsonFio.SURNAME);
        }

        @Override
        public String name() {
            return this.string(JsonFio.NAME);
        }

        @Override
        public String patronymic() {
            return this.string(JsonFio.PATRONYMIC);
        }

        private String string(String property) {
            return this.json.getAsJsonObject().get(property).getAsString();
        }

    }

}
