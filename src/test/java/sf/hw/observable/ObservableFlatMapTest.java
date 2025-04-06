package sf.hw.observable;

import org.mockito.Mockito;
import sf.hw.observer.FlatMapObserver;
import sf.hw.observer.Observer;


import java.util.function.Function;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class ObservableFlatMapTest extends AbstractObservableTest {
    private final Function<String, Observable<String>> MAPPER = val -> {
        Observable<String> observable = Mockito.mock(Observable.class);
        doAnswer(inc -> {
            Observer<String> observer = inc.getArgument(0);
            observer.onNext(val);
            return null;
        }).when(observable).subscribe(any(Observer.class));
        return observable;
    };


    @Override
    protected Class<? extends Observer> getExpectedWrapperClass() {
        return FlatMapObserver.class;
    }


    @Override
    Observable<String> getTestObservable() {
        return  new ObservableFlatMap<>(parentObservable, MAPPER);
    }

}
