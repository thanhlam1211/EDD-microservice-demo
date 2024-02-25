package com.capstone.orderservice.service;

import com.capstone.clients.inventory.InventoryClient;
import com.capstone.clients.inventory.InventoryResponse;
import com.capstone.orderservice.dto.OrderLineItemsDto;
import com.capstone.orderservice.dto.OrderRequest;
import com.capstone.orderservice.event.OrderPlaceEvent;
import com.capstone.orderservice.model.Order;
import com.capstone.orderservice.model.OrderLineItems;
import com.capstone.orderservice.repository.OrderRepository;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final InventoryClient inventoryClient;
    private final ObservationRegistry observationRegistry;
    private final ApplicationEventPublisher applicationEventPublisher;
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
        //Call inventory service, and place order if product in stock -> using web client
        //GET method because in inventory service using GET method in APT

//        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
//                .uri("http://inventory-service/api/inventory",
//                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
//                .retrieve()
//                //return type in get method in inventory service : in this case is boolean
//                .bodyToMono(InventoryResponse[].class)
//                .block();

        //using feign client
        InventoryResponse[] inventoryResponseArray = inventoryClient.isInStock(skuCodes).toArray(new InventoryResponse[0]);

        boolean allProductInStock = Arrays.stream(inventoryResponseArray).allMatch((InventoryResponse::isInStock));
        if(allProductInStock){
            orderRepository.save(order);
            //send topic into kafka queue
            // publish Order Placed Event
            applicationEventPublisher.publishEvent(new OrderPlaceEvent(this, order.getOrderNumber()));
            return "Order Places Successfully!";
        }else {
            throw new IllegalArgumentException("Product is not in stock, please try again");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
