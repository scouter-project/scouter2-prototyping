package scouter2.collector.sandbox;

import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 *
 * for dictionary - btree with no tran
 * for xlog index - hash with tran
 * for counter index - btree with tran
 */
@Ignore
public class MapDbBtreeTest {

    @Test
    //7.1sec
    public void test_btreee_write_tran() {
        DB db = DBMaker.fileDB("/Users/gunlee/temp/mapdb/test_btreee_write_tran")
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .make();

        ConcurrentMap map = db.treeMap("test_btreee_write_tran").createOrOpen();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.put(i, i);
            if (i % 5000 == 0) {
                db.commit();
            }
        }
        System.out.println("elapsed=" + (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.get(i);
        }
        System.out.println("elapsed-read=" + (System.nanoTime() - start) / 1000000);

        db.close();
    }

    @Test
    //5.4sec
    public void test_btreee_write_no_tran() {
        DB db = DBMaker.fileDB("/Users/gunlee/temp/mapdb/test_btreee_write_no_tran")
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .make();

        ConcurrentMap map = db.treeMap("test_btreee_write_no_tran").createOrOpen();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.put(i, i);
        }
        System.out.println("elapsed=" + (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.get(i);
        }
        System.out.println("elapsed-read=" + (System.nanoTime() - start) / 1000000);
        db.close();
    }

    @Test
    //14sec
    public void test_btreee_write_tran_no_mmap() {
        DB db = DBMaker.fileDB("/Users/gunlee/temp/mapdb/test3")
                .closeOnJvmShutdown()
                .transactionEnable()
                .make();

        ConcurrentMap map = db.treeMap("test_btreee_write_tran_no_mmap").createOrOpen();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.put(i, i);
        }
        System.out.println("elapsed=" + (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.get(i);
        }
        System.out.println("elapsed-read=" + (System.nanoTime() - start) / 1000000);
        db.close();
    }

    @Test
    //10sec
    public void test_btreee_write_no_tran_no_mmap() {
        DB db = DBMaker.fileDB("/Users/gunlee/temp/mapdb/test2")
                .closeOnJvmShutdown()
                .make();

        ConcurrentMap map = db.treeMap("test_btreee_write_no_tran_no_mmap").createOrOpen();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.put(i, i);
        }
        System.out.println("elapsed=" + (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.get(i);
        }
        System.out.println("elapsed-read=" + (System.nanoTime() - start) / 1000000);
        db.close();
    }

    @Test
    //10sec
    public void test_btreee_write_tran_filechannel() {
        DB db = DBMaker.fileDB("/Users/gunlee/temp/mapdb/test_btreee_write_tran_filechannel")
                .closeOnJvmShutdown()
                .fileChannelEnable()
                .transactionEnable()
                .make();

        ConcurrentMap map = db.treeMap("test_btreee_write_tran_filechannel").createOrOpen();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.put(i, i);
        }
        System.out.println("elapsed=" + (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.get(i);
        }
        System.out.println("elapsed-read=" + (System.nanoTime() - start) / 1000000);
        db.close();
    }

    @Test
    //3sec
    public void test_btreee_write_no_tran_filechannel() {
        DB db = DBMaker.fileDB("/Users/gunlee/temp/mapdb/test_btreee_write_no_tran_filechannel")
                .fileChannelEnable()
                .closeOnJvmShutdown()
                .make();

        ConcurrentMap map = db.treeMap("test_btreee_write_no_tran_filechannel").createOrOpen();
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.put(i, i);
        }
        System.out.println("elapsed=" + (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            map.get(i);
        }
        System.out.println("elapsed-read=" + (System.nanoTime() - start) / 1000000);
        db.close();
    }
}
