package com.yandex.ydb.demo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import com.yandex.ydb.auth.iam.CloudAuthHelper;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.demo.rest.RedirectServlet;
import com.yandex.ydb.demo.rest.URLServlet;
import com.yandex.ydb.demo.ydb.YdbDriver;
import com.yandex.ydb.demo.ydb.YdbRepository;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexandr Gorshenin
 */
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final int MIN_THREADS = 10;
    private static final int MAX_THREADS = 100;
    private static final int IDLE_TIMEOUT = 120;

    private static Application instance;

    private final Server server;
    private final YdbDriver driver;

    public Application(AppParams prms) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool(MAX_THREADS, MIN_THREADS, IDLE_TIMEOUT);
        server = new Server(threadPool);

        RpcTransport rpc = createRpcTransport(prms);
        driver = new YdbDriver(rpc, prms.database());

        setupJetty(prms.listenPort());

        instance = this;
    }

    private void setupJetty(int listenPort) {
        try {
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(listenPort);
            server.setConnectors(new Connector[] {connector});

            // Configure static resources handler
            URL f = Application.class.getClassLoader().getResource("webapp");
            if (f == null) {
                throw new RuntimeException("Unable to find resource directory");
            }

            // Resolve file to directory
            URI webRootUri = URI.create(f.toURI().toASCIIString());
            log.debug("WebRoot is {}", webRootUri);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setInitParameter("redirectWelcome", "true");

            context.setWelcomeFiles(new String[]{"index.html"});
            context.setContextPath("/");
            context.setBaseResource(Resource.newResource(webRootUri));
            context.addServlet(URLServlet.class, "/url");
            context.addServlet(RedirectServlet.class, "/");

            server.setHandler(context);
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("setup exception {}", e.getMessage());
            throw new RuntimeException("Can't setup jetty server", e);
        }
    }

    private RpcTransport createRpcTransport(AppParams prms) throws IOException {
        String endpoint = prms.endpoint();
        String database = prms.database();

        log.info("Creating rpc transport for endpoint={} database={}", endpoint, database);
        GrpcTransport.Builder builder = GrpcTransport.forEndpoint(endpoint, database)
                .withReadTimeout(Duration.ofSeconds(10));

        if (prms.useIam()) {
            log.info("...Configuring for YC IAM authentication");
            builder.withAuthProvider(CloudAuthHelper.getAuthProviderFromEnviron());
        }

        if (prms.certPath() != null) {
            log.info("...Configuring for TLS using certificate at {}", prms.certPath());
            builder.withSecureConnection(Files.readAllBytes(Paths.get(prms.certPath())));
        } else if (prms.useTls()) {
            log.info("...Configuring for TLS using default certificates");
            builder.withSecureConnection();
        }

        return builder.build();
    }

    void start() throws Exception {
        log.info("initialize ydb...");
        new YdbRepository(driver).initTable();

        log.info("start jetty web server...");
        server.start();
    }

    void join() throws InterruptedException {
        log.info("press Ctrl+C for stopping...");
        server.join();
    }

    void close() {
        try {
            log.info("stop application");
            server.stop();
            driver.close();
        } catch (Exception e) {
            log.error("application stop exception", e);
        }
    }

    public static void main(String... args) {
        try {
            Application app = new Application(AppParams.parseArgs(args));
            app.start();
            Runtime.getRuntime().addShutdownHook(new Thread(app::close));
            app.join();
        } catch (Exception e) {
            log.error("application exception, stopped", e);
        }
    }

    public static YdbDriver ydp() {
        return instance.driver;
    }
}
