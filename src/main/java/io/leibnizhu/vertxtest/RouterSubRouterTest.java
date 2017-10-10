package io.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 17:32.
 */
public class RouterSubRouterTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        server8080(vertx);
    }

    private static void server8080(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        Router mainRouter = Router.router(vertx);
        // 处理静态资源
        mainRouter.route("/static/*").handler(ctx -> {
            ctx.response().end("my Static Handler");
        });
        mainRouter.route("/template/*").handler(ctx -> {
            ctx.response().end("my Template Handler");
        });

        //新建一个子路由处理REST API
        Router restAPI = Router.router(vertx);
        restAPI.get("/prod/:productID").handler(rc -> {
            String prodId = rc.request().getParam("productID");
            rc.response().end("You got product which id = " + prodId);
        });
        restAPI.put("/prod/:productID").handler(rc -> {
            String prodId = rc.request().getParam("productID");
            rc.response().end("You put a product which id = " + prodId);
        });
        restAPI.delete("/prod/:productID").handler(rc -> {
            String prodId = rc.request().getParam("productID");
            rc.response().end("You deleted a product which id = " + prodId);
        });
        //将这个 sub router 通过一个挂载点挂载到主 router 上，子路由的请求路径会加上挂载时指定的前缀
        mainRouter.mountSubRouter("/api", restAPI);

        //Vert.x Web 解析 Accept-Language 消息头并提供了一些识别客户端偏好的语言，
        // 以及提供通过 quality 排序的语言偏好列表的方法
        //RoutingContext.acceptableLanguages()返回客户端能够理解的排序好的语言列表
        //RoutingContext.preferredLocale()返回列表的第一个元素

        //如果没有为请求匹配到任何路由，Vert.x Web 会声明一个 404 错误。
        //这可以被您自己实现的处理器处理，或者被我们提供的专用错误处理器（failureHandler）处理。
        //如果没有提供错误处理器，Vert.x Web 会发送一个基本的 404 (Not Found) 响应。
        mainRouter.get("/somepath/path1/").handler(routingContext -> {
            // 这里抛出一个 RuntimeException
            throw new RuntimeException("something happened!");
        });
        mainRouter.get("/somepath/path2").handler(routingContext -> {
            // 这里故意将请求处理为失败状态
            // 例如 403 - 禁止访问
            routingContext.fail(403);
        });

        // 定义一个失败处理器，上述的处理器发生错误时会调用这个处理器
        mainRouter.get("/somepath/*").failureHandler(failureRoutingContext -> {
            int statusCode = failureRoutingContext.statusCode();
            if (statusCode == -1 && failureRoutingContext.failure() != null) {
                statusCode = 500;
            }
            // 对于 RuntimeException 状态码会是 500，否则是 403
            HttpServerResponse response = failureRoutingContext.response();
            response.setStatusCode(statusCode).end("Sorry! Not today");
        });

        server.requestHandler(mainRouter::accept).listen(8080);
    }
}
