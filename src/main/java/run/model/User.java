package run.model;

import lombok.*;

/**
 *
 * @author Michelle
 * 
 * Based on Users table
 *      create table if not exists users
        (
            username varchar(20) not null,
            password varchar(10) not null,
            firstName varchar(20),
            lastName varchar(30),
            isAdmin boolean not null default FALSE,
            PRIMARY KEY (username)
        );
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor
@Builder
@Getter
public class User implements Comparable<User>
{
    @EqualsAndHashCode.Include
    private String username;
    @ToString.Exclude
    private String password;
    private String firstName;
    private String lastName;
    private boolean isAdmin;

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    @Override
    public int compareTo(User u) {
        return this.username.compareTo(u.username);
    }
}
