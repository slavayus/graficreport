package com.ifmo.graficreport.apparapi.oopnbody;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

/**
 * NBody implementing demonstrating Aparapi kernels.
 * 
 * For a description of the NBody problem.
 * 
 * @see http://en.wikipedia.org/wiki/N-body_problem
 * 
 *      We use JOGL to render the bodies.
 * @see http://jogamp.org/jogl/www/
 * 
 * @author gfrost
 * 
 */
public class Main{

   public static class NBodyKernel extends Kernel {

      protected final float delT = 0.5f;

      protected final float espSqr = 10000.0f;

      private final Range range;

      public Body[] bodies;

      /**
       * Constructor initializes xyz and vxyz arrays.
       * 
       * @param _bodies
       */
      public NBodyKernel(Range _range) {
         range = _range;
         bodies = new Body[range.getGlobalSize(0)];

         final float maxDist = 20f;
         for (int body = 0; body < range.getGlobalSize(0); body++) {
            final float theta = (float) (Math.random() * Math.PI * 2);
            final float phi = (float) (Math.random() * Math.PI * 2);
            final float radius;
            final float seed = (float) (Math.random() * 0.01f) + 0.99f;
            if ((body % 2) == 0) {
               radius = ((float) (seed * maxDist)) / 1f;
            } else {
               radius = ((float) (seed * maxDist)) / 0.5f;
            }

            // get the 3D dimensional coordinates
            float x = (float) (radius * Math.cos(theta) * Math.sin(phi));
            float y = (float) (radius * Math.sin(theta) * Math.sin(phi));
            float z = (float) (radius * Math.cos(phi));

            // divide into two 'spheres of bodies' by adjusting x
            if ((body % 2) == 0) {
               x += maxDist * 10.0f;
               bodies[body] = new Body(x, y, z, 0.01f, false);
            } else {
               bodies[body] = new Body(x, y, z, 1f, true);
            }
         }

         Body.allBodies = bodies;
      }

      /**
       * Here is the kernel entrypoint. Here is where we calculate the position of each body
       */
      @Override public void run() {
         final int body = getGlobalId();

         float accx = 0.f;
         float accy = 0.f;
         float accz = 0.f;

         float myPosx = bodies[body].getX();
         float myPosy = bodies[body].getY();
         float myPosz = bodies[body].getZ();

         for (int i = 0; i < getGlobalSize(0); i++) {

            final float dx = bodies[i].getX() - myPosx;
            final float dy = bodies[i].getY() - myPosy;
            final float dz = bodies[i].getZ() - myPosz;
            final float invDist = rsqrt((dx * dx) + (dy * dy) + (dz * dz) + espSqr);
            final float s = bodies[i].getM() * invDist * invDist * invDist;
            accx = accx + (s * dx);
            accy = accy + (s * dy);
            accz = accz + (s * dz);
         }

         accx = accx * delT;
         accy = accy * delT;
         accz = accz * delT;
         bodies[body].setX(myPosx + (bodies[body].getVx() * delT) + (accx * .5f * delT));
         bodies[body].setY(myPosy + (bodies[body].getVy() * delT) + (accy * .5f * delT));
         bodies[body].setZ(myPosz + (bodies[body].getVz() * delT) + (accz * .5f * delT));

         bodies[body].setVx(bodies[body].getVx() + accx);
         bodies[body].setVy(bodies[body].getVy() + accy);
         bodies[body].setVz(bodies[body].getVz() + accz);
      }

      /**
       * Render all particles to the OpenGL context
       * 
       * @param gl
       */

      protected void render(GL2 gl) {
         gl.glBegin(GL2.GL_QUADS);
         int sz = range.getGlobalSize(0);
         for (int i = 0; i < sz; i++) {

            Body currBody = bodies[i];
            if(currBody.isHeavy())
               gl.glColor3f(1f, 0f, 0f);
            else
               gl.glColor3f(0f, 0f, 1f);

            gl.glTexCoord2f(0, 1);
            gl.glVertex3f(currBody.getX(), currBody.getY() + 1, currBody.getZ());
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(currBody.getX(), currBody.getY(), currBody.getZ());
            gl.glTexCoord2f(1, 0);
            gl.glVertex3f(currBody.getX() + 1, currBody.getY(), currBody.getZ());
            gl.glTexCoord2f(1, 1);
            gl.glVertex3f(currBody.getX() + 1, currBody.getY() + 1, currBody.getZ());

         }
         gl.glEnd();
      }
   }

   public static int width;

   public static int height;

   public static boolean running;

   public static Texture texture = null;

   public static void main(String _args[]) {
      final int bodyCount = Integer.getInteger("bodies", 8192);

      //final Main kernel = new Main(bodyCount);
      final NBodyKernel kernel = new NBodyKernel(Range.create(bodyCount));

      final JFrame frame = new JFrame("NBody");

      final JPanel panel = new JPanel(new BorderLayout());
      final JPanel controlPanel = new JPanel(new FlowLayout());
      panel.add(controlPanel, BorderLayout.SOUTH);

      final JButton startButton = new JButton("Start");

      startButton.addActionListener(new ActionListener(){
         @Override public void actionPerformed(ActionEvent e) {
            running = true;
            startButton.setEnabled(false);
         }
      });
      controlPanel.add(startButton);
      //controlPanel.add(new JLabel(kernel.getExecutionMode().toString()));

      controlPanel.add(new JLabel("   Particles"));
      controlPanel.add(new JTextField("" + bodyCount, 5));

      controlPanel.add(new JLabel("FPS"));
      final JTextField framesPerSecondTextField = new JTextField("0", 5);

      controlPanel.add(framesPerSecondTextField);
      controlPanel.add(new JLabel("Score("));
      final JLabel miniLabel = new JLabel("<html><small>calcs</small><hr/><small>&micro;sec</small></html>");

      controlPanel.add(miniLabel);
      controlPanel.add(new JLabel(")"));

      final JTextField positionUpdatesPerMicroSecondTextField = new JTextField("0", 5);

      controlPanel.add(positionUpdatesPerMicroSecondTextField);
      final GLCapabilities caps = new GLCapabilities(null);
      final GLProfile profile = caps.getGLProfile();
      caps.setDoubleBuffered(true);
      caps.setHardwareAccelerated(true);
      final GLCanvas canvas = new GLCanvas(caps);

      final Dimension dimension = new Dimension(Integer.getInteger("width", 742 - 64), Integer.getInteger("height", 742 - 64));
      canvas.setPreferredSize(dimension);

      final Perspective perspective = new Perspective(0f, 0f, -800f, 0f, 0f, 0f, 1f);

      KeyboardFocusManager.getCurrentKeyboardFocusManager()
              .addKeyEventDispatcher(new KeyEventDispatcher() {
                 @Override
                 public boolean dispatchKeyEvent(KeyEvent e) {
                    switch(e.getKeyCode()) {
                       case 37:
                          perspective.setPhi(perspective.getPhi() - 0.1f);
                          break;
                       case 38:
                          perspective.setTheta(perspective.getTheta() + 0.1f);
                          break;
                       case 39:
                          perspective.setPhi(perspective.getPhi() + 0.1f);
                          break;
                       case 40:
                          perspective.setTheta(perspective.getTheta() - 0.1f);
                          break;
                    }
                    return false;
                 }
              });

      canvas.addGLEventListener(new GLEventListener(){
         private double ratio;

         private int frames;

         private long last = System.currentTimeMillis();

         @Override public void dispose(GLAutoDrawable drawable) {

         }

         @Override public void display(GLAutoDrawable drawable) {

            final GL2 gl = drawable.getGL().getGL2();
            texture.enable(gl);
            texture.bind(gl);
            gl.glLoadIdentity();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glColor3f(1f, 1f, 1f);

            final GLU glu = new GLU();
            glu.gluPerspective(45f, ratio, 1f, 1000f);

            glu.gluLookAt(perspective.getXeye(), perspective.getYeye(), perspective.getZeye(), perspective.getXat(), perspective.getYat(), perspective.getZat(), 0f, 1f, 0f);
            if (running) {
               //Arrays.parallel(bodies.toArray(new Body[1])).forEach(b -> {b.nextMove();});
               kernel.execute(kernel.range);

            }
            kernel.render(gl);

            final long now = System.currentTimeMillis();
            final long time = now - last;
            frames++;

            if (time > 1000) { // We update the frames/sec every second
               if (running) {
                  final float framesPerSecond = (frames * 1000.0f) / time;
                  final int updatesPerMicroSecond = (int) ((framesPerSecond * bodyCount * bodyCount) / 1000000);
                  framesPerSecondTextField.setText(String.format("%5.2f", framesPerSecond));
                  positionUpdatesPerMicroSecondTextField.setText(String.format("%4d", updatesPerMicroSecond));
               }
               frames = 0;
               last = now;
            }
            gl.glFlush();

         }

         @Override public void init(GLAutoDrawable drawable) {
            final GL2 gl = drawable.getGL().getGL2();

            gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);

            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            try {
               final InputStream textureStream = Main.class.getResourceAsStream("/particle.jpg");
               texture = TextureIO.newTexture(textureStream, false, null);
               texture.enable(gl);
            } catch (final IOException e) {
               e.printStackTrace();
            } catch (final GLException e) {
               e.printStackTrace();
            }

         }

         @Override public void reshape(GLAutoDrawable drawable, int x, int y, int _width, int _height) {
            width = _width;
            height = _height;

            final GL2 gl = drawable.getGL().getGL2();
            gl.glViewport(0, 0, width, height);

            ratio = (double) width / (double) height;

         }

      });

      panel.add(canvas, BorderLayout.CENTER);
      frame.getContentPane().add(panel, BorderLayout.CENTER);
      final FPSAnimator animator = new FPSAnimator(canvas, 100);

      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);

      animator.start();

   }

}
