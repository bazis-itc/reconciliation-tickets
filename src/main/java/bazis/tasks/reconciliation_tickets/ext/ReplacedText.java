package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Text;
import bazis.cactoos3.text.TextOf;
import java.util.regex.Pattern;

public final class ReplacedText implements Text {
    
    private final Text text;

    public ReplacedText(String text, String target, String replacement) {
        this(new TextOf(text), new TextOf(target), new TextOf(replacement));
    }

    public ReplacedText(String text, Pattern pattern, String replacement) {
        this(new TextOf(text), pattern, new TextOf(replacement));
    }

    public ReplacedText(final Text text,
                        final Pattern pattern, final Text replacement) {
        this(
            new Text() {
                @Override
                public String asString() throws Exception {
                    return pattern
                        .matcher(text.asString())
                        .replaceAll(replacement.asString());
                }
            }
        );
    }

    public ReplacedText(final Text text,
                        final Text target, final Text replacement) {
        this(
            new Text() {
                @Override
                public String asString() throws Exception {
                    return text.asString().replace(
                        target.asString(),
                        replacement.asString()
                    );
                }
            }
        );
    }

    private ReplacedText(Text text) {
        this.text = text;
    }

    @Override
    public String asString() throws Exception {
        return this.text.asString();
    }

}
