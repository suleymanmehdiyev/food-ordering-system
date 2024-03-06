package com.food.ordering.system.domain.model.enums;

public enum ErrorConstant {
    ORDER_IS_NOT_CORRECT("Order is not in correct state for initialization!"),
    MUST_BE_GREATER_THAN_ZERO("Total price must be greater than zero!"),
    TOTAL_PRICE_NOT_EQUAL_ORDER_ITEMS_TOTAL_PRICE("Total price:%s is not equal to Order items total:%s!"),
    ORDER_ITEM_PRICE_NOT_VALID("Order item price: %s is not valid for product %s"),
    ORDER_IS_NOT_CORRECT_STATE_FOR_OPERATION("Order is not in correct state for %s operation!"),
    RESTAURANT_IS_NOT_ACTIVE("Restaurant with id %s is currently not active!"),
    NOT_FOUND("Could not find %s with %s id:"),
    COULD_NOT_SAVED("Could not save %s!");

    private String value;

    ErrorConstant(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
