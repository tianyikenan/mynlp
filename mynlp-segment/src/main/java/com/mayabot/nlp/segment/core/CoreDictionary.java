/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mayabot.nlp.segment.core;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResouceExternalizable;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TreeMap;

/**
 * 内置的核心词典，词数大约20+万
 * key 存储 word
 * value 存储 词频
 *
 * @author jimichan
 */
@Singleton
public class CoreDictionary extends NlpResouceExternalizable {

    private InternalLogger logger = InternalLoggerFactory.getInstance(CoreDictionary.class);

    public final String path = "core-dict/CoreDict.txt";

    /**
     * 词频总和
     */
    public int totalFreq;

    private DoubleArrayTrieStringIntMap trie;

    @Inject
    public CoreDictionary(MynlpEnv mynlp) throws Exception {
        this.restore(mynlp);
    }

    @Override
    @SuppressWarnings(value = "rawtypes")
    public void loadFromSource(MynlpEnv mynlp) throws Exception {
        NlpResource dictResource = mynlp.loadResource(path);

        //词和词频
        TreeMap<String, Integer> map = new TreeMap<>();

        int maxFreq = 0;

        try (CharSourceLineReader reader = dictResource.openLineReader()) {
            while (reader.hasNext()) {
                String line = reader.next();

                String[] param = line.split("\\s");

                Integer count = Integer.valueOf(param[1]);
                map.put(param[0], count);
                maxFreq += count;
            }
        }

        this.totalFreq = maxFreq;

        //补齐，确保ID顺序正确
        for (String label : DictionaryAbsWords.allLabel()) {
            if (!map.containsKey(label)) {
                map.put(label, 1);
            }
        }

        if (map.isEmpty()) {
            throw new RuntimeException("not found core dict file ");
        }

        this.trie = new DoubleArrayTrieStringIntMap(map);
    }

    @Override
    public String sourceVersion(MynlpEnv mynlp) {
        return Hashing.murmur3_32().newHasher().
                putString(mynlp.hashResource(path), Charsets.UTF_8).
                putString("v2", Charsets.UTF_8)
                .hash().toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(totalFreq);
        trie.save(out);
        out.flush();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        this.totalFreq = in.readInt();
        this.trie = new DoubleArrayTrieStringIntMap(in);
    }

    /**
     * 获取条目
     *
     * @param key
     * @return
     */
    public int wordFreq(String key) {
        int c = trie.get(key);
        if (c == -1) {
            c = 0;
        }
        return c;
    }

    /**
     * 获取条目
     *
     * @param wordID
     * @return
     */
    public int wordFreq(int wordID) {
        return trie.get(wordID);
    }


    public int wordId(CharSequence key) {
        return trie.indexOf(key);
    }

    public int wordId(CharSequence key, int pos, int len, int nodePos) {
        return trie.indexOf(key, pos, len, nodePos);
    }

    public int wordId(char[] chars, int pos, int len) {
        return trie.indexOf(chars, pos, len);
    }

    public int wordId(char[] keyChars, int pos, int len, int nodePos) {
        return trie.indexOf(keyChars, pos, len, nodePos);
    }


    /**
     * 是否包含词语
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return trie.indexOf(key) >= 0;
    }

    /**
     * 获取词语的ID
     *
     * @param word
     * @return
     */
    public int getWordID(String word) {
        return trie.indexOf(word);
    }

    public DoubleArrayTrieStringIntMap.DATMapMatcherInt match(char[] text, int offset) {
        return trie.match(text, offset);
    }

    public int size() {
        return trie.size();
    }
}
