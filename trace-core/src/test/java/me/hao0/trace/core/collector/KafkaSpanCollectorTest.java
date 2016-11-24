package me.hao0.trace.core.collector;

import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.google.common.base.Stopwatch;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import me.hao0.trace.core.util.Networks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaSpanCollectorTest {

    private Random random = new Random();

    private KafkaSpanCollector collector;

    @Before
    public void init(){
        SpanCollectorMetricsHandler metrics = new SimpleMetricsHandler();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        KafkaSpanCollector.Config config =
                KafkaSpanCollector.Config.builder().kafkaProperties(props).flushInterval(0).build();

        collector = KafkaSpanCollector.create(config, metrics);
    }

    @After
    public void close() {
        if (collector != null){
            collector.close();
        }
    }

    @Test
    public void testLoginApi() throws InterruptedException {

        System.err.println("invoking loginApi start...");
        // 1. invoking LoginApi
        Stopwatch apiWatch = Stopwatch.createStarted();

        long timestamp;

        long traceId = random.nextLong();
        Span loginApiSpan = new Span();
        loginApiSpan.setId(traceId);
        loginApiSpan.setTrace_id(traceId);
        loginApiSpan.setName("/api/users/login");
        timestamp = new Date().getTime() * 1000;
        loginApiSpan.setTimestamp(timestamp);
        // sr annotation
        loginApiSpan.addToAnnotations(
                Annotation.create(timestamp, "sr",
                        Endpoint.create("/api/users/login", Networks.ip2Num("127.0.0.1"), 1234)));

        // 1.1 invoking loginService.login
        Stopwatch loginServiceWatch = Stopwatch.createStarted();
        Span loginServiceSpan = new Span();
        loginServiceSpan.setId(random.nextLong());
        loginServiceSpan.setTrace_id(traceId);
        loginServiceSpan.setName("loginService.login");
        loginServiceSpan.setParent_id(loginApiSpan.getId());
        timestamp = new Date().getTime() * 1000;
        loginServiceSpan.setTimestamp(timestamp);

        // cs annotation
        loginServiceSpan.addToAnnotations(
                Annotation.create(timestamp, "cs",
                        Endpoint.create("loginService.login", Networks.ip2Num("127.0.0.1"), 1234)));
        // do rpc invoke
        System.err.println("invoking loginServer.login start...");
        Thread.sleep(random.nextInt(2000));
        loginServiceSpan.setDuration(loginServiceWatch.stop().elapsed(TimeUnit.MICROSECONDS));

        // cr annotation
        loginServiceSpan.addToAnnotations(
                Annotation.create(timestamp, "cr",
                        Endpoint.create("loginService.login", Networks.ip2Num("127.0.0.1"), 1234)));
        System.err.println("invoking loginServer.login end...");
        collector.collect(loginServiceSpan);


        // 1.2 invoking accountService.myAccount
        Stopwatch accountServiceWatch = Stopwatch.createStarted();
        Span accountServiceSpan = new Span();
        accountServiceSpan.setId(random.nextLong());
        accountServiceSpan.setTrace_id(traceId);
        accountServiceSpan.setName("accountService.myAccount");
        accountServiceSpan.setParent_id(loginApiSpan.getId());
        timestamp = new Date().getTime() * 1000;
        accountServiceSpan.setTimestamp(timestamp);
        // cs annotation
        accountServiceSpan.addToAnnotations(
                Annotation.create(timestamp, "cs",
                        Endpoint.create("accountService.myAccount", Networks.ip2Num("127.0.0.1"), 1234)));
        // do rpc invoke
        System.err.println("invoking accountService.myAccount start...");
        Thread.sleep(random.nextInt(2000));
        accountServiceSpan.setDuration(accountServiceWatch.stop().elapsed(TimeUnit.MICROSECONDS));

        // cr annotation
        accountServiceSpan.addToAnnotations(
                Annotation.create(timestamp, "cr",
                        Endpoint.create("accountService.myAccount", Networks.ip2Num("127.0.0.1"), 1234)));
        System.err.println("invoking accountService.myAccount end...");
        collector.collect(accountServiceSpan);


        // LoginApi invoke chain finished
        // ss annotation
        loginApiSpan.addToAnnotations(
                Annotation.create(timestamp, "ss",
                        Endpoint.create("/api/users/login", Networks.ip2Num("127.0.0.1"), 1234)));

        loginApiSpan.setDuration(apiWatch.stop().elapsed(TimeUnit.MICROSECONDS));
        System.err.println("invoking loginApi end...");

        collector.collect(loginApiSpan);

        collector.flush();
    }

    static class SimpleMetricsHandler implements SpanCollectorMetricsHandler {

        final AtomicInteger acceptedSpans = new AtomicInteger();

        final AtomicInteger droppedSpans = new AtomicInteger();

        @Override
        public void incrementAcceptedSpans(int quantity) {
            acceptedSpans.addAndGet(quantity);
        }

        @Override
        public void incrementDroppedSpans(int quantity) {
            droppedSpans.addAndGet(quantity);
        }
    }
}
