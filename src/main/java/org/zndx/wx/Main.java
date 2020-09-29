package org.zndx.wx;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.UseHttp2;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.zndx.wx.api.EchoServiceHandlerFactory;
import org.zndx.wx.api.QueryServiceHandlerFactory;
import org.zndx.wx.core.EchoServiceImpl;
import org.zndx.wx.core.QueryServiceImpl;

import java.util.concurrent.CompletionStage;

public class Main {
    public static void main( String[] args ) {
        Config conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2=on," +
                "akka.actor.provider=cluster")
                .withFallback(ConfigFactory.defaultApplication())
                .resolve();
        ActorSystem core = ActorSystem.create("CORE", conf);

        Ignite ignite = initIgnite();

        IgniteCache<Integer, String> workspace = ignite.cache("workspace");

        Cluster cluster = Cluster.get(core);
        cluster.join(cluster.selfAddress());
        Materializer mat = ActorMaterializer.create(core);

        ActorRef objective = core.actorOf(Objective.props(ignite, workspace), "objective");

        //#concatOrNotFound
        Function<HttpRequest, CompletionStage<HttpResponse>> queryService =
                QueryServiceHandlerFactory.create(new QueryServiceImpl(mat, core, objective), mat, core);
        Function<HttpRequest, CompletionStage<HttpResponse>> echoService =
                EchoServiceHandlerFactory.create(new EchoServiceImpl(), mat, core);
        @SuppressWarnings("unchecked")
        Function<HttpRequest, CompletionStage<HttpResponse>> serviceHandlers =
                ServiceHandler.concatOrNotFound(queryService, echoService);

        Http.get(core).bindAndHandleAsync(
                serviceHandlers,
                ConnectHttp.toHost("127.0.0.1", 8080, UseHttp2.always()),
                mat)
                //#concatOrNotFound
                .thenAccept(binding -> {
                    System.out.println("gRPC server bound to: " + binding.localAddress());
                });

    }

    private static Ignite initIgnite() {

        // Ignite configuration.
        IgniteConfiguration igniteCfg = new IgniteConfiguration();

        // Durable Memory configuration.
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();

        // Creating a new data region.
        DataRegionConfiguration regionCfg = new DataRegionConfiguration();

        // Region name.
        regionCfg.setName("base-region");

        // Setting initial RAM size.
        regionCfg.setInitialSize(100L * 1024 * 1024);

        // Setting region max size equal to half physical RAM size(8 GB)
        regionCfg.setMaxSize(8L * 1024 * 1024 * 1024);

        // Enable swap space.
        regionCfg.setSwapPath("src/test/swap");

        // Setting the data region configuration.
        storageCfg.setDataRegionConfigurations(regionCfg);

        // Applying the new configuration.
        igniteCfg.setDataStorageConfiguration(storageCfg);

        CacheConfiguration cacheCfg = new CacheConfiguration("workspace-cfg");

        cacheCfg.setCacheMode(CacheMode.LOCAL);

        igniteCfg.setCacheConfiguration(cacheCfg);

        // Return a shared cache
        return Ignition.start(igniteCfg);
    }

}
