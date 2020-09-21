package sx.bazis.uninfoobmen.web.bean.operation;

import bazis.tasks.reconciliation_tickets.EncryptedText;
import bazis.tasks.reconciliation_tickets.FakeRegister;
import bazis.tasks.reconciliation_tickets.json.JsonChecks;
import bazis.tasks.reconciliation_tickets.json.JsonPersons;
import bazis.tasks.reconciliation_tickets.json.JsonText;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import sx.bazis.uninfoobmen.sys.store.DataObject;
import sx.bazis.uninfoobmen.sys.store.ReturnDataObject;

public final class ReconciliationTicketsOperation extends UIOperationBase {

    static {
        UIOperationFactory.registory(
            "reconciliation_tickets",
            "Задача сверки граждан (юр.помощь, проездные)",
            ReconciliationTicketsOperation.class
        );
    }

    @Override
    public ReturnDataObject exec(HashMap<String, String> hashMap,
        DataObject dataObject, HttpSession httpSession) {
        ReturnDataObject result;
        try {
            result = super.getReturnMessage(
                "COMPLETE", "", null,
                new EncryptedText(
                    new JsonText(
                        new JsonChecks(
                            new FakeRegister().check(
                                new JsonPersons(
                                    new JsonText(
                                        new EncryptedText(
                                            dataObject.getInputStream()
                                        )
                                    ).asJson()
                                )
                            )
                        )
                    )
                ).asBytes()
            );
            return result;
        } catch (final Throwable ex) {
            result = super.getReturnMessage(
                "ERROR",
                ex.getMessage() == null ? ex.toString() : ex.getMessage(),
                ex
            );
        }
        return result;
    }

}
