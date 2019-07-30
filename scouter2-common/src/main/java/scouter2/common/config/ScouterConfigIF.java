package scouter2.common.config;

import scouter2.common.helper.Props;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface ScouterConfigIF {
    List<ConfigItem> getAllConfigs();
    void refresh(Props props);
}
