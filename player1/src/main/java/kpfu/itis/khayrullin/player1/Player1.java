package kpfu.itis.khayrullin.player1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Player1 extends JPanel implements ActionListener, KeyListener {

    private static boolean showTitleScreen = true;
    private static boolean playing = false;
    private static boolean gameOver = false;

    private boolean upPressed = false;
    private boolean downPressed = false;

    private int ballX = 250;
    private int ballY = 250;
    private int diameter = 20;
    private int ballDeltaX = -1;
    private int ballDeltaY = 3;

    private int playerOneX = 25;
    private static int playerOneY = 250;
    private int playerOneWidth = 10;
    private int playerOneHeight = 50;

    private int playerTwoX = 465;
    private static int playerTwoY = 250;
    private int playerTwoWidth = 10;
    private int playerTwoHeight = 50;

    private int paddleSpeed = 5;

    private int playerOneScore = 0;
    private int playerTwoScore = 0;

    private static ServerSocket ss;
    private static Socket s;
    private static DataInputStream din;
    private static DataOutputStream dout;
    private static final int PORT = 8080;

    //construct a Player 1 panel
    public Player1() {

        setBackground(Color.BLACK);

        //listen to key presses
        setFocusable(true);
        addKeyListener(this);

        //call step() 60 fps
        Timer timer = new Timer(1000 / 60, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            step();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void step() throws IOException {
        if (playing) {
            //move player 1
            if (upPressed) {
                if (playerOneY - paddleSpeed > 0) {
                    playerOneY -= paddleSpeed;
                }
            }
            if (downPressed) {
                if (playerOneY + paddleSpeed + playerOneHeight < getHeight()) {
                    playerOneY += paddleSpeed;
                }
            }

            //where will the ball be after it moves?
            int nextBallLeft = ballX + ballDeltaX;
            int nextBallRight = ballX + diameter + ballDeltaX;
            int nextBallTop = ballY + ballDeltaY;
            int nextBallBottom = ballY + diameter + ballDeltaY;

            int playerOneRight = playerOneX + playerOneWidth;
            int playerOneTop = playerOneY;
            int playerOneBottom = playerOneY + playerOneHeight;

            float playerTwoLeft = playerTwoX;
            float playerTwoTop = playerTwoY;
            float playerTwoBottom = playerTwoY + playerTwoHeight;


            //ball bounces off top and bottom of screen
            if (nextBallTop < 0 || nextBallBottom > getHeight()) {
                ballDeltaY *= -1;
            }

            //will the ball go off the left side?
            if (nextBallLeft < playerOneRight) {
                //is it going to miss the paddle?
                if (nextBallTop > playerOneBottom || nextBallBottom < playerOneTop) {

                    playerTwoScore++;

                    if (playerTwoScore == 3) {
                        playing = false;
                        gameOver = true;
                    }

                    ballX = 250;
                    ballY = 250;
                } else {
                    ballDeltaX *= -1;
                }
            }

            //will the ball go off the right side?
            if (nextBallRight > playerTwoLeft) {
                //is it going to miss the paddle?
                if (nextBallTop > playerTwoBottom || nextBallBottom < playerTwoTop) {

                    playerOneScore++;

                    if (playerOneScore == 3) {
                        playing = false;
                        gameOver = true;
                    }

                    ballX = 250;
                    ballY = 250;
                } else {
                    ballDeltaX *= -1;
                }
            }

            //move the ball
            ballX += ballDeltaX;
            ballY += ballDeltaY;
        }
        //stuff has moved, tell this JPanel to repaint itself
        repaint();
    }

    //paint the game screen
    public void paintComponent(Graphics g){

        super.paintComponent(g);
        g.setColor(Color.WHITE);

        if (showTitleScreen) {
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 45));
            g.drawString("Ping Pong", 140, 200);
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
            g.drawString("Waiting for second player...", 120, 250);
        }
        else if (playing) {
            int playerOneRight = playerOneX + playerOneWidth;
            int playerTwoLeft =  playerTwoX;

            //draw dashed line down center
            for (int lineY = 0; lineY < getHeight(); lineY += 50) {
                g.drawLine(250, lineY, 250, lineY+25);
            }

            //draw "goal lines" on each side
            g.drawLine(playerOneRight, 0, playerOneRight, getHeight());
            g.drawLine(playerTwoLeft, 0, playerTwoLeft, getHeight());

            //draw the scores
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
            g.drawString(String.valueOf(playerOneScore), 100, 100);
            g.drawString(String.valueOf(playerTwoScore), 400, 100);

            //draw the ball
            g.fillOval(ballX, ballY, diameter, diameter);

            //draw the paddles
            g.fillRect(playerOneX, playerOneY, playerOneWidth, playerOneHeight);
            g.fillRect(playerTwoX, playerTwoY, playerTwoWidth, playerTwoHeight);
        }
        else if (gameOver) {

            g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
            g.drawString(String.valueOf(playerOneScore), 100, 100);
            g.drawString(String.valueOf(playerTwoScore), 400, 100);

            g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
            if (playerOneScore > playerTwoScore) {
                g.drawString("Player 1 Wins!", 165, 200);
            }
            else {
                g.drawString("Player 2 Wins!", 165, 200);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(playing){
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (playing) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = false;
            }
            else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = false;
            }
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Player1());
        frame.setBounds(200, 200, 500, 600);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        javax.swing.SwingUtilities.invokeLater(Player1::createAndShowGUI);
        try {
            ss = new ServerSocket(PORT);
            s = ss.accept();
            showTitleScreen = false;
            playing = true;
            din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true){
            dout.writeInt(playerOneY);
            playerTwoY = din.readInt();
        }

    }
}
