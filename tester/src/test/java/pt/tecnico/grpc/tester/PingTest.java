package pt.tecnico.grpc.tester;

import pt.tecnico.grpc.client.ClientLibrary;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class PingTest {

    @Test
  	public void PingDaoTest() {
        try {
            ClientLibrary frontend = new ClientLibrary("localhost", 8080, "localhost", 8081);
            assertEquals("Chat server is up!", frontend.pingDao());
        }
        catch(Exception e) {
            fail();
        }
  	}

    @Test
  	public void PingRegisterTest() {
        try{
            ClientLibrary frontend = new ClientLibrary("localhost", 8080, "localhost", 8081);
            assertEquals("Register server is up!", frontend.pingRegister());
        }
        catch(Exception e) {
            fail();
        }
  	}
}
