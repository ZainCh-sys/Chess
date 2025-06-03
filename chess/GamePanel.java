package Chess;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class GamePanel extends JPanel implements Runnable {

    // Drag-and-drop state
    private Piece draggedPiece = null;
    private int draggedPieceRow = -1;
    private int draggedPieceCol = -1;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int mouseX = 0;
    private int mouseY = 0;

    // Board background image
    private BufferedImage boardImage;

    // Piece images
    private BufferedImage WhitePawn, WhiteKnight, WhiteBishop, WhiteRook, WhiteQueen, WhiteKing;
    private BufferedImage BlackPawn, BlackKnight, BlackBishop, BlackRook, BlackQueen, BlackKing;

    // Tile and board placement
    private final int tileSize = 69;
    private final int boardStartX = 357;
    private final int boardStartY = 74;

    // Logical board: [row][col], 0 = top (black back rank), 7 = bottom (white back rank)
    private Piece[][] board = new Piece[8][8];

    // Game thread and state
    private Thread gameThread;
    private final int FPS = 30;
    private short gameState = 0;  // 0 = menu, 1 = playing, 2 = game over

    public GamePanel() {
      // Load all images in one place (catching IOExceptions inside)
      loadResources();

      // Set panel size based on boardImage (or fallback if null)
      if (boardImage != null) {
        int scaledWidth = boardImage.getWidth();
        int scaledHeight = boardImage.getHeight();
        setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        System.out.println("Panel size set to: " + scaledWidth + "x" + scaledHeight);
      }
      else{
        setPreferredSize(new Dimension(626, 626)); // fallback size
      }

      setFocusable(true);
      requestFocusInWindow();
      addKeyListener(new KeyHandler());
      System.out.println("GamePanel created");

      // Initialize board pieces in starting positions
      // Black pawns on row 1
      for (int col = 0; col < 8; col++) {
        board[1][col] = new Piece("Pawn", false);
      }
      // White pawns on row 6
      for (int col = 0; col < 8; col++) {
        board[6][col] = new Piece("Pawn", true);
      }
      // Black back rank (row 0)
      board[0][0] = new Piece("Rook",   false);
      board[0][1] = new Piece("Knight", false);
      board[0][2] = new Piece("Bishop", false);
      board[0][3] = new Piece("Queen",  false);
      board[0][4] = new Piece("King",   false);
      board[0][5] = new Piece("Bishop", false);
      board[0][6] = new Piece("Knight", false);
      board[0][7] = new Piece("Rook",   false);
      // White back rank (row 7)
      board[7][0] = new Piece("Rook",   true);
      board[7][1] = new Piece("Knight", true);
      board[7][2] = new Piece("Bishop", true);
      board[7][3] = new Piece("Queen",  true);
      board[7][4] = new Piece("King",   true);
      board[7][5] = new Piece("Bishop", true);
      board[7][6] = new Piece("Knight", true);
      board[7][7] = new Piece("Rook",   true);

      // Mouse listeners for drag-and-drop
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          handleMousePressed(e);
        }
        
        public void mouseReleased(MouseEvent e) {
          handleMouseReleased(e);
        }
      }
                       
      );
      
      addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
          handleMouseDragged(e);
        }
      }
    );
  }

    private void loadResources() {
      try {
        boardImage = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Board.png"));

        // White piece images
        WhitePawn   = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/White/WhitePawn.png"));
        WhiteKnight = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/White/WhiteKnight.png"));
        WhiteBishop = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/White/WhiteBishop.png"));
        WhiteRook   = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/White/WhiteRook.png"));
        WhiteQueen  = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/White/WhiteQueen.png"));
        WhiteKing   = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/White/WhiteKing.png"));

        // Black piece images
        BlackPawn   = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Black/BlackPawn.png"));
        BlackKnight = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Black/BlackKnight.png"));
        BlackBishop = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Black/BlackBishop.png"));
        BlackRook   = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Black/BlackRook.png"));
        BlackQueen  = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Black/BlackQueen.png"));
        BlackKing   = ImageIO.read(getClass().getResourceAsStream("/Chess/Textures/Black/BlackKing.png"));

        if (boardImage == null) {
          System.err.println("Board image is null. Check resource path.");
        }
        else {
          System.out.println("Board image loaded: " + boardImage.getWidth() + "x" + boardImage.getHeight());
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void startGameThread() {
      if (gameThread == null) {
        gameThread = new Thread(this);
        gameThread.start();
        System.out.println("Game thread started");
      }
    }

    public void run() {
      double drawInterval = 1_000_000_000.0 / FPS;
      double nextDrawTime = System.nanoTime() + drawInterval;

      while (gameThread != null) {
        update();
        repaint();
        try {
          double remainingTime = nextDrawTime - System.nanoTime();
          if (remainingTime < 0) remainingTime = 0;
          Thread.sleep((long) (remainingTime / 1_000_000));
          nextDrawTime += drawInterval;
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    private void update() {
    // Game logic updates go here (e.g., check for check/checkmate, toggle turns, etc.)
    }

    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      // Enable smooth image scaling
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

      switch (gameState) {
        case 0 -> drawMenu(g2);
        case 1 -> drawPlaying(g2);
        case 2 -> drawGameOver(g2);
      }
    }

    private void drawMenu(Graphics2D g2) {
      g2.setColor(Color.DARK_GRAY);
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setColor(Color.WHITE);
      g2.setFont(new Font("Arial", Font.BOLD, 60));
      g2.drawString("Chess Game", 400, 400);
      g2.setFont(new Font("Arial", Font.PLAIN, 30));
      g2.drawString("Press ENTER to Start", 450, 460);
    }

    private void drawPlaying(Graphics2D g2) {
      g2.setColor(Color.LIGHT_GRAY);
      g2.fillRect(0, 0, getWidth(), getHeight());

      if (boardImage != null) {
        int x = (getWidth() - boardImage.getWidth()) / 2;
        int y = (getHeight() - boardImage.getHeight()) / 2;
        g2.drawImage(boardImage, x, y, boardImage.getWidth(), boardImage.getHeight(), null);

        double widthp  = (double)(WhitePawn.getWidth()  / 3.2);
        double heightp = (double)(WhitePawn.getHeight() / 3.2);
        int widthpp  = (int) widthp;
        int heightpp = (int) heightp;

        // Draw each piece from the logical board array
        for (int row = 0; row < 8; row++) {
          for (int col = 0; col < 8; col++) {
            Piece piece = board[row][col];
              if (piece != null) {
                BufferedImage img = getImageForPiece(piece);
                if (img != null) {
                  int drawX = boardStartX + col * tileSize + 2;
                  int drawY = boardStartY + row * tileSize + 3;
                  g2.drawImage(img, drawX, drawY, widthpp, heightpp, null);
                }
              }
            }
          }
        }
      else {
        g2.setColor(Color.RED);
        g2.drawString("Board image failed to load!", 20, 20);
      }

      // If a piece is being dragged, draw it on top centered under the cursor
      if (draggedPiece != null) {
        BufferedImage img = getImageForPiece(draggedPiece);
        if (img != null) {
          int drawX = mouseX - dragOffsetX;
          int drawY = mouseY - dragOffsetY;
          g2.drawImage(img, drawX, drawY, tileSize, tileSize, null);
        }
      }
    }

    private void drawGameOver(Graphics2D g2) {
      // Translucent overlay
      g2.setColor(new Color(0, 0, 0, 150));
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setColor(Color.RED);
      g2.setFont(new Font("Arial", Font.BOLD, 60));
      g2.drawString("Game Over", getWidth() / 2 - 150, getHeight() / 2);
      g2.setFont(new Font("Arial", Font.PLAIN, 30));
      g2.drawString("Press R to Restart", getWidth() / 2 - 140, getHeight() / 2 + 50);
    }

    // Mouse event handlers for drag-and-drop
    private void handleMousePressed(MouseEvent e) {
      if (gameState != 1) return;

      int x = e.getX();
      int y = e.getY();
      int col = (x - boardStartX) / tileSize;
      int row = (y - boardStartY) / tileSize;

      if (row >= 0 && row < 8 && col >= 0 && col < 8) {
        Piece piece = board[row][col];
        if (piece != null) {
          draggedPiece = piece;
          draggedPieceRow = row;
          draggedPieceCol = col;
          dragOffsetX = tileSize / 2;
          dragOffsetY = tileSize / 2;
          board[row][col] = null;
        }
      }
    }

    private void handleMouseDragged(MouseEvent e) {
      if (draggedPiece != null) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
      }
    }

    private void handleMouseReleased(MouseEvent e) {
      if (draggedPiece != null) {
        int x = e.getX();
        int y = e.getY();
        int col = (x - boardStartX) / tileSize;
        int row = (y - boardStartY) / tileSize;

        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
          board[row][col] = draggedPiece;
        }
        else {
          // Return to original square if dropped outside
          board[draggedPieceRow][draggedPieceCol] = draggedPiece;
        }

        draggedPiece = null;
        draggedPieceRow = -1;
        draggedPieceCol = -1;
        repaint();
      }
    }

    // Key handler to start/restart/exit the game
    private class KeyHandler extends java.awt.event.KeyAdapter {
      public void keyPressed(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        if (code == java.awt.event.KeyEvent.VK_ENTER && gameState == 0) {
          startGame();
        }
        else if (code == java.awt.event.KeyEvent.VK_R && gameState == 2) {
            restartGame();
        }
        else if (code == java.awt.event.KeyEvent.VK_ESCAPE && gameState == 1) {
          gameState = 0;
          repaint();
        }
      }
    }

    private void startGame() {
      System.out.println("Game started");
      gameState = 1;
      repaint();
    }

    private void restartGame() {
      System.out.println("Game restarted");
      gameState = 1;
      // TODO: reset board array and any other game state here
      repaint();
    }

    // Helper to pick the correct image based on piece type and color
    private BufferedImage getImageForPiece(Piece piece) {
      if (piece == null) return null;
      boolean white = piece.isWhite();
      String type = piece.getType().toLowerCase();

      return switch (type) {
        case "pawn"   -> white ? WhitePawn   : BlackPawn;
        case "knight" -> white ? WhiteKnight : BlackKnight;
        case "bishop" -> white ? WhiteBishop : BlackBishop;
        case "rook"   -> white ? WhiteRook   : BlackRook;
        case "queen"  -> white ? WhiteQueen  : BlackQueen;
        case "king"   -> white ? WhiteKing   : BlackKing;
        default       -> null;
      };
    }

    // Nested Piece class (just type + color)
    private static class Piece {
      private final String type;
      private final boolean isWhite;

      public Piece(String type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
      }

      public String getType() {
        return type;
      }

      public boolean isWhite() {
        return isWhite;
      }
  }
}
