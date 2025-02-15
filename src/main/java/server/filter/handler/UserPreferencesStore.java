package server.filter.handler;

import server.filter.dto.ClientPreferences;
import server.filter.dto.FilterType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserPreferencesStore {
    private final Map<String, ClientPreferences> preferencesMap = new ConcurrentHashMap<>();

    public void updatePreferences(String sessionId, ClientPreferences preferences) {
        preferencesMap.put(sessionId, preferences);
    }

    public ClientPreferences getPreferences(String sessionId) {
        return preferencesMap.computeIfAbsent(sessionId, id -> new ClientPreferences(FilterType.FULL, 1));
    }

    public void removePreferences(String sessionId) {
        preferencesMap.remove(sessionId);
    }

    public void updateFrequency(String sessionId, int frequency) {
        preferencesMap.computeIfPresent(sessionId, (id, prefs) -> {
            prefs.setFrequency(frequency);
            return prefs;
        });
    }

    public void clearAllPreferences() {
        System.out.println("Clearing the entire UserPreferencesStore..");
        preferencesMap.clear();
    }
}