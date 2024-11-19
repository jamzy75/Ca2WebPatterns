package run.model;

import lombok.*;

import java.util.Objects;

/**
 *
 * @author Michelle
 *
 * Based on friends table:
 *      create table if not exists friends
(
friend1 varchar(10) not null,
friend2 varchar(10) not null,
PRIMARY KEY (friend1, friend2),
FOREIGN KEY (friend1) REFERENCES users(username) on delete cascade,
FOREIGN KEY (friend2) REFERENCES users(username) on delete cascade
);
 *  The friendships within an instance of this class will always
 *  be stored in alphabetical order of their username.
 *
 *  The equals method has been designed to check if the friendship is equal, no matter
 *  what order the friends have been inserted as.
 */
@Getter
@ToString
public class Friendship
{
    private User user1;
    private User user2;

    public Friendship(User user1, User user2) {
        User tmp;
        // If they're in the wrong order coming in, swap them.
        if(user1.compareTo(user2) > 0){
            tmp = user2;
            this.user2 = user1;
            this.user1 = tmp;
        }else{
            this.user1 = user1;
            this.user2 = user2;
        }
    }

    public void setUser1(User u1) {
        this.user1 = u1;
        correctFriendOrder();
    }

    public void setUser2(User u2) {
        this.user2 = u2;
        correctFriendOrder();
    }

    public void correctFriendOrder()
    {
        User tmp;
        // If they're in the wrong order, swap them.
        if(this.user1.compareTo(this.user2) > 0){
            tmp = this.user2;
            this.user2 = this.user1;
            this.user1 = tmp;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        correctFriendOrder();
        hash = 59 * hash + Objects.hashCode(this.user1);
        hash = 59 * hash + Objects.hashCode(this.user2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Friendship other = (Friendship) obj;

        // Make sure they both have their friends in alphabetical order.
        this.correctFriendOrder();
        other.correctFriendOrder();

        if (!Objects.equals(this.user1, other.user1)) {
            return false;
        }
        if (!Objects.equals(this.user2, other.user2)) {
            return false;
        }
        return true;
    }

    public static void main(String [] args)
    {
        User u1 = new User("Charlie", "pass", "Charles", "Young", false);
        User u2 = new User("Zoey", "pass", "Zoey", "Bartlett", false);

        Friendship firstFriends = new Friendship(u1, u2);
        Friendship testEqualsFriends = new Friendship(u2, u1);
        System.out.println("Details for firstFriends: " + firstFriends);

        System.out.println("Details for testEquals friendship: " + testEqualsFriends);

        if(firstFriends.equals(testEqualsFriends)){
            System.out.println("The two friendships are equal!");
        }else{
            System.out.println("The two friendships are NOT equal");
        }
    }
}
