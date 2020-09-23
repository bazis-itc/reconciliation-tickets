package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Scalar;
import bazis.cactoos3.Text;
import bazis.cactoos3.iterable.IterableEnvelope;
import bazis.cactoos3.scalar.CachedScalar;
import bazis.cactoos3.text.Lines;
import org.jooq.DSLContext;
import org.jooq.Record;

public final class Categories extends IterableEnvelope<Record> {

    public Categories(final DSLContext context) {
        this(
            new Scalar<Iterable<Record>>() {
                @Override
                public Iterable<Record> value() throws Exception {
                    final Text query = new Lines(
                        "SELECT ",
                        "  [id] = category.A_ID,",
                        "  [name] = category.A_NAME,",
                        "  [code] = linkToCode.A_CODE,",
                        "  [priority] = linkToCode.A_PRIORITY,",
                        "  [doc] = docType.GUID",
                        "FROM PPR_CAT category",
                        "  JOIN LINK_CATEGORY_TO_CODE linkToCode ",
                        "    ON linkToCode.A_CATEGORY = category.A_ID",
                        "    AND linkToCode.A_CODE IS NOT NULL",
                        "  LEFT JOIN LINK_CATEGORY_TO_DOC linkToDoc ",
                        "    ON linkToDoc.A_CATEGORY = category.A_ID",
                        "  LEFT JOIN PPR_DOC docType ",
                        "    ON docType.A_ID = linkToDoc.A_DOC_TYPE",
                        "WHERE ISNULL(category.A_STATUS, 10) = 10"
                    );
                    return context.fetch(query.asString());
                }
            }
        );
    }

    private Categories(Scalar<Iterable<Record>> scalar) {
        super(new CachedScalar<>(scalar));
    }

}
