package cz.uhk.mois.financialplanning.model.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 26.03.2020 2:25
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DeleteAllWishesDtoOut {

    /**
     * Information about successful deletion of all wishes of signed-in user.
     */
    private String message;
}
