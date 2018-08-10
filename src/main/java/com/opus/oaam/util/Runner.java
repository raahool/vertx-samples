package com.opus.oaam.util;

import java.util.function.Consumer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.Vertx;

public class Runner {

	private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

	/**
	 * @param clazz
	 */
	public static void run(Class<?> clazz) {
		String verticleID = clazz.getName();
		run(verticleID, new VertxOptions().setClustered(false), null);
	}

	/**
	 * @param verticleID
	 * @param options
	 * @param deploymentOptions
	 */
	public static void run(String verticleID, VertxOptions options, DeploymentOptions deploymentOptions) {
		if (options == null){
			options = new VertxOptions();
		}
		try{
			Consumer<Vertx> runner = vertx -> {

				if (deploymentOptions != null){
					vertx.deployVerticle(verticleID, deploymentOptions);
				} else{
					vertx.deployVerticle(verticleID);
				}

			};

			if (options.isClustered()){
				Vertx.clusteredVertx(options, res -> {
					if (res.succeeded()){
						Vertx vertx = res.result();
						runner.accept(vertx);
					} else{
						res.cause().printStackTrace();
					}
				});
			} else{
				Vertx vertx = Vertx.vertx(options);
				runner.accept(vertx);
			}
		} catch (Throwable t){
			LOGGER.error(t.getMessage(), t);
		}
	}

}
