package server.filter.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ClientPreferences {
    private FilterType filterType;
    private int frequency;         // Częstotliwość otrzymywania wiadomości: 1 - każda, 2 - co druga, ..., 5 - co piąta

    public ClientPreferences(FilterType filterType, int frequency) {
        this.filterType = filterType;
        setFrequency(frequency);
    }
    public void setFrequency(int frequency) {
        if (frequency < 1 || frequency > 5) {
            throw new IllegalArgumentException("Frequency must be between 1 and 5.");
        }
        this.frequency = frequency;
    }
}
