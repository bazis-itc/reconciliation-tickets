package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.exception.BazisException;
import com.google.gson.JsonElement;

public interface Jsonable {

    JsonElement asJson() throws BazisException;

}
