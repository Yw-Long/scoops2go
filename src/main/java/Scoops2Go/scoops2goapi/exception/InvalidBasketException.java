package Scoops2Go.scoops2goapi.exception;

public class InvalidBasketException extends RuntimeException {

  private final int basketSize;

  public InvalidBasketException(String message, int basketSize) {
    super(message);
    this.basketSize = basketSize;
  }

  public InvalidBasketException(String message, Throwable cause, int basketSize) {
    super(message, cause);
    this.basketSize = basketSize;
  }

  public int getBasketSize() {
    return basketSize;
  }
}
