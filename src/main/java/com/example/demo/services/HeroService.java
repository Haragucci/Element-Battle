package com.example.demo.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HeroService {

    //===============================================VARIABLES===============================================\\

    public static final String HEROES_FILE_PATH = "files/heros.json";
    public final ObjectMapper mapper = new ObjectMapper();
    public static List<Hero> heroes = new ArrayList<>();
    public static AtomicInteger counter = new AtomicInteger(1);

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hero(int id, String name, int HP, int Damage, String type, String extra) {}

    private static final List<String> heroNames = List.of("Ignis Flammenherz", "Pyra Glutsturm", "Vulcan Feuerschild", "Inferno Feuerschlag", "Helios Sonnentänzer", "Aqualis Meeresrufer", "Nerina Wellenweber", "Aquara Wasserschild", "Hydra Aquadonner", "Poseidon Meereswoge", "Flora Rankengriff", "Sylva Blattwerk", "Gaia Grünhüter", "Verdura Blattsturm", "Arborea Waldwisperer", "Voltaris Donnerklinge", "Electra Blitzschlag", "Raiden Donnerfaust", "Jolt Elektrosturm", "Thor Donnergroll", "Terran Steinschmied", "Gaia Erdenwächter", "Atlas Felsenfaust", "Terra Erdenschild", "Golem Felsensturm", "Glacies Frostschild", "Frostius Eisessturm", "Tundrus Eisbann", "Chillara Frosthauch", "Blizzara Frostfeuer", "Zephyrus Windrufer", "Aeris Luftklinge", "Aeolus Sturmbote", "Galea Windesflügel", "Caelus Wolke");
    private static final List<String> heroTypes = List.of("Feuer", "Feuer", "Feuer", "Feuer", "Feuer", "Wasser", "Wasser", "Wasser", "Wasser", "Wasser", "Pflanze", "Pflanze", "Pflanze", "Pflanze", "Pflanze", "Elektro", "Elektro", "Elektro", "Elektro", "Elektro", "Erde", "Erde", "Erde", "Erde", "Erde", "Eis", "Eis", "Eis", "Eis", "Eis", "Luft", "Luft", "Luft", "Luft", "Luft");
    private static final List<Integer> hpValues = List.of(12, 11, 14, 9, 13, 11, 10, 13, 12, 9, 10, 9, 12, 13, 11, 9, 8, 11, 10, 14, 15, 13, 10, 12, 11, 10, 12, 14, 11, 9, 9, 11, 13, 10, 12);
    private static final List<Integer> damageValues = List.of(12, 10, 8, 13, 14, 11, 9, 14, 12, 13, 10, 12, 9, 14, 13, 13, 14, 11, 10, 8, 9, 13, 8, 12, 14, 12, 10, 14, 13, 9, 14, 13, 10, 9, 12);
    private static final List<String> extras = List.of("Entfesselt ein feuriges Inferno!", "Brennt mit der Hitze von tausend Sonnen!", "Schmiedet Schwerter aus flüssiger Lava!", "Tanzt um Feuerstürme herum!", "Herrscht über Vulkan und Flamme!", "Beherrscht die Wellen und Meerestiere.", "Beruhigt und kontrolliert die Gezeiten.", "Tiefseeabenteuer und Ozeansymphonie!", "Ruft die Meeresgötter zu Hilfe!", "Wasserwirbel und Strudelmeisterschaft!", "Umschlingt Gegner mit Ranken!", "Meister der Photosynthese und des Waldwachstums.", "Blütenzauber und Fruchtbarkeitsernte!", "Wurzeln, die bis in die Tiefen reichen!", "Beherrscht den Wald in seiner ganzen Pracht!", "Schlägt mit der Kraft eines Gewitters zu.", "Elektrisiert das Schlachtfeld mit einem Funken.", "Blitze zucken durch die Luft!", "Ladung, die die Luft zum Zerreißen bringt!", "Donnerhall und elektrische Eruption!", "Fest wie ein Berg, unnachgiebig.", "Mutter der Erde und der Natur.", "Erdbeben und Steinschatten!", "Felsen, die wie Wasser fließen!", "Wurzeln, die sich tief in die Erde graben!", "Eisige Kältepeitsche!", "Frostiger Sturm!", "Schneesturm und gefrorene Stille!", "Glitzerndes Eis und kristalline Eleganz!", "Frostbeulen und gefrorene Schicksale!", "Ruft die Winde und Stürme herbei!", "Tanzt mit den Lüften!", "Flügel aus Wolken und federleichter Tanz!", "Sturmfänger und Wolkenwanderer!", "Sphärenklänge und Windgesang!");



    //===============================================REQUEST METHODS===============================================\\

    public List<Hero> showHero(){
        return heroes;
    }

    public ResponseEntity<Hero> createHero(@RequestBody Hero hero) {
        if (!heroTypes.contains(hero.type())) {
            return ResponseEntity.badRequest().build();
        }
        else {
            Hero newHero = new Hero(counter.getAndIncrement(), hero.name(), hero.HP(), hero.Damage(), hero.type(), hero.extra());
            heroes.add(newHero);
            return ResponseEntity.ok(newHero);
        }
    }

    public String createProfiles() {
        counter.set(1);
        heroes.clear();

        for (int i = 0; i < heroNames.size(); i++) {
            String name = heroNames.get(i);
            String element = heroTypes.get(i);
            int hp = hpValues.get(i);
            int damage = damageValues.get(i);
            String description = extras.get(i);

            Hero hero = new Hero(
                    counter.getAndIncrement(),
                    name,
                    hp,
                    damage,
                    element,
                    description
            );
            heroes.add(hero);
        }

        return "Profile wurden erfolgreich erstellt!";
    }

    public String delhero(@RequestBody(required = false) Hero herodel, @RequestParam(value = "id", required = false) Integer id) {
        if (herodel != null && herodel.id() >= 0) {
            for (int i = 0; i < heroes.size(); i++) {
                Hero hero = heroes.get(i);
                if (hero.id() == herodel.id()) {
                    heroes.remove(i);
                    return "Passt!";
                }
            }
        } else if (id != null && id >= 0) {
            for (int i = 0; i < heroes.size(); i++) {
                Hero hero = heroes.get(i);
                if (hero.id() == id) {
                    heroes.remove(i);
                    return "Passt!";
                }
            }
        }
        return "Passt nicht!";
    }

    public String deleteAllHeroes() {
        heroes.clear();
        counter.set(1);
        return "Alle Helden wurden gelöscht!";
    }

    public String editHero(@RequestBody Hero heroe) {
        if (heroe.HP() < 0 || heroe.HP() > 100 || heroe.Damage() < 0 || heroe.Damage() > 100 || heroe.type() == null) {
            return "Fehlgeschlagen!";
        }

        if (heroe.id() >= 0) {
            for (int i = 0; i < heroes.size(); i++) {
                Hero hero = heroes.get(i);
                if (hero.id() == heroe.id()) {
                    heroes.set(i, heroe);
                    return "Erfolgreich!";
                }
            }
        }

        return "Fehlgeschlagen!";
    }


    //===============================================FILE MANAGEMENT===============================================\\

    public void loadHeroes() {
        try {
            File file = new File(HEROES_FILE_PATH);
            if (file.exists()) {
                heroes = mapper.readValue(file, new TypeReference<>() {});
                if (!heroes.isEmpty()) {
                    counter.set(heroes.stream().mapToInt(Hero::id).max().getAsInt() + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Fehler beim laden der Helden!");
        }
    }

    public void saveHeroes() {
        try {
            mapper.writeValue(new File(HEROES_FILE_PATH), heroes);
        } catch (IOException e) {
            System.out.println("Fehler beim speichern der Helden!");
        }
    }
}
