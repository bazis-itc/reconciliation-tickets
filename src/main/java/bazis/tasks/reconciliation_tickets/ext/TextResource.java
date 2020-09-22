package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Text;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public final class TextResource implements Text {

    private final Class<?> cls;

    private final String path;

    private final Charset charset;

    public TextResource(String absolute, Charset charset) {
        this(TextResource.class, absolute, charset);
    }

    public TextResource(Class<?> cls, String relative, Charset charset) {
        this.cls = cls;
        this.path = relative;
        this.charset = charset;
    }

    @Override
    public String asString() throws IOException {
        final Writer writer = new StringWriter();
        try (
            final Reader reader = new BufferedReader(
                new InputStreamReader(
                    this.cls.getResourceAsStream(this.path),
                    this.charset
                )
            )
        ) {
            int count;
            final char[] buffer = new char[1024];
            while ((count = reader.read(buffer)) != -1)
                writer.write(buffer, 0, count);
        }
        return writer.toString();
    }

}
