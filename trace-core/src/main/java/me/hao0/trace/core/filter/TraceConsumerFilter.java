package me.hao0.trace.core.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.*;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import me.hao0.trace.core.TraceConstants;
import me.hao0.trace.core.TraceContext;
import me.hao0.trace.core.util.Ids;
import me.hao0.trace.core.util.Networks;
import me.hao0.trace.core.util.Times;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Trace Reference Filter
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (TraceContext.getTraceId() == null){
            // not need tracing
            return invoker.invoke(invocation);
        }

        // start the watch
        Stopwatch watch = Stopwatch.createStarted();

        Span consumeSpan = startTrace(invoker, invocation);

        System.err.println("consumer invoke before: ");
        TraceContext.print();

        Result result = invoker.invoke(invocation);
        RpcResult rpcResult = (RpcResult)result;

        System.err.println("consumer invoke after: ");
        TraceContext.print();

        System.err.println("sr time: " + rpcResult.getAttachment(TraceConstants.SR_TIME));
        System.err.println("ss time: " + rpcResult.getAttachment(TraceConstants.SS_TIME));

        endTrace(invoker, rpcResult, consumeSpan, watch);

        return rpcResult;
    }

    private Span startTrace(Invoker<?> invoker, Invocation invocation){

        // start consume span
        Span consumeSpan = new Span();
        consumeSpan.setId(Ids.get());
        long traceId = TraceContext.getTraceId();
        long parentId = TraceContext.getSpanId();
        consumeSpan.setTrace_id(traceId);
        consumeSpan.setParent_id(parentId);
        String serviceName = invoker.getInterface().getSimpleName() + "." + invocation.getMethodName();
        consumeSpan.setName(serviceName);
        long timestamp = Times.currentMicros();
        consumeSpan.setTimestamp(timestamp);

        // cs annotation
        URL provider = invoker.getUrl();
        int providerHost = Networks.ip2Num(provider.getHost());
        int providerPort = provider.getPort();
        consumeSpan.addToAnnotations(
            Annotation.create(timestamp, TraceConstants.ANNO_CS,
                Endpoint.create(serviceName, providerHost, providerPort)));

        String providerOwner = provider.getParameter("owner");
        if (!Strings.isNullOrEmpty(providerOwner)){
            // app owner
            consumeSpan.addToBinary_annotations(BinaryAnnotation.create(
                "owner", providerOwner, null
            ));
        }

        // attach trace data
        Map<String, String> attaches = invocation.getAttachments();
        attaches.put(TraceConstants.TRACE_ID, String.valueOf(consumeSpan.getTrace_id()));
        attaches.put(TraceConstants.SPAN_ID, String.valueOf(consumeSpan.getId()));

        return consumeSpan;
    }

    private void endTrace(Invoker invoker, Result result, Span consumeSpan, Stopwatch watch) {
        consumeSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        // cr annotation
        URL provider = invoker.getUrl();
        consumeSpan.addToAnnotations(
            Annotation.create(Times.currentMicros(), TraceConstants.ANNO_CR,
                Endpoint.create(consumeSpan.getName(), Networks.ip2Num(provider.getHost()), provider.getPort())));

        // exception catch
        Throwable throwable = result.getException();
        if (throwable != null){
            // attach exception
            consumeSpan.addToBinary_annotations(BinaryAnnotation.create(
                "Exception", Throwables.getStackTraceAsString(throwable), null
            ));
        }

        // collect the span
        TraceContext.addSpan(consumeSpan);
    }
}
