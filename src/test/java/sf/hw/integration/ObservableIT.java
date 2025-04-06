package sf.hw.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sf.hw.observable.Emitting;
import sf.hw.observable.Observable;
import sf.hw.observer.Observer;
import sf.hw.scheduler.ComputationScheduler;
import sf.hw.scheduler.IOScheduler;
import sf.hw.scheduler.Scheduler;
import sf.hw.scheduler.SingleThreadScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;


@Tag("integration")
public class ObservableIT {
    Emitting<Integer> emitter = observer -> {
        observer.onNext(10);
        observer.onNext(20);
        observer.onNext(30);
        observer.onComplete();
    };


    private Observable<Integer> getSaveNameThreadObservable(List<String> namesThreads, CountDownLatch latch) {
        return Observable.create(emitter -> {
            namesThreads.add(Thread.currentThread().getName());
            emitter.onNext(1);
            emitter.onComplete();
            latch.countDown();
        });
    }

    private Observer<Integer> getSaveNameThreadObserver(List<String> namesThreads, CountDownLatch latch) {
        return new Observer<>() {
            @Override
            public void onNext(Integer item) {
                namesThreads.add(Thread.currentThread().getName());
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };
    }

    private Function<Integer, Integer> getSaveNameThreadMapFun(List<String> namesThreads, CountDownLatch latch) {
        return x -> {
            namesThreads.add(Thread.currentThread().getName());
            latch.countDown();
            return x;
        };
    }


    @Test
    @DisplayName("Observable должен эмитировать элементы и завершаться")
    void shouldEmitItemsAndComplete() throws InterruptedException {
        // Arrange
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        // Act
        Observable<Integer> observable = Observable.create(emitter);
        observable.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(10, 20, 30), receivedItems);
        observable.unsubscribe(observer);
    }

    @Test
    @DisplayName("Должен обрабатывать цепочку операторов (filter + map)")
    void shouldProcessChainOfOperators() throws InterruptedException {
        // Arrange
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Observer<String> observer = new Observer<>() {
            @Override
            public void onNext(String item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        // Act
        Observable<String> observable = Observable.create(emitter)
                .filter(x -> x > 15)
                .map(x -> "Value-" + x);

        observable.subscribe(observer);
        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of("Value-20", "Value-30"), results);
        observable.unsubscribe(observer);
    }


    @Test
    @DisplayName("SubscribeOn должен выполнять работу в указанном Scheduler")
    void shouldHandleSubscribeOn() throws InterruptedException {
        // Arrange
        List<String> namesThreads = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(4);
        Scheduler scheduler = new IOScheduler();

        // Act
        Observable<Integer> observable = getSaveNameThreadObservable(namesThreads, latch)
                .map(getSaveNameThreadMapFun(namesThreads, latch))
                .subscribeOn(scheduler);
        Observer<Integer> observer = getSaveNameThreadObserver(namesThreads, latch);
        observable.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Timeout waiting for all threads");
        assertEquals(3, namesThreads.size(), "Not all stages were executed");
        assertEquals(3, namesThreads.stream()
                        .filter(name -> name.startsWith("IO")).count(),
                "Expected more than 1 different threads");
        observable.unsubscribe(observer);
    }


    @Test
    @DisplayName("ObserveOn должен доставлять события в указанном Scheduler")
    void shouldHandleObserverOn() throws InterruptedException {
        // Arrange
        List<String> namesThreads = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(4);
        Scheduler scheduler = new SingleThreadScheduler();

        // Act
        Observable<Integer> observable = getSaveNameThreadObservable(namesThreads, latch)
                .map(getSaveNameThreadMapFun(namesThreads, latch))
                .observeOn(scheduler);
        Observer<Integer> observer = getSaveNameThreadObserver(namesThreads, latch);

        observable.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Timeout waiting for all threads");
        assertEquals(3, namesThreads.size(), "Not all stages were executed");
        assertEquals(2, namesThreads.stream()
                        .filter(name -> name.startsWith("main")).count(),
                "The stage is made in the wrong thread");
        assertEquals(1, namesThreads.stream()
                        .filter(name -> name.startsWith("SingleThread")).count(),
                "The stage is made in the wrong thread");
        observable.unsubscribe(observer);
    }


    @Test
    @DisplayName("Должен корректно переключать потоки между SubscribeOn и ObserveOn")
    void shouldHandleThreadSwitching() throws InterruptedException {
        // Arrange
        List<String> namesThreads = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(4);
        Scheduler subscribeScheduler = new IOScheduler();
        Scheduler observeScheduler = new ComputationScheduler(2);

        // Act
        Observable<Integer> observable = getSaveNameThreadObservable(namesThreads, latch)
                .subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler)
                .map(getSaveNameThreadMapFun(namesThreads, latch));
        Observer<Integer> observer = getSaveNameThreadObserver(namesThreads, latch);
        observable.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Timeout waiting for all threads");
        assertEquals(3, namesThreads.size(), "Not all stages were executed");
        assertEquals(1, namesThreads.stream()
                        .filter(name -> name.startsWith("IO")).count(),
                "The stage is made in the wrong thread");
        assertEquals(2, namesThreads.stream()
                        .filter(name -> name.startsWith("Computation")).count(),
                "The stage is made in the wrong thread");
        observable.unsubscribe(observer);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать отписку (unsubscribe)")
    void shouldHandleUnsubscription() throws InterruptedException {
        // Arrange
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Observable<Integer> observable = Observable.create(emitter);
        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
                if (item == 20) {
                    observable.unsubscribe(this);
                }
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
            }
        };

        // Act

        observable.subscribe(observer);

        // Assert
        assertFalse(latch.await(300, TimeUnit.MILLISECONDS));
        assertEquals(0, observable.getDisposables().size());
        assertEquals(List.of(10, 20), receivedItems);
    }


    @Test
    @DisplayName("Должен корректно обрабатывать отписку при использовании FlatMap")
    void shouldHandleUnsubscriptionWithFlatMap() throws InterruptedException {
        // Arrange
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Observable<Integer> observable = Observable.create(emitter)
                .flatMap(val -> Observable.create(emitter -> {
                    emitter.onNext(val);
                    emitter.onNext(val + 5);
                }));
        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
                if (item > 20) {
                    observable.unsubscribe(this);
                }
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
            }
        };

        // Act
        observable.subscribe(observer);

        // Assert
        assertFalse(latch.await(300, TimeUnit.MILLISECONDS));
        assertEquals(0, observable.getDisposables().size());
        assertEquals(List.of(10, 15, 20, 25), receivedItems);
    }


    @Test
    @DisplayName("Должен корректно передавать ошибки через цепочку")
    void shouldPropagateErrors() throws InterruptedException {
        // Arrange
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        RuntimeException testError = new RuntimeException("Test error");
        Emitting<Integer> errorEmitter = emitter -> {
            emitter.onNext(1);
            emitter.onError(testError);
        };
        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                assertEquals(testError, t);
                latch.countDown();
            }

            @Override
            public void onComplete() {
                fail("Should not complete");
            }
        };

        // Act
        Observable<Integer> observable = Observable.create(errorEmitter);

        observable.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(1), receivedItems);

        observable.unsubscribe(observer);
    }

    @Test
    @DisplayName("FlatMap должен разворачивать вложенные Observable")
    void shouldFlattenObservables() throws InterruptedException {
        // Arrange
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Observer<String> observer = new Observer<>() {
            @Override
            public void onNext(String item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        // Act
        Observable<String> observable = Observable.create(emitter)
                .<String>flatMap(x -> Observable.create(e -> {
                    e.onNext("A" + x);
                    e.onNext("B" + x);
                    e.onComplete();
                }));
        observable.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(6, results.size());
        assertTrue(results.containsAll(List.of("A10", "B10", "A20", "B20", "A30", "B30")));
        observable.unsubscribe(observer);
    }

    @Test
    @DisplayName("FlatMap обрабатывает ошибки во внутренних Observable")
    void flatMap_shouldHandleInnerErrors() {
        // Arrange
        RuntimeException testError = new RuntimeException("Test error");

        Function<Integer, Observable<Integer>> failingMapper = val -> {
            Observable<Integer> observable = Mockito.mock(Observable.class);
            doAnswer(inv -> {
                Observer<Integer> observer = inv.getArgument(0);
                observer.onError(testError);
                return null;
            }).when(observable).subscribe(any());
            return observable;
        };
        Observable<Integer> observable = Observable.create(emitter).flatMap(failingMapper);
        AtomicReference<Throwable> receivedError = new AtomicReference<>();
        Observer<Integer> testObserver = new Observer<>() {
            @Override
            public void onNext(Integer value) {
                fail("Не должно быть значений при ошибке");
            }

            @Override
            public void onError(Throwable throwable) {
                receivedError.set(throwable);
            }

            @Override
            public void onComplete() {
                fail("Не должно быть завершения при ошибке");
            }
        };

        // Act
        observable.subscribe(testObserver);

        // Assert
        assertEquals(testError, receivedError.get(), "Должна быть получена тестовая ошибка");
        observable.unsubscribe(testObserver);
    }

    @Test
    @DisplayName("Обработка подписки и отписки на два Observable")
    void twoObservableSubscription() throws InterruptedException {
        // Arrange
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        // Act
        Observable<Integer> observable = Observable.create(emitter);
        Observable<Integer> observable2 = Observable.create(emitter);
        observable.subscribe(observer);
        observable2.subscribe(observer);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(10, 20, 30, 10, 20, 30), receivedItems);
        observable.unsubscribe(observer);
        observable2.unsubscribe(observer);
        assertTrue(observable.getDisposables().isEmpty());
        assertTrue(observable2.getDisposables().isEmpty());
    }

    @Test
    @DisplayName("Обработка подписки и отписки двух observer на один Observable")
    void twoObserverPerOneObservableSubscription() throws InterruptedException {
        // Arrange
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        Observer<Integer> observer1 = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };
        Observer<Integer> observer2 = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item+1);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        // Act
        Observable<Integer> observable = Observable.create(emitter);
        observable.subscribe(observer1);
        observable.subscribe(observer2);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(10, 20, 30, 11, 21, 31), receivedItems);
        observable.unsubscribe(observer1);
        observable.unsubscribe(observer2);
        assertTrue(observable.getDisposables().isEmpty());
    }

}
