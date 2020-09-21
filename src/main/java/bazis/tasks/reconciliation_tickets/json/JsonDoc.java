package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.exception.BazisException;
import bazis.tasks.reconciliation_tickets.Doc;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class JsonDoc implements Doc, Jsonable {

    private static final String SERIES = "series", NUMBER = "number";

    private final Doc origin;

    JsonDoc(JsonElement json) {
        this(new JsonDoc.Parsed(json));
    }

    JsonDoc(Doc origin) {
        this.origin = origin;
    }

    @Override
    public String series() throws BazisException {
        return this.origin.series();
    }

    @Override
    public String number() throws BazisException {
        return this.origin.number();
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonObject result = new JsonObject();
        result.addProperty(JsonDoc.SERIES, this.series());
        result.addProperty(JsonDoc.NUMBER, this.number());
        return result;
    }

    private static final class Parsed implements Doc {

        private final JsonElement json;

        private Parsed(JsonElement json) {
            this.json = json;
        }

        @Override
        public String series() {
            return this.json.getAsJsonObject()
                .get(JsonDoc.SERIES).getAsString();
        }

        @Override
        public String number() {
            return this.json.getAsJsonObject()
                .get(JsonDoc.NUMBER).getAsString();
        }

    }

}
