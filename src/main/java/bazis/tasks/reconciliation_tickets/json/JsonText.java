package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.text.CheckedText;
import bazis.cactoos3.text.TextOf;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@SuppressWarnings("LambdaUnfriendlyMethodOverload")
public final class JsonText implements Text, Jsonable {

    private final Text origin;

    public JsonText(final Jsonable json) {
        this(
            new Text() {
                @Override
                public String asString() throws BazisException {
                    return new GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(json.asJson());
                }
            }
        );
    }

    public JsonText(final String str) {
        this(new TextOf(str));
    }

    public JsonText(Text origin) {
        this.origin = origin;
    }

    @Override
    public String asString() throws BazisException {
        return new CheckedText(this.origin).asString();
    }

    @Override
    public JsonElement asJson() throws BazisException {
        return new JsonParser().parse(this.asString());
    }

}
