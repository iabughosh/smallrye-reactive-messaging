package io.smallrye.reactive.messaging.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.vertx.reactivex.core.Vertx;

@ApplicationScoped
public class ConsumptionBean {

    @Inject
    Vertx vertx;
    private List<Integer> list = new ArrayList<>();

    @Incoming("data")
    @Outgoing("sink")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Message<Integer> process(EventBusMessage<Integer> input) {
        return Message.of(input.getPayload() + 1, input::ack);
    }

    @Incoming("sink")
    public void sink(int val) {
        list.add(val);
    }

    @Produces
    public Config myConfig() {
        String prefix = "mp.messaging.incoming.data.";
        Map<String, Object> config = new HashMap<>();
        config.put(prefix + "address", "data");
        config.put(prefix + "connector", VertxEventBusConnector.CONNECTOR_NAME);
        return new MapBasedConfig(config);
    }

    List<Integer> getResults() {
        return list;
    }

    void produce() {
        AtomicInteger counter = new AtomicInteger();
        new Thread(() -> new EventBusUsage(vertx.eventBus().getDelegate())
                .produceIntegers("data", 10, true, null, counter::getAndIncrement))
                        .start();
    }

}
