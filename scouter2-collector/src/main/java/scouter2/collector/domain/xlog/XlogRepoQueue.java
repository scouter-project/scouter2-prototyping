package scouter2.collector.domain.xlog;

import lombok.extern.slf4j.Slf4j;
import scouter2.collector.common.ScouterConfig;
import scouter2.common.collection.PurgingQueue;
import scouter2.proto.Xlog;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class XlogRepoQueue {
    private ScouterConfig conf = ScouterConfig.getInstance();
    private PurgingQueue<Xlog> queue = new PurgingQueue<>(conf.getXlogQueueSize());

    public void offer(Xlog xlog) {
        boolean success = queue.offerOverflowClear(xlog);
        if (!success) {
            //TODO logging
        }
    }

    public Xlog take() throws InterruptedException {
        return queue.take();
    }
}
