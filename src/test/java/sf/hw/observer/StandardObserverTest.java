package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;

class StandardObserverTest extends AbstractObserverTest {


    @BeforeEach
    void childSetup() {
        testObserver = new StandardObserver<>(disposable, downstream);
    }

}