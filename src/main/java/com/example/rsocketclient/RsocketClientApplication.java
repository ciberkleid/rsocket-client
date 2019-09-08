package com.example.rsocketclient;

import io.rsocket.transport.netty.UriUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
@EnableConfigurationProperties(ConfigProperties.class)
@Slf4j
public class RsocketClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsocketClientApplication.class, args);
	}

	@Autowired
	ConfigProperties configProperties;

	@Bean
	public RSocketRequester rSocketRequester(RSocketRequester.Builder b) throws URISyntaxException {
		log.info("RSocketRequester: connecting to {} on port {} via {}", configProperties.getHost(), configProperties.getPort(), configProperties.getTransport());
		return b.connectTcp(configProperties.getHost(), configProperties.getPort()).block();
	}

}

@Slf4j
@RestController()
@RequiredArgsConstructor
class GreetingController {

	private final RSocketRequester requester;

	@GetMapping("hello")
	Mono<Void> hello() {
		return this.requester.route("hello").data(new Greeting("Welcome to Rsocket")).send();
	}

	@GetMapping("name/{name}")
	Mono<String> greet(@PathVariable String name) {
		return this.requester.route("greet." + name).data(new Greeting("Welcome to Rsocket")).retrieveMono(String.class);
	}

	@GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<String> greetStream() {
		return this.requester.route("greet-stream").data(new Greeting("Welcome to Rsocket"))
				.retrieveFlux(String.class)
				.doOnNext(msg -> log.info("received messages::" + msg));
	}

	@GetMapping(value = "channel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<String> greetChannel() {
		return this.requester.route("greet-channel")
				.data(
						Flux.range(0, 10)
								.map(i -> new Greeting("Welcome to Rsocket #" + i))
				)
				.retrieveFlux(String.class)
				.doOnNext(msg -> log.info("received messages::" + msg));
	}
}

@Data
@AllArgsConstructor
class Greeting {

	String message;
}
