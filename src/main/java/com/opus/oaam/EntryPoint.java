package com.opus.oaam;

import com.opus.oaam.util.Runner;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.templ.HandlebarsTemplateEngine;

public class EntryPoint extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

	public static void main(String[] args) {
		Runner.run(EntryPoint.class);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route().handler(StaticHandler.create().setCachingEnabled(false));

		HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();

		router.get().handler(ctx -> {
			ctx.put("title", "Log in Page");
			engine.render(ctx, "secure", "/index.hbs", res -> renderView(res, ctx));
		});

		router.post("/login").handler(ctx -> {
			String userName = ctx.request().getParam("userName");
			String password = ctx.request().getParam("password");

			boolean adminUser = userName.equals("admin") && password.equals("admin");
			boolean normalUser = userName.equals("opus") && password.equals("opus");

			boolean rendered = adminUser ? redirectView(ctx, engine, "Admin user", "/templates/admin/admin.hbs") : ((normalUser) ? redirectView(ctx, engine, "Normal User", "/templates/user/user.hbs") : redirectView(ctx, engine, "Invalid User", "/index.hbs"));
			if (rendered){
				LOGGER.info("Response redirected properly");
			}
		});

		vertx.createHttpServer().requestHandler(router::accept).rxListen(8080).subscribe();
		startFuture.complete();
	}

	/**
	 * Decide & Render appropriate view
	 * 
	 * @param ctx
	 * @param engine
	 * @param userType
	 * @param template
	 */
	private boolean redirectView(RoutingContext ctx, HandlebarsTemplateEngine engine, String userType, String template) {
		ctx.put("title", userType + "'s dashboard");
		ctx.put("user-type", userType);
		engine.render(ctx, "secure", template, res -> renderView(res, ctx));
		return true;
	}

	/**
	 * Common rendering operation
	 * 
	 * @param res
	 * @param ctx
	 */
	private void renderView(AsyncResult<Buffer> res, RoutingContext ctx) {
		if (res.succeeded()){
			ctx.response().end(res.result());
		} else{
			ctx.fail(res.cause());
		}
	}

}
