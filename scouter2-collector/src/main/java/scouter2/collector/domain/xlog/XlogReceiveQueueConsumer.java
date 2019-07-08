package scouter2.collector.domain.xlog;

import scouter2.collector.common.ScouterConfig;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.Xlog;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
public class XlogReceiveQueueConsumer extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static XlogReceiveQueueConsumer instance;

    private ScouterConfig conf;
    private XlogReceiveQueue xlogReceiveQueue;
    private XlogAdder xlogAdder;

    public synchronized static XlogReceiveQueueConsumer start(ScouterConfig conf,
                                                              XlogReceiveQueue xlogReceiveQueue,
                                                              XlogAdder xlogAdder) {
        if (instance != null) {
            throw new RuntimeException("Already working xlog consumer exists.");
        }
        instance = new XlogReceiveQueueConsumer(conf, xlogReceiveQueue, xlogAdder);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;
    }

    private XlogReceiveQueueConsumer(ScouterConfig conf, XlogReceiveQueue xlogReceiveQueue, XlogAdder xlogAdder) {
        this.conf = conf;
        this.xlogReceiveQueue = xlogReceiveQueue;
        this.xlogAdder = xlogAdder;
    }

    @Override
    public void run() {
        try {
            Xlog xlog = xlogReceiveQueue.take();
            xlogAdder.addXlog(xlog);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO consume
    }
}
