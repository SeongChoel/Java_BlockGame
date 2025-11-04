package blockGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Game extends JFrame implements Runnable, KeyListener {

    StartPanel start = new StartPanel();
    EndPanel end = new EndPanel();
    GamePanel game;

    int mode = 1; //화면상태
    int score = 0;
    int stage = 1;
    int ball = 1;
    int block = 9;

    Game() {
        Thread t = new Thread(this);
        t.start();

        setSize(800, 800);
        setTitle("Hw5");
        addKeyListener(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (mode == 1) { //시작화면 -> 게임화면
                game = new GamePanel();
                mode = 2;
            } else if (mode == 3) { //게임오버화면 -> 시작화면
                mode = 1;
            }
            repaint();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (mode == 1) {
                add(start);
                remove(end);
                score = 0;
                requestFocus();
                setFocusable(true);

            } else if (mode == 2) {
                add(game);
                remove(start);
                remove(end);

                game.requestFocus();
                game.setFocusable(true);

            } else if (mode == 3) {
                add(end);
                remove(game);

                stage = 1;
                ball = 1;
                block = 9;

                requestFocus();
                setFocusable(true);
            } else { //다음 스테이지
                remove(game);

                stage++;
                ball = 1;
                block = (stage * 3) * (stage * 3);

                game = new GamePanel();
                add(game);
                game.requestFocus();
                game.setFocusable(true);
                mode = 2;
            }
            repaint();
            setVisible(true);
        }
    }


    private abstract class Object{

        boolean delete = false; //삭제할 오브젝트 표시
        abstract void draw(Graphics g, Color c);
        abstract void collision(Object o);
        abstract void update(float time);
    }

    private class BackGround extends JPanel{ //배경
        BackGround(){}

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, Color.BLACK, 0,getHeight(),new Color(131,129,169));
            g2.setPaint(gp);
            g2.fill(new Rectangle2D.Float(0,0,getWidth(), getHeight()));
        }

    }

    private class Block extends Object {
        float x, y, w, h;
        int random;
        int special;
        boolean remove;
        Block(float _x, float _y, float _w, float _h, boolean _remove) {
            x = _x;
            y = _y;
            w = _w;
            h = _h;
            remove = _remove;
            if (remove == true) { //remove true이면 특수블록이 되거나 안되거나
                random = (int) (Math.random() * 10);
                if (random > 6) {
                    special = 1;
                } else {
                    special = 0;
                }
            }
        }
        void collision(Object o) {}
        void update(float time) {}
        void draw(Graphics g, Color c) {
            g.setColor(c);
            g.fill3DRect((int)x, (int)y, (int)w, (int)h, true);
        }
    }

    private class Ball extends Object {
        float px = 395, py = 500;
        float prex = px, prey = py;
        float vx, vy;
        float ax = 0, ay = 0;
        float size = 10;
        float speed = 100;
        float totalSpeed;
        Ball() {
            vx = -speed - (stage * 70); //왼쪽으로
            vy = -speed - (stage * 70); //위로
            totalSpeed = (vx*vx) + (vy*vy);
        }

        void collision(Object o) {
            if(o instanceof Block) {
                Block b = (Block) o;

                //충돌 판정 범위 공 크기 만큼 더 넓게
                float x1 = b.x - size / 2;
                float x2 = b.x+b.w+size;
                float y1 = b.y - size / 2;
                float y2 = b.y+b.h+size;

                if(px>x1 && px<x2 && py>y1 && py <y2){
                    if(prey < y1){	py = y1;vy = -vy;} //위
                    if(prey > y2){	py = y2;vy = -vy;} //아래
                    if(prex < x1){	px = x1;vx = -vx;} //왼쪽
                    if(prex > x2){	px = x2;vx = -vx;} //오른쪽

                    if (b.remove == true) {
                        b.delete = true;
                    }

                    if(b.special == 1) {
                        Ball one = new Ball();
                        one.px = px;
                        one.py = py;

                        Ball two = new Ball();
                        two.px = px;
                        two.py = py;

                        one.vx = vx * 0.8f;
                        one.vy = (float)Math.sqrt(totalSpeed - (one.vx*one.vx));

                        two.vx = vx * 1.18f;
                        two.vy = (float)Math.sqrt(totalSpeed - (two.vx*two.vx));


                        game.objects.add(one);

                        game.objects.add(two);

                        ball += 2;
                    }
                }
            }
            if(this.py > 763) { //공이 바닥으로가면 삭제
                this.delete = true;
            }
        }

        void update(float time) {
            prex = px;
            prey = py;
            vx = vx + ax*time;
            vy = vy + ay*time;
            px = px + vx*time;
            py = py + vy*time;
        }

        void draw(Graphics g, Color c) {
            g.setColor(c);
            g.fillOval((int)(px - size), (int)(py - size), (int)size, (int)size);
        }
    }

    private class StartPanel extends BackGround{
        String st1 = "Java Programming";
        String st2 = "Homework #5";
        String st3 = "BLOCK BREAKER";
        String st4 = "Press Spacebar to Play";

        Font f1 = new Font("Stencil", Font.PLAIN, 45);
        Font f2 = new Font("Algerian", Font.BOLD, 80);
        Font f3 = new Font("Algerian", Font.BOLD, 30);

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.WHITE);
            g.setFont(f1);
            g.drawString(st1, 200, 180);
            g.drawString(st2, 255, 250);

            g.setFont(f2);
            g.drawString(st3, 50, 430);

            g.setColor(Color.RED);
            g.setFont(f3);
            g.drawString(st4, 230, 600);
        }
    }

    private class EndPanel extends BackGround {
        int highScore = 0;
        String got = "Game Over";
        String hst = "High Score : " + highScore;
        String cst = "Your Score : " + score;
        String st4 = "Press Spacebar to Replay";

        Font f1 = new Font("Stencil", Font.BOLD, 100);
        Font f2 = new Font("Algerian", Font.PLAIN, 40);
        Font f3 = new Font("Algerian", Font.BOLD, 30);

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(highScore <= score)
                highScore = score;

            hst = "High Score : " + highScore;
            cst = "Your Score : " + score;

            g.setColor(Color.RED);
            g.setFont(f1);
            g.drawString(got, 125, 250);

            g.setColor(Color.WHITE);
            g.setFont(f2);
            g.drawString(hst, 262, 430);
            g.drawString(cst, 262, 490);

            g.setColor(Color.RED);
            g.setFont(f3);
            g.drawString(st4, 218, 600);
        }
    }

    private class GamePanel extends BackGround implements KeyListener, Runnable{
        int move = 1; //1은 왼쪽, 0은 오른쪽
        int w = 786;
        int h = 763;
        int bw = 766;
        int bh = 360; //블록 세로영역
        int moving = 0;
        LinkedList <Object> objects = new LinkedList<>();
        Block bar = new Block(350,580,100,10,false);

        GamePanel() {
            Thread t = new Thread(this);
            t.start();
            requestFocus();
            setFocusable(true);
            addKeyListener(this);
            objects.add(new Ball());
            objects.add(new Block(0, 0, w, 10, false)); //위 벽
            objects.add(new Block(0, 0, 10, h, false)); //왼쪽 벽
            objects.add(new Block(w - 10, 0, 10, h, false)); //오른쪽 벽

            for (int i = 0; i < stage * 3; i++) {
                for (int j = 0; j < stage * 3; j++) {
                    objects.add(new Block(
                            10 + 5 * (j + 1) + ((bw - (stage * 3 + 1) * 5) / (stage * 3)) * j,
                            10 + 5 * (i + 1) + ((bh - (stage * 3) * 5) / (stage * 3)) * i,
                            (bw - (stage * 3 + 1) * 5) / (stage * 3),
                            (bh - (stage * 3) * 5) / (stage * 3), true));
                }
            }
        }

        public void keyTyped(KeyEvent e) {}
        public void keyReleased(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                move = 1;
                moving = 0;
            }else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                move = 0;
                moving = 0;
            }
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < objects.size(); i++) {
                Object b = objects.get(i);
                if (b instanceof Block && ((Block) b).delete == true) {
                    objects.remove(i);
                    block--;
                    score += 10;
                    if (block == 0) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mode = 4;
                    }
                }
                if (b instanceof Ball && ((Ball) b).delete == true) {
                    objects.remove(i);
                    ball--;
                    if (ball == 0) {
                        mode = 3;
                    }
                }
            }

            for (int i = 0; i < objects.size(); i++) {
                Object b = objects.get(i);
                if (b instanceof Block) {
                    if(((Block)b).special == 1) {
                        b.draw(g,  new Color(197,201,3));
                    }else {
                        if (((Block) b).remove == true) {
                            b.draw(g, new Color(150,100,150));
                        }else {
                            b.draw(g,  Color.GRAY);
                        }
                    }
                }
                if (b instanceof Ball) {
                    b.draw(g, Color.WHITE);
                }
            }
            bar.draw(g, new Color(148,100,100));
        }

        public void run() {
            while (true) {
                try {
                    if (move == 1 && moving < 40 && bar.x > 10) {
                        bar.x -= 1;
                        moving += 1;
                    }
                    if (move == 0 && moving < 40 && bar.x + bar.w < w-10) {
                        bar.x += 1;
                        moving += 1;
                    }
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                for (int i = 0; i < objects.size(); i++) {
                    Object b = objects.get(i);
                    b.update(0.001f);
                }

                for (int i = 0; i < objects.size(); i++) {
                    Object b = objects.get(i);
                    if (b instanceof Ball) {
                        for (int j = 0; j < objects.size(); j++) {
                            Object cb = objects.get(j);

                            b.collision(cb);
                        }
                        b.collision(bar);
                    }
                }
                repaint();

            }
        }
    }

}

public class Homework5 {

    public static void main(String[] args) {

        Game game = new Game();
        game.setResizable(false);
    }
}
