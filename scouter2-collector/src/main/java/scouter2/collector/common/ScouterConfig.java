package scouter2.collector.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scouter2.common.config.ConfigItem;
import scouter2.common.config.ScouterConfigIF;

import java.util.List;
import java.util.Properties;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Getter
@Slf4j
public class ScouterConfig implements ScouterConfigIF {

    private static ScouterConfig instance;

    int xlogQueueSize = 10000;
    int xlogRepoThreadCount = 1;

    public static ScouterConfig getInstance() {
        return instance;
    }

    private ScouterConfig() {};

    @Override
    public List<ConfigItem> getAllConfigs() {
        return null;
    }

    @Override
    public void refresh(Properties properties) {

    }
}
