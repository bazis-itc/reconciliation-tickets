package bazis.tasks.reconciliation_tickets.ext;

import bazis.cactoos3.Func;
import bazis.cactoos3.Scalar;
import bazis.cactoos3.iterable.IterableEnvelope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class AsyncMappedIterable<T, R> extends IterableEnvelope<R> {

    public AsyncMappedIterable(final Iterable<T> origin,
        final Func<T, R> func, final ExecutorService executor) {
        super(
            new Scalar<Iterable<R>>() {
                @Override
                public Iterable<R> value() throws Exception {
                    final Collection<Future<R>> futures = new LinkedList<>();
                    for (final T item : origin) futures.add(
                        executor.submit(
                            new Callable<R>() {
                                @Override
                                public R call() throws Exception {
                                    return func.apply(item);
                                }
                            }
                        )
                    );
                    final Collection<R> result =
                        new ArrayList<>(futures.size());
                    for (final Future<R> future : futures)
                        result.add(future.get());
                    executor.shutdown();
                    return Collections.unmodifiableCollection(result);
                }
            }
        );
    }

}
