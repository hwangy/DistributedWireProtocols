package messenger;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
class ClientTest {
    @Test
    void testList(){
        System.out.println("This test method should be run");
        List mockedList = Mockito.mock(List.class);
        // or even simpler with Mockito 4.10.0+
        // List mockedList = mock();

        // using mock object - it does not throw any "unexpected interaction" exception
        mockedList.add("one");
        mockedList.clear();

        // selective, explicit, highly readable verification
        Mockito.verify(mockedList).add("one");
        Mockito.verify(mockedList).clear();
    }
}
