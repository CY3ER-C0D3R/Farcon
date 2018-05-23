/*
 * Farcon Software
 *
 * This program is a Group Collaboration and
 * Remote Control Software, free of charge,
 * for personal or commercial use.
 *
 * Open source, code written in javafx.
 * Written by: Yuval Stein @CY3ER-C0D3R
 *
 * https://github.com/CY3ER-C0D3R/Farcon
 *
 * 2018 (c) Farcon
 */

package Common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class WrappedImageView extends ImageView {
    
    WrappedImageView()
    {
        setPreserveRatio(false);
    }

    @Override
    public double minWidth(double height)
    {
        return 40;
    }

    @Override
    public double prefWidth(double height)
    {
        Image I = getImage();
        if (I==null) return minWidth(height);
        return I.getWidth();
    }

    @Override
    public double maxWidth(double height)
    {
        return 16384;
    }

    @Override
    public double minHeight(double width)
    {
        return 40;
    }

    @Override
    public double prefHeight(double width)
    {
        Image img = this.getImage();
        if (img == null) return minHeight(width);
        return img.getHeight();
    }

    @Override
    public double maxHeight(double width)
    {
        return 16384;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }
    
    public void centerImage() {
        Image img = this.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;

            double ratioX = this.getFitWidth() / img.getWidth();
            double ratioY = this.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if(ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            this.setX((this.getFitWidth() - w) / 2);
            this.setY((this.getFitHeight() - h) / 2);
        }
    }
    
    @Override
    public void resize(double width, double height)
    {
        setFitWidth(width);
        setFitHeight(height);
        centerImage();
    }
}
