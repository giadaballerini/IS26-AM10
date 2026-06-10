package it.polimi.ingsw.client.ui.GUI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static registry that maps flag names to their descriptions, loaded at class
 * initialisation time from the {@code /json/clientFlags.json} resource file.
 *
 * <p>Flag descriptions are used by the GUI to display contextual information
 * about game-state indicators shown to the player. The registry is populated
 * once via a {@code static} initialiser block; all subsequent access is
 * read-only through {@link #getDescription(String)}.</p>
 *
 * <p>The backing JSON file is expected to contain an array of objects that
 * deserialise into {@link FlagData} instances, each providing a {@code name}
 * and a {@code description} field.</p>
 *
 * @see FlagData
 */
public class FlagRegistry {

    /** Internal map from flag name to its human-readable description. */
    private static final Map<String, String> reg = new HashMap<>();

    static {
        loadFlags();
    }

    /**
     * Reads {@code /json/clientFlags.json} from the classpath and populates
     * {@link #reg} with the flag name-to-description mappings it contains.
     *
     * <p>Each entry in the JSON array is deserialised into a {@link FlagData}
     * object; its {@link FlagData#getName() name} is used as the key and its
     * {@link FlagData#getDescription() description} as the value.</p>
     *
     * <p>If the resource cannot be found or parsing fails, a diagnostic
     * message is printed and the registry is left empty. The application can
     * still run, but calls to {@link #getDescription(String)} will return
     * {@code null} for every key.</p>
     */
    private static void loadFlags() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = FlagRegistry.class.getResourceAsStream("/json/clientFlags.json")) {

            if (is == null) {
                throw new RuntimeException("Risorsa clientFlags.json non trovata");
            }
            List<FlagData> descriptions = mapper.readValue(is, new TypeReference<List<FlagData>>() {});

            for (FlagData flagData : descriptions) {
                reg.put(flagData.getName(), flagData.getDescription());
            }
            System.out.println("[JACKSON] Flags caricate con successo!");

        } catch (Exception e) {
            System.out.println("[ERRORE CRITICO] Fallimento nel caricamento clientFlags.json");
            e.printStackTrace();
        }
    }

    /**
     * Returns the description associated with the given flag name.
     *
     * @param flag the flag name whose description is requested; must match
     *             a {@code name} value present in {@code clientFlags.json}
     * @return the human-readable description string, or {@code null} if the
     *         flag name is not registered (either because it does not exist in
     *         the JSON file or because loading failed)
     */
    public static String getDescription(String flag) {
        return reg.get(flag);
    }
}