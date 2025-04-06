package sf.hw;

import sf.hw.observable.Observable;
import sf.hw.observer.Observer;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onComplete();
        });
        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
                System.out.println(item);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
                observable.unsubscribe(this);
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
                observable.unsubscribe(this);
            }
        });
        // Когда подписка больше не нужна:
        System.out.println(observable.getDisposables().size());
    }
}

