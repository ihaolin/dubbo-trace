package me.hao0.trace.core.custom;

import com.alibaba.dubbo.rpc.Result;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public class TraceResult {

    private Result result;

    /**
     * The sr time
     */
    private long srt;

    /**
     * The ss time
     */
    private long sst;

    public TraceResult(Result result, long srt, long sst) {
        this.result = result;
        this.srt = srt;
        this.sst = sst;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public long getSrt() {
        return srt;
    }

    public void setSrt(long srt) {
        this.srt = srt;
    }

    public long getSst() {
        return sst;
    }

    public void setSst(long sst) {
        this.sst = sst;
    }
}
