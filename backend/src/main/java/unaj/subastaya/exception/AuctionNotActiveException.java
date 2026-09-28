package unaj.subastaya.exception;

public class AuctionNotActiveException extends RuntimeException {
    public AuctionNotActiveException(String message) {
        super(message);
    }
}
