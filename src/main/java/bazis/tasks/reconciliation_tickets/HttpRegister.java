package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.map.MapOf;
import bazis.tasks.reconciliation_tickets.json.JsonChecks;
import bazis.tasks.reconciliation_tickets.json.JsonCitizens;
import bazis.tasks.reconciliation_tickets.json.JsonText;
import java.util.HashMap;
import java.util.Map;
import sx.bazis.uninfoobmen.web.ConnectServletException;
import sx.bazis.uninfoobmen.web.HttpMultipurposeClient;

public final class HttpRegister implements Register {

    private final String url;

    public HttpRegister(String url) {
        this.url = url;
    }

    @Override
    public Iterable<Check> check(Iterable<Citizen> citizens) throws BazisException {
        final HttpMultipurposeClient connection = new HttpMultipurposeClient();
        try {
            connection.setInputObject(
                new EncryptedText(new JsonText(new JsonCitizens(citizens)))
                    .asBytes()
            );
            final Map<String, String> response = connection.invoc(
                this.url,
                new HashMap<>(new MapOf<>("cmd", "reconciliation_tickets"))
            );
            final String result = response.get("RESULT");
            if ("ERROR".equals(result))
                throw new BazisException(response.get("ERROR_TITLE"));
            if (!"COMPLETE".equals(result)) throw new BazisException(
                String.format("Unknown response type '%s'", result)
            );
            return new JsonChecks(
                new JsonText(
                    new EncryptedText(
                        connection.getOutputObject().getInputStream()
                    ).asString()
                ).asJson()
            );
        } catch (final ConnectServletException ex) {
            throw new BazisException(
                String.format("Сервер недоступен: %s", this.url), ex
            );
        } finally {
            connection.destroy();
        }
    }

}
