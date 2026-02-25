package io.github.yuzhiang.qxb.model;

public class SearchSimilarity {

    public static class LcSubsequence extends Lcs {
        int[][] flag;

        public LcSubsequence(String str1, String str2) {
            super(str1, str2);
        }

        public int calculate() {
            if (len != 0) {
                return len;
            }
            int[][] cell = new int[s1Len + 1][s2Len + 1];
            flag = new int[s1Len + 1][s2Len + 1];
            for (int i = 1; i <= s1Len; i++) {
                for (int j = 1; j <= s2Len; j++) {
                    if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                        cell[i][j] = cell[i - 1][j - 1] + 1;
                        flag[i][j] = 1;
                    } else if (cell[i - 1][j] >= cell[i][j - 1]) {
                        cell[i][j] = cell[i - 1][j];
                        flag[i][j] = 0;
                    } else {
                        cell[i][j] = cell[i][j - 1];
                        flag[i][j] = -1;
                    }
                }
            }
            len = cell[s1Len][s2Len];
            return len;
        }

        public String Result() {
            if (flag == null) {
                calculate();
            }
            int i = s1Len;
            int j = s2Len;
            String result = "";
            while (i > 0 && j > 0) {
                if (flag[i][j] == 1) {
                    result = str1.charAt(i - 1) + result;
                    i--;
                    j--;
                } else if (flag[i][j] == -1) {
                    j--;
                } else {
                    i--;
                }
            }
            return result;
        }

        public float similarity() {
            if (len == 0) {
                calculate();
            }
            return len / (float) s1Len * 100;
        }
    }

    public static class LcSubstring extends Lcs {
        int[] flag;

        public LcSubstring(String str1, String str2) {
            super(str1, str2);
        }

        public int calculate() {
            if (len != 0) {
                return len;
            }
            int[][] cell = new int[s1Len + 1][s2Len + 1];
            int currentMax = 0;
            flag = new int[2];
            for (int i = 1; i <= s1Len; i++) {
                for (int j = 1; j <= s2Len; j++) {
                    if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                        cell[i][j] = cell[i - 1][j - 1] + 1;
                        if (cell[i][j] > currentMax) {
                            currentMax = cell[i][j];
                            flag[0] = i;
                            flag[1] = j;
                        }
                    } else {
                        cell[i][j] = 0;
                    }
                }
            }
            len = currentMax;
            return len;
        }

        public String Result() {

            if (flag == null) {
                calculate();
            }
            int i = flag[0];
            int j = flag[1];
            String result = "";
            while (i > 0 && j > 0) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    result = str1.charAt(i - 1) + result;
                    i--;
                    j--;
                } else {
                    return result;
                }
            }
            return result;
        }

        public float similarity() {
            if (len == 0) {
                calculate();
            }
            return len / (float) s1Len * 100;
        }

    }

}
