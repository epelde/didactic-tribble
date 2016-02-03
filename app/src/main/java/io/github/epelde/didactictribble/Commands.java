package io.github.epelde.didactictribble;

/**
 * Created by epelde on 02/02/2016.
 */
public class Commands {

    public static final byte[] FONT_BOLD_ON = {27, 69, 1};
    public static final byte[] FONT_BOLD_OFF = {27, 69, 0};
    public static final byte[] PRINT_ALIGNMENT_CENTER = {27, 97, 1};
    public static final byte[] PRINT_SPEED = {28, 115, 2};
    public static final byte[] INTERNATIONAL_CHARACTERSET = {27, 82, 7};
    public static final byte[] FONT_SIZE_NAME = {27, 77, 0};
    public static final byte[] FONT_SIZE_DESCRIPTION = {27, 77, 2};

}
