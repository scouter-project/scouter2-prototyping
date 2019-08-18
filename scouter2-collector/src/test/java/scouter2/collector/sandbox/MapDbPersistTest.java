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

package scouter2.collector.sandbox;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.util.StopWatch;
import scouter2.collector.domain.obj.Obj;
import scouter2.fixture.ObjFixture;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-15
 */
@Ignore
public class MapDbPersistTest {

    @Test
    @Ignore
    public void test() {
        String dir = System.getProperty("java.io.tmpdir") + "/persist-test";
        new File(dir).mkdir();

        DB db = DBMaker.fileDB(dir + "/persist.db")
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .make()
                ;

        HTreeMap<String, Long> objNameId = db.hashMap("objNameId")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();

        System.out.println("get1 : " + objNameId.get("test1"));
        objNameId.put("test1", 100L);
        System.out.println("get1 : " + objNameId.get("test1"));

        db.commit();
    }

    @Test
    @Ignore
    public void perf_test() throws IOException {
        String dir = System.getProperty("java.io.tmpdir") + "/persist-test2";
        FileUtils.deleteDirectory(new File(dir));
        CommonDb4Test commonDb = new CommonDb4Test(dir);

        HTreeMap<Long, Obj> map = commonDb.getObjMap();
        Atomic.Long objIdGenerator = commonDb.getObjIdGenerator();

        StopWatch stopWatch = new StopWatch("MapDB HashMap Perf Stop Watch");
        int checkCount = 10000;

        stopWatch.start("atomicLong increase time");
        List<Obj> objList = IntStream.range(0, checkCount)
                .mapToObj(no -> ObjFixture.getOne(objIdGenerator.incrementAndGet()))
                .collect(Collectors.toList());
        stopWatch.stop();

        stopWatch.start("obj insertion time");
        objList.forEach(obj -> map.put(obj.getObjId(), obj));
        stopWatch.stop();

        stopWatch.start("obj insertion time2");
        objList.forEach(obj -> map.put(obj.getObjId(), obj));
        stopWatch.stop();

        List<Obj> collect;

        stopWatch.start("get all values time");
        collect = map.values().stream().collect(Collectors.toList());
        stopWatch.stop();

        stopWatch.start("get all values time2");
        collect = map.values().stream().collect(Collectors.toList());
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
        System.out.println(collect.size());
    }

    @Test
    @Ignore
    public void perf_test_with_scheduled_commit() throws IOException {
        String dir = System.getProperty("java.io.tmpdir") + "/persist-test-with-scheduled-commit";
        FileUtils.deleteDirectory(new File(dir));
        CommonDb4Test commonDb = new CommonDb4Test(dir);

        HTreeMap<Long, Obj> map = commonDb.getObjMap();
        Atomic.Long objIdGenerator = commonDb.getObjIdGenerator();

        StopWatch stopWatch = new StopWatch("MapDB HashMap Perf Stop Watch");
        int checkCount = 10000;

        scheduleCommit(commonDb, 100);

        stopWatch.start("atomicLong increase time");
        List<Obj> objList = IntStream.range(0, checkCount)
                .mapToObj(no -> ObjFixture.getOne(objIdGenerator.incrementAndGet()))
                .collect(Collectors.toList());
        stopWatch.stop();

        stopWatch.start("obj insertion time");
        objList.forEach(obj -> map.put(obj.getObjId(), obj));
        stopWatch.stop();

        stopWatch.start("obj insertion time2");
        objList.forEach(obj -> map.put(obj.getObjId(), obj));
        stopWatch.stop();

        List<Obj> collect;

        stopWatch.start("get all values time");
        collect = map.values().stream().collect(Collectors.toList());
        stopWatch.stop();

        stopWatch.start("get all values time2");
        collect = map.values().stream().collect(Collectors.toList());
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
        System.out.println(collect.size());
    }

    @Test
    @Ignore
    public void check_persistency_with_scheduled_commit() throws IOException, InterruptedException {
        String dir = System.getProperty("java.io.tmpdir") + "/persistency-in-concurrency";
        FileUtils.deleteDirectory(new File(dir));
        CommonDb4Test commonDb = new CommonDb4Test(dir);

        HTreeMap<Long, Obj> map = commonDb.getObjMap();
        Atomic.Long objIdGenerator = commonDb.getObjIdGenerator();

        StopWatch stopWatch = new StopWatch("MapDB HashMap Perf Stop Watch");
        int checkCount = 10000;
        List<Obj> objList = IntStream.range(0, checkCount).mapToObj(no -> ObjFixture.getOne(no)).collect(Collectors.toList());

        scheduleCommit(commonDb, 10);
        CountDownLatch latch = new CountDownLatch(4);

        long start = System.currentTimeMillis();

        new Thread(() -> {
            System.out.println("start inc 1.");
            IntStream.range(0, checkCount).filter(this::even)
                    .forEach(n -> ObjFixture.getOne(objIdGenerator.incrementAndGet()));
            latch.countDown();
            System.out.println("end inc 1 : " + (System.currentTimeMillis() - start));
        }).start();

        new Thread(() -> {
            System.out.println("start inc 2.");
            IntStream.range(0, checkCount).filter(this::odd)
                    .forEach(n -> ObjFixture.getOne(objIdGenerator.incrementAndGet()));
            latch.countDown();
            System.out.println("end inc 2 : " + (System.currentTimeMillis() - start));
        }).start();

        new Thread(() -> {
            System.out.println("start add 1.");
            IntStream.range(0, checkCount).filter(this::even)
                    .forEach(n -> map.put(objList.get(n).getObjId(), objList.get(n)));
            latch.countDown();
            System.out.println("end add 1 : " + (System.currentTimeMillis() - start));
        }).start();

        new Thread(() -> {
            System.out.println("start add 2.");
            IntStream.range(0, checkCount).filter(this::odd)
                    .forEach(n -> map.put(objList.get(n).getObjId(), objList.get(n)));
            latch.countDown();
            System.out.println("end add 2 : " + (System.currentTimeMillis() - start));
        }).start();

        latch.await();
        System.out.println("end. all elapsed: "  + (System.currentTimeMillis() - start));

        assertThat(objIdGenerator.get()).isEqualTo(10000L);
        assertThat(map.size()).isEqualTo(10000);

        commonDb.getDb().commit();
        commonDb.getDb().close();

        CommonDb4Test commonDbReopen = new CommonDb4Test(dir);
        assertThat(commonDbReopen.getObjIdGenerator().get()).isEqualTo(10000L);
        assertThat(commonDbReopen.getObjMap().size()).isEqualTo(10000);

        commonDbReopen.getDb().close();
        FileUtils.deleteDirectory(new File(dir));
    }

    private boolean odd(int n) {
        return n%2 == 1;
    }

    private boolean even(int n) {
        return n%2 == 0;
    }

    private void scheduleCommit(CommonDb4Test commonDb, long millis) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(millis);
                        if (!commonDb.getDb().isClosed()) {
                            commonDb.getDb().commit();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
