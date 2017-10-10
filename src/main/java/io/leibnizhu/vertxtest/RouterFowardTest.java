package io.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 17:08.
 */
public class RouterFowardTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        server8080(vertx);
    }

    private static void server8080(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        //如果一个到达的请求包含路径 /some/path，首先第一个处理器向上下文添加了值，
        //然后路由到了下一个处理器。第二个处理器转发到了路径 /some/path/B，该处理器最后结束了响应。
        router.get("/some/path").handler(ctx -> {
            ctx.put("foo", "bar");
            ctx.next();
        });

        router.get("/some/path/B").handler(ctx -> {
            String foo = ctx.get("foo");
            ctx.response().end(foo);
        });

        router.get("/some/path").handler(ctx -> {
            ctx.reroute("/some/path/B");
        });
        //可以使用路径或者同时使用路径和方法来转发。注意，基于方法的重定向可能会带来安全问题

        //也可以在失败处理器中转发。由于转发的性质，在这种情况下，当前的状态码和失败原因也会被重置。因此在转发后的处理器应该根据需要生成正确的状态码
        //重定向是基于路径的。也就是说，如果需要在重定向的过程中添加或者保持状态，需要使用 RoutingContext 对象
        //虽然在重定向时会警告您查询参数会丢失，但是重定向的过程仍然会执行。并且会从路径上裁剪掉所有的查询参数或 HTML 锚点。
        router.get("/error").handler(ctx -> {
            ctx.response()
                    .setStatusCode(500)//生成正确的状态码,否则状态码是200
                    .end("Server error happened!!!");
        });
        router.get().handler(ctx -> {
            throw new IllegalArgumentException();
        }).failureHandler(ctx -> {
            if (ctx.statusCode() == -1) {
                ctx.reroute("/error");//不包含查询参数
            } else {
                ctx.next();
            }
        });

        server.requestHandler(router::accept).listen(8080);
    }
}
