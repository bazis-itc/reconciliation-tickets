package bazis.tasks;

import java.io.File;
import org.junit.Test;
import org.mockito.Mockito;
import sx.datastore.impl.fs.SXFileObj;

public final class ReconciliationTicketsTaskITCase {

    @Test
    public void test() throws Exception {
        final SXFileObj input = Mockito.mock(SXFileObj.class);
        Mockito.when(input.getFile()).thenReturn(
            new File("D:\\Exchange\\SC190601.dbf")
        );
        final SXFileObj output = Mockito.mock(SXFileObj.class);
        Mockito.when(output.getFile()).thenReturn(
            new File("D:\\Exchange")
        );
        final ReconciliationTicketsTask task = new ReconciliationTicketsTask();
        task.getLinkedData().put("importFile", input);
        task.getLinkedData().put("outputFolder", output);
        task.execute();
    }

}