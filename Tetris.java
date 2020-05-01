import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class Tetris extends JPanel implements ActionListener, KeyListener {

    // represents colors
    final static Color backgroundColour = Color.black;
    final static Color frameBlockColor1 = Color.darkGray;
    final static Color frameBlockColor2 = Color.gray;
    final static Color tetrimino1Color = Color.red;
    final static Color tetrimino2Color = Color.lightGray;
    final static Color tetrimino3Color = Color.cyan;
    final static Color tetrimino4Color = Color.yellow;
    final static Color tetrimino5Color = Color.magenta;
    final static Color tetrimino6Color = Color.blue;
    final static Color tetrimino7Color = Color.green;

    Timer timer = new Timer(400, this);

    static Block [][] board = new Block[10][20];    // represent game board

    private static class Block {        // internal class represent single block on the board
        boolean isMoving = true;
        Color color;
        Block(Color c) {
            color = c;
        }
        int posX,posY;
        boolean center; }

    static boolean stillFalling = false;    // true if actual blocks are falling dawn
    static boolean rotate = false;          // if pressed rotate button
    static int score = 0;                   // counting points;


    static void freezeAll() {   // freezing every blocks on the board
        for(int y=0; y<20; y++)
            for(int x=0; x<10; x++)
                if(board[x][y]!=null && board[x][y].isMoving)
                    board[x][y].isMoving = false;
        stillFalling = false;
        checkScore();
    }

    static void checkColision() {   // checking every possible collision if happends freeze it
        for (int x = 0; x < 10; x++)
            if(board[x][19]!=null && board[x][19].isMoving) {
                freezeAll();
                return; }

        for (int y = 19; y >= 0; y--)
            for (int x = 0; x < 10; x++)
                if(board[x][y]!=null && board[x][y].isMoving && board[x][y+1]!=null && !board[x][y+1].isMoving) {
                    freezeAll();
                    return; }
    }

    static void oneStepDown() {     // blocks fall one block down
        for (int y = 18; y >= 0; y--)
            for (int x = 0; x < 10; x++)
                if (board[x][y] != null && board[x][y].isMoving && board[x][y + 1] == null) {
                    board[x][y + 1] = new Block(board[x][y].color);
                    board[x][y+1].center = board[x][y].center;
                    board[x][y] = null;
                }
    }

    static void move() {        // move blocks to right, left of down
        switch(des) {
            case LEFT:
                for(int y=0; y<20; y++)     // check if any block is on last position
                    if(board[0][y]!=null && board[0][y].isMoving)
                        return;

                for(int y=19; y>=0; y--)
                    for(int x=1; x<10; x++)
                        if(board[x][y] != null && board[x][y].isMoving && board[x-1][y] == null) {
                            board[x-1][y] = new Block(board[x][y].color);
                            board[x-1][y].center = board[x][y].center;
                            board[x][y] = null;
                        } break;
            case RIGHT:
                for(int y=0; y<20; y++)     // check if any block is on last position
                    if(board[9][y]!=null && board[9][y].isMoving)
                        return;

                for(int y=19; y>=0; y--)
                    for(int x=8; x>=0; x--)
                        if(board[x][y] != null && board[x][y].isMoving && board[x+1][y] == null) {
                            board[x+1][y] = new Block(board[x][y].color);
                            board[x+1][y].center = board[x][y].center;
                            board[x][y] = null;
                        } break;
            case DOWN:
                for(int x=0; x<10; x++)
                    for(int y=18; y>=0; y--)
                        if(board[x][y] != null && board[x][y].isMoving && board[x][y+1] == null) {
                            board[x][y+1] = new Block(board[x][y].color);
                            board[x][y+1].center = board[x][y].center;
                            board[x][y] = null;
                        }
                break;
            case ANY: break;
            default: break;
        }
    }

    static void rotation() {        // rotate blocks
        Collection <Block> list = new ArrayList<Block>();

        for(int x=0; x<10; x++)                     // adding all blocks to the list
            for(int y=0; y<20; y++)
                if(board[x][y] != null && board[x][y].isMoving){
                    Block b = new Block(board[x][y].color);
                    b.center = board[x][y].center;
                    b.posX = x;
                    b.posY = y;
                    list.add(b);
                }

        int centerX = -1, centerY = -1;
        for ( Block b : list )        // the center block's coordinates
            if(b.center){
                centerX = b.posX;
                centerY = b.posY;
            }

        for ( Block b : list ) {        // changing the coordinates of each component
           if(!b.center) {
               if (b.posY == centerY) {     // 1,7,11,3
                   b.posY = centerY - (centerX - b.posX);
                   b.posX = centerX; }
               else if(b.posX == centerX){  // 2,9,5,4
                   b.posX = centerX + (centerY - b.posY);
                   b.posY = centerY; }
               else if(b.posX+1 == centerX && b.posY-1 == centerY)  // 6
                   b.posY = b.posY-2;
               else if(b.posX+1 == centerX && b.posY+1 == centerY)  // 8
                   b.posX = b.posX+2;
               else if(b.posX-1 == centerX && b.posY+1 == centerY)  // 10
                   b.posY = b.posY+2;
               else if(b.posX-1 == centerX && b.posY-1 == centerY) // 12
                   b.posX = b.posX-2;
           } }

        boolean canWriteDownBlocks = true;

        for (Block b : list)   // writing down the block back to the board
            if (b.posX < 0 || b.posX >=10 || b.posY < 0 || b.posY>=20 ) {
                canWriteDownBlocks = false;
                break;
            }

        if(canWriteDownBlocks) {
            // deleting old ones
            for(int x=0; x<10; x++)                     // adding all blocks to the list
                for(int y=0; y<20; y++)
                    if(board[x][y] != null && board[x][y].isMoving){
                        board[x][y] = null;
                    }

            // putting new ones
            for (Block b : list) {  // writing down the block back to the board
                board[b.posX][b.posY] = new Block(b.color);
                if (b.posX == centerX && b.posY == centerY)
                    board[b.posX][b.posY].center = true;
            }
        }
    }

    static void checkScore() {      // checking after freezeAll and increment score
        boolean lastCorrect = false;

        for (int y=19; y>=0; y--) {
            if (lastCorrect) y++;
            boolean correct = true;

            for (int x = 0; x < 10; x++)
                if (board[x][y] == null) correct = false;
            if (correct) {
                for (int i=0; i<10; i++)  // make null line
                    board[i][y] = null;

                for (int yi=y-1; yi>=0; yi--)   // move one step down every blocks
                    for (int xi=0; xi<10; xi++)
                        if (board[xi][yi]!=null) {
                            board[xi][yi+1] = new Block(board[xi][yi].color);
                            board[xi][yi+1].isMoving = false;
                            board[xi][yi] = null; }

                lastCorrect = true;
                score++;

            } else lastCorrect = false;
        }
    }

    static boolean checkLose() {        // return true if cannot generate new blocks
        if (board[3][1]!=null || board[4][1]!=null || board[5][1]!=null || board[6][1]!=null || board[3][0]!=null || board[4][0]!=null || board[5][0]!=null || board[5][1]!=null) {
            for (int y=0; y<20; y++)
                for (int x=0; x<10; x++)
                    board[x][y] = null;
            return true;
        } else return false;
    }


    static destination des = destination.ANY;               // represents destination to move
    private enum destination { ANY, DOWN, RIGHT, LEFT; }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension d = getSize();

        int sideX = d.width / 12;       // block dimensions
        int sideY = d.height / 22;

        g.setColor(backgroundColour);                                       // fill panel
        g.fillRect(0, 0, d.width - 1, d.height - 1);

        for(int i=0; i<12; i++) {                                           // fill upper and lower parts of frame by block
            g.setColor(frameBlockColor1);
            g.fillRect(i * sideX, 0, sideX, sideY);
            g.fillRect(i * sideX, 21 * sideY, sideX, sideY);
            g.setColor(frameBlockColor2);
            g.fillRect(i * sideX + 5, 5, sideX - 10, sideY - 10);
            g.fillRect(i * sideX + 5, 21 * sideY + 5, sideX - 10, sideY - 10); }
        for(int i=0; i<22; i++) {                                           // fill right and left part of the frame by block
            g.setColor(frameBlockColor1);
            g.fillRect(0, i * sideY, sideX, sideY);
            g.fillRect(11 * sideX, i * sideY, sideX, sideY);
            g.setColor(frameBlockColor2);
            g.fillRect(5, i * sideY + 5, sideX - 10, sideY - 10);
            g.fillRect(11 * sideX + 5, i * sideY + 5, sideX - 10, sideY - 10); }

        // generate new blocks
        if(!stillFalling) {

            if (checkLose()) {      // show message is lose
                JOptionPane.showMessageDialog(null, "YOU LOSE !\n your points: " + score);
                score = 0;
                return;
            }

            Random generator = new Random();                    // draw sequences of the blocks
            int draw = generator.nextInt(7);
            switch (draw) {
                case 0: // I
                    board[3][1] = new Block(tetrimino1Color);
                    board[4][1] = new Block(tetrimino1Color);
                    board[4][1].center = true;
                    board[5][1] = new Block(tetrimino1Color);
                    board[6][1] = new Block(tetrimino1Color);
                    break;
                case 1: // T
                    board[3][0] = new Block(tetrimino2Color);
                    board[4][0] = new Block(tetrimino2Color);
                    board[4][0].center = true;
                    board[5][0] = new Block(tetrimino2Color);
                    board[4][1] = new Block(tetrimino2Color);
                    break;
                case 2: // O
                    board[4][0] = new Block(tetrimino3Color);
                    board[5][0] = new Block(tetrimino3Color);
                    board[4][1] = new Block(tetrimino3Color);
                    board[4][1].center =  true;
                    board[5][1] = new Block(tetrimino3Color);
                    break;
                case 3: // L
                    board[3][0] = new Block(tetrimino4Color);
                    board[4][0] = new Block(tetrimino4Color);
                    board[4][0].center = true;
                    board[5][0] = new Block(tetrimino4Color);
                    board[3][1] = new Block(tetrimino4Color);
                    break;
                case 4: // J
                    board[3][0] = new Block(tetrimino5Color);
                    board[4][0] = new Block(tetrimino5Color);
                    board[4][0].center = true;
                    board[5][0] = new Block(tetrimino5Color);
                    board[5][1] = new Block(tetrimino5Color);
                    break;
                case 5: // S
                    board[4][0] = new Block(tetrimino6Color);
                    board[5][0] = new Block(tetrimino6Color);
                    board[3][1] = new Block(tetrimino6Color);
                    board[4][1] = new Block(tetrimino6Color);
                    board[4][1].center = true;
                    break;
                case 6: // Z
                    board[3][0] = new Block(tetrimino7Color);
                    board[4][0] = new Block(tetrimino7Color);
                    board[4][1] = new Block(tetrimino7Color);
                    board[4][1].center = true;
                    board[5][1] = new Block(tetrimino7Color);
                    break;
            }

            stillFalling = true;
        }

        // check collisions an freeze if happends
        checkColision();

        if (des != destination.ANY) move();
        if(rotate) rotation();


        for(int x=0; x<10; x++)                             // display all blocks on the board
            for(int y=0; y<20; y++)
                if(board[x][y] != null) {
                    g.setColor(board[x][y].color.darker());
                    g.fillRect(x * sideX + sideX, y * sideY + sideY, sideX, sideY);
                    g.setColor(board[x][y].color);
                    g.fillRect(x * sideX + sideX + 5, y * sideY + sideY + 5, sideX - 10, sideY - 10); }

        if (des != destination.ANY || rotate) {
            des = destination.ANY;
            rotate = false;
            return; }

        oneStepDown();

        timer.start();
    }


    @Override
    public void actionPerformed(ActionEvent e) { repaint(); }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 38:
                rotate = true;
                repaint();
                break;
            case 40:
                des = destination.DOWN;
                repaint();
                break;
            case 39:
                des = destination.RIGHT;
                repaint();
                break;
            case 37:
                des = destination.LEFT;
                repaint();
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }


    public static void main(String[] args) {

        JFrame frame = new JFrame("TETRIS APP");
        Dimension dimension = new Dimension(12 * 35, 22 * 35);
        Tetris b = new Tetris();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(dimension);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.add(b);
        frame.addKeyListener(b);
    }

}
