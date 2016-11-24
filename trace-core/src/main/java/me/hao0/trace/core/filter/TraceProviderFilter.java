package me.hao0.trace.core.filter;

import com.alibaba.dubbo.rpc.*;
import me.hao0.trace.core.*;
import me.hao0.trace.core.config.TraceConf;
import me.hao0.trace.core.config.TraceConfLoader;
import me.hao0.trace.core.util.Times;
import java.util.Map;

/**
 * 该版本封装
 * Trace Service Filter
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceProviderFilter implements Filter {

    private TraceConf conf = TraceConfLoader.load("trace.yml");

    private TraceAgent agent = new TraceAgent(conf.getServer());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (!conf.getEnable()){
            // not enable tracing
            return invoker.invoke(invocation);
        }

        Map<String, String> attaches = invocation.getAttachments();
        if (!attaches.containsKey(TraceConstants.TRACE_ID)){
            // don't need tracing
            return invoker.invoke(invocation);
        }

        // prepare trace context
        startTrace(attaches);

        Result result = invoker.invoke(invocation);

        endTrace();

        return result;
    }

    private void startTrace(Map<String, String> attaches) {

        long traceId = Long.parseLong(attaches.get(TraceConstants.TRACE_ID));
        long parentSpanId = Long.parseLong(attaches.get(TraceConstants.SPAN_ID));

        // start tracing
        TraceContext.start();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(parentSpanId);
    }

    private void endTrace() {
        agent.send(TraceContext.getSpans());
        TraceContext.clear();
    }
}
