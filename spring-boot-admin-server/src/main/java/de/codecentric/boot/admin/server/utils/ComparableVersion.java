/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.utils;

import java.util.Comparator;
import java.util.Scanner;

public class ComparableVersion implements Comparable<ComparableVersion> {
    private final String version;

    private ComparableVersion(String version) {
        this.version = version;
    }

    @Override
    public int compareTo(ComparableVersion other) {
        Scanner s1 = new Scanner(this.version);
        Scanner s2 = new Scanner(other.version);
        s1.useDelimiter("[.\\-+]");
        s2.useDelimiter("[.\\-+]");

        while (s1.hasNext() && s2.hasNext()) {
            int c;
            if (s1.hasNextInt() && s2.hasNextInt()) {
                c = Integer.compare(s1.nextInt(), s2.nextInt());
            } else {
                c = s1.next().compareTo(s2.next());
            }
            if (c != 0) {
                return c;
            }
        }

        if (s1.hasNext()) {
            return 1;
        } else if (s2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static ComparableVersion valueOf(String s) {
        return new ComparableVersion(s);
    }

    public static Comparator<String> ascending() {
        return Comparator.comparing(ComparableVersion::valueOf);
    }
}
