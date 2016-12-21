package kpfu.itis.khayrullin.player2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Player2 extends JPanel implements ActionListener, KeyListener{

    private boolean playing = true;
    private boolean gameOver = false;

    private boolean wPressed = false;
    private boolean sPressed = false;

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

    private static Socket s;
    private static DataInputStream din;
    private static DataOutputStream dout;
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;

    //construct a PongPanel
    public Player2(){
        setBackground(Color.BLACK);

        //listen to key presses
        setFocusable(true);
        addKeyListener(this);

        //call step() 60 fps
        Timer timer = new Timer(1000/60, this);
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
        if(playing){
            //move player 2
            if (wPressed) {
                if (playerTwoY-paddleSpeed > 0) {
                    playerTwoY -= paddleSpeed;
                }
            }
            if (sPressed) {
                if (playerTwoY + paddleSpeed + playerTwoHeight < getHeight()) {
                    playerTwoY += paddleSpeed;
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

                    playerTwoScore ++;

                    if (playerTwoScore == 3) {
                        playing = false;
                        gameOver = true;
                    }

                    ballX = 250;
                    ballY = 250;
                }
                else {
                    ballDeltaX *= -1;
                }
            }

            //will the ball go off the right side?
            if (nextBallRight > playerTwoLeft) {
                //is it going to miss the paddle?
                if (nextBallTop > playerTwoBottom || nextBallBottom < playerTwoTop) {

                    playerOneScore ++;

                    if (playerOneScore == 3) {
                        playing = false;
                        gameOver = true;
                    }

                    ballX = 250;
                    ballY = 250;
                }
                else {
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

    @Override
    public void paintComponent(Graphics g){

        super.paintComponent(g);
        g.setColor(Color.WHITE);
        if (playing) {


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
            if (e.getKeyCode() == KeyEvent.VK_W) {
                wPressed = true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_S) {
                sPressed = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (playing) {
            if (e.getKeyCode() == KeyEvent.VK_W) {
                wPressed = false;
            }
            else if (e.getKeyCode() == KeyEvent.VK_S) {
                sPressed = false;
            }
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Player2());
        frame.setBounds(200, 200, 500, 600);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        javax.swing.SwingUtilities.invokeLater(Player2::createAndShowGUI);
        try {
            s = new Socket(ADDRESS, PORT);
            din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true){
            playerOneY = din.readInt();
            dout.writeInt(playerTwoY);
        }
    }
}
