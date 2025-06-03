package Chess;

import java.awt.image.BufferedImage;

public class Piece {
    private String type;
    private boolean isWhite;
    private BufferedImage image;

    public Piece(String type, boolean isWhite, BufferedImage image) {
        this.type = type;
        this.isWhite = isWhite;
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
