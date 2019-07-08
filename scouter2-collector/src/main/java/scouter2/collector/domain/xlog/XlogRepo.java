package scouter2.collector.domain.xlog;

import scouter2.proto.Xlog;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface XlogRepo {
    void add(Xlog xlog);
}
