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
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.infrastructure.db.filedb.GxidMapping;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.collection.LruList;
import scouter2.common.lang.ByteArrayKeyMap2;
import scouter2.common.util.ThreadUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/08/2019
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalXlogGxidBuffer extends Thread {

    LruList<GxidMapping> innerBuffer;
    ByteArrayKeyMap2<GxidMapping> gxidMap;

    ConfigXlog configXlog;

    public LocalXlogGxidBuffer(ConfigXlog configXlog) {
        this.configXlog = configXlog;
        this.innerBuffer = new LruList<>(configXlog.get_xlogGxidWriteBufferSize());
        this.gxidMap = new ByteArrayKeyMap2<>();

        ShutdownManager.getInstance().register(this::flushAll);

        GxidLocalStoreBufferFlushScheduler thread = new GxidLocalStoreBufferFlushScheduler(this, configXlog);
        thread.setDaemon(true);
        thread.setName(ThreadUtil.getName(thread.getClass()));
        thread.start();
    }

    public void add(byte[] gxid, byte[] txid, long timestamp, HTreeMap<byte[], MutableList<byte[]>> gxidIndex) {
        GxidMapping mapping = gxidMap.get(gxid);

        if (mapping == null) {
            mapping = new GxidMapping(gxid, Lists.mutable.empty(), timestamp, gxidIndex);
            gxidMap.put(gxid, mapping);
            innerBuffer.addLast(mapping);
        }

        if (Arrays.equals(gxid, txid)) {
            mapping.registerEntry();
            return;
        }
        mapping.getTxidList().add(txid);
    }

    public @Nullable MutableList<byte[]> getTxidListFromGxid(byte[] gxid) {
        GxidMapping gxidMapping = gxidMap.get(gxid);
        if (gxidMapping == null) {
            return null;
        }
        return gxidMapping.getTxidList();
    }

    public void flushAll() {
        long now = U.now();
        while (true) {
            GxidMapping mapping = innerBuffer.pollFirst();
            if (mapping == null) {
                break;
            }
            gxidMap.remove(mapping.getGxid());
            add2Db(mapping, now);
        }
    }

    public void flushInSec(int bufferSec) {
        long now = U.now();
        List<GxidMapping> denyList = new ArrayList<>();
        while (true) {
            GxidMapping mapping = innerBuffer.pollFirst();
            if (mapping == null) {
                break;
            }
            if (mapping.getTimestamp() + bufferSec > now) {
                innerBuffer.addFirst(mapping);
                break;
            }
            gxidMap.remove(mapping.getGxid());
            if (!add2Db(mapping, now)) {
                denyList.add(mapping);
            }
        }

        for (int i = denyList.size() - 1; i >= 0; i--) {
            GxidMapping denied = denyList.get(i);
            innerBuffer.addFirst(denied);
            gxidMap.put(denied.getGxid(), denied);
        }
    }

    private boolean add2Db(GxidMapping mapping, long now) {
        if (mapping.isEntryIdRegistered()) {
            if (!mapping.getTxidList().isEmpty()) {
                mapping.getGxidIndex().put(mapping.getGxid(), mapping.getTxidList());
            }
            return true;

        } else {
            if (mapping.getTimestamp() + (300 * 1000) < now) {
                MutableList<byte[]> txidList = mapping.getGxidIndex().get(mapping.getGxid());
                if (txidList == null) {
                    txidList = Lists.mutable.empty();
                }
                txidList.addAll(mapping.getTxidList());
                mapping.getGxidIndex().put(mapping.getGxid(), txidList);

                return true;
            }
            return false;
        }
    }


    private static class GxidLocalStoreBufferFlushScheduler extends Thread {
        LocalXlogGxidBuffer buffer;
        ConfigXlog configXlog;

        public GxidLocalStoreBufferFlushScheduler(LocalXlogGxidBuffer buffer, ConfigXlog configXlog) {
            this.buffer = buffer;
            this.configXlog = configXlog;
        }

        @Override
        public void run() {
            ThreadUtil.sleep(configXlog.get_xlogGxidWriteBufferKeepMillis());
            while (CoreRun.isRunning()) {
                buffer.flushInSec(configXlog.get_xlogGxidWriteBufferKeepMillis());
                ThreadUtil.sleep(100);
            }
        }
    }
}
