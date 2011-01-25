package jp.co.ziro.report;

public enum Type {

    TEXT(1),
    LOGNTEXT(2),
    IMAGE(3);
 
    private int myValue;
    private Type(int type) {
        myValue = type;
    }

    public int get() {
        return myValue;
    }
}
