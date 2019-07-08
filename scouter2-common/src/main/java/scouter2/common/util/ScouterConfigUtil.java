package scouter2.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ScouterConfigUtil {

	public static Properties appendSystempProps(Properties props) {
		Properties appendedProps = new Properties();

		Map<Object, Object> args = new HashMap<Object, Object>();
		args.putAll(System.getenv());
		args.putAll(System.getProperties());

		appendedProps.putAll(args);

		for (Object o : props.keySet()) {
			String key = (String) o;
			String value = (String) props.get(key);
			appendedProps.put(key, new ParamText(StringUtils.trim(value)).getText(args));
		}
		return appendedProps;
	}
}
