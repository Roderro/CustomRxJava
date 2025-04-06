package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import sf.hw.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ObserveOnObserverTest extends AbstractObserverTest {
    private Scheduler scheduler;

    @BeforeEach
    void childSetup() {
        scheduler = Mockito.mock(Scheduler.class);
        testObserver = new ObserveOnObserver<>(disposable, downstream, scheduler);
        doAnswer(invocation -> {
            ObserveOnObserver<Integer> testObserverObserveOn = (ObserveOnObserver<Integer>) testObserver;
            testObserverObserveOn.run();
            return null;
        }).when(scheduler).execute(any(Runnable.class));

    }

    @Test
    @DisplayName("Планирует доставку onNext в указанном Scheduler")
    void shouldDeliverItemsOnScheduler() {
        // Act
        testObserver.onNext(1);
        // Assert
        verify(scheduler, times(1)).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Планирует доставку onComplete в указанном Scheduler")
    void shouldDeliverCompleteOnScheduler() {
        testObserver.onComplete();
        verify(scheduler, times(1)).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Планирует доставку onError в указанном Scheduler")
    void shouldDeliverErrorOnScheduler() {
        testObserver.onError(new RuntimeException());
        verify(scheduler, times(1)).execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Не доставляет события после dispose()")
    void shouldNotDeliverWhenDisposed() {
        when(disposable.isDisposed()).thenReturn(true);
        testObserver.onNext(1);
        testObserver.onError(new RuntimeException());
        testObserver.onComplete();
        verify(scheduler, never()).execute(any());
    }

    @Test
    @DisplayName("Планирует все события (onNext, onComplete) в правильном порядке")
    void shouldDeliverAllOnScheduler() {
        // Act
        testObserver.onNext(1);
        testObserver.onNext(2);
        testObserver.onComplete();
        // Assert
        verify(scheduler, times(3)).execute(any(Runnable.class));

        InOrder inOrder = inOrder(downstream);
        inOrder.verify(downstream).onNext(1);
        inOrder.verify(downstream).onNext(2);
        inOrder.verify(downstream).onComplete();
    }
}

