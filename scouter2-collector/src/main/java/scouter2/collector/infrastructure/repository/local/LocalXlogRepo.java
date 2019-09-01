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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.springframework.context.annotation.Conditional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.collector.domain.xlog.XlogOffset;
import scouter2.collector.domain.xlog.XlogRepo;
import scouter2.collector.domain.xlog.XlogRepoAdapter;
import scouter2.collector.domain.xlog.XlogStreamObserver;
import scouter2.collector.infrastructure.db.filedb.MinuteUnitWithSeconds;
import scouter2.collector.infrastructure.db.filedb.XlogFileDb;
import scouter2.collector.infrastructure.db.mapdb.DailySecondIndex;
import scouter2.collector.infrastructure.db.mapdb.XlogDb;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
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
    LocalMetricRepoCache cache;

    public LocalXlogRepo(XlogFileDb xlogFileDb, XlogDb xlogDb,
                         ObjService instanceService, LocalMetricRepoCache cache) {
        this.xlogFileDb = xlogFileDb;
        this.xlogDb = xlogDb;
        this.instanceService = instanceService;
        //TODO cache
        this.cache = cache;
    }

    @Override
    public void add(String applicationId, Xlog xlog) {
        try {
            if (xlog == null || xlog.getProto() == null || StringUtils.isEmpty(applicationId)) {
                return;
            }

            add2Cache(xlog);
            addToDb(xlog);

        } catch (Exception e) {
            //TODO error key
            log.error(e.getMessage(), "", e);
        }
    }

    @Override
    public void stream(String applicationId, long from, long to, Consumer<XlogP> consumer) {
        MutableLongSet objIds = instanceService.findByApplicationId(applicationId)
                .collectLong(Obj::getObjId).toSet();
        streamListByPeriod0(objIds, from, to, xlog -> publishMatched(applicationId, from, to, consumer, xlog));
    }

    @Override
    public void streamByObjs(String applicationId, LongSet objIds, long from, long to, Consumer<XlogP> consumer) {
        streamListByPeriod0(objIds, from, to, xlog -> publishMatched(applicationId, from, to, consumer, xlog));
    }

    @Override
    public void streamLatest(String applicationId, @Nullable XlogOffset lastOffset, int maxCount,
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
        DailySecondIndex dayIndex = xlogDb.getDailyPeriodicIndex(ymd);
        long offset = xlogFileDb.add(ymd, xlog);
        if (offset >= 0) {
            indexing(xlog, dayIndex, offset);
        }
    }

    private void add2Cache(Xlog xlog) {

    }

    private void indexing(Xlog xlog, DailySecondIndex dayIndex, long offset) {
        indexing4Period(xlog, dayIndex, offset);
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

    private void streamListByPeriod0(LongSet objIds, long from, long to,
                                     Consumer<XlogP> wrapperConsumer) {

        long fromDayUnit = DateUtil.getDayUnit(from);
        long fromMinuteUnit = DateUtil.getMinuteUnit(from);
        long fromSecUnit = DateUtil.getSecUnit(from);
        long toDayUnit = DateUtil.getDayUnit(to);
        long toMinuteUnit = DateUtil.getMinuteUnit(to);

        for (long day = fromDayUnit; day <= toDayUnit; day++) {
            long dayTimestamp = DateUtil.reverseDayUnit(day);
            String ymd = DateUtil.yyyymmdd(dayTimestamp);
            DailySecondIndex dayIndex = xlogDb.getDailyPeriodicIndex(ymd);

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
                streamInDay(ymd, objIds, startOffset, to, wrapperConsumer);
            }
        }
    }

    private long findStartOffset(String ymd, MinuteUnitWithSeconds minuteUnitWithSeconds, long fromSecUnit) {
        NavigableSet<Long> withInRange = minuteUnitWithSeconds.getSecondUnits()
                .subSet(fromSecUnit, true, fromSecUnit + 60, true);

        Long secUnit = withInRange.pollFirst();
        if (secUnit == null) {
            return -1;
        }
        Long offset = xlogDb.getDailyPeriodicIndex(ymd).getSecondIndex().get(secUnit);
        return offset == null ? -1 : offset;
    }

    private void streamInDay(String ymd, LongSet objIds, long startOffset, long toMillis,
                             Consumer<XlogP> consumer) {
        try {
            xlogFileDb.readPeriod(ymd, startOffset, toMillis, objIds, consumer);
        } catch (IOException e) {
            log.error(e.getMessage(), S_0049, e);
            throw new RuntimeException(e);
        }
    }
}
