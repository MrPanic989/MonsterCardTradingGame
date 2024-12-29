package at.mctg.app.dto;

import lombok.*;

/**
 * A DTO that only exposes Name, Bio, and Image of a user.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private String name;
    private String bio;
    private String image;
}
