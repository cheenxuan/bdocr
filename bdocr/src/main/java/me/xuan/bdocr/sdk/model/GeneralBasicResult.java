package me.xuan.bdocr.sdk.model;

import java.util.List;

/**
 * Author: xuan
 * Created on 2019/10/23 14:51.
 * <p>
 * Describe:
 */
public class GeneralBasicResult extends ResponseResult {
    protected List<? extends WordSimple> wordList;
    protected int wordsResultNumber;
    private int direction = -1;

    public GeneralBasicResult() {
    }

    public int getWordsResultNumber() {
        return this.wordsResultNumber;
    }

    public void setWordsResultNumber(int wordsResultNumber) {
        this.wordsResultNumber = wordsResultNumber;
    }

    public List<? extends WordSimple> getWordList() {
        return this.wordList;
    }

    public void setWordList(List<Word> wordList) {
        this.wordList = wordList;
    }

    public int getDirection() {
        return this.direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}