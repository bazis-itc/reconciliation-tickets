package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Text;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.text.CheckedText;
import bazis.platform.interaction.database.JsonResultSet;
import bazis.platform.interaction.metric.MetricOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import sx.bazis.uninfoobmen.sys.function.Function;
import sx.bazis.uninfoobmen.sys.function.ServerInfo;
import sx.bazis.uninfoobmen.sys.function.stream.InputStreamNotClose;
import sx.bazis.uninfoobmen.sys.function.stream.VariableOutputStream;
import sx.bazis.uninfoobmen.web.ConnectServletException;
import sx.bazis.uninfoobmen.web.HttpMultipurposeClient;

public final class FakeDatabase implements Database {

    private final String url;

    public FakeDatabase(String url) {
        this.url = url;
    }

    @Override
    public Result<Record> select(Text query) throws BazisException {
        HashMap<String, String> PARAM = new HashMap<>(0);
        PARAM.put("cmd", "QUERY");
        PARAM.put("url", "");
        PARAM.put("logInfo", ServerInfo.get("QUERY", "ExecSelectRayon"));

        HttpMultipurposeClient cds = new HttpMultipurposeClient();
        String id = UUID.randomUUID().toString();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try (VariableOutputStream vos = new VariableOutputStream(id)) {
                try (
                    MetricOutputStream mos = new MetricOutputStream(
                        vos, id, "Упаковка скрипта (сервер) --с возвратом результата ["
                        + PARAM.get("Config") + "][" + PARAM.get("url") + "]"
                    );
                    ZipOutputStream zos = new ZipOutputStream(
                        new DigestOutputStream(
                            new BufferedOutputStream(mos, 2048), md
                        ),
                        Charset.forName("CP1251")
                    )
                ) {
                    zos.setLevel(9);
                    ZipEntry ze = new ZipEntry("data.que");
                    zos.putNextEntry(ze);
                    zos.write(
                        new CheckedText(query).asString()
                            .getBytes(Charset.forName("CP1251"))
                    );
                    zos.closeEntry();
                }
                vos.close();
                cds.setInputObject(vos.getDataObject(Function.byteArrayToHexString(md.digest())));
            }

            Map<String, String> res = cds.invoc(this.url, PARAM);

            if (res.get("RESULT").equals("COMPLETE")) {
                try (
                    ZipInputStream zis = new ZipInputStream(
                        new BufferedInputStream(cds.getOutputObject().getInputStream(), 2048),
                        Charset.forName("CP1251")
                    )
                ) {
                    ZipEntry ze = zis.getNextEntry();
                    if (ze != null) {
                        try (JsonResultSet resultSet = new JsonResultSet(new InputStreamNotClose(zis))) {
                            final ResultSetMetaData meta = resultSet.getMetaData();
                            final Field<?>[] fields = new Field<?>[meta.getColumnCount()];
                            for (int index = 1; index <= meta.getColumnCount(); index++)
                                fields[index - 1] = DSL.field(meta.getColumnName(index));

                            final DSLContext context = DSL.using(SQLDialect.DEFAULT);
                            final Result<Record> result = context.newResult(fields);
                            while (resultSet.next()) {
                                final Record record = context.newRecord(fields);
                                for (int index = 1; index <= meta.getColumnCount(); index++)
                                    record.setValue(
                                        (Field<Object>) fields[index - 1], resultSet.getObject(index)
                                    );
                                result.add(record);
                            }
                            zis.closeEntry();
                            return result;
                        }
                    }
                } finally {
                    if (cds.getOutputObject() != null) {
                        cds.getOutputObject().destroy();
                    }
                }
            }
            String mes = "Сервер не опрошен (" + res.get("ERROR_TITLE")
                + " , время обработки " + res.get("TIME") + ")\n";
            if (res.containsKey("ERROR_STACK_TRACE")) {
                mes = mes + res.get("ERROR_STACK_TRACE");
            }
            throw new BazisException(mes);
        } catch (final ConnectServletException ex) {
            throw new BazisException("Сервер не доступен", ex);
        } catch (final IOException | SQLException | NoSuchAlgorithmException ex) {
            throw new BazisException(ex);
        } finally {
            cds.destroy();
        }
    }

}
