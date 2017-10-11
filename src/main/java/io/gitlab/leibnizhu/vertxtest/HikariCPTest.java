package io.gitlab.leibnizhu.vertxtest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-11 11:56.
 */
public class HikariCPTest extends AbstractVerticle {

    public static void main(String[] args) throws ClassNotFoundException {
        Vertx vertx = Vertx.vertx();
        Class.forName("com.mysql.jdbc.Driver");
        vertx.deployVerticle(HikariCPTest.class.getName(), new DeploymentOptions().setConfig(new JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:mysql://192.168.1.235:3306/fission?useUnicode=true&characterEncoding=UTF-8&useSSL=false&autoReconnect=true&failOverReadOnly=false")
                .put("driverClassName", "com.mysql.jdbc.Driver")
                .put("username", "root")
                .put("password", "turingdi")
                .put("maximumPoolSize", 30)));
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        JDBCClient client = JDBCClient.createShared(vertx, config());
        Router router = Router.router(vertx);
        router.route("/hikari").handler(rc -> {
            rc.response().putHeader("content-type", "application/json; charset=utf-8");
            client.getConnection(res -> {
                if (res.succeeded()) {
                    SQLConnection conn = res.result();// 获得一个连接
                    conn.query("select * from campaign;", ar -> {
                        if (ar.succeeded()) {
                            ResultSet rs = ar.result();
                            rc.response().end(rs.getResults().toString(), "UTF-8");
                        } else {
                            rc.response().end("读取数据库失败");
                        }
                    });
                } else {
                    // 获取连接失败 - 处理异常
                    rc.response().end("获取数据库链接失败");
                }
            });
        });
        server.requestHandler(router::accept).listen(8080);
    }

    private HikariDataSource getHikariPoll() {
        HikariConfig config = new HikariConfig("/hikari.properties");
//        config.setJdbcUrl("jdbc:mysql://192.168.1.235:3306/fission?useUnicode=true&characterEncoding=UTF-8&useSSL=false&autoReconnect=true&failOverReadOnly=false");
//        config.setUsername("root");
//        config.setPassword("turingdi");
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }
}
