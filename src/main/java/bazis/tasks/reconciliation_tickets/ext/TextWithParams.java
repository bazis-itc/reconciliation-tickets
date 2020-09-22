package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.Text;
import bazis.cactoos3.iterable.IterableOf;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.cactoos3.map.MapEnvelope;
import bazis.cactoos3.scalar.CachedScalar;
import bazis.cactoos3.text.TextOf;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextWithParams implements Text {

    private final Text text;

    private final Map<String, String> params;

    public TextWithParams(String text, String... params) {
        this(
            new TextOf(text),
            new MappedIterable<>(
                new IterableOf<>(params),
                new Func<String, Text>() {
                    @Override
                    public Text apply(String str) {
                        return new TextOf(str);
                    }
                }
            )
        );
    }

    public TextWithParams(Text text, Text... params) {
        this(text, new IterableOf<>(params));
    }

    public TextWithParams(Text text, Iterable<Text> params) {
        this(text, new TextWithParams.Params(params));
    }

    public TextWithParams(Text text, Map.Entry<Text, Text>... params) {
        this(text, new TextWithParams.Params(params));
    }

    private TextWithParams(Text text, Map<String, String> params) {
        this.text = text;
        this.params = params;
    }

    private static final class Params extends MapEnvelope<String, String> {

        private Params(final Iterable<Text> params) {
            this(
                new Scalar<Map<String, String>>() {
                    @Override
                    public Map<String, String> value() throws Exception {
                        final Map<String, String> result = new HashMap<>(0);
                        int index = 0;
                        for (final Text param : params)
                            result.put(
                                Integer.toString(index++), param.asString()
                            );
                        return Collections.unmodifiableMap(result);
                    }
                }
            );
        }

        private Params(final Map.Entry<Text, Text>... params) {
            this(
                new Scalar<Map<String, String>>() {
                    @Override
                    public Map<String, String> value() throws Exception {
                        final Map<String, String>
                            result = new HashMap<>(params.length);
                        for (final Map.Entry<Text, Text> param : params)
                            result.put(
                                param.getKey().asString(),
                                param.getValue().asString()
                            );
                        return Collections.unmodifiableMap(result);
                    }
                }
            );
        }

        private Params(Scalar<Map<String, String>> scalar) {
            super(new CachedScalar<>(scalar));
        }

    }

    @Override
    public String asString() throws Exception {
        final StringBuffer result = new StringBuffer(0);
        final Matcher matcher =
            Pattern.compile("[{](\\S+)}").matcher(this.text.asString());
        while (matcher.find())
            if (this.params.containsKey(matcher.group(1)))
                matcher.appendReplacement(
                    result, this.params.get(matcher.group(1))
                );
        matcher.appendTail(result);
        return result.toString();
    }

}
