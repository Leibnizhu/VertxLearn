package io.gitlab.leibnizhu.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 15:04.
 */
public class HttpServerTest extends AbstractVerticle {

    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();

        server.requestHandler(request -> {
            // 所有的请求都会调用这个处理器处理
            HttpServerResponse response = request.response();
            response.putHeader("content-type", "text/plain");
            // 写入响应并结束处理
            response.end("Hello World!");
        });

        server.listen(8080);
    }
}
