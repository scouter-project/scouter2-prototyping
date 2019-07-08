package scouter2.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public class MiscUtil {

    private static final char UNDERSCORE = '_';
    private static final String UNDERSCORE_S = "_";

    public static String camelCaseToUnderscore(String str) {
        if (str == null) return null;

        String[] splits = StringUtils.splitByCharacterTypeCamelCase(str);
        if (splits.length <= 1) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str.length() * 2);
        for (String split : splits) {
            sb.append(split.toLowerCase());
            if (!split.endsWith(UNDERSCORE_S)) {
                sb.append(UNDERSCORE);
            }
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static String underscoreToCamelCase(String str) {
        if (str == null) return null;

        char[] chars = str.toCharArray();
        int leadingUnderscoreLength = 0;
        for (char ch : chars) {
            if (ch == UNDERSCORE) {
                leadingUnderscoreLength++;
            } else {
                break;
            }
        }

        String[] splits = StringUtils.split(str, '_');
        if (splits.length <= 1) {
            return StringUtils.repeat(UNDERSCORE, leadingUnderscoreLength) + str;
        }

        StringBuilder sb = new StringBuilder(str.length());
        sb.append(StringUtils.repeat(UNDERSCORE, leadingUnderscoreLength));
        for (int i = 0; i < splits.length; i++) {
            if (i == 0) {
                sb.append(splits[i]);
            } else {
                sb.append(StringUtils.capitalize(splits[i]));
            }
        }
        return sb.toString();
    }
}
