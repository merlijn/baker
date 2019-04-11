package com.ing.baker.recipe.dsl.examples;

import com.ing.baker.recipe.dsl.annotations.FiresEvent;
import com.ing.baker.recipe.dsl.annotations.ProcessId;
import com.ing.baker.recipe.dsl.Interaction;
import com.ing.baker.recipe.dsl.InteractionFailureStrategy;
import com.ing.baker.recipe.dsl.Recipe;

import javax.inject.Named;
import java.time.Duration;

/**
 *  Webshop recipe in java code.
 */
public class WebshopJava {

    public static class CustomerInfo {
        public final String name;
        public final String address;
        public final String email;

        public CustomerInfo(String name, String address, String email) {
            this.name = name;
            this.address = address;
            this.email = email;
        }
    }

    public static class OrderPlaced {
        public final String order;
        public OrderPlaced(String order) {
            this.order = order;
        }
    }

    public static class CustomerInfoReceived {
        public final CustomerInfo customerInfo;

        public CustomerInfoReceived(CustomerInfo customerInfo) {
            this.customerInfo = customerInfo;
        }
    }

    public interface ValidateOrder {

        interface Outcome { }

        class Failed implements Outcome { }

        class Valid implements Outcome { }

        @FiresEvent(oneOf = {Failed.class, Valid.class})
        Outcome apply(@ProcessId String processId, @Named("order") String key);
    }

    public interface ManufactureGoods {
        class GoodsManufactured {
            public final String goods;
            public GoodsManufactured(String goods) {
                this.goods = goods;
            }
        }

        @FiresEvent(oneOf = { GoodsManufactured.class })
        GoodsManufactured apply(@Named("order") String order);
    }

    public interface SendInvoice {

        class InvoiceWasSent { }

        @FiresEvent(oneOf = { InvoiceWasSent.class})
        InvoiceWasSent apply(@Named("customerInfo") CustomerInfo customerInfo);
    }

    public interface ShipGoods {

        class GoodsShipped {
            public final String trackingId;

            public GoodsShipped(String trackingId) {
                this.trackingId = trackingId;
            }
        }

        @FiresEvent(oneOf = { GoodsShipped.class })
        GoodsShipped apply(@Named("goods") String goods, @Named("customerInfo") CustomerInfo customerInfo);
    }

    public static class PaymentMade { }

    public final static Recipe webshopRecipe = new Recipe("webshop")
            .withSensoryEvents(
                    OrderPlaced.class,
                    CustomerInfoReceived.class,
                    PaymentMade.class)
            .withInteractions(
                    Interaction.reflect(ValidateOrder.class),
                    Interaction.reflect(ManufactureGoods.class)
                            .withRequiredEvents(PaymentMade.class, ValidateOrder.Valid.class),
                    Interaction.reflect(SendInvoice.class)
                            .withRequiredEvents(ShipGoods.GoodsShipped.class),
                    Interaction.reflect(ShipGoods.class))
            .withDefaultFailureStrategy(
                    new InteractionFailureStrategy.RetryWithIncrementalBackoffBuilder()
                            .withInitialDelay(Duration.ofMillis(100))
                            .withDeadline(Duration.ofHours(24))
                            .withMaxTimeBetweenRetries(Duration.ofMinutes(10))
                            .build());
}
