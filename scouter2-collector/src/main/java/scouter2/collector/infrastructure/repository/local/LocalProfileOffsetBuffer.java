/*
 * Copyright 2019. The Scouter2 Authors.
 *
 *  @https://github.com/scouter-project/scouter2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scouter2.collector.infrastructure.repository.local;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.infrastructure.db.filedb.TxidProfileOffsetMapping;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.collector.springconfig.ThreadNameDecorator;
import scouter2.common.collection.LruList;
import scouter2.common.lang.ByteArrayKeyMap2;
import scouter2.common.util.ThreadUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/08/2019
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalProfileOffsetBuffer extends Thread {

    public static final ThrottleConfig S_0065 = ThrottleConfig.of("S0065");

    LruList<TxidProfileOffsetMapping> innerBuffer;
    ByteArrayKeyMap2<TxidProfileOffsetMapping> txidMap;

    ConfigXlog configXlog;

    public LocalProfileOffsetBuffer(ConfigXlog configXlog) {
        this.configXlog = configXlog;
        this.innerBuffer = new LruList<>(configXlog.get_xlogProfileOffsetWriteBufferSize());
        this.txidMap = new ByteArrayKeyMap2<>();

        ShutdownManager.getInstance().register(this::flushAll);

        ProfileOffsetLocalStoreBufferFlushScheduler thread =
                new ProfileOffsetLocalStoreBufferFlushScheduler(this, configXlog);
        thread.setDaemon(true);
        thread.setName(ThreadUtil.getName(thread.getClass()));
        thread.start();
    }

    public void add(byte[] txid, long offset, long timestamp, HTreeMap<byte[], LongList> txidProfileOffsetIndex) {
        TxidProfileOffsetMapping mapping = txidMap.get(txid);
        if (mapping == null) {
            mapping = new TxidProfileOffsetMapping(txid, new LinkedBlockingQueue<>(), timestamp, txidProfileOffsetIndex);
            txidMap.put(txid, mapping);
            innerBuffer.addLast(mapping);
        }

        mapping.getOffsetQueue().offer(offset);
    }

    public @Nullable MutableLongList getOffsetList(byte[] txid) {
        TxidProfileOffsetMapping mapping = txidMap.get(txid);
        if (mapping == null) {
            return null;
        }
        return queue2List(mapping.getOffsetQueue());
    }

    public void flushAll() {
        while (true) {
            TxidProfileOffsetMapping mapping = innerBuffer.pollFirst();
            if (mapping == null) {
                break;
            }
            txidMap.remove(mapping.getTxid());
            add2Db(mapping);
        }
    }

    public void flushInSec(int bufferSec) {
        long now = U.now();
        List<TxidProfileOffsetMapping> denyList = new ArrayList<>();
        while (true) {
            TxidProfileOffsetMapping mapping = innerBuffer.pollFirst();
            if (mapping == null) {
                break;
            }
            if (mapping.getTimestamp() + bufferSec > now) {
                innerBuffer.addFirst(mapping);
                break;
            }
            txidMap.remove(mapping.getTxid());
            add2Db(mapping);
        }
    }

    private void add2Db(TxidProfileOffsetMapping mapping) {
        mapping.getTxidProfileOffsetIndex().put(mapping.getTxid(), queue2List(mapping.getOffsetQueue()));
    }

    private MutableLongList queue2List(LinkedBlockingQueue<Long> queue) {
        Object[] gxids = queue.toArray();
        MutableLongList gxidList = LongLists.mutable.empty();
        for (Object gxid : gxids) {
            gxidList.add((long) gxid);
        }
        return gxidList;
    }

    private static class ProfileOffsetLocalStoreBufferFlushScheduler extends Thread {
        LocalProfileOffsetBuffer buffer;
        ConfigXlog configXlog;

        public ProfileOffsetLocalStoreBufferFlushScheduler(LocalProfileOffsetBuffer buffer, ConfigXlog configXlog) {
            this.buffer = buffer;
            this.configXlog = configXlog;
        }

        @Override
        public void run() {
            ThreadUtil.sleep(configXlog.get_xlogProfileOffsetWriteBufferKeepMillis());
            while (CoreRun.isRunning()) {
                try {
                    ThreadNameDecorator.run(() ->
                            buffer.flushInSec(configXlog.get_xlogProfileOffsetWriteBufferKeepMillis()));

                    ThreadUtil.sleep(100);

                } catch (Exception e) {
                    log.error(e.getMessage(), S_0065, e);
                }
            }
        }
    }
}
