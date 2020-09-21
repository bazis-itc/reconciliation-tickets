package bazis.tasks.reconciliation_tickets;

public interface Check {

    Person person();

    boolean success();

    String message();

}
