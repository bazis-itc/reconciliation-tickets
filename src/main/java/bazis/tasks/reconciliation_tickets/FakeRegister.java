package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Func;
import bazis.cactoos3.exception.BazisException;
import bazis.cactoos3.iterable.MappedIterable;

public final class FakeRegister implements Register {

    @Override
    public Iterable<Check> check(Iterable<Person> persons) throws BazisException {
        return new MappedIterable<>(
            persons,
            new Func<Person, Check>() {
                @Override
                public Check apply(final Person person) {
                    return new Check() {
                        @Override
                        public Person person() {
                            return person;
                        }
                        @Override
                        public boolean success() {
                            return false;
                        }
                        @Override
                        public String message() {
                            return "Не найдено ФИО";
                        }
                    };
                }
            }
        );
    }

}
