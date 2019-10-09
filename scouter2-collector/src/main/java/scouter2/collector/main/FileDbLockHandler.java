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

package scouter2.collector.main;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-28
 */
@Getter
@Slf4j
public class FileDbLockHandler {
    private static FileDbLockHandler instance = new FileDbLockHandler();
    private String dbDir;

    public static FileDbLockHandler getInstance() {
        return instance;
    }

    private FileDbLockHandler() {}

    private String getFileDbPath() {
        return null;
    }

    public void setDbDir(String dbDir) {
        this.dbDir = dbDir;
    }

    public boolean createLock() {
        File dir = new File(dbDir);
        if (!dir.canRead()) {
            dir.mkdirs();
        }
        File lock = new File(dbDir, "lock.dat");
        if (lock.exists() && lock.lastModified() < System.currentTimeMillis() - 5000) {
            lock.delete();
        }
        boolean created = false;
        try {
            created = lock.createNewFile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (created) {
            lock.deleteOnExit();
        } else {
            log.error("Can't lock the database");
            log.error("Please remove the lock : " + lock.getAbsoluteFile());
        }
        return created;
    }

    public void updateLock() {
        File lock = new File(dbDir, "lock.dat");
        if (lock.canWrite()) {
            lock.setLastModified(System.currentTimeMillis());
        }
    }
}
