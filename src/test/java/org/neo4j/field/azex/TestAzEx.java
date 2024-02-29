package org.neo4j.field.azex;

import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

public class TestAzEx {

    final static String uri = "bolt://localhost:7687";
    final static String user = "neo4j";
    final static String pwd = "password";
    private static Driver driver = null;
    
    @BeforeClass
    public static void init() throws Exception {
        try {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, pwd));
            driver.verifyConnectivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testAz() {
        Map<String, Object> params = Map.of("jparam1", "value1", "jparam2", "value2");
        AuthToken at = AuthTokens.custom(user, pwd, null, "basic", params);
        try {
            Session sess = driver.session(Session.class, at);
            Result r = sess.run("RETURN true;");
            assertTrue(r.single().get(0).asBoolean());
            sess.close();
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }
    
    
    
    
}
