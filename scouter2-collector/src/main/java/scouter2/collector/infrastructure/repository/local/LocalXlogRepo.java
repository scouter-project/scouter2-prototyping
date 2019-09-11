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
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.context.annotation.Conditional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.xlog.RealtimeXlogOffset;
import scouter2.collector.domain.xlog.RealtimeXlogStreamObserver;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.collector.domain.xlog.XlogLoopCache;
import scouter2.collector.domain.xlog.XlogOffset;
import scouter2.collector.domain.xlog.XlogRepo;
import scouter2.collector.domain.xlog.XlogRepoAdapter;
import scouter2.collector.domain.xlog.XlogStreamObserver;
import scouter2.collector.infrastructure.db.filedb.MinuteUnitWithSeconds;
import scouter2.collector.infrastructure.db.filedb.XlogFileDb;
import scouter2.collector.infrastructure.db.mapdb.DailySecondIndex;
import scouter2.collector.infrastructure.db.mapdb.XlogDb;
import scouter2.collector.infrastructure.db.mapdb.XlogIdIndex;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.support.XlogIdSupport;
import scouter2.common.util.DateUtil;
import scouter2.proto.XlogP;

import java.io.IOException;
import java.util.NavigableSet;
import java.util.function.Consumer;

import static scouter2.common.util.DateUtil.MILLIS_PER_DAY;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-17
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalXlogRepo extends XlogRepoAdapter implements XlogRepo, NonThreadSafeRepo {

    public static final ThrottleConfig S_0049 = ThrottleConfig.of("S0049");
    public static final ThrottleConfig S_0050 = ThrottleConfig.of("S0050");
    public static final ThrottleConfig S_0055 = ThrottleConfig.of("S0055");
    public static final ThrottleConfig S_0056 = ThrottleConfig.of("S0056");

    @Override
    public String getRepoType() {
        return LocalRepoConstant.TYPE_NAME;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    private MinuteUnitWithSeconds currentMinuteBucket;

    private XlogFileDb xlogFileDb;
    private XlogDb xlogDb;
    private ObjService instanceService;
    LocalXlogRepoCache cache;
    LocalXlogGxidBuffer gxidBuffer;

    public LocalXlogRepo(XlogFileDb xlogFileDb, XlogDb xlogDb,
                         ObjService instanceService,
                         LocalXlogRepoCache cache,
                         LocalXlogGxidBuffer gxidBuffer) {
        this.xlogFileDb = xlogFileDb;
        this.xlogDb = xlogDb;
        this.instanceService = instanceService;
        this.cache = cache;
        this.gxidBuffer = gxidBuffer;
    }

    @Override
    public void add(Xlog xlog) {
        try {
            if (xlog == null || xlog.getProto() == null) {
                return;
            }
            add2Cache(xlog);
            addToDb(xlog);

        } catch (Exception e) {
            log.error(e.getMessage(), S_0055, e);
        }
    }

    @Override
    public void stream(String applicationId, long from, long to, long maxReadCount, Consumer<XlogP> consumer) {
        MutableLongSet objIds = instanceService.findByApplicationId(applicationId)
                .collectLong(Obj::getObjId).toSet();
        streamListByPeriod0(objIds, from, to, maxReadCount, xlog -> publishMatched(applicationId, from, to, consumer, xlog));
    }

    @Override
    public void streamByObjs(String applicationId, LongSet objIds, long from, long to, long maxReadCount,
                             Consumer<XlogP> consumer) {
        streamListByPeriod0(objIds, from, to, maxReadCount, xlog -> publishMatched(applicationId, from, to, consumer, xlog));
    }

    @Override
    public void streamLatest(String applicationId,
                             @Nullable XlogOffset lastOffset,
                             int maxReadCount,
                             XlogStreamObserver streamObserver) {

        streamLatestInCache(applicationId, lastOffset, maxReadCount, streamObserver);
    }

    @Override
    public MutableList<XlogP> findXlogs(MutableSet<byte[]> xlogIds) {
        if (xlogIds == null || xlogIds.isEmpty()) {
            return Lists.mutable.empty();
        }

        MutableList<XlogP> xlogs = Lists.mutable.empty();

        String ymd = DateUtil.yyyymmdd(XlogIdSupport.timestamp(xlogIds.getAny()));
        XlogIdIndex idIndex = xlogDb.getIdIndex(ymd);
        try {
            for (byte[] id : xlogIds) {
                Long offset = idIndex.getTxidIndex().get(id);
                if (offset == null) continue;
                xlogs.add(xlogFileDb.get(ymd, offset));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), S_0056, e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return xlogs;
    }

    @Override
    public MutableList<XlogP> findXlogsByGxid(byte[] gxid) {
        String ymd = DateUtil.yyyymmdd(XlogIdSupport.timestamp(gxid));
        XlogIdIndex idIndex = xlogDb.getIdIndex(ymd);
        MutableSet<byte[]> txidList = getTxidListFromGxid(gxid, idIndex);
        return findXlogs(txidList);
    }

    protected void streamLatestInCache(String applicationId,
                                       @Nullable XlogOffset lastOffset,
                                       int maxCount,
                                       XlogStreamObserver stream) {

        XlogLoopCache loopCache = this.cache.getCache(applicationId);
        long loop = lastOffset == null ? 0 : ((RealtimeXlogOffset) lastOffset).getLoop();
        int index = lastOffset == null ? 0 : ((RealtimeXlogOffset) lastOffset).getIndex();

        loopCache.getAndHandleRealTimeXLog(null, loop, index, maxCount, new RealtimeXlogStreamObserver() {
            @Override
            public void onLoad(RealtimeXlogOffset lastOffset) {
            }

            @Override
            public void onNext(XlogP xlogP) {
                stream.onNext(xlogP);
            }

            @Override
            public void onComplete(RealtimeXlogOffset lastOffset) {
                stream.onComplete(lastOffset);
            }
        });
    }

    protected void streamLatestInDb(String applicationId,
                                    @Nullable XlogOffset lastOffset,
                                    int maxCount,
                                    XlogStreamObserver stream) {
        try {
            String yyyymmdd = DateUtil.yyyymmdd(U.now());
            long offset = lastOffset == null ? -1 : ((LocalXlogOffset) lastOffset).getOffsetValue();
            long fileOffset = xlogFileDb.findLatestOffset(yyyymmdd);
            if (offset == -1 || offset > fileOffset) {
                offset = fileOffset;
            }
            long nextOffset = xlogFileDb.readToLatest(yyyymmdd, offset, maxCount, stream::onNext);
            stream.onComplete(new LocalXlogOffset(nextOffset));

        } catch (IOException e) {
            log.error(e.getMessage(), S_0050, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void addToDb(Xlog xlog) throws IOException {
        String ymd = DateUtil.yyyymmdd(xlog.getTimestamp());
        long offset = xlogFileDb.add(ymd, xlog);
        if (offset >= 0) {
            indexing(ymd, xlog, offset);
        }
    }

    private void add2Cache(Xlog xlog) {
        XlogLoopCache loopCache = this.cache.getCache(xlog.getApplicationId());
        loopCache.add(xlog.getProto());
    }

    private void indexing(String ymd, Xlog xlog, long offset) {
        DailySecondIndex dayIndex = xlogDb.getPeriodicIndex(ymd);
        XlogIdIndex idIndex = xlogDb.getIdIndex(ymd);

        indexing4Period(xlog, dayIndex, offset);
        indexing4Id(xlog, idIndex, offset);
    }

    private void indexing4Period(Xlog xlog, DailySecondIndex dayIndex, long offset) {
        long minuteUnit = DateUtil.getMinuteUnit(xlog.getTimestamp());
        if (currentMinuteBucket == null) {
            currentMinuteBucket = resetCurrentMinutesListModel(dayIndex, minuteUnit);
        }
        if (currentMinuteBucket.getMinuteUnit() != minuteUnit) {
            currentMinuteBucket = resetCurrentMinutesListModel(dayIndex, minuteUnit);
        }

        long secUnit = DateUtil.getSecUnit(xlog.getTimestamp());
        if (!currentMinuteBucket.getSecondUnits().contains(secUnit)) {
            currentMinuteBucket.getSecondUnits().add(secUnit);
            dayIndex.getMinuteUnitMap().put(minuteUnit, currentMinuteBucket);
            dayIndex.getSecondIndex().put(secUnit, offset);
        }
    }

    private void indexing4Id(Xlog xlog, XlogIdIndex idIndex, long offset) {
        XlogP p = xlog.getProto();
        idIndex.getTxidIndex().put(p.getTxid().toByteArray(), offset);
        gxidBuffer.add(p.getGxid().toByteArray(), p.getTxid().toByteArray(), p.getEndTime(), idIndex.getGxidIndex());
    }

    private MutableSet<byte[]> getTxidListFromGxid(byte[] gxid, XlogIdIndex idIndex) {
        MutableList<byte[]> txidListFromGxid = gxidBuffer.getTxidListFromGxid(gxid);
        if (txidListFromGxid == null) {
            txidListFromGxid = idIndex.getGxidIndex().get(gxid);
        }
        if (txidListFromGxid == null) {
            txidListFromGxid = Lists.mutable.empty();
        }
        txidListFromGxid.add(gxid);

        return txidListFromGxid.toSet();
    }

    private MinuteUnitWithSeconds resetCurrentMinutesListModel(DailySecondIndex dayIndex, long minute) {
        return dayIndex.getMinuteUnitMap()
                .computeIfAbsent(minute, k -> new MinuteUnitWithSeconds(minute));
    }

    private void publishMatched(String applicationId, long from, long to, Consumer<XlogP> consumer, XlogP xlog) {
        Obj obj = instanceService.findById(xlog.getObjId());
        if (obj == null) return;

        if (xlog.getEndTime() >= from
                && xlog.getEndTime() <= to
                && applicationId.equals(obj.getApplicationId())) {

            consumer.accept(xlog);
        }
    }

    private void streamListByPeriod0(LongSet objIds, long from, long to, long maxReadCount,
                                     Consumer<XlogP> wrapperConsumer) {

        long fromDayUnit = DateUtil.getDayUnit(from);
        long fromMinuteUnit = DateUtil.getMinuteUnit(from);
        long fromSecUnit = DateUtil.getSecUnit(from);
        long toDayUnit = DateUtil.getDayUnit(to);
        long toMinuteUnit = DateUtil.getMinuteUnit(to);

        for (long day = fromDayUnit; day <= toDayUnit && maxReadCount > 0; day++) {
            long dayTimestamp = DateUtil.reverseDayUnit(day);
            String ymd = DateUtil.yyyymmdd(dayTimestamp);
            DailySecondIndex dayIndex = xlogDb.getPeriodicIndex(ymd);

            long dayMaxMinuteUnit = DateUtil.getMinuteUnit(dayTimestamp + MILLIS_PER_DAY - 1000);
            dayMaxMinuteUnit = Math.min(dayMaxMinuteUnit, toMinuteUnit);

            long startOffset = -1;
            for (long minute = fromMinuteUnit; minute <= dayMaxMinuteUnit; minute++) {
                MinuteUnitWithSeconds minuteUnitWithSeconds = dayIndex.getMinuteUnitMap().get(minute);
                if (minuteUnitWithSeconds != null) {
                    long offset = findStartOffset(ymd, minuteUnitWithSeconds, fromSecUnit);
                    if (offset != -1) {
                        startOffset = offset;
                        break;
                    }
                }
            }

            if (startOffset >= 0) {
                long readCount = streamInDay(ymd, objIds, startOffset, to, maxReadCount, wrapperConsumer);
                maxReadCount -= readCount;
            }
        }
    }

    private long findStartOffset(String ymd, MinuteUnitWithSeconds minuteUnitWithSeconds, long fromSecUnit) {
        NavigableSet<Long> withInRange = minuteUnitWithSeconds.getSecondUnits()
                .subSet(fromSecUnit, true, Long.MAX_VALUE, true);

        Long secUnit = withInRange.pollFirst();
        if (secUnit == null) {
            return -1;
        }
        Long offset = xlogDb.getPeriodicIndex(ymd).getSecondIndex().get(secUnit);
        return offset == null ? -1 : offset;
    }

    /**
     * @return read count
     */
    private long streamInDay(String ymd, LongSet objIds, long startOffset, long toMillis, long maxReadCount,
                             Consumer<XlogP> consumer) {
        if (maxReadCount <= 0) {
            return 0;
        }
        try {
            return xlogFileDb.readPeriod(ymd, startOffset, toMillis, objIds, maxReadCount, consumer);
        } catch (IOException e) {
            log.error(e.getMessage(), S_0049, e);
            throw new RuntimeException(e);
        }
    }
}
