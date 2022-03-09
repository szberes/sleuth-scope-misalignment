package com.java.tracing.sleuthissuereprodction;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.cloud.sleuth.instrument.reactor.ReactorSleuth;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.PostConstruct;

@EnableConfigurationProperties
@RequiredArgsConstructor
@SpringBootApplication
public class SleuthIssueReproductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SleuthIssueReproductionApplication.class, args);
	}

	private static final Logger logger = Loggers.getLogger(SleuthIssueReproductionApplication.class);
	private final BeanFactory beanFactory;
	private final TestBean testBean;

	@PostConstruct
	void init() {
		Schedulers.addExecutorServiceDecorator("tracing", (scheduler, scheduledExecutorService) ->
				TraceableScheduledExecutorService.wrap(beanFactory, scheduledExecutorService));

		testBean.testFlux().subscribe();
	}

	@Component
	@RequiredArgsConstructor
	public static class TestBean {

		private final Tracer tracer;
		private final AdditionalTraceIdBaggageField tescoTraceIdBaggage;

		public Flux<String> testFlux() {
			return ReactorSleuth.tracedFlux(tracer, tracer.nextSpan(), () ->
					Flux.just("testFluxData")
							.doOnNext(data -> tescoTraceIdBaggage.updateValue("test-trace-id"))
							.doOnNext(data -> logger.warn("Start testFlux"))
//                       .publishOn(Schedulers.parallel())
							.flatMap(this::testFlux2)
//                        .publishOn(Schedulers.newParallel("test-misaligned-1"))
							.doOnNext(cmdMsg -> logger.warn("my custom message"))
							// todo: this is the line needed to break the app (to change scheduler for the Flux)
							.subscribeOn(Schedulers.parallel())
							.doOnNext(success -> logger.info("data at the end: " + success))
							.doOnError(error -> logger.error("Error: {}", error.getMessage()))
							.doOnSubscribe(subscription1 -> logger.warn("subscribed to testFlux")));
		}

		private Flux<String> testFlux2(String param) {
			return Flux.just(param).map(String::toUpperCase);
		}
	}
}


