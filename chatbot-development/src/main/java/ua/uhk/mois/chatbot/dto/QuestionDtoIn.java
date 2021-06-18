package ua.uhk.mois.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author KVN
 * @since 26.03.2021 9:43
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QuestionDtoIn {

    private String question;
}
