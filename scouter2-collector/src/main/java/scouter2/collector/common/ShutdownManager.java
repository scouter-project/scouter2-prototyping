package scouter2.collector.common;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
@Slf4j
public class ShutdownManager {

    private static ShutdownManager instance = new ShutdownManager();
    MutableList<ShutdownHook> shutdownHooks = Lists.mutable.empty();

    private ShutdownManager() {}

    public static ShutdownManager getInstance() {
        return instance;
    }

    public void register(ShutdownHook shutdownHook) {
        shutdownHooks.add(shutdownHook);
    }

    public synchronized void shutdown() {
        if (shutdownHooks.size() == 0) return;
        log.info("Collector on shutdown.");
        shutdownHooks.forEach(ShutdownHook::shutdown);
        shutdownHooks.clear();
    }
}
