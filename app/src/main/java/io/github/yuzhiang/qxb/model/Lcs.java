package io.github.yuzhiang.qxb.model;

abstract class Lcs {
    String str1;
    String str2;
    int s1Len;
    int s2Len;
    int len;

    Lcs(String str1, String str2) {
        this.str1 = str1;
        this.str2 = str2;
        s1Len = str1.length();
        s2Len = str2.length();
    }

    abstract int calculate();

    abstract String Result();
}
