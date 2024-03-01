package org.neo4j.field.azex;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
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
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    final static String bolturl = "bolt://localhost:7687";
    final static String user = "neo4j";
    final static String pwd = "password";
    private static Driver driver = null;
    private static String httpurl = "http://localhost:7474/db/neo4j/tx";
    
    @BeforeClass
    public static void init() throws Exception {
        try {
            driver = GraphDatabase.driver(bolturl, AuthTokens.basic(user, pwd));
            driver.verifyConnectivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testAzBolt() {
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
    
    @Test
    public void testAzHttp() {
        OkHttpClient client = new OkHttpClient();
        String json = "{\"statements\":[{\"statement\":\"RETURN true\"}]}";
        RequestBody body = RequestBody.create(JSON, json);
        String cred = Credentials.basic(user, pwd);
        Request request = new Request.Builder()
                .addHeader("Content-type", "application/json")
                .addHeader("Authorization", cred)
                .url(httpurl)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String rs = response.body().string();
            assertTrue(rs.contains("[true]"));
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }
}
