package scouter2.collector.domain.xlog;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import scouter2.collector.common.ScouterConfig;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.Xlog;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
public class XlogRepoQueueConsumer extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static ImmutableList<XlogRepoQueueConsumer> instances;

    ScouterConfig conf;
    XlogRepoQueue xlogRepoQueue;
    XlogRepo xlogRepo;

    public synchronized static ImmutableList<XlogRepoQueueConsumer> start(@Nonnull ScouterConfig conf,
                                                                          @Nonnull XlogRepoQueue xlogRepoQueue,
                                                                          @Nonnull XlogRepo xlogRepo) {

        if (instances != null) {
            throw new RuntimeException("Already working xlog repo queue consumer exists.");
        }

        instances = IntInterval.zeroTo(getConsumerCount(conf, xlogRepo) - 1)
                .collect(n -> createXlogRepoQueueConsumer(conf, xlogRepoQueue, xlogRepo))
                .toImmutable();

        return instances;
    }

    private static XlogRepoQueueConsumer createXlogRepoQueueConsumer(ScouterConfig conf, XlogRepoQueue xlogRepoQueue, XlogRepo xlogRepo) {
        XlogRepoQueueConsumer consumer = new XlogRepoQueueConsumer(conf, xlogRepoQueue, xlogRepo);
        consumer.setDaemon(true);
        consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
        consumer.start();

        return consumer;
    }

    /**
     * always 1 on NoneThreadSafeXlogRepo type
     */
    private static int getConsumerCount(ScouterConfig conf, XlogRepo xlogRepo) {
        return xlogRepo instanceof NoneThreadSafeXlogRepo ? 1 : conf.getXlogRepoThreadCount();
    }

    private XlogRepoQueueConsumer(ScouterConfig conf, XlogRepoQueue xlogRepoQueue, XlogRepo xlogRepo) {
        this.conf = conf;
        this.xlogRepoQueue = xlogRepoQueue;
        this.xlogRepo = xlogRepo;
    }

    @Override
    public void run() {
        try {
            Xlog xlog = xlogRepoQueue.take();
            xlogRepo.add(xlog);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
