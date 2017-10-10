package io.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 16:12.
 */
public class RouterPathTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        server8080(vertx);
    }

    private static void server8080(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        //默认的路由的匹配顺序与添加到 Router 的顺序一致。
        Router router = Router.router(vertx);
        //忽略结尾的 /，所以路径 /some/path 或者 /some/path// 的请求也是匹配的：
        router.route("/p1/aa").handler(ctx -> {
            ctx.response().end("/p1/aa");
        });
        //为所有以某些路径开始的请求设置 Route。在声明 Route 的路径时使用一个 * 作为结尾
        router.route("/p1/*").handler(ctx -> {
            ctx.response().end("/p1/*");
        });
        //可以通过占位符声明路径参数并在处理请求时通过 params 方法获取：
        //还可以指定请求的HTTP方法，或可以使用 method 方法router.route().method(HttpMethod.POST)
        router.route(HttpMethod.POST, "/p2/:a/:b").handler(ctx -> {
            String a = ctx.request().getParam("a");
            String b = ctx.request().getParam("b");
            ctx.response().end("p2,a=" + a + ",b=" + b);
        });
        //也可以使用对应的 get、post、put 等方法指定HTTP方法
        router.put("/put/:a").handler(ctx -> {
            String a = ctx.request().getParam("a");
            ctx.response().end("p2,a=" + a);
        });
        //如果想要让一个路由匹配不止一个 HTTP Method，可以调用 method 方法多次：
        router.route("/putget/:a")
                .method(HttpMethod.PUT).method(HttpMethod.GET)
                .handler(ctx -> {
                    String a = ctx.request().getParam("a");
                    ctx.response().end("p2,a=" + a);
                });
        //正则匹配路径： router.route().pathRegex(".*foo");
        //或    router.routeWithRegex(".*foo");
        router.routeWithRegex(".+?/\\d+?").handler(ctx -> {
            ctx.response().end(ctx.request().uri());
        });
        server.requestHandler(router::accept).listen(8080);
    }
}
