package ua.uhk.mois.chatbot.dto;

import ua.uhk.mois.chatbot.response.AbsDtoOut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 26.03.2021 9:44
 */

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDtoOut extends AbsDtoOut<QuestionDtoOut> {

    private String answer;
}
