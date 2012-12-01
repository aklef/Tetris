package Tetris2P;

import java.util.Random;
import java.io.Serializable;
import java.lang.Math;

/**
 * Origninal source for this file can be found at {@link http://www.zetcode.com}.
 * Published under no license and is assumed to be in the public domain.
 * All comments are ours. Modifications have been made in the rotate function.
 * 
 * @author Jan Bodnar
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */
public class Shape implements Serializable{
    /**
     * The Tetromino enum holds markers for the 8 pieces used in the Tetris game, 
     * including the empty shape.
     */
	public enum Tetromino { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape };
    /**
     * The {@code Tetromino} shape this piece has.
     */
    private Tetromino pieceShape;
    /**
     * Holds the coordinates of the Tetris piece
     * Defines its shape
     */
    private int[][] coords;
    /**
     * Stores all possible shapes of Tetris pieces
     */
    private int[][][] coordsTable;
    
    /**
     *  Random variable used to generate random shapes
     */
    private Random r;

    /**
     * Initializes a shape
     */
    protected Shape() {
        coords = new int[4][2];
        setShape(Tetromino.NoShape);
        r = new Random();
    }

    /**
     * Defines a shape
     */
    public void setShape(Tetromino shape) {
    	
    	// Defining the matrix for all possible Tetris pieces
    	// is 8x4x2
         coordsTable = new int[][][] {
            { {  0,  0 }, { 0,  0 },  { 0,  0 }, { 0,  0 } },
            { {  0, -1 }, { 0,  0 },  { -1, 0 }, { -1, 1 } },
            { {  0, -1 }, { 0,  0 },  { 1,  0 }, { 1,  1 } },
            { {  0, -1 }, { 0,  0 },  { 0,  1 }, { 0,  2 } },
            { { -1,  0 }, { 0,  0 },  { 1,  0 }, { 0,  1 } },
            { {  0,  0 }, { 1,  0 },  { 0,  1 }, { 1,  1 } },
            { { -1, -1 }, { 0, -1 },  { 0,  0 }, { 0,  1 } },
            { {  1, -1 }, { 0, -1 },  { 0, 0 },  { 0,  1 } }
        };
        
        //Initializes the specific shape by putting a row of coordinate values to
        //the coords array. The row is specified by shape.ordinal() method
        for (int i = 0; i < 4 ; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }
        
        pieceShape = shape; //creating a shape instance
        
    }

    /**
     * Setting the x coordinate of the shape.
     */
    private void setX (int index, int x)
    {
    	coords[index][0] = x;
    }
    /**
     * Setting the y coordinates of the shape.
     */
    private void setY (int index, int y)
    {
    	coords[index][1] = y;
    }
    /**
     * Returns the x coordinate of one of a Shape's squares.
     */
    protected int x (int index)
    {
    	return coords[index][0];
    }
    /**
     * Returns the y coordinate of one of a Shape's squares.
     */
    protected int y (int index)
    {
    	return coords[index][1];
    }
    /**
     * Returns the shape
     */
    public Tetromino getShape()
    {
    	return pieceShape;
    }

    /**
     * Creates a random shape to output to the board
     */
    protected void setRandomShape()
    {
        int x = Math.abs(r.nextInt()) % 7 + 1;
        Tetromino[] values = Tetromino.values(); 
        setShape(values[x]);
    }

    /**
     * Determines the smallest x coordinate
     */
    protected int minX()
    {
      int m = coords[0][0];
      for (int i=0; i < 4; i++) {
          m = Math.min(m, coords[i][0]);
      }
      return m;
    }

    /**
     * Determines the smallest y coordinate
     */
    protected int minY() 
    {
      int m = coords[0][1];
      for (int i=0; i < 4; i++) {
          m = Math.min(m, coords[i][1]);
      }
      return m;
    }

    /**
     * Rotates the current shape clockwise.
     */
    protected Shape rotate()
    {
        if (pieceShape == Tetromino.SquareShape)
            return this;
        
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        
        for (int i = 0; i < 4; ++i) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        return result;
    }
    
	/**
	 * Returns a {@code String} representation of this {@code Shape}.
	 */
	@Override
	public String toString()
	{
		return "[SHAPE] "+pieceShape.toString();
	}
}