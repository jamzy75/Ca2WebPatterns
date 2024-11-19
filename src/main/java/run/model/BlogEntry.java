package run.model;

import lombok.*;

/**
 *
 * @author James
 *
 * Based on blog_entries table:
 *      create table if not exists blog_entries
(
entryID int not null UNIQUE AUTO_INCREMENT,
username varchar(10) not null,
title varchar(150),
content varchar(600),
PRIMARY KEY (entryID),
FOREIGN KEY (username) REFERENCES users(username) on delete cascade
);
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
@AllArgsConstructor
public class BlogEntry implements Comparable<BlogEntry>{
    @EqualsAndHashCode.Include
    private int entryId;
    private String username;
    private String title;
    private String content;

    public BlogEntry(String username, String title, String content) {
        this.username = username;
        this.title = title;
        this.content = content;
    }

    @Override
    public int compareTo(BlogEntry o)
    {
        int result = this.entryId - o.getEntryId();
        return result * -1;
    }
}
