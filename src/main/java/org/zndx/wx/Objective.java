package org.zndx.wx;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

public class Objective extends AbstractLoggingActor {

    private final Ignite ignite;
    private IgniteCache<Long, String> workspace;

    enum Ack {
        INSTANCE;
    }

    static class StreamInitialized {}

    static class StreamCompleted {}

    static class StreamFailure {
        private final Throwable cause;

        public StreamFailure(Throwable cause) {
            this.cause = cause;
        }

        public Throwable getCause() {
            return cause;
        }
    }

    public static Props props(Ignite ignite, IgniteCache<Integer, String> workspace) {
        return Props.create(Objective.class, ignite, workspace);
    }

    private Objective(Ignite ignite, IgniteCache<Long, String> workspace) {
        this.ignite = ignite;
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        StreamInitialized.class,
                        init -> {
                            log().info("Stream initialized");
                            sender().tell(Ack.INSTANCE, self());
                        })
                .match(
                        StreamCompleted.class,
                        completed -> {
                            log().info("Stream completed");
                        })
                .match(
                        StreamFailure.class,
                        failed -> {
                            log().error(failed.getCause(), "Stream failed!");
                        })
                .build();
    }
}
