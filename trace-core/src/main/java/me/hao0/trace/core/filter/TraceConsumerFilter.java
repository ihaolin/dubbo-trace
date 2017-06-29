package me.hao0.trace.core.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.*;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import me.hao0.trace.core.TraceAgent;
import me.hao0.trace.core.TraceConstants;
import me.hao0.trace.core.TraceContext;
import me.hao0.trace.core.config.TraceConf;
import me.hao0.trace.core.config.TraceConfLoader;
import me.hao0.trace.core.util.Ids;
import me.hao0.trace.core.util.Networks;
import me.hao0.trace.core.util.Times;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Trace Reference Filter
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceConsumerFilter implements Filter {
    private TraceConf conf = TraceConfLoader.load("trace.yml");

    private TraceAgent agent = new TraceAgent(conf.getServer());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (!conf.getEnable()){
            // not enable tracing
            return invoker.invoke(invocation);
        }

        // start the watch
        Stopwatch watch = Stopwatch.createStarted();
        List<Span>  rootSpans = TraceContext.getSpans();
        boolean fromUrl = (rootSpans != null && rootSpans.isEmpty() == false);

        Span consumeSpan = startTrace(invoker, invocation,fromUrl);

        System.err.println("consumer invoke before:  ");
        System.err.println(new Gson().toJson(consumeSpan));

        TraceContext.print();

        Result result = invoker.invoke(invocation);
        RpcResult rpcResult = (RpcResult)result;

        System.err.println("consumer invoke after: ");
        TraceContext.print();
        System.err.println(new Gson().toJson(consumeSpan));

        System.err.println("sr time: " + rpcResult.getAttachment(TraceConstants.SR_TIME));
        System.err.println("ss time: " + rpcResult.getAttachment(TraceConstants.SS_TIME));

        endTrace(invoker, rpcResult, consumeSpan, watch,fromUrl);
        System.err.println(new Gson().toJson(rpcResult.getAttachments()));
        return rpcResult;
    }

    private Span startTrace(Invoker<?> invoker, Invocation invocation,boolean fromUrl){

        // start consume span
        long id = Ids.get();
        Span consumeSpan = new Span();
        long traceId = id;
        long parentId = id;

        
		// 判断是不是要创建新的span
        if(fromUrl){
            // 来源于url,直接继承
            traceId = (TraceContext.getTraceId());
            consumeSpan.setParent_id(parentId); // 这个使用不当,如果放在else分支,会导致zipkin ui js溢出
        }else{
            // 开始span
            TraceContext.start();
            TraceContext.setTraceId(id);
            TraceContext.setSpanId(id);
        }

        consumeSpan.setId(id);
        consumeSpan.setTrace_id(traceId);
        String serviceName = invoker.getInterface().getSimpleName() + "." + invocation.getMethodName() + "." + "C";
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

    private void endTrace(Invoker invoker, Result result, Span consumeSpan, Stopwatch watch,boolean fromUrl) {
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

        // 来源于url的span,在本地发送
        if(fromUrl == false) {
            // 将span发送出去
            agent.send(TraceContext.getSpans());
            TraceContext.clear();
        }
    }
}
