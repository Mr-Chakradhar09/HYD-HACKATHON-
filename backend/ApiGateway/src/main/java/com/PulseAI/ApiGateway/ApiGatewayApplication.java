package com.PulseAI.ApiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> redirectSwaggerUi() {
		return route(
				request -> request.path().equals("/swagger-ui/index.html") || request.path().equals("//swagger-ui/index.html"),
				request -> ServerResponse.temporaryRedirect(URI.create("/webjars/swagger-ui/index.html")).build()
		);
	}
}
