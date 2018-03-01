import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class BonzeeGame implements ActionListener {
	
	// Class to denote the coordinates on the Board
	class Coord {
		int x;
		int y;
		public Coord(int a, int b) {
			x = a;
			y = b;
		}
	}
	
	// Class that contains information about a player's movement.
	class Movement {
		int rowFrom;
		int colFrom;
		int rowTo;
		int colTo;
		String direction;
		int hval=0;	//heuristic value
		public Movement() { }
		public Movement(int rf, int cf, int rt, int ct) {
			this.rowFrom = rf;
			this.colFrom = cf;
			this.rowTo = rt;
			this.colTo = ct;
		}
	}
	
	private final static int MAX_COLS = 9;
	private final static int MAX_ROWS = 5;
	private final static int MAX_DEPTH = 7;
	
	// 2D array for Board.
	int [][] board = new int[MAX_COLS][MAX_ROWS];
	int [][] tboard = new int[MAX_COLS][MAX_ROWS];
	int [][][] hboard = new int[MAX_DEPTH][MAX_COLS][MAX_ROWS];
	boolean [][] boardFilled = new boolean[MAX_COLS][MAX_ROWS];
	
	//MyCanvas class for drawing tokens on the board.
	private MyCanvas myCanvas = new MyCanvas();
	
	int gNumTokenRed = 0;
	int gNumTokenGreen = 0;
	int whoisPlaying;	// G:1, R:2
	int noAttackCnt = 0;
	boolean isPlaying = false;
	int gameMode = 1;	//1:Manual, 2:Auto(G), 3:Auto(R)
	String [] letters = {"A","B","C","D","E"};
	long gstart;
	int ttSearch = 0;
	
	Comparator<Movement> moveComparator;
	Queue<Movement> queue;
	
	
	JFrame frame;
	JTextField p1_text;
	JTextField p2_text;
	JButton p1_btn_play;
	JButton p2_btn_play;
	JButton btn_start;
	JButton btn_end;
	JLabel lbl_tieCnt;
	JComboBox<String> combo;
	Movement curMove;
	
	
	public BonzeeGame() {
		curMove = new Movement();
		moveComparator = new Comparator<BonzeeGame.Movement>() {
			@Override
			public int compare(Movement o1, Movement o2) {
				if( o1.hval < o2.hval ){
					return 1;
				}else{
					return -1;
				}
			}
		};
		
//		System.out.println("board[4][2]:" + board[4][2]);
//		System.out.println("R:" + gNumTokenRed + ". G:" + gNumTokenGreen);
	}
	
	private void setupBoard() {
		//-- initialize array 
		boolean isFill = true;
		
		gNumTokenRed = 0;
		gNumTokenGreen = 0;
		noAttackCnt = 0;
		whoisPlaying = 1;		//player G start first.
		
		for(int i=0; i < 5 ; i++){
			for(int j=0; j < 9 ; j++){
				
				if(isFill){
					boardFilled[j][i] = true;
					isFill = false;
					 
				} else {
					boardFilled[j][i] = false;
					isFill = true;
				}
				
				if(i < 2){		
					board[j][i] = 2;	//  R:2
					gNumTokenRed++;
				}else if( i == 2) {
					if(j < 4) {
						board[j][i] = 1;	//  G:1
						gNumTokenGreen++;
					} else if ( j > 4) {
						board[j][i] = 2;	//  R:2
						gNumTokenRed++;
					} else {
						board[j][i] = 0;	
					}
				}else {
					board[j][i] = 1;	//  G:1
					gNumTokenGreen++;
				}
			}
		}
		// initialize array --//
		for(int i=0; i < 5 ; i++){
			for(int j=0; j < 9 ; j++){
				System.out.print(board[j][i] + " ,");
			}
			System.out.println("");
		}
		
		noAttackCnt = 0;
		lbl_tieCnt.setText("Non-Attacking Count: 0");
		
		/*
		for(int i=0; i < 5 ; i++){
			for(int j=0; j < 9 ; j++){
				System.out.print(boardFilled[j][i] + " ,");
			}
			System.out.println("");
		}*/
		System.out.println("======== Bonzee Game started.. =========");
	}
	
	private void setupUI() {
		
		frame = new JFrame("Bonzee(Gang of Two)");
		
//		frame.setLayout(null);
		
		JPanel pnlTop = new JPanel();
		
		pnlTop.setBounds(0, 0, 800, 70);
		
		JLabel lblTitle = new JLabel("2017 Concordia Bonzee Tournament");
		
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 29));
		lblTitle.setForeground(Color.white);
		pnlTop.setBackground(new Color(32,93,191));
		pnlTop.add(lblTitle);
		
		
		JPanel pnlCenter = new JPanel();
//		pnlCenter.setSize(400, 330);
		pnlCenter.setBackground(new Color(237,234,166));
		pnlCenter.setBounds(0, 70, 600, 410);
		
		pnlCenter.add(myCanvas);
		
		
		JPanel pnlRight = new JPanel();
		pnlRight.setLayout(null);
//		pnlRight.setBackground(Color.red);
		pnlRight.setBackground(new Color(165,169,175));
//		pnlRight.setSize(300, 400);
//		pnlRight.setLocation(600, 70);
//		pnlRight.setSize(800, 480);
		pnlRight.setBounds(600, 70, 200, 410);
		
		String [] items = {"Manual Mode", "Automatic Mode(G)", "Automatic Mode(R)"};
		DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<String>(items);
		combo = new JComboBox<String>(comboModel);
		combo.setSelectedIndex(0);
		combo.setActionCommand("COMBO");
		combo.setBounds(610, 75, 180, 20);
		

		
		JLabel p1_lbl_title = new JLabel("Player G");
		p1_lbl_title.setFont(new Font("Tahoma", Font.BOLD, 16));
		p1_lbl_title.setForeground(new Color(44, 129, 193));
		p1_lbl_title.setBounds(620, 100, 180, 30);
		
		JLabel p2_lbl_title = new JLabel("Player R");
		p2_lbl_title.setFont(new Font("Tahoma", Font.BOLD, 16));
		p2_lbl_title.setForeground(new Color(44, 129, 193));
		p2_lbl_title.setBounds(620, 220, 180, 30);
		
		lbl_tieCnt = new JLabel("Non-Attacking Count: 0");
		lbl_tieCnt.setFont(new Font("Tahoma", Font.BOLD, 11));
		lbl_tieCnt.setForeground(Color.black);
		lbl_tieCnt.setBounds(620, 320, 180, 30);
		
		
		
		p1_text = new JTextField("");
		p1_text.setBounds(620, 130, 130, 30);
		
		p2_text = new JTextField("");
		p2_text.setBounds(620, 245, 130, 30);
		
		
		
		p1_btn_play = new JButton("Play");
		p1_btn_play.setForeground(Color.white);
		p1_btn_play.setBackground(new Color(50,11,229));
		p1_btn_play.setBounds(620, 170, 150, 30);
		p1_btn_play.setOpaque(true);
		p1_btn_play.setBorderPainted(false);
		p1_btn_play.setActionCommand("P1PLAY");
		
		p2_btn_play = new JButton("Play");
		p2_btn_play.setForeground(Color.white);
		p2_btn_play.setBackground(new Color(50,11,229));
		p2_btn_play.setBounds(620, 280, 150, 30);
		p2_btn_play.setOpaque(true);
		p2_btn_play.setBorderPainted(false);
		p2_btn_play.setEnabled(false);
		p2_btn_play.setActionCommand("P2PLAY");
		
		btn_start = new JButton("Start Game");
		btn_start.setForeground(Color.white);
		btn_start.setBackground(new Color(226,103,20));
		btn_start.setBounds(620, 380, 150, 30);
		btn_start.setOpaque(true);
		btn_start.setBorderPainted(false);
		btn_start.setActionCommand("START");
		
		btn_end = new JButton("End Game");
		btn_end.setForeground(Color.white);
		btn_end.setBackground(new Color(99,88,81));
		btn_end.setBounds(620, 415, 150, 30);
		btn_end.setOpaque(true);
		btn_end.setBorderPainted(false);
		btn_end.setEnabled(false);
		btn_end.setActionCommand("END");
		
		p1_btn_play.addActionListener(this);
		p2_btn_play.addActionListener(this);
		
		btn_start.addActionListener(this);
		btn_end.addActionListener(this);
//		combo.addActionListener(this);
		
//		pnlRight.add(rightInPnl);
		
		pnlRight.add(p1_lbl_title);
		pnlRight.add(p2_lbl_title);
		pnlRight.add(lbl_tieCnt);
		
//		pnlRight.add(empty_lbl);
		pnlRight.add(p1_text);
		pnlRight.add(p2_text);
		pnlRight.add(p1_btn_play);
		pnlRight.add(p2_btn_play);
		pnlRight.add(btn_start);
		pnlRight.add(btn_end);
		pnlRight.add(combo);
		
		
//		pnlRight.add(p1_btn_play);
		
//		JButton jb = new JButton("Click Me");
//		jb.setSize(150, 50);
//		pnlRight.add(jb);
		
//		frame.add(pnlTop, BorderLayout.NORTH);
//		frame.add(pnlCenter, BorderLayout.CENTER);
//		frame.add(pnlRight, BorderLayout.CENTER);
		
		frame.add(pnlTop);
		frame.add(pnlCenter);
		frame.add(pnlRight);
		
		
		frame.setResizable(false);
		frame.setSize(800, 480);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setVisible(true);
	}
	
	private void runGame() {
		setupUI();
		
	}
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	private boolean validateMove(String coord) {
		
		if(coord == null || "".equals(coord) || coord.length() < 5){
			return false;
		}
		coord = coord.toUpperCase();
		if(!coord.matches("[A-E][1-9] [A-E][1-9]")){		//e.g. coord: "D3 C2"
			return false;
		}
		
		// parsing the string and save info to curMove
		int idx = -1;
		for(int i=0; i < letters.length; i++)
			if((coord.substring(0, 1)).equals(letters[i])){
				idx = i;
			}
			
		curMove.rowFrom = idx;
		curMove.colFrom = Integer.parseInt(coord.substring(1, 2)) - 1;
		
		
		for(int i=0; i < letters.length; i++)
			if((coord.substring(3, 4)).equals(letters[i])){
				idx = i;
			}
		curMove.rowTo = idx;
		curMove.colTo = Integer.parseInt(coord.substring(4,5)) - 1;
		
		// check the direction.
		curMove.direction = getDirection(curMove);
		
		if(!checkPositionValidation()){
			return false;
		}
		
		System.out.print("Player " + (whoisPlaying==1?"G":"R" ) + " moved a token from (" + letters[curMove.rowFrom] + (curMove.colFrom+1)); 
		System.out.println(") to (" + letters[curMove.rowTo] + (curMove.colTo+1) + ")");
		
		return true;
	}
	
	private String getDirection(Movement m){
		String sRtn = "";
		if(m.rowFrom > m.rowTo && m.colTo > m.colFrom ){
			sRtn = "D1";
		} else if ( m.rowFrom == m.rowTo && m.colTo > m.colFrom ) {
			sRtn = "E";
		} else if ( m.rowTo > m.rowFrom && m.colTo > m.colFrom  ) {
			sRtn = "D2";
		} else if ( m.rowTo > m.rowFrom && m.colFrom == m.colTo ) {
			sRtn = "S";
		} else if ( m.rowTo > m.rowFrom  && m.colFrom > m.colTo ) {
			sRtn = "D3";
		} else if ( m.rowFrom == m.rowTo && m.colFrom > m.colTo ) {
			sRtn = "W";
		} else if ( m.rowFrom > m.rowTo && m.colFrom > m.colTo ) {
			sRtn = "D4";
		} else if ( m.colFrom == m.colTo &&  m.rowFrom > m.rowTo ) {
			sRtn = "N";
		}
		return sRtn;
	}
	
	private boolean checkPositionValidation(){
		// 2) Check the validation:
		// array boundary, rules for black and white cells.
		if(!checkMaxCol(curMove.colFrom) || !checkMaxCol(curMove.colTo)
				|| !checkMaxRow(curMove.rowFrom) || !checkMaxRow(curMove.rowTo) ){
			return false;
		}
		// same position.
		if( curMove.colFrom == curMove.colTo && curMove.rowFrom == curMove.rowTo ){
			return false;
		}
		
		// check difference(1) and direction by cells 
		if( Math.abs( curMove.colFrom - curMove.colTo ) > 1  || Math.abs( curMove.rowFrom - curMove.rowTo ) > 1 ){
			return false;
		}
		
		if( board[curMove.colFrom][curMove.rowFrom] != whoisPlaying || 
				board[curMove.colTo][curMove.rowTo] != 0 ) {
			return false;
		}
		
		// white cell and diagonal -> invalid move.
		if( !boardFilled[curMove.colFrom][curMove.rowFrom] && 
				( "D1".equals(curMove.direction) || "D2".equals(curMove.direction) 
					|| "D3".equals(curMove.direction) || "D3".equals(curMove.direction) ) ){
			return false;
		}
		return true;
	}
	
	private List<Movement> getPossibleAttackList(int who){
		List<Movement> result = new ArrayList<BonzeeGame.Movement>();
		int [][] orderBlack = {{0,-1},{1,-1},{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1}};
		int [][] orderWhite = {{0,-1},{1,0},{0,1},{-1,0}};
		int [][] order = null;
		int rt = 0, ct = 0;
		
		// - check white cell
		for(int i=0; i < MAX_ROWS; ++i){
			for(int j=0; j < MAX_COLS; ++j){
				if(tboard[j][i] == who){
					if(boardFilled[j][i] ){		//blackcell
						order = orderBlack;
					}else{						//whitecell
						order = orderWhite;
					}

					for(int [] co : order ){
						rt = i + co[1];
						ct = j + co[0];
						if(checkArrayBoundaryEmptyTemp( rt , ct )){
							result.add(new Movement(i, j, rt, ct));
						}
					}
					
				}
				
			}
		}
		
		return result;
	}
	
	private Movement minimax(int depth, int who){
		
		int opponent = who==1?2:1;
		int bestScore = (who==1) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		Movement bestMove = null,rltMove = null;
		List<Movement> listMove = null; 
		copyBoardFromHistBoard(depth);
		
		if(depth != 0)
			listMove = getPossibleAttackList(who);
		else
			listMove = new ArrayList<Movement>();
		
		
		if(depth == 0 || listMove.isEmpty() ){ 
			bestMove = new Movement(curMove.rowFrom, curMove.colFrom, curMove.rowTo, curMove.colTo);
			bestMove.hval = evaluateHeuristic();
			
		}else{

			for(Movement tmpMove: listMove){
				
				copyBoardFromHistBoard(depth);			//first apply this move to tboard
				curMove = tmpMove;
				curMove.direction = getDirection(curMove);
				
				processMoveTemp(who);
				copyBoardToHistBoard( tboard, depth-1 );		//save tboard to temp array
			 
				if(who == 1){	// G maximize
					rltMove = minimax(depth-1, opponent);
					 
					if(rltMove.hval > bestScore){
						bestMove = tmpMove;
						bestMove.hval = rltMove.hval;
						bestScore = rltMove.hval;
					}
					
				}else{			//R minimize
					rltMove = minimax(depth-1, opponent);
					
					if( rltMove.hval < bestScore ){
						bestMove = tmpMove;
						bestMove.hval = rltMove.hval;
						bestScore = rltMove.hval;
					}
					
				}
					
			}
			
		}
		
		return bestMove;
	}
	
	private Movement alphabeta(int depth, int alpha, int beta, int who){
		
		
		
		int opponent = who==1?2:1;
		int bestScore = (who==1) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		Movement bestMove = null, rltMove = null;
		List<Movement> listMove = null;
		copyBoardFromHistBoard(depth);
		
		if(depth != 0)
			listMove = getPossibleAttackList(who);
		else
			listMove = new ArrayList<Movement>();
		
		if(depth == 0 || listMove.isEmpty() ){
			bestMove = new Movement(curMove.rowFrom, curMove.colFrom, curMove.rowTo, curMove.colTo);
//			bestMove.hval = evaluateHeuristic();
			bestMove.hval = advHeuristic();
		}else{
			
			for(Movement tmpMove: listMove){
				
//				if( (System.currentTimeMillis() - gstart) > 2700 ){
//					break;
//				}
				++ttSearch;
				
				copyBoardFromHistBoard(depth);			//first apply this move to tboard
				
				curMove = tmpMove;
				curMove.direction = getDirection(curMove);
				
				processMoveTemp(who);
				
				copyBoardToHistBoard( tboard, depth-1 );		//save tboard to temp array
				
				if(who == 1){				// Player G maximize
					rltMove = alphabeta(depth-1, alpha, beta, opponent);
//					System.out.print("[MX"+ depth +"]:" + rltMove.hval + "/" + bestScore);
					if(rltMove != null && bestScore < rltMove.hval){
						bestScore = rltMove.hval;
						bestMove = tmpMove;
						bestMove.hval = rltMove.hval;
					}
					if(bestScore > alpha ){
						alpha = bestScore;
					}
					if( beta <= alpha ){
//						System.out.print("*PRUNE*");
						break;
					}
					
				}else{						// Player R minimize
					rltMove = alphabeta(depth-1, alpha, beta, opponent);
//					System.out.print("[MN"+ depth +"]:" + rltMove.hval + "/" + bestScore);
					if(rltMove != null && bestScore > rltMove.hval){
						bestScore = rltMove.hval;
						bestMove = tmpMove;
						bestMove.hval = rltMove.hval;
					}
					if( bestScore < beta )
						beta = bestScore;
					if( beta <= alpha){
//						System.out.print("*PRUNE*");
						break;
					}
					
				}
//				System.out.print("//");
				
			}
//			System.out.println("");
		}
		
		
		return bestMove;
	}
	
	// simple heuristic given by the prof.
	private int evaluateHeuristic(){
		
		int valGreen = 0;
		int valRed = 0;
		
		for(int i=0; i < MAX_ROWS ; i++){
			for(int j=0; j < MAX_COLS ; j++){
				if(tboard[j][i] == 1){
					valGreen += (j+1) * 50 + (i+1) * 100;
				}else if(tboard[j][i] == 2){
					valRed += (j+1) * -50 + (i+1) * -100;
				}
			}
		}
		
		return valGreen + valRed;
	}
	
	private int advHeuristic_tmp(){
		
		int [] ptG = {10,30,50,70,90};
		int [] ptR = {-90,-70,-50,-30,-10};
		
		int valGreen = 0 , valRed = 0;
		
		for(int i=0; i < MAX_ROWS ; i++){
			for(int j=0; j < MAX_COLS ; j++){
				if(tboard[j][i] == 1){			//G
					if(boardFilled[j][i]){		//black
						valGreen += (j+1) * 90 + (i+1) * ptG[i];
					}else{
						valGreen += (j+1) * 70 + (i+1) * ptG[i];
					}
				}else if(tboard[j][i] == 2){	//R
					if(boardFilled[j][i]){
						valRed += (j+1) * -90 + (i+1) * ptR[i];
					}else{
						valRed += (j+1) * -70 + (i+1) * ptR[i];
					}
					 
				}
				
			}
		}
		
		return valGreen + valRed;
	}
	
	// ******* Final heuristic ******* 
	private int advHeuristic(){
		
		int [] ptG = {10,30,50,70,30};
		int [] ptR = {-30,-70,-50,-30,-10};
		int [] ptCol = {50,80,85,90,100,90,85,80,50};
		
		int valGreen = 0 , valRed = 0;
		
		for(int i=0; i < MAX_ROWS ; i++){
			for(int j=0; j < MAX_COLS ; j++){
				if(tboard[j][i] == 1){			//G
					if(boardFilled[j][i]){		//black
						valGreen += ptCol[j] + 20 + ptG[i];
					}else{
						valGreen += ptCol[j] + ptG[i];
					}
				}else if(tboard[j][i] == 2){	//R
					if(boardFilled[j][i]){
						valRed += -ptCol[j]- 20 + ptR[i];
					}else{
						valRed += -ptCol[j] + ptR[i];
					}
					 
				}
				
			}
		}
		
		return valGreen + valRed;
	}


	
	private boolean checkMaxCol(int idx){
		if( idx < 0 || idx >= MAX_COLS ){
			return false;
		}else {
			return true;
		}
	}
	
	private boolean checkMaxRow(int idx){
		if( idx < 0 || idx >= MAX_ROWS ){
			return false;
		}else {
			return true;
		}
	}
	

	private List<Coord> getAttackResult(int mode, int who) {
		int [][] board = null;
		if(mode == 2){		//2: minimax evaluation
			board = this.tboard;
		}else{		//1: real move
			board = this.board;
		}	
		
		List<Coord> list = new ArrayList<Coord>();
		int opponent = who==1?2:1;
		boolean isForward = false;
		
		if("D1".equals( curMove.direction) ){
			
			// forward attack
			int p = curMove.colTo +1 ;
			int q = curMove.rowTo - 1;
			while(true) {
				if( q < 0 || p >= MAX_COLS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				++p;
				--q;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom - 1;
				int k = curMove.rowFrom + 1;
				
				while(true) {
					if( j < 0 || k >= MAX_ROWS ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					--j;
					++k;
					
				}
				
			}
			
		} else if("D2".equals( curMove.direction) ){
			
			// forward attack
			int p = curMove.colTo + 1 ;
			int q = curMove.rowTo + 1;
			
			while(true) {
				if( q >= MAX_ROWS || p >= MAX_COLS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				++p;
				++q;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom - 1;
				int k = curMove.rowFrom - 1;
				
				while(true) {
					if( j < 0 || k < 0 ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					--j;
					--k;
					
				}
				
			}
						
			
		} else if("D3".equals( curMove.direction) ){
			// forward attack
			int p = curMove.colTo - 1 ;
			int q = curMove.rowTo + 1;
			
			while(true) {
				if( q >= MAX_ROWS || p < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				--p;
				++q;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom + 1;
				int k = curMove.rowFrom - 1;
				
				while(true) {
					if( j >= MAX_COLS || k < 0 ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					++j;
					--k;
					
				}
				
			}
			
		} else if("D4".equals( curMove.direction) ){
			// forward attack
			int p = curMove.colTo - 1 ;
			int q = curMove.rowTo - 1;
			
			while(true) {
				if( q < 0 || p < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				--p;
				--q;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom + 1;
				int k = curMove.rowFrom + 1;
				
				while(true) {
					if( j >= MAX_COLS || k >= MAX_ROWS ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					++j;
					++k;
					
				}
				
			}			
		} else if("E".equals( curMove.direction) ){
			// forward attack
			int p = curMove.colTo + 1 ;
			int q = curMove.rowTo;
			
			while(true) {
				if( p >= MAX_COLS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				++p;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom - 1;
				int k = curMove.rowFrom;
				
				while(true) {
					if( j < 0 ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					--j;
					
				}
				
			}				
		} else if("W".equals( curMove.direction) ){
			// forward attack
			int p = curMove.colTo - 1 ;
			int q = curMove.rowTo;
			
			while(true) {
				if( p < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				--p;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom + 1;
				int k = curMove.rowFrom;
				
				while(true) {
					if( j >= MAX_COLS ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					++j;
					
				}
				
			}				
		} else if("N".equals( curMove.direction) ){
			// forward attack
			int p = curMove.colTo;
			int q = curMove.rowTo - 1;
			
			while(true) {
				if( q < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				--q;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom;
				int k = curMove.rowFrom + 1;
				
				while(true) {
					if( k >= MAX_ROWS ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					++k;
					
				}
				
			}				
		} else if("S".equals( curMove.direction) ){
			// forward attack
			int p = curMove.colTo;
			int q = curMove.rowTo + 1;
			
			while(true) {
				if( q >= MAX_ROWS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				list.add(new Coord(p, q));
				isForward = true;
				
				++q;
			}
			
			// backward attack
			if(!isForward) {	// if not forward attack. 
				int j = curMove.colFrom;
				int k = curMove.rowFrom - 1;
				
				while(true) {
					if( k < 0 ){
						break;
					}
					if( board[j][k] != opponent ){
						break;
					}
					list.add(new Coord(j, k));
					--k;
					
				}
				
			}
		}

		return list;
	}
	
	private void processMoveTemp(int who) {
		
		// check possible attack
		List<Coord> attackedCells = getAttackResult(2, who);
		
		//delete prev token
		tboard[curMove.colFrom][curMove.rowFrom] = 0;
		
		//draw new token
		tboard[curMove.colTo][curMove.rowTo] = who;
		
		// attacked tokens
		if( attackedCells != null && attackedCells.size() > 0 ) {	//attack
			
			// killing the opponent's tokens.
			for(Coord c: attackedCells) {
				tboard[c.x][c.y] = 0;				
			}
		}
		// if not, - defensive move
		
		
		/*
		System.out.println("====tbrd");
		for(int i=0; i < 5 ; i++){
			for(int j=0; j < 9 ; j++){
				System.out.print(tboard[j][i] + " ,");
			}
			System.out.println("");
		}*/
		
//		System.out.println(">>> Current Tokens: Player G=[" + gNumTokenGreen + "]  , Player R=[" + gNumTokenRed +"]");
		
		

	}
	
	
	private void processMove() {
		
		// check possible attack
		List<Coord> attackedCells = getAttackResult(1, whoisPlaying);
		
		//delete prev token
		board[curMove.colFrom][curMove.rowFrom] = 0;
		myCanvas.updateToken(curMove.colFrom, curMove.rowFrom, 3, boardFilled[curMove.colFrom][curMove.rowFrom]);
		//draw new token
		board[curMove.colTo][curMove.rowTo] = whoisPlaying;
		myCanvas.updateToken(curMove.colTo, curMove.rowTo, whoisPlaying, boardFilled[curMove.colTo][curMove.rowTo]);
		
		if( attackedCells != null && attackedCells.size() > 0 ) {	//attack
			
			// killing the opponent's tokens.
			for(Coord c: attackedCells) {
				board[c.x][c.y] = 0;
				myCanvas.updateToken(c.x, c.y, 3, boardFilled[c.x][c.y]);
				if(whoisPlaying == 1){
					--gNumTokenRed;
				}else{
					--gNumTokenGreen;
				}
			}
			noAttackCnt = 0;
		} else {	// defensive move
			++noAttackCnt;
			
		}
		lbl_tieCnt.setText("Non-Attacking Count: " + noAttackCnt);
		/*
		System.out.println("====");
		for(int i=0; i < 5 ; i++){
			for(int j=0; j < 9 ; j++){
				System.out.print(board[j][i] + " ,");
			}
			System.out.println("");
		}*/
		
		System.out.println(">>> Current Tokens: Player G=[" + gNumTokenGreen + "]  , Player R=[" + gNumTokenRed +"]");
		
		checkEndOfGame();
		
	}
	
	
	private void checkEndOfGame() {
		boolean isEnd = false;
		if(gNumTokenGreen == 0){
			showAlertMsg("Red Player Wins");
			isEnd = true;
		}
		else if( gNumTokenRed == 0){
			showAlertMsg("Green Player Wins");
			isEnd = true;
		}
		else if( noAttackCnt >= 10 ){
			showAlertMsg("10 consecutive non-attacking move! Game Tie!");
			isEnd = true;
		}
		
		// some works to finish
		if(isEnd){
			isPlaying = false;
			btn_end.setEnabled(false);
			btn_start.setEnabled(true);
			combo.setEnabled(true);
			System.out.println("======== Bonzee Game ended.. =========");
		}
		
	}
	
	
	private void p1Play() {
		
		if( !validateMove(p1_text.getText().trim()) ) {
			showAlertMsg("Invalid Move!! Try again!");
			return;
		}
		
		processMove();
		
		p1_text.setText("");
		p1_btn_play.setEnabled(false);
		p2_btn_play.setEnabled(true);
		whoisPlaying = 2;
	}
	
	private void p2Play() {
		if( !validateMove(p2_text.getText().trim()) ) {
			showAlertMsg("Invalid Move!! Try again!");
			return;
		}
		
		processMove();
		
		p2_text.setText("");
		p2_btn_play.setEnabled(false);
		p1_btn_play.setEnabled(true);
		whoisPlaying = 1;
	}
	
	private void showAlertMsg(String str) {
		JOptionPane.showMessageDialog(null, str);
	}
	
	
	private boolean checkArrayBoundaryEmpty(int row, int col){
		if( row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLS ){
			return false;
		}
		if(board[col][row] != 0){
			return false;
		}
		
		return true;
	}
	
	private boolean checkArrayBoundaryEmptyTemp(int row, int col){
		if( row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLS ){
			return false;
		}
		if(tboard[col][row] != 0){
			return false;
		}
		
		return true;
	}
	
	
	private int getPossibleAttackNo(Movement m){
		int count = 0;
		int opponent = whoisPlaying==1?2:1;
		
		if("D1".equals( m.direction) ){
			// forward attack
			int p = m.colTo +1 ;
			int q = m.rowTo - 1;
			while(true) {
				if( q < 0 || p >= MAX_COLS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				
				++p;
				--q;
				++count;
			}
		}else if("D2".equals( m.direction) ){
			// forward attack
			int p = m.colTo + 1 ;
			int q = m.rowTo + 1;
			
			while(true) {
				if( q >= MAX_ROWS || p >= MAX_COLS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				++count;
				++p;
				++q;
			}			
		}else if("D3".equals( m.direction) ){
			// forward attack
			int p = m.colTo - 1 ;
			int q = m.rowTo + 1;
			
			while(true) {
				if( q >= MAX_ROWS || p < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				++count;
				--p;
				++q;
			}			
		}else if("D4".equals( m.direction) ){
			// forward attack
			int p = m.colTo - 1 ;
			int q = m.rowTo - 1;
			
			while(true) {
				if( q < 0 || p < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				++count;
				--p;
				--q;
			}			
		}else if("E".equals( m.direction) ){
			
			// forward attack
			int p = m.colTo + 1 ;
			int q = m.rowTo;
			
			while(true) {
				if( p >= MAX_COLS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				++p;
				++count;
			}
						
			
		}else if("W".equals( m.direction) ){
			// forward attack
			int p = m.colTo - 1 ;
			int q = m.rowTo;
			
			while(true) {
				if( p < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				++count;
				--p;
			}			
		}else if("N".equals( m.direction) ){
			// forward attack
			int p = m.colTo;
			int q = m.rowTo - 1;
			
			while(true) {
				if( q < 0 ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				--q;
				++count;
			}			
		}else if("S".equals( m.direction) ){
			// forward attack
			int p = m.colTo;
			int q = m.rowTo + 1;
			
			while(true) {
				if( q >= MAX_ROWS ) {
					break;
				}
				if( board[p][q] != opponent ){
					break;
				}
				++count;
				++q;
			}
		}
		
		return count;
		
	}
	
	private void autoCalculateBestMove(){
		// ******* NOT USED *******
		
		//run in 3 secs.
		int [][] order = {{0,-1},{1,-1},{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1}};
		
		queue.clear();
//		int opponent = whoisPlaying==1?2:1;
		int tmpAttackNo = 0;
		Movement move;
		
//		long startTime = System.currentTimeMillis();
		
		for(int i=0; i < MAX_ROWS; ++i){
			for(int j=0; j < MAX_COLS; ++j){
				
//				if((System.currentTimeMillis()-startTime) > 2800){ break; } 
				
				if( board[j][i] == whoisPlaying ){
					for(int [] inorder : order){
						if(!checkArrayBoundaryEmpty( i+inorder[1], j+inorder[0] )){
							continue;
						}
						move = new Movement(i, j, i+inorder[1], j+inorder[0]);
						move.direction = getDirection(move);
						
						//check attack, get possible number of token to attack.
						tmpAttackNo = getPossibleAttackNo(move);
						
						if(tmpAttackNo != 0){
							move.hval = tmpAttackNo;
							queue.add(move);
						}
						
					}
					
					
				}
				
				
			}
		}
		
	}
	
	private void copyBoardToHistBoard(int [][] brd , int d){
		for(int i=0; i < MAX_ROWS ; i++){
			for(int j=0; j < MAX_COLS ; j++){
				hboard[d][j][i] = brd[j][i];
			}
		}

	}
	
	private void copyBoardFromHistBoard(int d){
		for(int i=0; i < MAX_ROWS ; i++){
			for(int j=0; j < MAX_COLS ; j++){
				tboard[j][i] = hboard[d][j][i];
			}
		}

	}
	
	private Movement startMiniMax(int depth){
		// ### copy board to hboard
		
		if(depth > MAX_DEPTH){
			return null;
		}
		
		copyBoardToHistBoard(board, depth);
		return minimax(depth, whoisPlaying);
	}
	
	
	private Movement startAlphaBeta(int depth){
		
		if(depth > MAX_DEPTH){
			return null;
		}
		
		copyBoardToHistBoard(board, depth);
		return alphabeta(depth, Integer.MIN_VALUE, Integer.MAX_VALUE, whoisPlaying);
	}
	
	
	private void checkAutomaticMove(){
		if(isPlaying){
			if( gameMode == 2 && whoisPlaying == 1 || gameMode == 3 && whoisPlaying == 2 ){	// AI plays...
				ttSearch = 0;
//				curMove = startMiniMax(3);
				gstart = System.currentTimeMillis();
				curMove = startAlphaBeta(6);
				
				System.out.println("Time: "+ ( ((System.currentTimeMillis() - gstart)/1000d)  ) + "ttSearch=" + ttSearch);
				
				if(curMove == null){
					showAlertMsg("No Best Movement! Error");
				}
				
				System.out.println("");
				System.out.print("AI Player " + (whoisPlaying==1?"G":"R" ) + " moved a token from (" + letters[curMove.rowFrom] + (curMove.colFrom+1)); 
				System.out.println(") to (" + letters[curMove.rowTo] + (curMove.colTo+1) + "), e(n)=" + curMove.hval);
				
				showAlertMsg("AI Player is going to move from (" + letters[curMove.rowFrom] + (curMove.colFrom+1) + ") to (" + letters[curMove.rowTo] + (curMove.colTo+1) + ") , e(n)=" + curMove.hval);
				
				processMove();
				
				if(whoisPlaying == 1 && gameMode == 2){
					p2_text.setText("");
					p2_btn_play.setEnabled(true);
					p1_btn_play.setEnabled(false);
					whoisPlaying = 2;
				}else if(whoisPlaying == 2 && gameMode == 3){
					p1_text.setText("");
					p1_btn_play.setEnabled(true);
					p2_btn_play.setEnabled(false);
					whoisPlaying = 1;
				}
			}
		}
		
		
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
		
		if("P1PLAY".equals( cmd )) {
			p1Play();
			//check AI
			checkAutomaticMove();
			
		} else if("P2PLAY".equals( cmd )) {
			p2Play();
			checkAutomaticMove();
		} else if("START".equals( cmd )) {
			isPlaying = true;
			setupBoard();
			myCanvas.setupInitialBoard( );
			
			p1_btn_play.setEnabled(true);
			p2_btn_play.setEnabled(false);
			
			btn_start.setEnabled(false);
			btn_end.setEnabled(true);
			
			combo.setEnabled(false);
			
			gameMode = combo.getSelectedIndex()+1;
			System.out.println("game mode: " + gameMode);
			
			p1_text.setText("");
			p2_text.setText("");
			p1_text.setEnabled(true);
			p2_text.setEnabled(true);
			p1_btn_play.setEnabled(true);
			p2_btn_play.setEnabled(true);

			if(gameMode == 2){ 
				p1_text.setText("[Automatic Mode]");
				p1_text.setEnabled(false);
				p1_btn_play.setEnabled(false);
			} else if(gameMode == 3){
				p2_text.setText("[Automatic Mode]");
				p2_text.setEnabled(false);
				p2_btn_play.setEnabled(false);
			}
			
			// check AI player's turn
			checkAutomaticMove();
			
			
			
		} else if("END".equals( cmd )) {
			isPlaying = false;
			myCanvas.clearBoard();
			System.out.println("======== The Game is ended by a player =========");
			
			btn_end.setEnabled(false);
			btn_start.setEnabled(true);
			combo.setEnabled(true);
		} 
        
	}
	
	public static void main(String[] args) {
		BonzeeGame game = new BonzeeGame();
		game.runGame();
		
	}
}
