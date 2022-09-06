package com.tagging.entity;

public class LineReplaceEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 行号
     */
    private String lineStr;

    /**
     * 替换内容
     */
    private String replaceStr;

    public String getLineStr() {
        return lineStr;
    }

    public void setLineStr(String lineStr) {
        this.lineStr = lineStr;
    }

    public String getReplaceStr() {
        return replaceStr;
    }

    public void setReplaceStr(String replaceStr) {
        this.replaceStr = replaceStr;
    }

    public LineReplaceEntity(String lineStr, String replaceStr) {
        this.lineStr = lineStr;
        this.replaceStr = replaceStr;
    }
}
