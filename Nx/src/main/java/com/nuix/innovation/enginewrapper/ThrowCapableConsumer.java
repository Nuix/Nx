package com.nuix.innovation.enginewrapper;

/***
 * An interface similar to {@link java.util.function.Consumer}, but allows for throwing checked exceptions.
 * @param <T> The type of the argument accepted.
 */
@FunctionalInterface
public interface ThrowCapableConsumer<T> {
    void accept(T value) throws Exception;
}
