import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;


public class MyCanvas  extends Canvas{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final int sz = 60;
	Color myGreen;
	
	
	public MyCanvas() {
		setBackground( new Color(237,234,166));
		setSize(600, 420);
		myGreen = new Color(16,181,71);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		// === draw a clean board 
		clearBoard();
		
		// draw cords 
		for(int i=0; i < 9; i++ ) {
			g.setColor(Color.black);
			g.setFont(new Font("Tahoma", Font.BOLD, 20));
			g.drawString((i+1)+"", 40 + (i * sz) , 340);
		}
		
		String [] alpha = {"A","B","C","D","E"};
		for(int j=0; j < 5; j++) {
			g.setColor(Color.black);
			g.setFont(new Font("Tahoma", Font.BOLD, 20));
			g.drawString( alpha[j] , 570 , 55 + (j * sz));
		}
		
		
//		setupInitialBoard();
		
		/*
		g.setColor(Color.black);
		g.fillRect(20, 20, sz, sz);
		
		g.setFont(new Font("Tahoma", Font.BOLD, 30));
		g.setColor(Color.red);
		g.drawString("R", 40, 60);
		*/
//		g.drawRect(30, 30, 50, 50);
//		g.drawRect(80, 30, 50, 50);
		
	}
	
	void setupInitialBoard() {
		Graphics g = this.getGraphics();
		boolean isFill = true;
		
		for(int i=0; i < 5 ; i++){
//			isFill = !(isFill);
			
			for(int j=0; j < 9 ; j++){
				
				if(isFill){
					g.setColor(Color.black);
					g.fillRect(20 + (j * sz), 20 + (i * sz), sz, sz);
					isFill = false;
				}else{
					g.setColor(Color.white);
					g.fillRect(20 + (j * sz), 20 + (i * sz), sz, sz);
					isFill = true;
				}
				
				if(i < 2){		// draw R
					g.setFont(new Font("Tahoma", Font.BOLD, 30));
					g.setColor(Color.red);
					g.drawString("R", 40 + (sz * j), 60 + ( sz * i));
					
				} else if (i == 2){	// draw G and R
					
					if(j < 4){
						g.setFont(new Font("Tahoma", Font.BOLD, 30));
						g.setColor(myGreen);
						g.drawString("G", 40 + (sz * j), 60 + ( sz * i));
					} else if( j > 4){
						g.setFont(new Font("Tahoma", Font.BOLD, 30));
						g.setColor(Color.red);
						g.drawString("R", 40 + (sz * j), 60 + ( sz * i));
					}
					
				} else {	//draw G
					g.setFont(new Font("Tahoma", Font.BOLD, 30));
					g.setColor(myGreen);
					g.drawString("G", 40 + (sz * j), 60 + ( sz * i));
				}
				
				
				
				
			}
		}
	}
	
	
	void clearBoard() {
		Graphics g = this.getGraphics();
		
		// draw board
		boolean isFill = true;
		for(int i=0; i < 5 ; i++){
//					isFill = !(isFill);
			for(int j=0; j < 9 ; j++){
				
				if(isFill){
					g.setColor(Color.black);
					g.fillRect(20 + (j * sz), 20 + (i * sz), sz, sz);
					isFill = false;
				}else{
					g.setColor(Color.white);
					g.fillRect(20 + (j * sz), 20 + (i * sz), sz, sz);
					isFill = true;
				}
				
			}
		}
				
	}
	
	void updateToken(int cols, int rows, int whoIsPlaying, boolean isFill) {
		Graphics g = this.getGraphics();
		//val= 1:G, 2:R, 3:empty
		
		if(isFill){
			g.setColor(Color.black);
		}else{
			g.setColor(Color.white);
		}
		g.fillRect(20 + ( sz * cols), 20 + ( sz * rows ), sz, sz);

		if( whoIsPlaying == 1 ){
			g.setFont(new Font("Tahoma", Font.BOLD, 30));
			g.setColor(myGreen);
			g.drawString("G", 40 + ( sz * cols ) , 60 + ( sz * rows ));
		} if( whoIsPlaying == 2 ){
			g.setFont(new Font("Tahoma", Font.BOLD, 30));
			g.setColor(Color.red);
			g.drawString("R", 40 + ( sz * cols ) , 60 + ( sz * rows ));
		}
		
		
		
		
	}

}
