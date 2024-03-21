package org.neo4j.field.azex;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.neo4j.server.security.enterprise.auth.plugin.api.AuthProviderOperations;
import com.neo4j.server.security.enterprise.auth.plugin.api.AuthToken;
import com.neo4j.server.security.enterprise.auth.plugin.api.AuthenticationException;
import com.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthInfo;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthPlugin;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Request;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.server.web.JettyHttpConnection;

public class AzExample extends AuthPlugin.Adapter { 
    private AuthProviderOperations api = null;
    private LoadingCache<String, Object> rolesCache = null;
    
    @Override
    public void initialize(AuthProviderOperations apo) {
         api = apo;
         rolesCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).refreshAfterWrite(5, TimeUnit.MINUTES).build(k -> setRole(k));
    }
    
    @Override
    public void start() {
        loadRoles();
    }
    
    @Override
    public AuthInfo authenticateAndAuthorize(AuthToken x) throws AuthenticationException {
        String username = x.principal();
        api.log().info("Principal::" + username);
        JettyHttpConnection connection = JettyHttpConnection.getCurrentJettyHttpConnection();
        if (connection != null) { // REST
            api.log().info("REST request");
            Request rq = connection.getHttpChannel().getRequest();
            for (String hn : Collections.list(rq.getHeaderNames())) {
                api.log().info("header param:" + hn + "val: " + rq.getHeader(hn));
            }
            String hr = rq.getHeader("testRole");
            api.log().info("role from header:" + hr);
            if (hr != null) {
                Boolean cr = (Boolean)rolesCache.get(hr);
                System.out.println("check res: " + cr);
            }
        } else { // BOLT - TODO look at diffs between python driver and java driver
            api.log().info("Bolt request " + x.parameters());
        }
        return AuthInfo.of(username, Collections.singleton(PredefinedRoles.ADMIN));
    }
    
    private Map<String, Boolean> setRole(String k) {
        api.log().info("creating role:" + k);
        Map<String, Boolean> res = new HashMap<>();
        GraphDatabaseAPI db = ExposeConfigExtensionFactory.db;

        try ( Transaction tx = db.beginTransaction(KernelTransaction.Type.IMPLICIT, LoginContext.AUTH_DISABLED)) {
            Result r1 = tx.execute("CREATE OR REPLACE ROLE " + k);
            api.log().info("r1:" + r1.resultAsString());
            tx.commit();
            tx.close();
            res.put(k, Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    
    private void loadRoles() {
        Map<String, Boolean> res = new HashMap<>();
        GraphDatabaseAPI db = ExposeConfigExtensionFactory.db;
        try ( Transaction tx = db.beginTransaction(KernelTransaction.Type.IMPLICIT, LoginContext.AUTH_DISABLED)) {
            Result r = tx.execute("SHOW ROLES");
            while (r.hasNext()) {
                String role = (String)r.next().get("role");
                if (!role.matches("admin|reader|architect|editor|PUBLIC|publisher")) {
                    rolesCache.put(role, Boolean.FALSE);
                }
            }
            tx.close();
        }
    } 
}
