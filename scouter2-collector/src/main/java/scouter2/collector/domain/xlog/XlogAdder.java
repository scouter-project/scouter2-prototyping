package scouter2.collector.domain.xlog;

import scouter2.collector.common.ScouterConfig;
import scouter2.proto.Xlog;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
public class XlogAdder {

    ScouterConfig conf;
    XlogRepoQueue repoQueue;

    public XlogAdder(ScouterConfig conf, XlogRepoQueue repoQueue) {
        this.conf = conf;
        this.repoQueue = repoQueue;
    }

    public void addXlog(Xlog xlog) {
        repoQueue.offer(xlog);
    }
}
