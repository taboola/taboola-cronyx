package com.taboola.cronyx.util.crash;

import org.crsh.cli.spi.Completion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CompleterUtils {

    public static Completion getCompletion(List<String> strings, String prefix) throws Exception {
        Supplier<Stream<String>> stringsStream = () -> strings.stream().filter(s -> s.startsWith(prefix));
        if (stringsStream.get().count() == 0) {
            return Completion.create();
        } else if (stringsStream.get().count() == 1) {
            return Completion.create(stringsStream.get().findFirst().get().substring(prefix.length()), true);
        } else {
            String pref = longestCommonPrefix(stringsStream.get().collect(Collectors.toList()));
            if (pref.length() > prefix.length()){
                return Completion.create(pref.substring(prefix.length()), false);
            }
            Map<String, Boolean> map = new HashMap<>();
            stringsStream.get().forEach(s -> map.put(s.substring(pref.length()>prefix.length() ? pref.length() : prefix.length()), true));
            return Completion.create(pref, map);
        }
    }

    private static String longestCommonPrefix(List<String> strings) {
        if (strings.size() == 0) {
            return "";   // Or maybe return null?
        }

        for (int prefixLen = 0; prefixLen < strings.get(0).length(); prefixLen++) {
            char c = strings.get(0).charAt(prefixLen);
            for (int i = 1; i < strings.size(); i++) {
                if ( prefixLen >= strings.get(i).length() ||
                        strings.get(i).charAt(prefixLen) != c ) {
                    // Mismatch found
                    return strings.get(i).substring(0, prefixLen);
                }
            }
        }
        return strings.get(0);
    }

}
