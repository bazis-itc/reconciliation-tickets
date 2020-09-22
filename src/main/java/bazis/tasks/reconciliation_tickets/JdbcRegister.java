package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Func;
import bazis.cactoos3.Text;
import bazis.cactoos3.collection.ListOf;
import bazis.cactoos3.collection.SetOf;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.EmptyIterable;
import bazis.cactoos3.iterable.JoinedIterable;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.cactoos3.scalar.And;
import bazis.cactoos3.scalar.Any;
import bazis.cactoos3.scalar.Equality;
import bazis.cactoos3.scalar.Or;
import bazis.cactoos3.text.CheckedText;
import bazis.cactoos3.text.Lines;
import bazis.cactoos3.text.TextOf;
import bazis.tasks.reconciliation_tickets.ext.AsyncMappedIterable;
import bazis.tasks.reconciliation_tickets.ext.IntRange;
import bazis.tasks.reconciliation_tickets.ext.ReplacedText;
import bazis.tasks.reconciliation_tickets.ext.TextResource;
import bazis.tasks.reconciliation_tickets.ext.TextWithParams;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

public final class JdbcRegister implements Register {

    private final DSLContext central;

    private final Map<Integer, Database> boroughs;

    public JdbcRegister(DSLContext central, Map<Integer, Database> boroughs) {
        this.central = central;
        this.boroughs = boroughs;
    }

    @Override
    public Iterable<Check> check(Iterable<Citizen> citizens)
        throws BazisException {
        final List<Citizen> list = new ListOf<>(citizens)
            .subList(0, 100)
        ;
        final AtomicInteger counter = new AtomicInteger(0);
        final Text query = new ReplacedText(
            new TextResource(
                "/bazis/tasks/reconciliation_tickets/identification.sql",
                Charset.forName("CP1251")
            ),
            new TextOf("--insert"),
            new Lines(
                new MappedIterable<>(
                    list,
                    new Func<Citizen, Text>() {
                        @Override
                        public Text apply(Citizen citizen) throws BazisException {
                            return new TextWithParams(
                                "INSERT @citizen (listIndex, surname, name, patronymic, birthdate) " +
                                "VALUES ({0}, '{1}', '{2}', '{3}', '{4}')",
                                Integer.toString(counter.getAndIncrement()),
                                citizen.fio().surname(),
                                citizen.fio().name(),
                                citizen.fio().patronymic(),
                                citizen.birthdate().has()
                                    ? new SimpleDateFormat("yyyy-MM-dd")
                                        .format(citizen.birthdate().get())
                                    : ""
                            );
                        }
                    }
                )
            )
        );
        final Result<Record> result =
            this.central.fetch(new CheckedText(query).asString());
        System.out.println(result);
        this.fetchDocs(result);
        final Map<Integer, Result<Record>> identity =
            result.intoGroups(DSL.field("listIndex", Integer.class));
        return new MappedIterable<>(
            new IntRange(0, list.size() - 1),
            new Func<Number, Check>() {
                @Override
                public Check apply(Number index) throws Exception {
                    return JdbcRegister.identify(
                        list.get(index.intValue()),
                        identity.containsKey(index.intValue())
                            ? identity.get(index.intValue())
                            : new EmptyIterable<Record>()
                    );
                }
            }
        );
    }

    private Iterable<Record> fetchDocs(Result<Record> persons) {
        final Iterable<Record> docs = new JoinedIterable<>(
            new AsyncMappedIterable<>(
                new SetOf<>(persons.getValues("borough", Integer.class)),
                new Func<Integer, Iterable<Record>>() {
                    @Override
                    public Iterable<Record> apply(Integer boroughId)
                        throws BazisException {
                        if (!JdbcRegister.this.boroughs.containsKey(boroughId))
                            throw new BazisException("Borough not found");
                        return JdbcRegister.this.boroughs.get(boroughId).select(
                            new TextOf("select tmp = 1")
                        );
                    }
                },
                Executors.newFixedThreadPool(5)
            )
        );
        System.out.println(new ListOf<>(docs).size());
        return docs;
    }

    private static Check identify(final Citizen citizen,
        Iterable<Record> persons) throws Exception {
        return new Any<>(
            persons,
            new Func<Record, Boolean>() {
                @Override
                public Boolean apply(Record person)
                    throws Exception {
                    return new Or(
                        new And(
                            new Equality(
                                citizen.passport().series(),
                                new NoNulls(person).string("passport.series")
                            ),
                            new Equality(
                                citizen.passport().number(),
                                new NoNulls(person).string("passport.number")
                            )
                        ),
                        new SnilsEquality(
                            citizen.snils(),
                            new NoNulls(person).string("snils")
                        )
                    ).value();
                }
            }
        ).value()
            ? new Check.Positive(citizen, "approved")
            : new Check.Negative(citizen, "not found");
    }

}
