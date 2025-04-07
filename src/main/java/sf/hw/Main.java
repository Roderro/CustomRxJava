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
            }

            @Override
            public void onComplete() {
                //Смотри подписки
                System.out.println(observable.getDisposables().size());
                System.out.println("Completed");
            }
        });
        // Проверка автоматическую отписку
        System.out.println(observable.getDisposables().size());
    }
}

