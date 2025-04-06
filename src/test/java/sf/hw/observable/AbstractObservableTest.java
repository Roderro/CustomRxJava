package sf.hw.observable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sf.hw.observer.Disposable;
import sf.hw.observer.Observer;
import sf.hw.observer.StandardDisposable;
import sf.hw.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

abstract class AbstractObservableTest {
    Observable<String> testObservable;
    Observable<String> parentObservable;
    Observer<String> observer;
    Emitting<String> emittingSource;
    AtomicInteger emissionsCount;
    AtomicReference<String> lastValue;
    AtomicReference<Throwable> error;
    AtomicBoolean completed;
    Disposable disposable;

    @BeforeEach
    void setUp() {
        emissionsCount = new AtomicInteger(0);
        lastValue = new AtomicReference<>();
        error = new AtomicReference<>();
        completed = new AtomicBoolean(false);
        parentObservable = getParentObservable();
        emittingSource = getEmittingSource();
        observer = getTestObserver();
        disposable = new StandardDisposable();
        testObservable = getTestObservable();
    }

    @AfterEach
    void afterEach() {
        Observable.disposableMap.clear();
    }

    abstract Class<? extends Observer> getExpectedWrapperClass();

    abstract Observable<String> getTestObservable();

    Emitting<String> getEmittingSource() {
        return observer -> {
            observer.onNext("Hello");
            observer.onNext("World");
            observer.onComplete();
        };
    }

    Observer<String> getTestObserver() {
        return new Observer<>() {
            @Override
            public void onNext(String value) {
                emissionsCount.incrementAndGet();
                lastValue.set(value);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }
        };
    }

    Observable<String> getParentObservable() {
        Observable<String> parentObservable = mock(Observable.class);
        doAnswer(inv -> {
            Observer<String> testObserver = inv.getArgument(0);
            emittingSource.emit(testObserver);
            return null;
        }).when(parentObservable).subscribeActual(any(Observer.class));
        return parentObservable;
    }

    Scheduler getScheduler() {
        Scheduler mockScheduler = Mockito.mock(Scheduler.class);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).execute(any(Runnable.class));
        return mockScheduler;
    }

    @Test
    @DisplayName("Создание Observable с помощью метода create()")
    void shouldCreateObservableWithCreateMethod() {
        Observable<String> obs = Observable.create(emittingSource);
        assertNotNull(obs);
        assertTrue(obs instanceof ObservableStandard);
    }

    @Test
    @DisplayName("Подписка вызывает методы onNext, onComplete у Observer")
    void subscribe_shouldCallObserverMethods() {
        testObservable.subscribe(observer);
        assertEquals(2, emissionsCount.get());
        assertEquals("World", lastValue.get());
        assertTrue(completed.get());
        assertNull(error.get());
    }


    @Test
    @DisplayName("Отписка удаляет Observer из disposableMap")
    void unsubscribe_shouldRemoveObserver() {
        testObservable.subscribe(observer);
        assertNotNull(testObservable.getDisposable(observer));
        testObservable.unsubscribe(observer);
        assertNull(testObservable.getDisposable(observer));
    }


    @Test
    @DisplayName("Обработка ошибок в Observable (onError)")
    void subscribe_shouldHandleErrors() {
        RuntimeException testError = new RuntimeException("Test error");
        Emitting<String> errorSource = observer -> {
            observer.onNext("First");
            throw testError;
        };
        Observable<String> errorObservable = Observable.create(errorSource);
        errorObservable.subscribe(observer);

        assertEquals(1, emissionsCount.get());
        assertEquals("First", lastValue.get());
        assertFalse(completed.get());
        assertEquals(testError, error.get());
    }


    @Test
    @DisplayName("Проверка обертки Observer в нужный класс")
    void shouldWrapObserver() {
        testObservable.subscribe(observer);
        verify(parentObservable)
                .subscribeActual(argThat(arg -> getExpectedWrapperClass().isInstance(arg)));
    }

    @Test
    @DisplayName("Оператор map преобразует значения")
    void map_shouldTransformValues() {
        testObservable.map(String::toUpperCase).subscribe(observer);
        assertEquals(2, emissionsCount.get());
        assertEquals("WORLD", lastValue.get());
    }

    @Test
    @DisplayName("Оператор filter фильтрует значения")
    void filter_shouldFilterValues() {
        testObservable.filter(s -> s.startsWith("H")).subscribe(observer);
        assertEquals(1, emissionsCount.get());
        assertEquals("Hello", lastValue.get());
    }

    @Test
    @DisplayName("Удаление всех Observer при отписке")
    void delAllObservers_shouldRemoveAllObserversWithGivenDisposable() {
        Observer<String> observer1 = getTestObserver();
        Observer<String> observer2 = getTestObserver();

        testObservable.subscribe(observer1);
        testObservable.subscribe(observer2);
        assertTrue(Observable.disposableMap.size() >= 2);
        testObservable.unsubscribe(observer1);
        testObservable.unsubscribe(observer2);
        assertTrue(Observable.disposableMap.isEmpty());
    }
}