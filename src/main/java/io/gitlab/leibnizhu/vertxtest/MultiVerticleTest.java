package io.gitlab.leibnizhu.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-12 09:38.
 */
public class MultiVerticleTest {


    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Router router = Router.router(vertx);
                router.route("/v1").handler(rc -> {
                    rc.response().end("This is Verticle 1 !");
                });
                vertx.createHttpServer().requestHandler(router::accept).listen(8080);
            }
        }, h -> {
            vertx.deployVerticle(new AbstractVerticle() {
                @Override
                public void start() throws Exception {
                    Router router = Router.router(vertx);
                    router.route("/v2").handler(rc -> {
                        rc.response().end("This is Verticle 2 !");
                    });
                    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
                }
            });
        });

    }
}