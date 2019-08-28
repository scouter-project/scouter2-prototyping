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

package scouter2.collector.transport.legacy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.TimeTypeEnum;
import scouter.lang.pack.InteractionPerfCounterPack;
import scouter.lang.pack.ObjectPack;
import scouter.lang.pack.Pack;
import scouter.lang.pack.PackEnum;
import scouter.lang.pack.PerfCounterPack;
import scouter.lang.pack.XLogPack;
import scouter.net.NetCafe;
import scouter.util.BytesUtil;
import scouter.util.HashUtil;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.domain.metric.MetricReceiveQueue;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjReceiveQueue;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.xlog.XlogReceiveQueue;
import scouter2.collector.legacy.CounterManager;
import scouter2.collector.main.CoreRun;
import scouter2.common.collection.PurgingQueue;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.XlogP;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-24
 */
@Slf4j
@Getter
public class LegacyUdpDataProcessor {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static LegacyUdpDataProcessor instance;

    private ConfigLegacy conf;
    private PurgingQueue<NetData> queue = null;

    private LegacyUdpDataProcessor(ConfigLegacy conf) {
        this.conf = conf;
        queue = new PurgingQueue<>(2048);
    }

    public synchronized static LegacyUdpDataProcessor start(ConfigLegacy conf) {
        if (instance != null) {
            throw new RuntimeException("Already working legacy udp processor exists.");
        }
        instance = new LegacyUdpDataProcessor(conf);
        UdpMultipacketProcessor udpMultipacketProcessor = new UdpMultipacketProcessor(conf);

        for (int i = 0; i < conf.get_legacyNetUdpWorkerThreadCount(); i++) {
            LegacyUdpDataProcessorThread processorThread =
                    new LegacyUdpDataProcessorThread(instance.getQueue(), udpMultipacketProcessor, conf);
            processorThread.setDaemon(true);
            processorThread.setName(ThreadUtil.getName(processorThread.getClass(), threadNo.getAndIncrement()));
            processorThread.start();
        }
        return instance;
    }

    public void offer(byte[] data, InetAddress addr) {
        boolean added = queue.offerOverflowClear(new NetData(data, addr));
        if (!added) {
            log.warn("S158", 10, "overflow recv queue!!");
        }
    }

    public void offer(Pack pack, InetAddress addr) throws IOException {
        DataOutputX out = new DataOutputX();
        out.write(NetCafe.CAFE);
        out.write(new DataOutputX().writePack(pack).toByteArray());
        offer(out.toByteArray(), addr);
    }

    @Slf4j
    static class LegacyUdpDataProcessorThread extends Thread {
        public static final ThrottleConfig S_0020 = ThrottleConfig.of("S0020");
        public static final ThrottleConfig S_0021 = ThrottleConfig.of("S0021");
        public static final ThrottleConfig S_0022 = ThrottleConfig.of("S0022");
        public static final ThrottleConfig S_0023 = ThrottleConfig.of("S0023");
        ConfigLegacy conf;
        UdpMultipacketProcessor udpMultipacketProcessor;

        PurgingQueue<NetData> udpDataQueue;
        ObjReceiveQueue objReceiveQueue;
        MetricReceiveQueue metricReceiveQueue;
        XlogReceiveQueue xlogReceiveQueue;

        ObjService objService;

        public LegacyUdpDataProcessorThread(PurgingQueue<NetData> udpDataQueue,
                                            UdpMultipacketProcessor udpMultipacketProcessor,
                                            ConfigLegacy conf) {
            this.conf = conf;
            this.udpDataQueue = udpDataQueue;
            this.udpMultipacketProcessor = udpMultipacketProcessor;

            this.objReceiveQueue = ObjReceiveQueue.getInstance();
            this.metricReceiveQueue = MetricReceiveQueue.getInstance();
            this.xlogReceiveQueue = XlogReceiveQueue.getInstance();

            this.objService = ObjService.getInstance();
        }

        @Override
        public void run() {
            try {
                while (CoreRun.isRunning()) {
                    try {
                        NetData data = udpDataQueue.take();
                        process(data);
                    } catch(Throwable t) {
                        log.error(t.getMessage(), S_0020, t);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), S_0021, e);
            }
        }

        private void process(NetData netData) {
            try {
                DataInputX in = new DataInputX(netData.data);
                int cafe = in.readInt();
                switch (cafe) {
                    case NetCafe.UDP_CAFE:
                        processCafe(in, netData.addr);
                        break;
                    case NetCafe.UDP_CAFE_N:
                        processCafeN(in, netData.addr);
                        break;
                    case NetCafe.UDP_CAFE_MTU:
                        processCafeMTU(in, netData.addr);
                        break;
                    case NetCafe.UDP_JAVA:
                        processCafe(in, netData.addr);
                        break;
                    case NetCafe.UDP_JAVA_N:
                        processCafeN(in, netData.addr);
                        break;
                    case NetCafe.UDP_JAVA_MTU:
                        processCafeMTU(in, netData.addr);
                        break;
                    default:
                        log.error("Receive unknown data, length=" + BytesUtil.getLength(netData.data)
                                + " from " + netData.addr, S_0022);
                }

            } catch(Throwable t) {
                log.error("invalid data", S_0023, t);
            }
        }

        private void processCafeMTU(DataInputX in, InetAddress addr) throws IOException {
            int objHash = in.readInt();
            long pkid = in.readLong();
            short total = in.readShort();
            short num = in.readShort();
            byte[] data = in.readBlob();
            byte[] done = udpMultipacketProcessor.add(pkid, total, num, data, objHash, addr);
            if (done != null) {
                Pack pack = new DataInputX(done).readPack();
                process0(pack, addr);
            }
        }

        private void processCafe(DataInputX in, InetAddress addr) throws IOException {
            Pack pack = in.readPack();
            process0(pack, addr);
        }
        private void processCafeN(DataInputX in, InetAddress addr) throws IOException {
            int n = in.readShort();
            for (int i = 0; i < n; i++) {
                Pack pack = in.readPack();
                process0(pack, addr);
            }
        }

        private void process0(Pack p, InetAddress addr) {
            if (p == null)
                return;

            //TODO
//            if (conf.log_udp_packet) {
//                System.out.println(p)
//            }

            switch (p.getPackType()) {
                case PackEnum.PERF_COUNTER:
                    PerfCounterPack counterPack = (PerfCounterPack) p;
                    if (counterPack.time == 0) {
                        counterPack.time = U.now();
                    }
                    if (counterPack.timetype == 0) {
                        counterPack.timetype = TimeTypeEnum.REALTIME;
                    }
                    metricReceiveQueue.offer(LegacyMapper.toMetric(counterPack));
                    if (conf.isLegacyLogUdpCounter()) {
                        log.debug("Legacy UDP counterPack received: {}", counterPack);
                    }
                    break;

                case PackEnum.PERF_INTERACTION_COUNTER:
                    InteractionPerfCounterPack iCounterPack = (InteractionPerfCounterPack) p;
                    //TODO InteractionPerfCountCore.add(iCounterPack);
//                    if (conf.log_udp_interaction_counter) {
//                        System.out.println("DEBUG UDP INTERACTION COUNTER: " + p)
//                    }
                    break;

                case PackEnum.XLOG:
                    XLogPack xlogPack = (XLogPack) p;
                    Long objId = objService.findIdByName(String.valueOf(xlogPack.objHash));
                    if (objId == null) {
                        break;
                    }
                    if (xlogPack.endTime == 0) {
                        xlogPack.endTime = U.now();
                    }
                    XlogP xlogP = LegacyMapper.toXlog(xlogPack, objId);
                    xlogReceiveQueue.offer(xlogP);
                    if (conf.isLegacyLogUdpXlog()) {
                        log.debug("Legacy UDP xlogPack received: {}", xlogPack);
                    }
                    break;

                case PackEnum.XLOG_PROFILE:
                    //TODO
//                    ProfileCore.add(p.asInstanceOf[XLogProfilePack])
//                    if (conf.log_udp_profile) {
//                        System.out.println("DEBUG UDP PROFILE: " + p)
//                    }
                    break;

                case PackEnum.TEXT:
                    //TODO
//                    TextCore.add(p.asInstanceOf[TextPack])
//                    if (conf.log_udp_text) {
//                        System.out.println("DEBUG UDP TEXT: " + p)
//                    }
                    break;
                case PackEnum.ALERT:
                    //TODO
//                    AlertCore.add(p.asInstanceOf[AlertPack])
//                    if (conf.log_udp_alert) {
//                        System.out.println("DEBUG UDP ALERT: " + p)
//                    }
                    break;
                case PackEnum.OBJECT:
                    ObjectPack objectPack = (ObjectPack) p;
                    if (StringUtils.isBlank(objectPack.address)) {
                        objectPack.address = addr.getHostAddress();
                    }
                    if (objectPack.objHash == 0) {
                        objectPack.objHash = HashUtil.hash(objectPack.objName);
                    }
                    CounterManager counterManager = CounterManager.getInstance();
                    counterManager.addObjectTypeIfNotExist(objectPack);
                    String family = counterManager.getCounterEngine().getFamilyNameFromObjType(objectPack.objType);

                    objReceiveQueue.offer(LegacyMapper.toObjP(objectPack, family));
                    if (conf.isLegacyLogUdpObj()) {
                        log.debug("Legacy UDP counterPack received: {}", objectPack);
                    }
                    break;
                case PackEnum.PERF_STATUS:
                    //TODO
//                    StatusCore.add(p.asInstanceOf[StatusPack])
//                    if (conf.log_udp_status) {
//                        System.out.println("DEBUG UDP STATUS: " + p)
//                    }
                    break;
                case PackEnum.STACK:
                    //TODO
//                    StackAnalyzerCore.add(p.asInstanceOf[StackPack])
//                    if (conf.log_udp_stack) {
//                        System.out.println("DEBUG UDP STACK: " + p)
//                    }
                    break;
                case PackEnum.SUMMARY:
                    //TODO
//                    SummaryCore.add(p.asInstanceOf[SummaryPack])
//                    if (conf.log_udp_summary) {
//                        System.out.println("DEBUG UDP SUMMARY: " + p)
//                    }
                    break;
                case PackEnum.BATCH:
                    //TODO
//                    BatchCore.add(p.asInstanceOf[BatchPack])
//                    if (conf.log_udp_batch) {
//                        System.out.println("DEBUG UDP Batch: " + p)
//                    }
                    break;
                case PackEnum.SPAN_CONTAINER:
                    //TODO
//                    SpanCore.add(p.asInstanceOf[SpanContainerPack])
//                    if (conf.log_udp_span) {
//                        System.out.println("DEBUG UDP SPAN CONTAINER: " + p)
//                    }
                    break;
                default:
                    //TODO PackExtProcessChain.doChain(p);
            }
        }
    }

    static class NetData {
        byte[] data;
        InetAddress addr;

        NetData(byte[] data, InetAddress addr) {
            this.data = data;
            this.addr = addr;
        }
    }
}
