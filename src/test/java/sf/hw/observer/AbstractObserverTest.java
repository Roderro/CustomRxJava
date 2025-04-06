package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.mockito.Mockito.*;

abstract class AbstractObserverTest {
    Disposable disposable;
    Observer<Integer> downstream;
    Observer<Integer> testObserver;

    @BeforeEach
    void baseSetup() {
        disposable = Mockito.mock(Disposable.class);
        downstream = Mockito.mock(Observer.class);
    }

    @Test
    @DisplayName("Передает onNext событие downstream-наблюдателю")
    void observerShouldLowerNext() {
        testObserver.onNext(1);
        verify(downstream, times(1)).onNext(any());
    }

    @Test
    @DisplayName("Передает все события (onNext, onComplete) downstream-наблюдателю")
    void observerShouldLowerAllEvents() {
        testObserver.onNext(1);
        testObserver.onNext(2);
        testObserver.onComplete();

        // Assert
        verify(downstream, times(2)).onNext(any());
        verify(downstream, times(1)).onComplete();
        verify(downstream, never()).onError(any());
    }

    @Test
    @DisplayName("Передает ошибку (onError) downstream-наблюдателю")
    void observerShouldLowerError() {
        Throwable error = new RuntimeException("Test error");
        testObserver.onError(error);
        verify(downstream, times(1)).onError(error);
        verify(downstream, never()).onComplete();
    }

    @Test
    @DisplayName("Передает завершение (onComplete) downstream-наблюдателю")
    void observerShouldLowerComplete() {
        Throwable error = new RuntimeException("Test error");
        testObserver.onError(error);
        verify(downstream, times(1)).onError(error);
        verify(downstream, never()).onComplete();
    }

    @Test
    @DisplayName("Не передает события после вызова dispose()")
    void shouldNotLowerAllEventsAfterDispose() {
        // Arrange
        when(disposable.isDisposed()).thenReturn(true);

        // Act
        testObserver.onNext(1);
        testObserver.onError(new RuntimeException());
        testObserver.onComplete();

        // Assert
        verifyNoInteractions(downstream);
        verify(disposable, atLeastOnce()).isDisposed();
    }

    @Test
    @DisplayName("Завершает работу только один раз (игнорирует дублирующие onComplete)")
    void shouldCompleteOnlyOnce() {
        // Act
        testObserver.onComplete();
        testObserver.onComplete(); // Дублирующий вызов

        // Assert
        verify(downstream, times(1)).onComplete();
    }

    @Test
    @DisplayName("Обрабатывает ошибку только один раз (игнорирует дублирующие onError)")
    void shouldErrorOnlyOnce() {
        // Act
        testObserver.onError(new RuntimeException());
        testObserver.onError(new RuntimeException());

        // Assert
        verify(downstream, times(1)).onError(any());
    }

    @Test
    @DisplayName("Передает ошибку, если onNext вызывает исключение")
    void shouldHandleErrorOnNext() {
        RuntimeException error = new RuntimeException();
        doAnswer(invocation -> {
            throw error;
        }).when(downstream).onNext(any());

        testObserver.onNext(1);
        verify(downstream, times(1)).onError(error);
    }

    @Test
    @DisplayName("Обрабатывает NullPointerException при val==null onNext")
    void shouldHandleErrorWhenNextNull() {
        testObserver.onError(null);
        verify(downstream, times(1)).onError(any(NullPointerException.class));
    }

    @Test
    @DisplayName("Обрабатывает NullPointerException при error==null onError")
    void shouldHandleErrorWhenThrowableNull() {
        testObserver.onError(null);
        verify(downstream, times(1)).onError(any(NullPointerException.class));
    }


}
