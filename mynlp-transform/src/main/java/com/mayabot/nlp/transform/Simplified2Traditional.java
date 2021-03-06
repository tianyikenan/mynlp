package com.mayabot.nlp.transform;

import javax.inject.Singleton;
import java.util.TreeMap;

/**
 * 简体转繁体的词典
 *
 * @author jimichan
 */
@Singleton
public class Simplified2Traditional extends BaseTransformDictionary {

    public static final String rsName = "ts-dict/s2t.txt";

    public Simplified2Traditional() {
    }

    @Override
    public TreeMap<String, String> loadDictionary() {
        return loadFromResouce(rsName);
    }
}
