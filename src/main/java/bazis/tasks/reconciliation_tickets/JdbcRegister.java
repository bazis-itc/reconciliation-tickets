package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Func;
import bazis.cactoos3.Text;
import bazis.cactoos3.collection.ListOf;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.EmptyIterable;
import bazis.cactoos3.iterable.FilteredIterable;
import bazis.cactoos3.iterable.JoinedIterable;
import bazis.cactoos3.iterable.MappedIterable;
import bazis.cactoos3.map.Entries;
import bazis.cactoos3.map.MapOf;
import bazis.cactoos3.scalar.And;
import bazis.cactoos3.scalar.Equality;
import bazis.cactoos3.scalar.IsEmpty;
import bazis.cactoos3.scalar.Not;
import bazis.cactoos3.scalar.Or;
import bazis.cactoos3.scalar.ScalarOf;
import bazis.cactoos3.text.Lines;
import bazis.cactoos3.text.TextOf;
import bazis.cactoos3.text.UncheckedText;
import bazis.tasks.reconciliation_tickets.ext.AsyncMappedIterable;
import bazis.tasks.reconciliation_tickets.ext.CachedText;
import bazis.tasks.reconciliation_tickets.ext.Contains;
import bazis.tasks.reconciliation_tickets.ext.IntRange;
import bazis.tasks.reconciliation_tickets.ext.ReplacedText;
import bazis.tasks.reconciliation_tickets.ext.SortedIterable;
import bazis.tasks.reconciliation_tickets.ext.SyncText;
import bazis.tasks.reconciliation_tickets.ext.TextResource;
import bazis.tasks.reconciliation_tickets.ext.TextWithParams;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Iterator;
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

    private final Iterable<Record> categories;

    public JdbcRegister(DSLContext central, Map<Integer, Database> boroughs) {
        this.central = central;
        this.boroughs = boroughs;
        this.categories = new Categories(central);
    }

    @Override
    public Iterable<Check> check(Iterable<Citizen> input) {
        final List<Citizen> citizens = new ListOf<>(input)
//            .subList(0, 100)
        ;
        final Result<Record> persons = this.fetchPersons(citizens);
        final Map<Integer, String> docs = this.fetchDocs(persons, citizens);
        final Map<Integer, Result<Record>> identity =
            persons.intoGroups(DSL.field("listIndex", Integer.class));
        return new MappedIterable<>(
            new IntRange(0, citizens.size() - 1),
            new Func<Number, Check>() {
                @Override
                public Check apply(Number index) throws Exception {
                    return JdbcRegister.this.identify(
                        citizens.get(index.intValue()),
                        identity.containsKey(index.intValue())
                            ? identity.get(index.intValue())
                            : new EmptyIterable<Record>(),
                        docs
                    );
                }
            }
        );
    }

    private Result<Record> fetchPersons(List<Citizen> citizens) {
        final AtomicInteger counter = new AtomicInteger(0);
        final Text query = new ReplacedText(
            new TextResource(
                "/bazis/tasks/reconciliation_tickets/identification.sql",
                Charset.forName("CP1251")
            ),
            new TextOf("--insert"),
            new Lines(
                new MappedIterable<>(
                    citizens,
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
            this.central.fetch(new UncheckedText(query).asString());
//        System.out.println(result);
        return result;
    }

    private Map<Integer, String> fetchDocs(
        Result<Record> persons, final List<Citizen> citizens) {
        final Text query = new SyncText(
            new CachedText(
                new TextResource(
                    "/bazis/tasks/reconciliation_tickets/docs.sql",
                    Charset.forName("CP1251")
                )
            )
        );
        final Map<Integer, Result<Record>> groups =
            persons.intoGroups(DSL.field("borough", Integer.class));
        final Func<Record, Text> inserts = new Func<Record, Text>() {
            @Override
            public Text apply(Record person) throws BazisException {
                final Doc doc = citizens.get(
                    new NoNulls(person).number("listIndex").get().intValue()
                ).certificate();
                return new TextWithParams(
                    "INSERT @person (centralId, localId, docSeries, docNumber) " +
                    "VALUES ({0}, {1}, '{2}', '{3}')",
                    Integer.toString(new Person(person).centralId()),
                    Integer.toString(
                        new NoNulls(person).number("localId", 0).intValue()
                    ),
                    doc.series(), doc.number()
                );
            }
        };
        final Iterable<Record> list = new JoinedIterable<>(
            new AsyncMappedIterable<>(
                groups.keySet(),
                new Func<Integer, Iterable<Record>>() {
                    @Override
                    public Iterable<Record> apply(Integer boroughId)
                        throws BazisException {
                        if (!JdbcRegister.this.boroughs.containsKey(boroughId))
                            throw new BazisException("Borough not found");
                        final Result<Record> result =
                            JdbcRegister.this.boroughs.get(boroughId).select(
                                new ReplacedText(
                                    query,
                                    new TextOf("--insert"),
                                    new Lines(
                                        new MappedIterable<>(
                                            groups.get(boroughId), inserts
                                        )
                                    )
                                )
                            );
//                        System.out.println(result);
                        return result;
                    }
                },
                Executors.newFixedThreadPool(5)
            )
        );
        return new MapOf<>(
            new Entries<>(
                list,
                new Func<Record, Integer>() {
                    @Override
                    public Integer apply(Record doc) {
                        return new NoNulls(doc).number("person")
                            .get().intValue();
                    }
                },
                new Func<Record, String>() {
                    @Override
                    public String apply(Record doc) {
                        return new NoNulls(doc).string("docType");
                    }
                }
            )
        );
    }

    private Check identify(final Citizen citizen,
        Iterable<Record> persons, final Map<Integer, String> docs) {
        final Iterable<Record> filtered = new ListOf<>(
            new FilteredIterable<>(
                persons,
                new Func<Record, Boolean>() {
                    @Override
                    public Boolean apply(Record person)
                        throws Exception {
                        return new Or(
                            new Contains(
                                docs.keySet(), new Person(person).centralId()
                            ),
                            new And(
                                new Equality(
                                    citizen.passport().series(),
                                    new NoNulls(person)
                                        .string("passport.series")
                                ),
                                new Equality(
                                    citizen.passport().number(),
                                    new NoNulls(person)
                                        .string("passport.number")
                                )
                            ),
                            new SnilsEquality(
                                citizen.snils(),
                                new NoNulls(person).string("snils")
                            )
                        ).value();
                    }
                }
            )
        );
        final Check result;
        if (new IsEmpty(filtered).value())
            result = new Check.Negative(citizen, "Личное дело не найдено");
        else {
            final Func<Record, Iterable<Record>> toCategories =
                new Func<Record, Iterable<Record>>() {
                    @Override
                    public Iterable<Record> apply(final Record person) {
                        final int centralId =
                            new Person(person).centralId();
                        return new FilteredIterable<>(
                            JdbcRegister.this.categories,
                            new Func<Record, Boolean>() {
                                @Override
                                public Boolean apply(Record category)
                                    throws Exception {
                                    return new And(
                                        new ScalarOf<>(
                                            new Person(person).hasCategory(
                                                new NoNulls(category)
                                                    .number("id", 0)
                                            )
                                        ),
                                        new Or(
                                            new Not(
                                                new Contains(
                                                    docs.keySet(), centralId
                                                )
                                            ),
                                            new Equality(
                                                new NoNulls(category)
                                                    .string("doc"),
                                                docs.get(centralId)
                                            )
                                        )
                                    ).value();
                                }
                            }
                        );
                    }
                };
            final Iterator<Record> iterator = new SortedIterable<>(
                new JoinedIterable<>(
                    new MappedIterable<>(filtered, toCategories)
                ),
                new Func<Record, Integer>() {
                    @Override
                    public Integer apply(Record category) {
                        return new NoNulls(category)
                            .number("priority", Integer.MAX_VALUE)
                            .intValue();
                    }
                }
            ).iterator();
            result = iterator.hasNext()
                ? new Check.Positive(
                    citizen,
                    new NoNulls(iterator.next()).number("code").get()
                )
                : new Check.Negative(
                    citizen, "Не подтверждена льготная категория"
                );
        }
        return result;
    }

}
