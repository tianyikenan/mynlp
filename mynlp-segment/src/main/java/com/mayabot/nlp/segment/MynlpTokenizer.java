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

package com.mayabot.nlp.segment;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;

/**
 * MynlpTokenizer分词器接口,只需要作用是把一句话或一小段有限长度的文本分词.
 *
 * 切词器的实现类是个无状态，所以可以多线程安全，同一个配置只需要一个实例共享使用。
 * 所以一个固定的算法的切分,只需要一个实例。
 *
 * @author jimichan
 * @see PipelineTokenizer
 * @see MynlpTokenizerBuilder
 * @see PipelineTokenizerBuilder
 */
public interface MynlpTokenizer {

    /**
     * 对text进行分词，结构保存在List<WordTerm>,
     * token不会对target清空，只会add。
     *
     * @param text     分词的文本
     * @param consumer 对WordTerm对象消费者
     */
    void token(char[] text, Consumer<WordTerm> consumer);


    /**
     * 对text是String字符串的一个便捷包装.
     *
     * @param text     分词文本
     * @param consumer token消费者
     */
    default void token(String text, Consumer<WordTerm> consumer) {
        token(text.toCharArray(), consumer);
    }

    /**
     * 便捷方法。不适用于超大文本
     * @param text 需要分词的文本
     * @return Sentence 中文句子
     */
    default Sentence parse(String text) {
        if (text == null || text.isEmpty()) {
            return new Sentence();
        }

        List<WordTerm> target = Lists.newArrayListWithExpectedSize(Math.min(text.length() / 2, 4));

        token(text.toCharArray(), target::add);

        return new Sentence(target);
    }

}
