package me.hao0.trace.core.filter;

import com.alibaba.dubbo.rpc.*;
import com.google.gson.Gson;
import me.hao0.trace.core.*;
import me.hao0.trace.core.config.TraceConf;
import me.hao0.trace.core.config.TraceConfLoader;
import me.hao0.trace.core.util.Times;
import java.util.Map;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import me.hao0.trace.core.util.Ids;
import me.hao0.trace.core.util.Networks;
import me.hao0.trace.core.util.Times;
import com.alibaba.dubbo.common.URL;
import java.util.concurrent.TimeUnit;


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
        // start the watch
        Stopwatch watch = Stopwatch.createStarted();

        attaches.put(TraceConstants.SR_TIME,String.valueOf(Times.currentMicros()));

        // start tracing
        TraceContext.start();

        // prepare trace context
        Span providerSpan = startTrace(invoker,invocation,attaches);
        TraceContext.setTraceId(providerSpan.getTrace_id());
        TraceContext.setSpanId(providerSpan.getId());

        Result result = invoker.invoke(invocation);
        RpcResult rpcResult = (RpcResult)result;

        endTrace(invoker, rpcResult, providerSpan, watch);

        attaches.put(TraceConstants.SS_TIME,String.valueOf(Times.currentMicros()));
        return result;
    }

    private Span startTrace(Invoker<?> invoker,Invocation invocation,Map<String, String> attaches){

        // start provider span
        Span providerSpan = new Span();
        providerSpan.setId(Ids.get());
        long traceId = Long.parseLong(attaches.get(TraceConstants.TRACE_ID));
        long parentSpanId = Long.parseLong(attaches.get(TraceConstants.SPAN_ID));
        providerSpan.setTrace_id(traceId);
        providerSpan.setParent_id(parentSpanId);
        String serviceName = invoker.getInterface().getSimpleName() + "." + invocation.getMethodName() + "." + "P";
        providerSpan.setName(serviceName);
        long timestamp = Times.currentMicros();
        providerSpan.setTimestamp(timestamp);

        // cs annotation
        URL provider = invoker.getUrl();
        int providerHost = Networks.ip2Num(provider.getHost());
        int providerPort = provider.getPort();
        providerSpan.addToAnnotations(
               Annotation.create(timestamp, TraceConstants.ANNO_SR,
                        Endpoint.create(serviceName, providerHost, providerPort)));

        String providerOwner = provider.getParameter("owner");
        if (!Strings.isNullOrEmpty(providerOwner)){
            // app owner
            providerSpan.addToBinary_annotations(BinaryAnnotation.create(
                    "owner", providerOwner, null
            ));
        }


        return providerSpan;
    }

    private void endTrace(Invoker invoker, Result result, Span providerSpan, Stopwatch watch) {
        providerSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        // cr annotation
        URL provider = invoker.getUrl();
        providerSpan.addToAnnotations(
                Annotation.create(Times.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(providerSpan.getName(), Networks.ip2Num(provider.getHost()), provider.getPort())));

        // exception catch
        Throwable throwable = result.getException();
        if (throwable != null){
            // attach exception
            providerSpan.addToBinary_annotations(BinaryAnnotation.create(
                    "Exception", Throwables.getStackTraceAsString(throwable), null
            ));
        }

        // collect the span
        TraceContext.addSpan(providerSpan);
        agent.send(TraceContext.getSpans());

        TraceContext.clear();
    }
}
