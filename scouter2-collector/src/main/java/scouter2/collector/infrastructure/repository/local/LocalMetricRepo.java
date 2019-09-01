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
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.metric.MetricRepo;
import scouter2.collector.domain.metric.MetricRepoAdapter;
import scouter2.collector.domain.metric.Metrics;
import scouter2.collector.domain.metric.SingleMetricDatum;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.infrastructure.db.filedb.HourUnitWithMinutes;
import scouter2.collector.infrastructure.db.filedb.MetricFileDb;
import scouter2.collector.infrastructure.db.mapdb.DailyMinuteIndex;
import scouter2.collector.infrastructure.db.mapdb.MetricDb;
import scouter2.collector.infrastructure.db.mapdb.MetricDictDb;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.TimeTypeP;

import java.io.IOException;
import java.util.Map;
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
public class LocalMetricRepo extends MetricRepoAdapter implements MetricRepo, NonThreadSafeRepo {

    public static final ThrottleConfig S_0004 = ThrottleConfig.of("S0004");
    public static final ThrottleConfig S_0005 = ThrottleConfig.of("S0005");

    private MutableIntObjectMap<HourUnitWithMinutes> currentHourBucketMap = IntObjectMaps.mutable.empty();

    private MetricDictDb metricDictDb;
    private MetricFileDb metricFileDb;
    private MetricDb metricDb;
    private ObjService instanceService;
    LocalMetricRepoCache cache;

    public LocalMetricRepo(MetricDictDb metricDictDb, MetricFileDb metricFileDb, MetricDb metricDb,
                           ObjService instanceService, LocalMetricRepoCache cache) {
        this.metricDictDb = metricDictDb;
        this.metricFileDb = metricFileDb;
        this.metricDb = metricDb;
        this.instanceService = instanceService;
        this.cache = cache;
    }

    @Override
    public String getRepoType() {
        return LocalRepoConstant.TYPE_NAME;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        metricDb.closeAll();
        metricDictDb.getDb().close();
    }

    @Override
    public void add(String applicationId, Metric4RepoP metric) {
        try {
            if (metric == null || StringUtils.isEmpty(applicationId)) {
                return;
            }
            String ymd = DateUtil.yyyymmdd(metric.getTimestamp());
            DailyMinuteIndex dayIndex = metricDb.getDailyIndex(ymd, metric.getTimeType());

            long offset = metricFileDb.add(ymd, metric);
            if (offset >= 0) {
                indexing(metric, dayIndex, offset);
            }

            if (metric.getTimeType() == TimeTypeP.REALTIME) {
                for (Map.Entry<Long, Double> e : metric.getMetricsMap().entrySet()) {
                    cache.addCurrentMetric(applicationId, metric.getObjId(), e.getKey(), e.getValue(),
                            metric.getTimestamp(), Metrics.getAliveMillisOfType(metric.getTimeType()));
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), S_0004, e);
        }
    }

    @Override
    public MutableList<SingleMetricDatum> findCurrentMetrics(String applicationId, LongSet objIds, LongSet metricIds) {
        return cache.findCurrentMetrics(applicationId, objIds, metricIds, U.now());
    }

    private void indexing(Metric4RepoP metric, DailyMinuteIndex dayIndex, long offset) {
        long hourUnit = DateUtil.getHourUnit(metric.getTimestamp());
        HourUnitWithMinutes currentHourBucket = currentHourBucketMap.get(metric.getTimeTypeValue());
        if (currentHourBucket == null) {
            currentHourBucket = resetCurrentMinutesListModel(dayIndex, hourUnit);
            currentHourBucketMap.put(metric.getTimeTypeValue(), currentHourBucket);
        }
        if (currentHourBucket.getHourUnit() != hourUnit) {
            currentHourBucket = resetCurrentMinutesListModel(dayIndex, hourUnit);
            currentHourBucketMap.put(metric.getTimeTypeValue(), currentHourBucket);
        }

        long minuteUnit = DateUtil.getMinuteUnit(metric.getTimestamp());
        if (!currentHourBucket.getMinuteUnits().contains(minuteUnit)) {
            currentHourBucket.getMinuteUnits().add(minuteUnit);
            dayIndex.getHourUnitMap().put(hourUnit, currentHourBucket);
            dayIndex.getMinuteIndex().put(minuteUnit, offset);
        }
    }

    private HourUnitWithMinutes resetCurrentMinutesListModel(DailyMinuteIndex dayIndex, long hour) {
        return dayIndex.getHourUnitMap()
                .computeIfAbsent(hour, k -> new HourUnitWithMinutes(hour));
    }

    @Override
    public void stream(String applicationId, TimeTypeP timeTypeP, long from, long to,
                       Consumer<Metric4RepoP> consumer) {
        streamListByPeriod0(timeTypeP, from, to,
                metric -> publishMatched(applicationId, from, to, consumer, metric));
    }

    @Override
    public void streamByObjs(String applicationId, LongSet objIds, TimeTypeP timeTypeP, long from, long to,
                             Consumer<Metric4RepoP> consumer) {
        streamListByPeriod0(timeTypeP, from, to,
                metric -> publishMatchedWithObjs(applicationId, objIds, from, to, consumer, metric));
    }

    private void publishMatched(String applicationId, long from, long to, Consumer<Metric4RepoP> consumer,
                                Metric4RepoP metric) {
        Obj obj = instanceService.findById(metric.getObjId());
        if (obj == null) return;

        if (metric.getTimestamp() >= from
                && metric.getTimestamp() <= to
                && applicationId.equals(obj.getApplicationId())) {

            consumer.accept(metric);
        }
    }

    private void publishMatchedWithObjs(String applicationId, LongSet objIds, long from, long to,
                                        Consumer<Metric4RepoP> consumer, Metric4RepoP metric) {

        Obj obj = instanceService.findById(metric.getObjId());
        if (obj == null) return;

        if (metric.getTimestamp() >= from
                && metric.getTimestamp() <= to
                && applicationId.equals(obj.getApplicationId())
                && (objIds.isEmpty() || objIds.contains(metric.getObjId()))) {

            consumer.accept(metric);
        }
    }

    private void streamListByPeriod0(TimeTypeP timeTypeP, long from, long to,
                                     Consumer<Metric4RepoP> wrapperConsumer) {

        long fromDayUnit = DateUtil.getDayUnit(from);
        long fromHourUnit = DateUtil.getHourUnit(from);
        long fromMinUnit = DateUtil.getMinuteUnit(from);
        long toDayUnit = DateUtil.getDayUnit(to);
        long toHourUnit = DateUtil.getHourUnit(to);

        for (long day = fromDayUnit; day <= toDayUnit; day++) {
            long dayTimestamp = DateUtil.reverseDayUnit(day);
            String ymd = DateUtil.yyyymmdd(dayTimestamp);
            DailyMinuteIndex dayIndex = metricDb.getDailyIndex(ymd, timeTypeP);

            long dayMaxHourUnit = DateUtil.getHourUnit(dayTimestamp + MILLIS_PER_DAY - 1000);
            dayMaxHourUnit = Math.min(dayMaxHourUnit, toHourUnit);

            long startOffset = -1;
            for (long hour = fromHourUnit; hour <= dayMaxHourUnit; hour++) {
                HourUnitWithMinutes hourUnitWithMinutes = dayIndex.getHourUnitMap().get(hour);
                if (hourUnitWithMinutes != null) {
                    long offset = findStartOffset(ymd, timeTypeP, hourUnitWithMinutes, fromMinUnit);
                    if (offset != -1) {
                        startOffset = offset;
                        break;
                    }
                }
            }

            if (startOffset >= 0) {
                streamInDay(timeTypeP, ymd, startOffset, to, wrapperConsumer);
            }
        }
    }


    private void streamInDay(TimeTypeP timeTypeP, String ymd, long startOffset, long toMillis,
                             Consumer<Metric4RepoP> consumer) {

        try {
            metricFileDb.readPeriod(ymd, timeTypeP, startOffset, toMillis, consumer);
        } catch (IOException e) {
            log.error(e.getMessage(), S_0005, e);
            throw new RuntimeException(e);
        }
    }

    private long findStartOffset(String ymd, TimeTypeP timeTypeP,
                                 HourUnitWithMinutes hourUnitWithMinutes, long fromMinUnit) {
        NavigableSet<Long> withInRange = hourUnitWithMinutes.getMinuteUnits()
                .subSet(fromMinUnit, true, fromMinUnit + 60, true);

        Long minUnit = withInRange.pollFirst();
        if (minUnit == null) {
            return -1;
        }
        Long offset = metricDb.getDailyIndex(ymd, timeTypeP).getMinuteIndex().get(minUnit);
        return offset == null ? -1 : offset;
    }

    @Override
    public long findMetricIdAbsentGen(String metricName) {
        Long metricId = metricDictDb.getMetricReverseDict().get(metricName);
        if (metricId == null) {
            metricId = metricDictDb.getMetricKeyGenerator().incrementAndGet();
            metricDictDb.getMetricReverseDict().put(metricName, metricId);
            metricDictDb.getMetricDict().put(metricId, metricName);
            metricDictDb.commit();
        }
        return metricId;
    }

    @Override
    public String findMetricNameById(long metricId) {
        return metricDictDb.getMetricDict().get(metricId);
    }
}
