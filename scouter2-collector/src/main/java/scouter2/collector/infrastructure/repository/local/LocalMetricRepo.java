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

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.metric.MetricRepo;
import scouter2.collector.domain.metric.MetricRepoAdapter;
import scouter2.collector.domain.metric.Metrics;
import scouter2.collector.domain.metric.SingleMetric;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.infrastructure.filedb.HourUnitWithMinutes;
import scouter2.collector.infrastructure.filedb.MetricFileDb;
import scouter2.collector.infrastructure.mapdb.MetricDb;
import scouter2.collector.infrastructure.mapdb.MetricDbDaily;
import scouter2.collector.infrastructure.mapdb.MetricDictDb;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableSet;
import java.util.function.Consumer;

import static scouter2.common.util.DateUtil.MILLIS_PER_DAY;
import static scouter2.common.util.DateUtil.MILLIS_PER_HOUR;

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

    private HourUnitWithMinutes currentHourBucket;

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
    }

    @Override
    public void add(String applicationId, Metric4RepoP metric) {
        try {
            if (metric == null || StringUtils.isEmpty(applicationId)) {
                return;
            }
            String ymd = DateUtil.yyyymmdd(metric.getTimestamp());
            MetricDbDaily dayDb = metricDb.getDailyDb(ymd);

            long offset = metricFileDb.add(ymd, metric);
            if (offset >= 0) {
                indexing(metric, dayDb, offset);
            }

            for (Map.Entry<Long, Double> e : metric.getMetricsMap().entrySet()) {
                cache.addCurrentMetric(applicationId, metric.getObjId(), e.getKey(), e.getValue(),
                        metric.getTimestamp(), Metrics.getAliveMillisOfType(metric.getTimeType()));
            }

            findCurrentMetrics(applicationId,
                    LongSets.mutable.with(metric.getObjId()), LongSets.mutable.with(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
            );

        } catch (Exception e) {
            log.error(e.getMessage(), S_0004, e);
        }
    }

    public MutableList<SingleMetric> findCurrentMetrics(String applicationId, LongSet objIds, LongSet metricIds) {
        return cache.findCurrentMetrics(applicationId, objIds, metricIds, U.now());
    }

    @Override
    public void streamListByPeriod(String applicationId, long from, long to, StreamObserver<Metric4RepoP> stream) {
        streamListByPeriod0(applicationId, from, to, stream,
                metric -> publishMatched(applicationId, from, to, stream, metric));
    }

    @Override
    public void streamListByPeriodAndObjs(String applicationId, LongSet instanceIds, long from, long to,
                                          StreamObserver<Metric4RepoP> stream) {
        streamListByPeriod0(applicationId, from, to, stream,
                metric -> publishMatchedWithObjs(applicationId, instanceIds, from, to, stream, metric));
    }

    public void findCurrentByName(String applicationId, LongSet instanceIds, long now) {
//        String ymd = DateUtil.yyyymmdd(now);
//        currentMetricCache.values()
    }

    private void indexing(Metric4RepoP metric, MetricDbDaily dayDb, long offset) {
        long hourUnit = DateUtil.getHourUnit(metric.getTimestamp());
        if (currentHourBucket == null) {
            resetCurrentMinutesListModel(dayDb, hourUnit);
        }
        if (currentHourBucket.getHourUnit() != hourUnit) {
            resetCurrentMinutesListModel(dayDb, hourUnit);
        }

        long minuteUnit = DateUtil.getMinuteUnit(metric.getTimestamp());
        if (!currentHourBucket.getMinuteUnits().contains(minuteUnit)) {
            currentHourBucket.getMinuteUnits().add(minuteUnit);
            dayDb.getHourUnitMap().put(hourUnit, currentHourBucket);
            dayDb.getMinuteIndex().put(minuteUnit, offset);
        }
    }

    private void resetCurrentMinutesListModel(MetricDbDaily dayDb, long hour) {
        this.currentHourBucket = dayDb.getHourUnitMap()
                .computeIfAbsent(hour, k -> new HourUnitWithMinutes(hour));
    }

    private void publishMatched(String applicationId, long from, long to, StreamObserver<Metric4RepoP> stream,
                                Metric4RepoP metric) {
        Obj obj = instanceService.findById(metric.getObjId());
        if (obj == null) return;

        if (metric.getTimestamp() >= from
                && metric.getTimestamp() <= to
                && applicationId.equals(obj.getApplicationId())) {

            stream.onNext(metric);
        }
    }

    private void publishMatchedWithObjs(String applicationId, LongSet instanceIds,
                                        long from, long to,
                                        StreamObserver<Metric4RepoP> stream, Metric4RepoP metric) {

        Obj obj = instanceService.findById(metric.getObjId());
        if (obj == null) return;

        if (metric.getTimestamp() >= from
                && metric.getTimestamp() <= to
                && applicationId.equals(obj.getApplicationId())
                && instanceIds.contains(metric.getObjId())) {

            stream.onNext(metric);
        }
    }

    private void streamListByPeriod0(String applicationId, long from, long to,
                                     StreamObserver<Metric4RepoP> stream,
                                     Consumer<Metric4RepoP> metric4RepoPConsumer) {

        long fromDayUnit = DateUtil.getDayUnit(from);
        long fromHourUnit = DateUtil.getHourUnit(from);
        long fromMinUnit = DateUtil.getMinuteUnit(from);
        long toDayUnit = DateUtil.getDayUnit(to);
        long toHourUnit = DateUtil.getHourUnit(to);

        for (long day = fromDayUnit; day <= toDayUnit; day++) {
            long dayTimestamp = DateUtil.reverseDayUnit(day);
            String ymd = DateUtil.yyyymmdd(dayTimestamp);
            MetricDbDaily dayDb = metricDb.getDailyDb(ymd);

            long dayMaxHourUnit = DateUtil.getHourUnit(dayTimestamp + MILLIS_PER_DAY - 1000);
            dayMaxHourUnit = Math.min(dayMaxHourUnit, toHourUnit);

            long startOffset = -1;
            for (long hour = fromHourUnit; hour <= dayMaxHourUnit; hour++) {
                HourUnitWithMinutes hourUnitWithMinutes = dayDb.getHourUnitMap().get(hour);
                if (hourUnitWithMinutes != null) {
                    long offset = findStartOffset(ymd, hourUnitWithMinutes, fromMinUnit);
                    if (offset != -1) {
                        startOffset = offset;
                        break;
                    }
                }
            }

            if (startOffset >= 0) {
                try {
                    streamInDay(ymd, startOffset, to, metric4RepoPConsumer);

                } catch (Exception e) {
                    log.error("error on streamListByPeriod.", S_0005, e);
                    stream.onError(e);
                    break;
                }
            }
        }
        stream.onCompleted();
    }


    private void streamInDay(String ymd, long startOffset, long toMillis, Consumer<Metric4RepoP> metric4RepoPConsumer)
            throws IOException {

        metricFileDb.readPeriod(ymd, startOffset, toMillis, metric4RepoPConsumer);
    }

    private long findStartOffset(String ymd, HourUnitWithMinutes hourUnitWithMinutes, long fromMinUnit) {
        long timestamp = DateUtil.reverseMinuteUnit(fromMinUnit);
        long hourLastMinUnit = DateUtil.getMinuteUnit(DateUtil.truncHour(timestamp) + MILLIS_PER_HOUR - 1000);
        NavigableSet<Long> withInRange = hourUnitWithMinutes.getMinuteUnits()
                .subSet(fromMinUnit, true, hourLastMinUnit, true);

        Long minUnit = withInRange.pollFirst();
        if (minUnit == null) {
            return -1;
        }
        Long offset = metricDb.getDailyDb(ymd).getMinuteIndex().get(minUnit);
        return offset == null ? 0 : offset;
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
