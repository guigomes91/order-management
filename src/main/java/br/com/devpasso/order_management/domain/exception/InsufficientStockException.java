package br.com.devpasso.order_management.domain.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for product '" + productName + "': requested " + requested + ", available " + available);
    }
}
