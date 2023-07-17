import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Image;

public class GoBang {
    // 定义五子棋游戏窗口
    private JFrame f = new JFrame("五子棋游戏");
    JPanel p1 = new JPanel();

    // 声明四个BufferedImage类，分别记录四张图片
    Image table;
    Image black;
    Image white;
    Image select;
    Image background;

    // 声明棋盘的宽和高
    final int TABLE_WIDTH = 530 + 235 + 200;
    final int TABLE_HEIGHT = 530 + 131;

    // 声明棋盘横向和纵向分别可以下多少子,他们都为15
    final int BOARD_SIZE = 15;

    // 声明变量，记录棋子对于x方向和y方向的偏移量
    final int X_OFFSET = 235 + 200;// 5
    final int Y_OFFSET = 131;// 6

    // 声明每个棋子占用棋盘的比率
    final int RATE = (TABLE_WIDTH - X_OFFSET) / BOARD_SIZE;

    // 声明一个二维数组，记录棋子,如果索引[i][j]处的值为 0——没有棋子 1-为白棋 2-为黑棋
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    // 同时声明一个空白棋盘
    private int[][] emptyboard = new int[BOARD_SIZE][BOARD_SIZE];

    // 声明红色选择框的坐标 该坐标其实是二维数组board中的索引
    private int selected_X = -1;
    private int selected_Y = -1;

    // 为了实现悔棋，用三维数组储存每一次落子数据,同时用一个step记录下到了第几步
    private int step = 0;
    private int[][][] backBox = new int[BOARD_SIZE * BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];

    // 定义一个变量sign标记游戏是否能进行
    private int sign = 0;

    // 声明一个参数决定现在是人机下还是人人下载,0表示人人对战，1表示人机对战
    private int Robot = 0;

    // 定义一个机器人
    GoBangAI robot = new GoBangAI();

    // 声明选择框所需要的组件
    JPanel p = new JPanel();
    JTextField pointOut = new JTextField("游戏未开始");
    JButton beginButton = new JButton("开始新游戏");
    JButton giveUpButton = new JButton("认输");
    JButton backBattle = new JButton("返回上一棋");
    String[] boxname = { "人人对战", "人机对战" };
    JComboBox box = new JComboBox(boxname);

    // 自定义类，继承Jpanel
    private class ChessBoard extends JPanel {
        @Override
        public void paint(Graphics g) {
            // 绘图
            g.drawImage(background, 0, 0, p1.getWidth(), p1.getHeight(), p1);
            // 绘制棋盘
            g.drawImage(table, 230 + 200, 125, null);

            // 绘制选择框
            if (selected_X >= 0 && selected_Y >= 0) {
                g.drawImage(select, selected_X * RATE + X_OFFSET, selected_Y * RATE + Y_OFFSET, null);
            }

            // 绘制棋子
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    // 绘制黑棋
                    if (board[i][j] == 2) {
                        g.drawImage(black, i * RATE + X_OFFSET, j * RATE + Y_OFFSET, null);
                    }

                    // 绘制白棋
                    if (board[i][j] == 1) {
                        g.drawImage(white, i * RATE + X_OFFSET, j * RATE + Y_OFFSET, null);
                    }

                }
            }
        }
    }

    ChessBoard chessBoard = new ChessBoard();

    public void refreshBtnColor(Color whiteBtnColor, Color blackBtnColor, Color deleteBtnColor) {
        beginButton.setBackground(whiteBtnColor);
        giveUpButton.setBackground(blackBtnColor);
        backBattle.setBackground(deleteBtnColor);
    }
    // 准备各种监听器

    // 人机事件监听
    ItemListener robotListener = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            if (e.getItem().equals("人人对战")) {
                Robot = 0;
                copy(emptyboard, board);
                step = 0;
                sign = 0;
            } else if (e.getItem().equals("人机对战")) {
                Robot = 1;
                copy(emptyboard, board);
                step = 0;
                sign = 0;
            }
        };
    };

    ActionListener Listener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("开始新游戏")) {
                // 开始新游戏
                // 刷新按钮的颜色
                refreshBtnColor(Color.GREEN, Color.gray, Color.gray);
                // 将棋盘清空，并且将所有参数返回初始值
                copy(emptyboard, board);
                sign = 1;
                step = 0;
                chessBoard.repaint();
                // 更改提示
                pointOut.setText("游戏开始，请黑棋落子");
            } else if (e.getActionCommand().equals("认输")) {
                // 认输
                refreshBtnColor(Color.gray, Color.green, Color.gray);
                if (sign == 1) {
                    // 弹出是否确认认输弹窗
                    if (step % 2 == 0) {
                        CustomDialog a = new CustomDialog("黑棋认输", "未到山穷水尽时，胜负犹应未可知");
                        a.setVisible(true);
                    } else if (step % 2 == 1) {
                        CustomDialog a = new CustomDialog("白棋认输", "未到山穷水尽时，胜负犹应未可知");
                        a.setVisible(true);
                    }
                }
                ;

            } else if (e.getActionCommand().equals("返回上一棋")) {
                // 悔棋
                refreshBtnColor(Color.gray, Color.gray, Color.green);
                // 只有大于第一步时才能悔棋
                // 如果是人机对战每次悔棋悔两步，防止悔人机棋人机不动
                if (Robot == 0) {
                    if (step > 0) {
                        step = step - 1;
                        copy(backBox[step], board);
                    }
                    chessBoard.repaint();
                } else if (Robot == 1) {

                    if (step > 0) {
                        step = step - 2;
                        copy(backBox[step], board);
                        chessBoard.repaint();
                    }

                }
            }
        };

    };

    // 鼠标移动
    MouseMotionAdapter mouseMove = new MouseMotionAdapter() {
        // 当鼠标移动时会调用该方法
        @Override
        public void mouseMoved(java.awt.event.MouseEvent e) {
            // 限定选择框的移动范围
            if (e.getX() < TABLE_WIDTH - 20 && e.getY() < TABLE_HEIGHT - 20) {
                selected_X = (e.getX() - X_OFFSET) / RATE;
                selected_Y = (e.getY() - Y_OFFSET) / RATE;
                chessBoard.repaint();
            }
            if (step != 0 && step % 2 == 0) {
                pointOut.setText("请黑棋落子");
            } else if (step != 0 && step % 2 == 1) {
                pointOut.setText("请白棋落子");
            }

        }

    };

    // 鼠标点击
    MouseAdapter mouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (sign == 1) {
                int xPos = (e.getX() - X_OFFSET) / RATE;
                int yPos = (e.getY() - Y_OFFSET) / RATE;
                // 用Robot进入人机模式
                if (Robot == 0) {
                    // 用step作为判断下什么棋子的判断，偶数下黑棋，奇数下白棋
                    if ((step % 2 == 1) && (board[xPos][yPos] == 0)) {
                        board[xPos][yPos] = 1;
                        step += 1;
                        // 将更改的数据储存进棋局记录器中
                        copy(board, backBox[step]);
                        // 更改提示
                        pointOut.setText("请黑棋落子");
                        // 判断是否连成五子并决定输赢
                        if (isFiveChess(xPos, yPos, 1)) {
                            JOptionPane.showMessageDialog(f, "白棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                                    new ImageIcon("D:/java课设/期末课程设计/img/white.png"));
                            // 将标记设置为0
                            sign = 0;
                            // 刷新棋盘
                            copy(emptyboard, board);
                            step = 0;
                            // 更改提示语
                            pointOut.setText("游戏未开始");
                        }

                    } else if (step % 2 == 0 && (board[xPos][yPos] == 0)) {
                        board[xPos][yPos] = 2;
                        step += 1;
                        // 将更改的数据储存进棋局记录器中
                        copy(board, backBox[step]);
                        // 更改提示
                        pointOut.setText("请白棋落子");
                        // 判断是否连成五子并决定输赢
                        if (isFiveChess(xPos, yPos, 2)) {
                            JOptionPane.showMessageDialog(f, "黑棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                                    new ImageIcon("D:/java课设/期末课程设计/img/black.png"));
                            // 刷新棋盘
                            copy(emptyboard, board);
                            step = 0;
                            sign = 0;
                            // 更改提示语
                            pointOut.setText("游戏未开始");
                        }

                    }
                } else if (Robot == 1) {
                    // 用step作为判断下什么棋子的判断，偶数玩家下，奇数机器下
                    if ((step % 2 == 1) && (board[xPos][yPos] == 0)) {
                        board[xPos][yPos] = 1;
                        step += 1;
                        // 将更改的数据储存进棋局记录器中
                        copy(board, backBox[step]);
                        // 更改提示
                        pointOut.setText("请黑棋落子");
                        // AI落子
                        board = robot.robotStep(board);
                        step += 1;
                        pointOut.setText("请白棋落子");
                        chessBoard.repaint();
                        // 判断是否连成五子并决定输赢
                        if (isFiveChess(xPos, yPos, 1)) {
                            JOptionPane.showMessageDialog(f, "白棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                                    new ImageIcon("D:/java课设/期末课程设计/img/white.png"));
                            // 将标记设置为0
                            sign = 0;
                            // 刷新棋盘
                            copy(emptyboard, board);
                            step = 0;
                            // 更改提示语
                            pointOut.setText("游戏未开始");
                        } else if (isFiveChess(xPos, yPos, 2)) {
                            JOptionPane.showMessageDialog(f, "黑棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                                    new ImageIcon("D:/java课设/期末课程设计/img/black.png"));
                            // 刷新棋盘
                            copy(emptyboard, board);
                            step = 0;
                            sign = 0;
                            // 更改提示语
                            pointOut.setText("游戏未开始");
                        }

                    } else if (step % 2 == 0 && (board[xPos][yPos] == 0)) {
                        board[xPos][yPos] = 2;
                        step += 1;
                        // 将更改的数据储存进棋局记录器中
                        copy(board, backBox[step]);
                        // 更改提示
                        pointOut.setText("请白棋落子");
                        // AI落子
                        copy(robot.robotStep(board), board);
                        step += 1;
                        pointOut.setText("请黑棋落子");
                        copy(board, backBox[step]);
                        chessBoard.repaint();
                        // 判断是否连成五子并决定输赢
                        if (isFiveChess(robot.xPos, robot.yPos, 1)) {
                            JOptionPane.showMessageDialog(f, "白棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                                    new ImageIcon("D:/java课设/期末课程设计/img/white.png"));
                            // 将标记设置为0
                            sign = 0;
                            // 刷新棋盘
                            copy(emptyboard, board);
                            step = 0;
                            // 更改提示语
                            pointOut.setText("游戏未开始");
                        }
                        if (isFiveChess(xPos, yPos, 2)) {
                            JOptionPane.showMessageDialog(f, "黑棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                                    new ImageIcon("D:/java课设/期末课程设计/img/black.png"));
                            // 刷新棋盘
                            copy(emptyboard, board);
                            step = 0;
                            sign = 0;
                            // 更改提示语
                            pointOut.setText("游戏未开始");
                        }

                    }

                }

                chessBoard.repaint();
                if (isFull(board)) {
                    // 刷新棋盘
                    sign = 0;
                    copy(emptyboard, board);
                    step = 0;
                    ImageIcon icon = new ImageIcon("D:/java课设/期末课程设计/img/dogfallpic.jpg");
                    JOptionPane.showMessageDialog(f, "棋逢对手", "平局", JOptionPane.DEFAULT_OPTION, icon);
                    pointOut.setText("游戏未开始");
                }
            }
        }

        public void mouseExited(java.awt.event.MouseEvent e) {
            selected_X = -1;
            selected_Y = -1;
            chessBoard.repaint();
        }
    };

    public void init() {
        // 组装视图，编写逻辑

        // 先设置一个空白棋局，给悔棋backboard的第一步给定空白棋局

        copy(emptyboard, backBox[0]);

        // 按键的事件监听
        beginButton.addActionListener(Listener);
        giveUpButton.addActionListener(Listener);
        backBattle.addActionListener(Listener);

        // 组装棋盘
        try {
            table = new ImageIcon("D:/java课设/期末课程设计/img/chessboard2.jpg").getImage();
            white = ImageIO.read(new File("D:/java课设/期末课程设计/img/white.png"));
            black = ImageIO.read(new File("D:/java课设/期末课程设计/img/black.png"));
            select = ImageIO.read(new File("D:/java课设/期末课程设计/img/selected.gif"));
            background = ImageIO.read(new File("D:/java课设/期末课程设计/img/background.jpg"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 处理鼠标移动
        chessBoard.addMouseMotionListener(mouseMove);

        // 处理鼠标点击
        chessBoard.addMouseListener(mouseClick);
        // 组装所有组件
        // 组装窗口左边
        chessBoard.setPreferredSize(new Dimension(1300, 1000));
        box.addItemListener(robotListener);
        p1.setSize(new Dimension(1300, 1000));
        p1.add(chessBoard);
        Container c = f.getContentPane();
        c.setLayout(new FlowLayout());
        c.add(p1);
        // 组装窗口右边
        pointOut.setEditable(false);
        Dimension ButtonDim = new Dimension(150, 40);
        box.setPreferredSize(ButtonDim);
        Font fn = new Font("宋体", Font.BOLD, 14);
        box.setFont(fn);
        pointOut.setPreferredSize(ButtonDim);
        pointOut.setFont(fn);
        beginButton.setPreferredSize(ButtonDim);
        giveUpButton.setPreferredSize(ButtonDim);
        backBattle.setPreferredSize(ButtonDim);
        p.setLayout(new FlowLayout());
        p.setPreferredSize(new Dimension(150, 1000));
        p.add(pointOut);
        p.add(beginButton);
        p.add(giveUpButton);
        p.add(backBattle);
        p.add(box);
        c.add(p);

        f.pack();
        f.setVisible(true);
    }

    public static void main(String[] args) {
        GoBang a = new GoBang();
        a.init();

    }

    // 定义一个复制并返回二维数组的方法
    public void copy(int[][] board, int[][] board2) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board2[i][j] = board[i][j];
            }
        }
    }

    public int[][] expenseArray(int[][] board) {
        // 为了处理isFiveChess中坐标越界的情况，我们将它左右上下各扩大四格
        int[][] temp = new int[BOARD_SIZE + 8][BOARD_SIZE + 8];
        for (int i = 4; i < BOARD_SIZE + 4; i++) {
            for (int j = 4; j < BOARD_SIZE + 4; j++) {
                temp[i][j] = board[i - 4][j - 4];
            }
        }
        return temp;
    }

    // 定义判断是否连成五子的方法
    public boolean isFiveChess(int x, int y, int sign) {
        // 定义一个count记录连续的棋子个数
        int count = 0;
        // 处理坐标防止越界
        x = x + 4;
        y = y + 4;
        int[][] temp = expenseArray(board);
        // 从落子处往四个方位搜查
        // 左右
        for (int i = 0; i < 9; i++) {
            if (temp[x - 4 + i][y] == sign) {
                count += 1;
            } else {
                // 碰到空格或者其他颜色的棋子，count清零
                count = 0;
            }
            ;
            if (count == 5) {
                return true;
            }

        }
        // 上下
        count = 0;
        for (int i = 0; i < 9; i++) {
            if (temp[x][y - 4 + i] == sign) {
                count += 1;
            } else {
                count = 0;
            }
            if (count == 5) {
                return true;
            }
        }
        // 右斜上下
        count = 0;
        for (int i = 0; i < 9; i++) {
            if (temp[x - 4 + i][y - 4 + i] == sign) {
                count += 1;
            } else {
                count = 0;
            }
            if (count == 5) {
                return true;
            }
        }
        // 左斜上下
        count = 0;
        for (int i = 0; i < 9; i++) {
            if (temp[x + 4 - i][y - 4 + i] == sign) {
                count += 1;
            } else {
                count = 0;
            }
            if (count == 5) {
                return true;
            }
        }
        // 没有连成五子返回false
        return false;
    }

    public boolean isFull(int[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    class CustomDialog extends JDialog implements ActionListener {
        String title;
        String content;
        String ok = "确定";
        String cancel = "取消";

        public CustomDialog(String title, String content) {
            this.title = title;
            this.content = content;
            int width = 45, height = 45;
            // 创建1个图标实例,注意image目录要与src同级
            ImageIcon icon = new ImageIcon("D:/java课设/期末课程设计/img/whiteFlag.jpg");
            icon.setImage(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
            // 1个图片标签,显示图片
            JLabel jlImg = new JLabel(icon);
            jlImg.setSize(width, height);
            jlImg.setBounds(20, 44, width, height);
            // 1个文字标签,显示文本
            JLabel jLabel = new JLabel(content);
            jLabel.setFont(new Font("楷书", Font.BOLD, 11));
            // 设置文字的颜色为蓝色
            jLabel.setForeground(Color.black);
            jLabel.setBounds(75, 43, 180, 45);
            JButton okBut = new JButton(ok);
            JButton cancelBut = new JButton(cancel);
            okBut.setBackground(Color.LIGHT_GRAY);
            okBut.setBorderPainted(false);
            okBut.setBounds(65, 126, 98, 31);
            cancelBut.setBounds(175, 126, 98, 31);
            cancelBut.setBackground(Color.LIGHT_GRAY);
            cancelBut.setBorderPainted(false);
            // 给按钮添加响应事件
            okBut.addActionListener(this);
            cancelBut.addActionListener(this);
            // 向对话框中加入各组件
            add(jlImg);
            add(jLabel);
            add(okBut);
            add(cancelBut);
            // 对话框流式布局
            setLayout(null);
            // 窗口左上角的小图标
            setIconImage(icon.getImage());
            // 设置标题
            setTitle(title);
            // 设置为模态窗口,此时不能操作父窗口
            setModal(true);
            // 设置对话框大小
            setSize(300, 210);
            // 对话框局域屏幕中央
            setLocationRelativeTo(null);
            // 对话框不可缩放
            setResizable(false);
            // 点击对话框关闭按钮时,销毁对话框
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        // 当按钮被点击时会执行下面的方法
        @Override
        public void actionPerformed(ActionEvent e) {
            // 判断是不是确定按钮被点击
            if (ok.equals(e.getActionCommand())) {
                // 对话框不可见
                this.setVisible(false);
                if (title.equals("黑棋认输")) {
                    sign = 0;
                    JOptionPane.showMessageDialog(f, "白棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                            new ImageIcon("D:/java课设/期末课程设计/img/white.png"));
                    // 刷新棋盘
                    copy(emptyboard, board);
                    step = 0;
                    pointOut.setText("游戏未开始");
                } else if (title.equals("白棋认输")) {
                    JOptionPane.showMessageDialog(f, "黑棋获胜", "完成对局", JOptionPane.DEFAULT_OPTION,
                            new ImageIcon("D:/java课设/期末课程设计/img/black.png"));
                    // 刷新棋盘
                    copy(emptyboard, board);
                    step = 0;
                    pointOut.setText("游戏未开始");
                }

            }
            if (cancel.equals(e.getActionCommand())) {
                this.setVisible(false);
                this.dispose();
            }
        }
    }
}
