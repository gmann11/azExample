package org.neo4j.field.azex;

import com.neo4j.server.security.enterprise.auth.plugin.api.AuthProviderOperations;
import com.neo4j.server.security.enterprise.auth.plugin.api.AuthToken;
import com.neo4j.server.security.enterprise.auth.plugin.api.AuthenticationException;
import com.neo4j.server.security.enterprise.auth.plugin.api.PredefinedRoles;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthInfo;
import com.neo4j.server.security.enterprise.auth.plugin.spi.AuthPlugin;
import java.util.Collections;
import org.eclipse.jetty.server.Request;
import org.neo4j.server.web.JettyHttpConnection;

public class AzExample extends AuthPlugin.Adapter {
    
    private AuthProviderOperations api = null;
    
    @Override
    public void initialize(AuthProviderOperations apo) {
         api = apo;
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
        } else { // BOLT - TODO look at diffs between python driver and java driver
            api.log().info("Bolt request " + x.parameters());
        }
        return AuthInfo.of(username, Collections.singleton(PredefinedRoles.ADMIN));
    }
}
