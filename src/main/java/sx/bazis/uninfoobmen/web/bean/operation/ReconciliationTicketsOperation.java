package sx.bazis.uninfoobmen.web.bean.operation;

import bazis.tasks.reconciliation_tickets.BoroughMap;
import bazis.tasks.reconciliation_tickets.EncryptedText;
import bazis.tasks.reconciliation_tickets.JdbcRegister;
import bazis.tasks.reconciliation_tickets.json.JsonChecks;
import bazis.tasks.reconciliation_tickets.json.JsonCitizens;
import bazis.tasks.reconciliation_tickets.json.JsonText;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sx.bazis.uninfoobmen.sys.store.DataObject;
import sx.bazis.uninfoobmen.sys.store.ReturnDataObject;
import sx.datastore.SXDsFactory;

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
            final DSLContext context = DSL.using(
                SXDsFactory.getDs().getDb().getDataSource(),
                SQLDialect.DEFAULT
            );
            result = super.getReturnMessage(
                "COMPLETE", "", null,
                new EncryptedText(
                    new JsonText(
                        new JsonChecks(
                            new JdbcRegister(context, new BoroughMap(context))
                                .check(
                                    new JsonCitizens(
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
