package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Func;
import bazis.cactoos3.iterable.MappedIterable;

public final class FakeRegister implements Register {

    @Override
    public Iterable<Check> check(Iterable<Citizen> citizens) {
        return new MappedIterable<>(
            citizens,
            new Func<Citizen, Check>() {
                @Override
                public Check apply(final Citizen citizen) {
                    return new Check() {
                        @Override
                        public Citizen citizen() {
                            return citizen;
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
