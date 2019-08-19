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

package scouter2.collector.domain.metric;

import io.grpc.stub.StreamObserver;
import org.eclipse.collections.api.set.primitive.LongSet;
import scouter2.proto.Metric4RepoP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
public abstract class MetricRepoAdapter implements MetricRepo {
    @Override
    public void add(String applicationId, Metric4RepoP metric) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamListByPeriod(String applicationId, long from, long to, StreamObserver<Metric4RepoP> stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamListByPeriodAndObjs(String applicationId, LongSet instanceIds, long from, long to, StreamObserver<Metric4RepoP> stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long findMetricIdAbsentGen(String metricName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String findMetricNameById(long metricId) {
        throw new UnsupportedOperationException();
    }
}
