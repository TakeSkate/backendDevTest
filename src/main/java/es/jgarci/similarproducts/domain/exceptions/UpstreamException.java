package es.jgarci.similarproducts.domain.exceptions;

public class UpstreamException extends RuntimeException {
    public UpstreamException(String message) {
        super(message);
    }
}
