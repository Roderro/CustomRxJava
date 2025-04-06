package sf.hw.observable;

import sf.hw.observer.Observer;

@FunctionalInterface
public interface Emitting<T> {
   void emit(Observer<? super T> observer);
}
