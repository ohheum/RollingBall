package project;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class RollingBall  implements Runnable {
	private ArrayBlockingQueue<MyMessage> dataQueue;	// receive packets from server
	public RollingBall()
	{
		makeGUI();
		String ip = "127.0.0.1";
		String port = "31944";
		dataQueue = new ArrayBlockingQueue<MyMessage>( 1000 );
		Server server;
		try {
			server = new Server(dataQueue, null, port);
			server.initializer();
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	JPanel thePanel;
	private void makeGUI()
	{
		thePanel = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g); // paint background, border
				g.drawOval((int)(Position[0]*10000.0) + 500, (int)(Position[1]*10000.0) + 500, 10, 10);
			}
		};
		thePanel.setPreferredSize(new Dimension(800,800));
		
		JFrame frame = new JFrame("Rolling Ball");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(thePanel);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
    public double Radius = 0.03;
    public double  SamplePeriod = 1.0 / 256.0;
    public double [] [] RotationMatrix = new double [] [] {  	{1.0, 0.0, 0.0},
                                   															{0.0, 1.0, 0.0},
                                   															{0.0, 0.0, 1.0} };
    
    public double [] Position = new double [] { 0.0, 0.0 };
    public double [] Velocity = new double [] { 0.0, 0.0 };
    
	public void processMsg(MyMessage msg)
	{
        double [] angularVelocity = new double [] { 0.0, 0.0 };    
        angularVelocity[0] = msg.rotationMatrix[0][0] * msg.gyroVector[0] + msg.rotationMatrix[0][1] * msg.gyroVector[1] +  msg.rotationMatrix[0][2] * msg.gyroVector[2];
        angularVelocity[1] = msg.rotationMatrix[1][0] * msg.gyroVector[0] + msg.rotationMatrix[1][1] * msg.gyroVector[1] + msg.rotationMatrix[1][2] * msg.gyroVector[2];

        // Velocity on Earth XY plane (cross product with Earth Z axis)
        Velocity[0] = angularVelocity[1] * Radius;
        Velocity[1] = -1.0f * angularVelocity[0] * Radius;

        // Update position (integrate velocity)
        Position[0] += Velocity[0] * SamplePeriod;
        Position[1] += Velocity[1] * SamplePeriod;
        System.out.println("Position update: " + Position[0]  + " " + Position[1] );
        thePanel.repaint();
	}
	
	public void start()
	{
		Thread thread = new Thread( this );		
		thread.start();		
	}
	
	@Override
    public void run()			
    {
		while( true)
		{
			try
            {	
				MyMessage msg = dataQueue.take();				
				processMsg(msg);
            }
            catch ( InterruptedException e )
            {
	            e.printStackTrace();
            }
		}	
    }


	public static void main(String[] args) {
		RollingBall ball = new RollingBall();
		ball.start();
	}
}
