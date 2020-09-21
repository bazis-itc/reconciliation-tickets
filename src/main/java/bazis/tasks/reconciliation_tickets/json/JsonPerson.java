package bazis.tasks.reconciliation_tickets.json;

import bazis.cactoos3.Opt;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.opt.EmptyOpt;
import bazis.cactoos3.opt.OptOf;
import bazis.tasks.reconciliation_tickets.Doc;
import bazis.tasks.reconciliation_tickets.Fio;
import bazis.tasks.reconciliation_tickets.Person;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

final class JsonPerson implements Person, Jsonable {

    private static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("dd.MM.yyyy");

    private static final String
        FIO = "fio", SNILS = "snils", BIRTHDATE = "birthdate",
        PASSPORT = "passport", CERTIFICATE = "certificate",
        CARRIER = "carrier";

    private final Person origin;

    JsonPerson(JsonElement json) {
        this(new JsonPerson.Parsed(json));
    }

    JsonPerson(Person origin) {
        this.origin = origin;
    }

    @Override
    public Fio fio() {
        return this.origin.fio();
    }

    @Override
    public String snils() throws BazisException {
        return this.origin.snils();
    }

    @Override
    public Opt<Date> birthdate() throws BazisException {
        return this.origin.birthdate();
    }

    @Override
    public Doc passport() {
        return this.origin.passport();
    }

    @Override
    public Doc certificate() {
        return this.origin.certificate();
    }

    @Override
    public String carrier() throws BazisException {
        return this.origin.carrier();
    }

    @Override
    public JsonElement asJson() throws BazisException {
        final JsonObject result = new JsonObject();
        final Opt<Date> birthdate = this.birthdate();
        result.add(JsonPerson.FIO, new JsonFio(this.fio()).asJson());
        result.addProperty(JsonPerson.SNILS, this.snils());
        result.addProperty(
            JsonPerson.BIRTHDATE,
            birthdate.has()
                ? JsonPerson.DATE_FORMAT.format(birthdate.get())
                : ""
        );
        result.add(JsonPerson.PASSPORT, new JsonDoc(this.passport()).asJson());
        result.add(
            JsonPerson.CERTIFICATE, new JsonDoc(this.certificate()).asJson()
        );
        result.addProperty(JsonPerson.CARRIER, this.carrier());
        return result;
    }

    private static final class Parsed implements Person {

        private final JsonElement json;

        private Parsed(JsonElement json) {
            this.json = json;
        }

        @Override
        public Fio fio() {
            return new JsonFio(this.json.getAsJsonObject().get(JsonPerson.FIO));
        }

        @Override
        public String snils() {
            return this.json.getAsJsonObject()
                .get(JsonPerson.SNILS).getAsString();
        }

        @Override
        public Opt<Date> birthdate() throws BazisException {
            try {
                final String birthdate = this.json.getAsJsonObject()
                    .get(JsonPerson.BIRTHDATE).getAsString();
                return birthdate.isEmpty()
                    ? new EmptyOpt<Date>()
                    : new OptOf<>(JsonPerson.DATE_FORMAT.parse(birthdate));
            } catch (final ParseException ex) {
                throw new BazisException(ex);
            }
        }

        @Override
        public Doc passport() {
            return new JsonDoc(
                this.json.getAsJsonObject().get(JsonPerson.PASSPORT)
            );
        }

        @Override
        public Doc certificate() {
            return new JsonDoc(
                this.json.getAsJsonObject().get(JsonPerson.CERTIFICATE)
            );
        }

        @Override
        public String carrier() {
            return this.json.getAsJsonObject()
                .get(JsonPerson.CARRIER).getAsString();
        }

    }

}
