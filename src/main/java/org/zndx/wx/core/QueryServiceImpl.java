package org.zndx.wx.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.zndx.wx.api.BaseRequest;
import org.zndx.wx.api.BaseResponse;
import org.zndx.wx.api.PipelineConfig;
import org.zndx.wx.api.QueryService;

import java.util.concurrent.CompletableFuture;

public class QueryServiceImpl implements QueryService {
    private final Materializer mat;
    private final ActorSystem core;
    private final ActorRef objective;

    public QueryServiceImpl(Materializer mat, ActorSystem core, ActorRef objective) {
        this.mat = mat;
        this.core = core;
        this.objective = objective;
    }

    @Override
    public CompletableFuture<BaseResponse> fetch(BaseRequest inbound) {
        BaseResponse.Builder reply = BaseResponse.newBuilder();
        PipelineConfig config = inbound.getConfig();
        // TODO: set reply status
        return CompletableFuture.completedFuture(reply.build());
    }
}
//#full-service-impl
