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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import scouter2.collector.main.CollectorMain;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 09/09/2019
 */
@SpringBootConfiguration
@ComponentScan
@Slf4j
public class SpringBoot implements CommandLineRunner {
    private final CollectorMain collectorMain;

    public SpringBoot(CollectorMain collectorMain) {
        this.collectorMain = collectorMain;
    }

    @Override
    public void run(String... args) throws Exception {
        collectorMain.start(args);
    }

}
