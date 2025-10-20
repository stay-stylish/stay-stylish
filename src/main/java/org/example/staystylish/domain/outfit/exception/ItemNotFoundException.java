package org.example.staystylish.domain.outfit.exception;

/**
 * 아이템을 찾을 수 없을 때 발생하는 예외 클래스입니다.
 */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {

        super(message);
    }
}
