package io.leibnizhu.vertxtest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-11 09:48.
 */
public class MySQLTest extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MySQLTest.class.getName(), new DeploymentOptions().setConfig(new JsonObject()
                .put("host", "192.168.1.235")
                .put("port", 3306)
                .put("maxPoolSize", 10)
                .put("username", "root")
                .put("password", "turingdi")
                .put("database", "fission")
                .put("charset", "UTF-8")
                .put("queryTimeout", 10000)));
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        //在创建一个客户端实例的时候指定数据源的名称,如果不同的客户端对象使用了相同的 Vert.x 对象和相同的数据源名称，那么它们将共享数据源。
        //只有在第一次调用 MySQLClient.createShared 或者 PostgreSQLClient.createShared 方法的时候，才会真正的根据 config 参数创建一个数据源。
        //之后再调用此方法，只会返回一个新的客户端对象，但使用的是相同的数据源。这时 config 参数也就不再有作用
        AsyncSQLClient client = MySQLClient.createShared(vertx, config());
        Router router = Router.router(vertx);
        router.route("/mysql").handler(rc -> {
            rc.response().putHeader("content-type", "text/plain; charset=utf-8");
            client.getConnection(res -> {
                if (res.succeeded()) {
                    SQLConnection conn = res.result();// 获得一个连接
                    conn.query("select * from enterprise_user;", ar -> {
                        if (ar.succeeded()) {
                            ResultSet rs = ar.result();
                            rc.response().end(rs.getResults().toString(), "UTF-8");
                        } else {
                            rc.response().write("读取数据库失败").end();
                        }
                    });
                } else {
                    // 获取连接失败 - 处理异常
                    rc.response().write("获取数据库链接失败").end();
                }
            });
        });
        server.requestHandler(router::accept).listen(8080);
    }
}
