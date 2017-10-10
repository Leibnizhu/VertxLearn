package io.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
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
        //将这个 sub router 通过一个挂载点挂载到主 router 上
        mainRouter.mountSubRouter("/api", restAPI);

        server.requestHandler(mainRouter::accept).listen(8080);
    }
}
