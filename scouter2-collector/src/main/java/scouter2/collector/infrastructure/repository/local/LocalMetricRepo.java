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
import org.eclipse.collections.api.set.primitive.LongSet;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.metric.MetricRepo;
import scouter2.collector.infrastructure.filedb.HourUnitWithMinutes;
import scouter2.collector.infrastructure.filedb.MetricFileDb;
import scouter2.collector.infrastructure.mapdb.CommonDb;
import scouter2.collector.infrastructure.mapdb.MetricDb;
import scouter2.collector.infrastructure.mapdb.MetricDbDaily;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;

import java.io.IOException;
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
public class LocalMetricRepo extends LocalRepoAdapter implements MetricRepo, NonThreadSafeRepo {

    HourUnitWithMinutes currentHourBucket;

    private CommonDb commonDb;
    private MetricFileDb metricFileDb;
    private MetricDb metricDb;
    private ObjService instanceService;

    public LocalMetricRepo(CommonDb commonDb, MetricFileDb metricFileDb, MetricDb metricDb,
                           ObjService instanceService) {
        this.commonDb = commonDb;
        this.metricFileDb = metricFileDb;
        this.metricDb = metricDb;
        this.instanceService = instanceService;
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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

    public void streamListByPeriod(String applicationId, long from, long to, StreamObserver<Metric4RepoP> stream) {
        streamListByPeriod0(applicationId, from, to, stream,
                metric -> publishMatched(applicationId, from, to, stream, metric));
    }

    public void streamListByPeriodAndInstances(String applicationId, LongSet instanceIds, long from, long to,
                                               StreamObserver<Metric4RepoP> stream) {
        streamListByPeriod0(applicationId, from, to, stream,
                metric -> publishMatchedWithInstances(applicationId, instanceIds, from, to, stream, metric));
    }

    private void publishMatched(String applicationId, long from, long to, StreamObserver<Metric4RepoP> stream,
                                Metric4RepoP metric) {
        Obj instance = instanceService.findById(metric.getObjId());
        if (instance == null) return;

        if (metric.getTimestamp() >= from
                && metric.getTimestamp() <= to
                && applicationId.equals(instance.getProto().getApplicationId())) {

            stream.onNext(metric);
        }
    }

    private void publishMatchedWithInstances(String applicationId, LongSet instanceIds,
                                             long from, long to,
                                             StreamObserver<Metric4RepoP> stream, Metric4RepoP metric) {

        Obj instance = instanceService.findById(metric.getObjId());
        if (instance == null) return;

        if (metric.getTimestamp() >= from
                && metric.getTimestamp() <= to
                && applicationId.equals(instance.getProto().getApplicationId())
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
                    log.error("error on streamListByPeriod.", e);
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
        return commonDb.getMetricReverseDict()
                .computeIfAbsent(metricName,
                        k -> commonDb.getMetricKeyGenerator().incrementAndGet());
    }

    @Override
    public long findTagIdAbsentGen(String tagName) {
        return commonDb.getMetricTagReverseDict()
                .computeIfAbsent(tagName,
                        k -> commonDb.getMetricTagKeyGenerator().incrementAndGet());
    }
}
