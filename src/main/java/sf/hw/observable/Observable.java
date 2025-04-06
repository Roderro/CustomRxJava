package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.Observer;
import sf.hw.observer.StandardDisposable;
import sf.hw.scheduler.Scheduler;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;


public abstract class Observable<T> {
    private final Map<Observer<?>, Disposable> disposableMap;

    protected Observable(Map<Observer<?>, Disposable> disposableMap) {
        this.disposableMap = disposableMap;
    }


    public static <T> Observable<T> create(Emitting<T> source) {
        return new ObservableStandard<>(source);
    }

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new ObservableMap<>(this, mapper);
    }

    public Observable<T> filter(Predicate<? super T> filter) {
        return new ObservableFilter<>(this, filter);
    }

    public final Observable<T> subscribeOn(Scheduler scheduler) {
        return new ObservableSubscribeOn<>(this, scheduler);
    }

    public final Observable<T> observeOn(Scheduler scheduler) {
        return new ObservableObserveOn<>(this, scheduler);
    }

    public final <R> Observable<R> flatMap(Function<? super T, ? extends Observable<? extends R>> mapper) {
        return new ObservableFlatMap<>(this, mapper);
    }

    public void subscribe(Observer<? super T> observer) {
        if (!disposableMap.containsKey(observer)) {
            Disposable disposable = new StandardDisposable();
            chainDisposable(disposable, observer);
        }
        subscribeActual(observer);
    }

    protected void subscribeWithDisposable(Observer<? super T> observer, Disposable disposable) {
        chainDisposable(disposable, observer);
        subscribeActual(observer);
    }

    public void unsubscribe(Observer<? super T> observer) {
        if (disposableMap.containsKey(observer)) {
            Disposable disposable = disposableMap.get(observer);
            disposable.dispose();
            delAllObservers(disposable);
        } else {
            System.out.println("This Observer is not subscribed");
        }

    }

    public Map<Observer<?>, Disposable> getDisposables() {
        return disposableMap;
    }


    protected abstract void subscribeActual(Observer<? super T> observer);

    protected Disposable getDisposable(Observer<? super T> observer) {
        return disposableMap.get(observer);
    }

    protected void chainDisposable(Disposable disposable, Observer<? super T> observer) {
        disposableMap.put(observer, disposable);
    }

    protected void delAllObservers(Disposable targetDisposable) {
        disposableMap.entrySet().removeIf(entry -> targetDisposable.equals(entry.getValue()));
    }
}