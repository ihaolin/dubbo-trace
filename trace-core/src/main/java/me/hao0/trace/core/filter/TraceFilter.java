package me.hao0.trace.core.filter;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import me.hao0.trace.core.*;
import me.hao0.trace.core.config.TraceConf;
import me.hao0.trace.core.config.TraceConfLoader;
import me.hao0.trace.core.config.TracePoint;
import me.hao0.trace.core.util.Ids;
import me.hao0.trace.core.util.ServerInfo;
import me.hao0.trace.core.util.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TraceFilter.class);

    private final String TRACE_CONF_FILE = System.getProperty("trace.conf.file", "trace.yml");

    private final TraceConf conf = TraceConfLoader.load(TRACE_CONF_FILE);

    private TraceAgent agent;

    @Override
    public void init(FilterConfig config) throws ServletException {

        if (!conf.getEnable()){
            return;
        }

        agent = new TraceAgent(conf.getServer());

        log.info("init the trace filter with config({}).", new Object[]{config});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!conf.getEnable()){
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest)request;
        String uri = req.getRequestURI();

        TracePoint point = matchTrace(uri);
        if (point == null){
            // not need to trace
            chain.doFilter(request, response);
        } else {
            // do trace

            Stopwatch watch = Stopwatch.createStarted();

            // start root span
            Span rootSpan = startTrace(req, point);

            // prepare trace context
            TraceContext.start();
            TraceContext.setTraceId(rootSpan.getTrace_id());
            TraceContext.setSpanId(rootSpan.getId());
            TraceContext.addSpan(rootSpan);

            // executor other filters
            chain.doFilter(request, response);

            // end root span
            endTrace(req, rootSpan, watch);

        }
    }

    private Span startTrace(HttpServletRequest req, TracePoint point) {

        String apiName = req.getRequestURI();
        Span apiSpan = new Span();

        // span basic data
        long id = Ids.get();
        apiSpan.setId(id);
        apiSpan.setTrace_id(id);
        apiSpan.setName(point.getKey());
        long timestamp = Times.currentMicros();
        apiSpan.setTimestamp(timestamp);

        // sr annotation
        apiSpan.addToAnnotations(
                Annotation.create(timestamp, TraceConstants.ANNO_SR,
                        Endpoint.create(apiName, ServerInfo.IP4, req.getLocalPort())));

        // app name
        apiSpan.addToBinary_annotations(BinaryAnnotation.create(
            "name", conf.getName(), null
        ));

        // app owner
        apiSpan.addToBinary_annotations(BinaryAnnotation.create(
            "owner", conf.getOwner(), null
        ));

        // trace desc
        if (!Strings.isNullOrEmpty(point.getDesc())){
            apiSpan.addToBinary_annotations(BinaryAnnotation.create(
                "description", point.getDesc(), null
            ));
        }

        return apiSpan;
    }

    private void endTrace(HttpServletRequest req, Span span, Stopwatch watch) {
        // ss annotation
        span.addToAnnotations(
                Annotation.create(Times.currentMicros(), TraceConstants.ANNO_SS,
                        Endpoint.create(span.getName(), ServerInfo.IP4, req.getLocalPort())));

        span.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));

        // send trace spans
        agent.send(TraceContext.getSpans());
    }

    private TracePoint matchTrace(String uri) {

        List<TracePoint> points = conf.getPoints();
        if (points != null && !points.isEmpty()){
            for (TracePoint point : points){
                if (Pattern.compile(point.getPattern()).matcher(uri).matches()){
                    return point;
                }
            }
        }

        return null;
    }

    @Override
    public void destroy() {
        // clear trace context
        TraceContext.clear();
    }
}
