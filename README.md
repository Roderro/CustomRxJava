# Архитектура реализованной системы

Реализованная система представляет собой упрощенную версию реактивного фреймворка, аналогичного RxJava.

## Основные компоненты системы

### Интерфейсы и базовые классы:

- **`Observable<T>`**: Абстрактный класс, представляющий источник данных. Реализует паттерн "Наблюдатель" (Observer) и
  предоставляет методы для создания и преобразования потоков данных.
- **`Observer<T>`**: Интерфейс, определяющий методы для обработки событий (`onNext`, `onError`, `onComplete`).
- **`Disposable`**: Интерфейс для управления подписками и их отмены.
- **`Scheduler`**: Интерфейс для управления многопоточностью, позволяющий выполнять задачи в разных потоках.

### Операторы:

- **`map`**: Преобразует элементы потока с помощью функции.
- **`filter`**: Фильтрует элементы потока по условию.
- **`flatMap`**: Преобразует элементы в новые потоки и объединяет их в один.
- **`subscribeOn`**: Указывает, в каком потоке выполнять подписку.
- **`observeOn`**: Указывает, в каком потоке получать данные.

### Реализации Observable:

- **`ObservableStandard`**: Базовый Observable, создаваемый через `create`.
- **`ObservableMap`**, **`ObservableFilter`**, **`ObservableFlatMap`**: Реализации операторов преобразования.
- **`ObservableSubscribeOn`**, **`ObservableObserveOn`**: Реализации для управления потоками.

### Реализации Observer:

- **`StandardObserver`**: Базовая реализация Observer.
- **`MapObserver`**, **`FilterObserver`**, **`FlatMapObserver`**: Обертки для обработки операторов.
- **`ObserveOnObserver`**: Реализация для асинхронной доставки событий.

### Schedulers:

- **`ComputationScheduler`**: Использует фиксированный пул потоков для вычислительных задач.
- **`IOThreadScheduler`**: Использует кэшируемый пул потоков для I/O операций.
- **`SingleThreadScheduler`**: Использует один поток для последовательного выполнения задач.

### Тестирование:

- Юнит-тесты для `Observer` и операторов.
- Интеграционные тесты для проверки работы цепочек операторов и многопоточности.

## Принципы работы Schedulers, их различия и области применения

**`Scheduler`**:

- Использует `SchedulerTreadFactory` для создания потоков.
- Определяет, в каком потоке выполняются задачи.
- Основной метод — `execute(Runnable task)`, который запускает задачу в соответствующем потоке.

**`SchedulerTreadFactory`**:
- создает демон-потоки
- Устанавливает потокам имена(`ComputationScheduler` - "Computationscheduler-worker-threadNumber", `IOThreadScheduler` - "IO-scheduler-worker-threadNumber", `SingleThreadScheduler` - "SingleThread-scheduler-worker-threadNumber" )


### Типы Schedulers:

**`ComputationScheduler`**:

- Использует фиксированный пул потоков (количество равно числу ядер процессора).
- Применение: Для CPU-интенсивных задач (например, сложные вычисления).

**`IOThreadScheduler`**:

- Использует кэшируемый пул потоков (создает новые потоки по мере необходимости).
- Применение: Для I/O операций (например, работа с сетью или файлами).

**`SingleThreadScheduler`**:

- Использует один поток для всех задач.
- Применение: Для последовательного выполнения задач (например, обновление UI).

### Различия:

- `ComputationScheduler` ограничен числом ядер, что предотвращает перегрузку CPU.
- `IOThreadScheduler` масштабируется под нагрузку, но может создавать много потоков.
- `SingleThreadScheduler` гарантирует порядок выполнения задач.

### Примеры использования:

- `subscribeOn(new IOScheduler())`: Для подписки на данные из сети.
- `observeOn(new SingleThreadScheduler())`: Для обновления UI в главном потоке.

## Процесс тестирования и основные сценарии

### Юнит-тесты:

Проверяют отдельные компоненты (`Observer`, операторы).

**Примеры**:

- `MapObserverTest`: Проверка преобразования элементов.
- `FilterObserverTest`: Проверка фильтрации элементов.
- `ObserveOnObserverTest`: Проверка асинхронной доставки событий.

### Интеграционные тесты:

Проверяют работу цепочек операторов и многопоточность.

**Примеры**:

- `ObservableIT.shouldProcessChainOfOperators`: Проверка цепочки `filter` + `map`.
- `ObservableIT.shouldHandleThreadSwitching`: Проверка переключения потоков между `subscribeOn` и `observeOn`.
- `ObservableIT.shouldHandleUnsubscription`: Проверка отписки.

### Основные сценарии:

1. Эмиссия элементов и завершение потока.
2. Обработка ошибок.
3. Преобразование и фильтрация данных.
4. Многопоточная доставка событий.
5. Отписка и управление ресурсами.

## Примеры использования библиотеки
## ⚠️ Важное предупреждение

**Важно отписывать неиспользуемые Observer от Observable** для избежания утечек памяти!

### Почему это критично:
- 🚨 Неотписанные Observer могут продолжать получать события
- 💾 Удерживают ссылки на объекты в памяти
- 📈 Могут привести к постепенному росту потребления памяти

### Как правильно:
```java
// При создание Observable сохраняем на него ссылку
Observable<String> observable = Observable.create(emitter);
// При создание Observer сохраняем на него ссылку
Observer<String> observer = new Observer<>();
// Когда Observer больше не нужен
observable.unsubscribe(observer);
```

### Создание Observable и observer подписка и отписка:

```java
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
```
### Цепочка операторов:
```java
Observable<String> observable = Observable.<String>create(emitter -> {
          emitter.onNext("Hello");
          emitter.onNext("World");
          emitter.onComplete();
        })
        .filter(str -> str.startsWith("H"))
        .map(str -> str.substring(2));
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
```
### Многопоточность:
```java
Observable<String> observable = Observable.<String>create(emitter -> {
    emitter.onNext("Hello");
    emitter.onNext("World");
    emitter.onComplete();
  })
  .subscribeOn(new IOScheduler())
  .filter(str -> str.startsWith("H"))
  .observeOn(new ComputationScheduler(4))
  .map(str -> str.substring(2));
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
Thread.sleep(500);
// Когда подписка больше не нужна:
observable.unsubscribe(observer);
    
```
### FlatMap:
```java
Observable<String> observable = Observable.<String>create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onComplete();
        })
        .subscribeOn(new IOScheduler())
        .filter(str -> str.startsWith("H"))
        .observeOn(new ComputationScheduler(4))
        .map(str -> str.substring(2))
        .flatMap(x -> Observable.create(e -> {
            e.onNext(x + "oneNext");
            e.onNext(x + "twoNext");
            e.onComplete();
        }));
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
Thread.sleep(500);
// Когда подписка больше не нужна:
observable.unsubscribe(observer);
```
# Заключение

Реализованная система предоставляет:

## Основные возможности
- ✅ **Гибкий инструмент** для работы с асинхронными потоками данных
- ✅ Поддержку **основных операторов**:
  - `map` - преобразование элементов
  - `filter` - фильтрация элементов
  - `flatMap` - преобразование с раскрытием вложенных потоков
- ✅ **Управление многопоточностью** через Schedulers:
  - `subscribeOn` - задает поток для выполнения источника
  - `observeOn` - задает поток для получения данных
- ✅ **Механизм отписки observer от observable**:
  - Функция `unsubscribe()` у `Observable`
  - Автоматическая очистка ресурсов

## Ключевые рекомендации по использованию

### Управление ресурсами
🔹 **Всегда сохраняйте ссылку на observable и observer** для возможности отписаться и освободить ресурсы:
```java
// Подписка observer на observable
observable.subscribe(observer);
// Когда подписка больше не нужна:
observable.unsubscribe(observer);
```
