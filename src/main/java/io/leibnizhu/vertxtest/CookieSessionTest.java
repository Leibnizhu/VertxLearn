package io.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.Optional;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 21:08.
 */
public class CookieSessionTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        server8080(vertx);
    }

    private static void server8080(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        //Vert.x Web 通过 Cookie 处理器 CookieHandler 来支持 cookie。
        //需要保证 cookie 处理器器能够匹配到所有需要这个功能的请求。
        router.route().handler(CookieHandler.create());
        router.route("/cookie/:key").handler(rc -> {
            String key = rc.request().getParam("key");
            Cookie cookie = rc.getCookie(key);
            if (cookie != null) {
                rc.response().end(Buffer.buffer()
                        .appendString("key=").appendString(key)
                        .appendString("\nvalue=").appendString(cookie.getValue())
                        .appendString("\ndomain=").appendString(Optional.ofNullable(cookie.getDomain()).orElse(""))
                        .appendString("\npath=").appendString(Optional.ofNullable(cookie.getPath()).orElse("")));
            } else {
                rc.response().end("No such cookie with key = " + key);
            }
        });

        //Vert.x Web 使用会话 cookie(5) 来标示一个会话
        //需要在匹配的 Route 上注册会话处理器 SessionHandler 来启用会话功能，并确保它能够在应用逻辑之前执行
        //本地会话存储将会话保存在内存中，并只在当前实例中有效。
        //这个存储适用于只有一个 Vert.x 实例的情况，或者正在使用粘性会话。也就是说可以配置的负载均衡器来确保所有请求（来自同一用户的）永远被派发到同一个 Vert.x 实例上
        //sessiom回收的周期可以通过 LocalSessionStore.create 来配置
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        //可以通过 put 方法来向会话中设置数据，通过 get 方法来获取数据，通过 remove 方法来删除数据。
        router.get("/session/:key").handler(rc -> {
            String key = rc.request().getParam("key");
            String value = rc.session().get(key);
            rc.response().end("Get session " + key + "=" + value);
        });
        //会话中的键的类型必须是字符串。
        // 本地会话存储的值可以是任何类型；
        // 集群会话存储的值类型可以是基本类型，或者 Buffer、JsonObject、JsonArray 或可序列化对象。因为这些值需要在集群中进行序列化
        router.put("/session/:key/:value").handler(rc -> {
            String key = rc.request().getParam("key");
            String value = rc.request().getParam("value");
            rc.session().put(key, value);
            rc.response().end("Successfully add session " + key + "=" + value);
        });
        server.requestHandler(router::accept).listen(8080);
    }
}
