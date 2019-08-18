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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-17
 */
public class NextLogIdFinder {

    private static final String patternValue = "ThrottleConfig\\.of\\(\\s*\"S(\\d+)\"";
    private static final Pattern pattern = Pattern.compile(patternValue);

    public static void main(String[] args) throws IOException {
        List<String> idList = new ArrayList<>();

        File srcRoot = new File("./scouter2-collector/src/main/java");
        String[] extensions = {"java"};
        Collection<File> files = FileUtils.listFiles(srcRoot, extensions, true);
        for (File file : files) {
            String contents = FileUtils.readFileToString(file, "utf-8");
            Matcher matcher = pattern.matcher(contents);
            while (matcher.find()) {
                idList.add(matcher.group(1));
            }
        }

        TreeSet<Integer> idSet =idList.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(TreeSet::new));

        int next = idSet.stream().mapToInt(v -> v).max().orElseGet(() -> 0) + 1;

        System.out.println("==========================");
        System.out.println("Next Log Key : S" + StringUtils.leftPad(next + "", 4, '0'));
        System.out.println("==========================");
    }
}
