
public class GoBangAI {
    // 定义所需数据
    public static final int CHESSBOARD_SIZE = 15;
    public int xPos, yPos;
    public static int FIRST = 1;// 先手，-1表示机器，1表示人类，与Location类中的对应
    private int[][] chessboard = new int[CHESSBOARD_SIZE][CHESSBOARD_SIZE];// 与界面棋盘对应，0代表空，-1代表机器，1代表人类
    private int[][] score = new int[CHESSBOARD_SIZE][CHESSBOARD_SIZE];// 每个位置得分

    public int[][] robotStep(int[][] board) {
        turn1(board);
        chessboard = copy(board);
        searchLocation();
        int[][] step = new int[15][15];
        step = copy(chessboard);
        turn2(step);
        return step;
    }

    public void searchLocation() {
        // 每次都初始化下score评分数组
        for (int i = 0; i < CHESSBOARD_SIZE; i++) {
            for (int j = 0; j < CHESSBOARD_SIZE; j++) {
                score[i][j] = 0;
            }
        }

        // 每次机器找寻落子位置，评分都重新算一遍（虽然算了很多多余的，因为上次落子时候算的大多都没变）
        // 先定义一些变量
        int playerNumber = 0;// 五元组中的黑棋数量
        int robotnumber = 0;// 五元组中的白棋数量
        int tempTuplescore = 0;// 五元组得分临时变量

        int goalX = -1;// 目标位置x坐标
        int goalY = -1;// 目标位置y坐标
        int maxScore = -1;// 最大分数

        // 1.扫描横向的15个行
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                int k = j;
                while (k < j + 5) {

                    if (chessboard[i][k] == -1)
                        robotnumber++;
                    else if (chessboard[i][k] == 1)
                        playerNumber++;

                    k++;
                }
                tempTuplescore = tupleScore(playerNumber, robotnumber);
                // 为该五元组的每个位置添加分数
                for (k = j; k < j + 5; k++) {
                    score[i][k] += tempTuplescore;
                }
                // 置零
                playerNumber = 0;// 五元组中的黑棋数量
                robotnumber = 0;// 五元组中的白棋数量
                tempTuplescore = 0;// 五元组得分临时变量
            }
        }

        // 2.扫描纵向15行
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                int k = j;
                while (k < j + 5) {
                    if (chessboard[k][i] == -1)
                        robotnumber++;
                    else if (chessboard[k][i] == 1)
                        playerNumber++;

                    k++;
                }
                tempTuplescore = tupleScore(playerNumber, robotnumber);
                // 为该五元组的每个位置添加分数
                for (k = j; k < j + 5; k++) {
                    score[k][i] += tempTuplescore;
                }
                // 置零
                playerNumber = 0;// 五元组中的黑棋数量
                robotnumber = 0;// 五元组中的白棋数量
                tempTuplescore = 0;// 五元组得分临时变量
            }
        }

        // 3.扫描右上角到左下角上侧部分
        for (int i = 14; i >= 4; i--) {
            for (int k = i, j = 0; j < 15 && k >= 0; j++, k--) {
                int m = k;
                int n = j;
                while (m > k - 5 && k - 5 >= -1) {
                    if (chessboard[m][n] == -1)
                        robotnumber++;
                    else if (chessboard[m][n] == 1)
                        playerNumber++;

                    m--;
                    n++;
                }
                // 注意斜向判断的时候，可能构不成五元组（靠近四个角落），遇到这种情况要忽略掉
                if (m == k - 5) {
                    tempTuplescore = tupleScore(playerNumber, robotnumber);
                    // 为该五元组的每个位置添加分数
                    for (m = k, n = j; m > k - 5; m--, n++) {
                        score[m][n] += tempTuplescore;
                    }
                }

                // 置零
                playerNumber = 0;// 五元组中的黑棋数量
                robotnumber = 0;// 五元组中的白棋数量
                tempTuplescore = 0;// 五元组得分临时变量

            }
        }

        // 4.扫描右上角到左下角下侧部分
        for (int i = 1; i < 15; i++) {
            for (int k = i, j = 14; j >= 0 && k < 15; j--, k++) {
                int m = k;
                int n = j;
                while (m < k + 5 && k + 5 <= 15) {
                    if (chessboard[n][m] == -1)
                        robotnumber++;
                    else if (chessboard[n][m] == 1)
                        playerNumber++;

                    m++;
                    n--;
                }
                // 注意斜向判断的时候，可能构不成五元组（靠近四个角落），遇到这种情况要忽略掉
                if (m == k + 5) {
                    tempTuplescore = tupleScore(playerNumber, robotnumber);
                    // 为该五元组的每个位置添加分数
                    for (m = k, n = j; m < k + 5; m++, n--) {
                        score[n][m] += tempTuplescore;
                    }
                }
                // 置零
                playerNumber = 0;// 五元组中的黑棋数量
                robotnumber = 0;// 五元组中的白棋数量
                tempTuplescore = 0;// 五元组得分临时变量

            }
        }

        // 5.扫描左上角到右下角上侧部分
        for (int i = 0; i < 11; i++) {
            for (int k = i, j = 0; j < 15 && k < 15; j++, k++) {
                int m = k;
                int n = j;
                while (m < k + 5 && k + 5 <= 15) {
                    if (chessboard[m][n] == -1)
                        robotnumber++;
                    else if (chessboard[m][n] == 1)
                        playerNumber++;

                    m++;
                    n++;
                }
                // 注意斜向判断的时候，可能构不成五元组（靠近四个角落），遇到这种情况要忽略掉
                if (m == k + 5) {
                    tempTuplescore = tupleScore(playerNumber, robotnumber);
                    // 为该五元组的每个位置添加分数
                    for (m = k, n = j; m < k + 5; m++, n++) {
                        score[m][n] += tempTuplescore;
                    }
                }

                // 置零
                playerNumber = 0;// 五元组中的黑棋数量
                robotnumber = 0;// 五元组中的白棋数量
                tempTuplescore = 0;// 五元组得分临时变量

            }
        }

        // 6.扫描左上角到右下角下侧部分
        for (int i = 1; i < 11; i++) {
            for (int k = i, j = 0; j < 15 && k < 15; j++, k++) {
                int m = k;
                int n = j;
                while (m < k + 5 && k + 5 <= 15) {
                    if (chessboard[n][m] == -1)
                        robotnumber++;
                    else if (chessboard[n][m] == 1)
                        playerNumber++;

                    m++;
                    n++;
                }
                // 注意斜向判断的时候，可能构不成五元组（靠近四个角落），遇到这种情况要忽略掉
                if (m == k + 5) {
                    tempTuplescore = tupleScore(playerNumber, robotnumber);
                    // 为该五元组的每个位置添加分数
                    for (m = k, n = j; m < k + 5; m++, n++) {
                        score[n][m] += tempTuplescore;
                    }
                }

                // 置零
                playerNumber = 0;// 五元组中的黑棋数量
                robotnumber = 0;// 五元组中的白棋数量
                tempTuplescore = 0;// 五元组得分临时变量

            }
        }

        // 从空位置中找到得分最大的位置
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (chessboard[i][j] == 0 && score[i][j] > maxScore) {
                    goalX = i;
                    goalY = j;
                    maxScore = score[i][j];

                }
            }
        }
        xPos = goalX;
        yPos = goalY;
        chessboard[goalX][goalY] = -1;
    }

    // 各种五元组情况评分表
    public int tupleScore(int playerNumber, int robotnumber) {
        // 1.既有人类落子，又有机器落子，判分为0
        if (playerNumber > 0 && robotnumber > 0) {
            return 0;
        }
        // 2.全部为空，没有落子，判分为7
        if (playerNumber == 0 && robotnumber == 0) {
            return 7;
        }
        // 3.机器落1子，判分为35
        if (robotnumber == 1) {
            return 35;
        }
        // 4.机器落2子，判分为800
        if (robotnumber == 2) {
            return 800;
        }
        // 5.机器落3子，判分为15000
        if (robotnumber == 3) {
            return 15000;
        }
        // 6.机器落4子，判分为800000
        if (robotnumber == 4) {
            return 800000;
        }
        // 7.人类落1子，判分为15
        if (playerNumber == 1) {
            return 15;
        }
        // 8.人类落2子，判分为400
        if (playerNumber == 2) {
            return 400;
        }
        // 9.人类落3子，判分为1800
        if (playerNumber == 3) {
            return 1800;
        }
        // 10.人类落4子，判分为100000
        if (playerNumber == 4) {
            return 100000;
        }
        return -1;// 若是其他结果肯定出错了。这行代码根本不可能执行
    }

    public int[][] copy(int[][] board) {
        int[][] temp = new int[CHESSBOARD_SIZE][CHESSBOARD_SIZE];
        for (int i = 0; i < CHESSBOARD_SIZE; i++) {
            for (int j = 0; j < CHESSBOARD_SIZE; j++) {
                temp[i][j] = board[i][j];
            }
        }
        return temp;
    }

    public void turn1(int[][] board) {
        for (int i = 0; i < CHESSBOARD_SIZE; i++) {
            for (int j = 0; j < CHESSBOARD_SIZE; j++) {
                if (board[i][j] == 2) {
                    board[i][j] = 1;
                } else if (board[i][j] == 1) {
                    board[i][j] = -1;
                }

            }
        }
    }

    public void turn2(int[][] board) {
        for (int i = 0; i < CHESSBOARD_SIZE; i++) {
            for (int j = 0; j < CHESSBOARD_SIZE; j++) {
                if (board[i][j] == 1) {
                    board[i][j] = 2;
                } else if (board[i][j] == -1) {
                    board[i][j] = 1;
                }

            }
        }
    }
}
