package scouter2.common.config;

import java.util.List;
import java.util.Properties;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface ScouterConfigIF {
    List<ConfigItem> getAllConfigs();
    void refresh(Properties properties);
}
