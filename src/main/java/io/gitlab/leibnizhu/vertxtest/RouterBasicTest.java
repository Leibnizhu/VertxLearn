package io.gitlab.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 15:31.
 */
public class RouterBasicTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        server8080(vertx);
        server8081(vertx);
    }

    private static void server8080(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        //创建了一个没有匹配条件的 Route，这个 route 会匹配所有到达这个服务器的请求。
        //调用处理器的参数是一个 RoutingContext 对象。它不仅包含了 Vert.x 中标准的 HttpServerRequest 和 HttpServerResponse，还包含了各种用于简化 Vert.x Web 使用的东西。
        router.route().handler(routingContext -> {
            // 所有的请求都会调用这个处理器处理
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");
            // 写入响应并结束处理
            response.end("Hello World from Vert.x-Web!");
        });
        server.requestHandler(router::accept).listen(8080);
    }

    /**
     * route1 向响应里写入了数据，
     * 2秒之后 route2 向响应里写入了数据，
     * 再2秒之后 route3 向响应里写入了数据并结束了响应。
     * 注意，所有发生的这些没有线程阻塞。
     * @param vertx
     */
    private static void server8081(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/p1/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            // 由于我们会在不同的处理器里写入响应，因此需要启用分块传输
            // 仅当需要通过多个处理器输出响应时才需要
            response.setChunked(true);
            response.write("route1\n");
            // 5 秒后调用下一个处理器
            //如果不在处理器里结束这个响应，需要调用 next 方法让其他匹配的 Route 来处理请求（如果有）。
            routingContext.vertx().setTimer(2000, tid -> routingContext.next());
        });

        router.route("/p1").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.write("route2\n");
            // 5 秒后调用下一个处理器
            routingContext.vertx().setTimer(2000, tid ->  routingContext.next());
        });

        router.route("/p1").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.write("route3");
            // 结束响应
            routingContext.response().end();
        });
        server.requestHandler(router::accept).listen(8081);
    }
}
