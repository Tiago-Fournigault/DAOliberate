package pt.tecnico.grpc.register;

import io.grpc.Context;
import io.grpc.Context.Key;
import javax.net.ssl.SSLSession;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptor;
import io.grpc.Metadata;
import io.grpc.ServerCallHandler;
import io.grpc.Grpc;
import io.grpc.Contexts;

/**
 * The AuthorizationInterceptor class intercepts incoming grpc calls.
 */
public class AuthorizationInterceptor implements ServerInterceptor {
    public final static Context.Key<SSLSession> SSL_SESSION_CONTEXT =  Context.key("SSLSession");
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
    Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        SSLSession sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
        if (sslSession == null) {
            return next.startCall(call, headers);
        }
        return Contexts.interceptCall(Context.current().withValue(SSL_SESSION_CONTEXT, sslSession), call, headers, next);
    }
}