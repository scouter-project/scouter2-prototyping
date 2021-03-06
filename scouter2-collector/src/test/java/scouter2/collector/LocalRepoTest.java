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

package scouter2.collector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.config.ConfigXlog;
import scouter2.common.helper.Props;
import scouter2.common.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
@RunWith(SpringRunner.class)
@Import(LocalRepoTest.LocalRepoTestConfig.class)
public abstract class LocalRepoTest {

    public static final String DB_DIR = U.systemTmpDir() + RandomUtils.nextLong() + "/scouter2/database";

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("repoType", "local");
        Runtime.getRuntime().addShutdownHook(new Thread(LocalRepoTest::clearDb));
    }

    @AfterClass
    public static void afterClass() {
    }

    private static boolean clearCalled = false;
    public synchronized static void clearDb() {
        if (clearCalled) {
            return;
        }
        ThreadUtil.sleep(200);
        clearCalled = true;

        File file = new File(DB_DIR);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    ConfigCommon configCommon;

    @TestConfiguration
    @ComponentScan(basePackages = {
            "scouter2.collector.infrastructure.db.filedb",
            "scouter2.collector.infrastructure.db.mapdb",
            "scouter2.collector.infrastructure.repository.local",
            "scouter2.collector.config",
            "scouter2.collector.domain.obj"
    })
    public static class LocalRepoTestConfig {

        @Bean
        public ConfigCommon configCommon() {
            ConfigCommon configCommon = new ConfigCommon();

            Properties properties = new Properties();
            properties.setProperty("dbDir", DB_DIR);
            Props props = new Props(properties);
            configCommon.refresh(props);

            return configCommon;
        }

        @Bean
        public ConfigXlog configXlog() {
            ConfigXlog configXlog = new ConfigXlog();

            Properties properties = new Properties();
            properties.setProperty("_xlogGxidWriteBufferSize", "100");
            properties.setProperty("_xlogGxidWriteBufferKeepMillis", "500");
            properties.setProperty("_xlogProfileOffsetWriteBufferSize", "100");
            properties.setProperty("_xlogProfileOffsetWriteBufferKeepMillis", "500");
            Props props = new Props(properties);
            configXlog.refresh(props);

            return configXlog;
        }
    }
}
