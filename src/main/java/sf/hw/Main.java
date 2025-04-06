package sf.hw;


import sf.hw.observable.Observable;
import sf.hw.observer.Observer;
import sf.hw.scheduler.ComputationScheduler;
import sf.hw.scheduler.IOScheduler;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onComplete();
        });
        Observer<String> observer = new Observer<>() {
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
                System.out.println("Completed");
            }
        };
// Подписка observer на observable
        observable.subscribe(observer);
// Когда подписка больше не нужна:
        observable.unsubscribe(observer);
    }
}

