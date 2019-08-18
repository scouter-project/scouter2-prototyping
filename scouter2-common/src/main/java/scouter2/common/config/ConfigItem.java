package scouter2.common.config;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public class ConfigItem {
    String key;
    String value;
    String defaultValue;
    String description;
    int configValueType;

    public ConfigItem() {
    }

    public ConfigItem(String key, String value, String defaultValue, String description, int configValueType) {
        this.key = key;
        this.value = value;
        this.defaultValue = defaultValue;
        this.description = description;
        this.configValueType = configValueType;
    }
}
