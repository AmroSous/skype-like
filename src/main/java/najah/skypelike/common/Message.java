package najah.skypelike.common;

import java.io.Serializable;

public record Message (
        String from, String to, String msg, String timestamp, int index)
        implements Serializable {}
