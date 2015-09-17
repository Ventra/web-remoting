package org.kluge.remoting.server.http;

import org.eclipse.jetty.server.Handler;

/**
 *
 * @author Alexey Andreev
 */
public interface HttpHandlerConsumer {
    void addHandler(Handler handler);
}
