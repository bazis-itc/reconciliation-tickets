package bazis.tasks.reconciliation_tickets;

import bazis.cactoos3.Scalar;

public final class SnilsEquality implements Scalar<Boolean> {

    private final String first, second;

    public SnilsEquality(String first, String second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Boolean value() {
        return !this.first.isEmpty() && !this.second.isEmpty()
            && SnilsEquality.normalized(this.first).equals(
                SnilsEquality.normalized(this.second)
            );
    }

    private static String normalized(String snils) {
        return snils
            .replace(" ", "")
            .replace("-", "");
    }

}
