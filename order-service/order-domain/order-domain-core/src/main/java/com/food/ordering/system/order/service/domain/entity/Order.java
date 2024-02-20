package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.model.entity.AggregateRoot;
import com.food.ordering.system.domain.model.enums.ErrorConstant;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;


public class Order extends AggregateRoot<OrderId> {
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;
    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    public void initializeOrder(){
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }

    public void validateOrder(){
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }


    public void pay(){
        if(orderStatus != OrderStatus.PENDING){
            throw new OrderDomainException(String.format(ErrorConstant.ORDER_IS_NOT_CORRECT_STATE_FOR_OPERATION.getResponse(),"pay"));
        }
        orderStatus = OrderStatus.PAID;
    }

    public void approve(){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException(String.format(ErrorConstant.ORDER_IS_NOT_CORRECT_STATE_FOR_OPERATION.getResponse(),"approve"));
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException(String.format(ErrorConstant.ORDER_IS_NOT_CORRECT_STATE_FOR_OPERATION.getResponse(),"initCancel"));
        }
        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    public void cancel(List<String> failureMessages){
        if(!(orderStatus == OrderStatus.CANCELLING || orderStatus == OrderStatus.PENDING)){
            throw new OrderDomainException(String.format(ErrorConstant.ORDER_IS_NOT_CORRECT_STATE_FOR_OPERATION.getResponse(),"cancel"));
        }
        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if(this.failureMessages != null && failureMessages != null){
            this.failureMessages.addAll(failureMessages.stream().filter(message -> !message.isEmpty()).toList());
        }
        if(this.failureMessages == null){
            this.failureMessages = failureMessages;
        }
    }


    private void validateInitialOrder() {
        if(orderStatus != null || getId() != null){
            throw new OrderDomainException(ErrorConstant.ORDER_IS_NOT_CORRECT.getResponse());
        }
    }

    private void validateTotalPrice() {
        if(price == null || !price.isGreaterThanZero()){
            throw new OrderDomainException(ErrorConstant.MUST_BE_GREATER_THAN_ZERO.getResponse());
        }
    }

    private void validateItemsPrice() {
      Money orderItemsTotal =  items.stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO,Money::add);

      if(!price.equals(orderItemsTotal)){
          throw new OrderDomainException(String.format(
                  ErrorConstant.TOTAL_PRICE_NOT_EQUAL_ORDER_ITEMS_TOTAL_PRICE.getResponse(),price.getAmount(),
                  orderItemsTotal.getAmount()));
      }
    }

    private void validateItemPrice(OrderItem orderItem) {
        if(!orderItem.isPriceValid()){
            throw new OrderDomainException(String.format(ErrorConstant.ORDER_ITEM_PRICE_NOT_VALID.getResponse(),
                    orderItem.getPrice().getAmount(),orderItem.getProduct().getId().getValue()));
        }
    }

    private void initializeOrderItems() {
        long itemId = 1;
        for(OrderItem orderItem : items){
            orderItem.initializeOrderItem(super.getId(),new OrderItemId(itemId++));
        }
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }

    public static Builder builder() {
       return new Builder();
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }


    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public Builder id(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
