package najah.skypelike.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * User record to maintain online users information,
 * used by clients processes or server.
 * It implements Serializable interface to enable serializing it and
 * sending it into sockets as objects.
 *
 * @param name
 * @param ip
 * @param port
 */
public record User(String name, String ip, String port) implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(ip, user.ip) && Objects.equals(port, user.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ip, port);
    }
}
