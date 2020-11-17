package uk.gov.justice.digital.delius.service;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class Result<T, X extends Throwable> {
    private T value;
    private X error;

    public static <T, X extends Throwable> Result<T, X> of(T value) {
        return new Result<>(value);
    }
    public static <T, X extends Throwable> Result<T, X> ofError(X error) {
        return new Result<>(error);
    }
    private Result(T value) {
        this.value = value;
    }
    private Result(X error) {
        this.error = error;
    }

    public T onError(Function<X, ? extends RuntimeException> exceptionSupplier) {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.apply(error);
        }
    }

    public T get() {
        if (value != null) {
            return value;
        } else {
            throw new NoSuchElementException("Result contains no value, did you call get() instead of onError?");
        }
    }
    public X getError() {
        if (error != null) {
            return error;
        } else {
            throw new NoSuchElementException("Result contains no error, did you call getError() instead of onError?");
        }
    }
    public boolean isError() {
        return error != null;
    }
}
