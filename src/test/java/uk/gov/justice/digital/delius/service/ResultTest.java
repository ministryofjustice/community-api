package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResultTest {

    @Nested
    class Get {
        @Test
        void noValueWillThrowException() {
            assertThatThrownBy(() -> Result.ofError(new FileNotFoundException()).get()).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void whenHasValueTheValueIsReturned() {
            assertThat(Result.of("Hello World").get()).isEqualTo("Hello World");
        }
    }

    @Nested
    class onError {
        class MyException extends RuntimeException {
            public MyException(String message, Throwable e) {
                super(message, e);
            }
        }
        @Nested
        class WithError {
            @Test
            void willThrowConvertException() {
                assertThatThrownBy(() -> Result.ofError(new FileNotFoundException())
                        .onError(e -> {throw new MyException("All gone wrong", e);}))
                        .isInstanceOf(MyException.class)
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("All gone wrong")
                        .hasRootCauseInstanceOf(FileNotFoundException.class);
            }
        }
        @Nested
        class WithValue {
            @Test
            void willReturnValue() {
                val value = Result.of("Hello World")
                        .onError(e -> {throw new MyException("All gone wrong", e);});

                assertThat(value).isEqualTo("Hello World");
            }

        }
    }
}