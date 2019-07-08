package scouter2.collector.common;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import scouter2.common.config.ScouterConfigIF;

import java.util.Properties;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public class ConfigPublisher {
    private static ConfigPublisher instance = new ConfigPublisher();

    private MutableList<ScouterConfigIF> configs = Lists.mutable.empty();

    private ConfigPublisher() {}

    public static ConfigPublisher getInstance() {
        return instance;
    }

    public void refresh(Properties properties) {
        configs.forEach(conf -> {
            conf.refresh(properties);
        });
    }

    public void register(ScouterConfigIF configIF) {
        configs.add(configIF);
    }
}
