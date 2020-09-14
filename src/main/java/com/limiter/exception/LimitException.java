package com.limiter.exception;

/**
 * @author yangge
 * @version 1.0.0
 * @title: LimitException
 * @date 2020/9/10 14:14
 */
public class LimitException extends RuntimeException {

    public LimitException() {
        super();
    }

    public LimitException(String message) {
        super(message);
    }

    public LimitException(String message, Throwable cause) {
        super(message, cause);
    }


}
