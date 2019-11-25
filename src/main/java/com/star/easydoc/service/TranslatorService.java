package com.star.easydoc.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.components.ServiceManager;
import com.star.easydoc.config.EasyJavadocConfigComponent;
import com.star.easydoc.model.EasyJavadocConfiguration;
import com.star.easydoc.service.translator.Translator;
import com.star.easydoc.service.translator.impl.BaiduTranslator;
import com.star.easydoc.service.translator.impl.JinshanTranslator;
import com.star.easydoc.service.translator.impl.YoudaoTranslator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wangchao
 * @date 2019/08/25
 */
public class TranslatorService {

    private EasyJavadocConfiguration config = ServiceManager.getService(EasyJavadocConfigComponent.class).getState();

    private Map<String, Translator> translatorMap = ImmutableMap.<String, Translator>builder()
        .put("百度翻译", new BaiduTranslator())
        .put("金山翻译", new JinshanTranslator())
        .put("有道翻译", new YoudaoTranslator())
        .build();

    public String translate(String source) {
        List<String> words = split(source);
        if (isCustomMode(words)) {
            // 有自定义单词，使用默认模式，单个单词翻译
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                String res = getFromCustom(word);
                if (StringUtils.isBlank(res)) {
                    res = getFromOthers(word);
                }
                if (StringUtils.isBlank(res)) {
                    res = word;
                }
                sb.append(res);
            }
            return sb.toString();
        } else {
            // 没有自定义单词，使用整句翻译，翻译更准确
            return getFromOthers(StringUtils.join(words, StringUtils.SPACE));
        }
    }

    private List<String> split(String word) {
        word = word.replaceAll("(?<=[^A-Z])[A-Z][^A-Z]", "_$0");
        word = word.replaceAll("[A-Z]{2,}", "_$0");
        word = word.replaceAll("_+", "_");
        return Arrays.asList(word.split("_"));
    }

    /**
     * 是否自定义模式
     *
     * @param words 单词
     * @return boolean
     */
    private boolean isCustomMode(List<String> words) {
        return CollectionUtils.containsAny(config.getWordMap().keySet(), words);
    }

    private String getFromCustom(String word) {
        return config.getWordMap().get(word.toLowerCase());
    }

    private String getFromOthers(String word) {
        Translator translator = translatorMap.get(config.getTranslator());
        if (Objects.isNull(translator)) {
            return StringUtils.EMPTY;
        }
        return translator.translate(word);
    }

}
