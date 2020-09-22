package bazis.tasks.reconciliation_tickets;

public interface Check {

    Citizen citizen();

    boolean success();

    String message();

    final class Positive implements Check {

        private final Citizen citizen;

        private final String message;

        public Positive(Citizen citizen, String message) {
            this.citizen = citizen;
            this.message = message;
        }

        @Override
        public Citizen citizen() {
            return this.citizen;
        }

        @Override
        public boolean success() {
            return true;
        }

        @Override
        public String message() {
            return this.message;
        }

    }

    final class Negative implements Check {

        private final Citizen citizen;

        private final String message;

        public Negative(Citizen citizen, String message) {
            this.citizen = citizen;
            this.message = message;
        }

        @Override
        public Citizen citizen() {
            return this.citizen;
        }

        @Override
        public boolean success() {
            return false;
        }

        @Override
        public String message() {
            return this.message;
        }

    }

}
