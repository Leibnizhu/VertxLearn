package io.leibnizhu.vertxtest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * @author Leibniz.Hu
 * Created on 2017-10-10 21:41.
 */
public class JwtAuthTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        server8080(vertx);
    }

    private static void server8080(Vertx vertx) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        /**
         * 生成keystore文件唯一需要的工具是keytool，运行的时候，你可以指定你需要使用的算法：
         * keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA256 -keysize 2048 -alias HS256 -keypass secret
         * keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA384 -keysize 2048 -alias HS384 -keypass secret
         * keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA512 -keysize 2048 -alias HS512 -keypass secret
         * keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS256 -keypass secret -sigalg SHA256withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
         * keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS384 -keypass secret -sigalg SHA384withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
         * keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS512 -keypass secret -sigalg SHA512withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
         * keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES256 -keypass secret -sigalg SHA256withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
         * keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES384 -keypass secret -sigalg SHA384withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
         * keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES512 -keypass secret -sigalg SHA512withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
         */
        JsonObject config = new JsonObject().put("keyStore", new JsonObject()
                .put("path", "keystore.jceks") //此处要与生成keystore的时候用的type、keypass一致
                .put("type", "jceks")
                .put("password", "secret"));
        JWTAuth provider = JWTAuth.create(vertx, config);
        /**
         * 先访问http://localhost:8080/jwt/paulo/super_secret
         * 此时直接访问http://localhost:8080/pro/p1会返回“Unauthorized”
         * 加上Authorization请求头，值是前面响应的内容，再次访问，可以获取provider.generateToken()里面放进去的信息（第一个参数JSON）
         */
        router.route("/jwt/:user/:pswd").handler(rc -> {
            String user = rc.request().getParam("user");
            String pswd = rc.request().getParam("pswd");
            // 在验证的终点上，一旦你通过它的用户名/密码验证了用户的id
            if ("paulo".equals(user) && "super_secret".equals(pswd)) {
                String token = provider.generateToken(new JsonObject().put("sub", "paulo").put("someKey", "some value"), new JWTOptions());
                //对于持有令牌的客户端，唯一需要做的是在 所有 后续的的 HTTP 请求中包含消息头 Authoriztion 并写入 Bearer <token>
                rc.response().end("Bearer " + token);
            } else {
                rc.fail(401);
            }
        });
        router.route("/pro/*").handler(JWTAuthHandler.create(provider));

        router.route("/pro/p1").handler(rc -> {
            rc.response().end("Current auth user: " + rc.user().principal());
        });

        //可以使用静态资源处理器 StaticHandler 来提供诸如 .html、.css、.js 或其他类型的静态资源。
        //每一个被静态资源处理器处理的请求都会返回文件系统的某个目录或 classpath 里的文件。文件的根目录是可以配置的，默认为 webroot。
        //当 Vert.x 在 classpath 中第一次找到一个资源时，会将它提取到一个磁盘的缓存目录中以避免每一次都重新提取。
        //所有访问根路径 / 的请求会被定位到索引页。默认的该文件为 233.html。可以通过 setIndexPage 方法来设置。
        router.route("/static/*").handler(StaticHandler.create().setWebRoot("static").setIndexPage("233.html"));
        server.requestHandler(router::accept).listen(8080);
    }
}
