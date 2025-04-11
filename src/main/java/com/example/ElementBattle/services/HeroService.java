package com.example.ElementBattle.services;

import com.example.ElementBattle.repositories.HeroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.example.ElementBattle.classes.Hero;
import java.util.NoSuchElementException;

@Service
public class HeroService {

    private final HeroRepository heroRepository;

    @Autowired
    public HeroService(HeroRepository heroRepository) {
        this.heroRepository = heroRepository;
    }

    //===============================================VARIABLES===============================================\\

    private static final List<String> heroNames = List.of("Ignis Flammenherz", "Pyra Glutsturm", "Vulcan Feuerschild", "Inferno Feuerschlag", "Helios Sonnentänzer", "Aqualis Meeresrufer", "Nerina Wellenweber", "Aquara Wasserschild", "Hydra Aquadonner", "Poseidon Meereswoge", "Flora Rankengriff", "Sylva Blattwerk", "Gaia Grünhüter", "Verdura Blattsturm", "Arborea Waldwisperer", "Voltaris Donnerklinge", "Electra Blitzschlag", "Raiden Donnerfaust", "Jolt Elektrosturm", "Thor Donnergroll", "Terran Steinschmied", "Gaia Erdenwächter", "Atlas Felsenfaust", "Terra Erdenschild", "Golem Felsensturm", "Glacies Frostschild", "Frostius Eisessturm", "Tundrus Eisbann", "Chillara Frosthauch", "Blizzara Frostfeuer", "Zephyrus Windrufer", "Aeris Luftklinge", "Aeolus Sturmbote", "Galea Windesflügel", "Caelus Wolke");
    private static final List<String> heroTypes = List.of("Feuer", "Feuer", "Feuer", "Feuer", "Feuer", "Wasser", "Wasser", "Wasser", "Wasser", "Wasser", "Pflanze", "Pflanze", "Pflanze", "Pflanze", "Pflanze", "Elektro", "Elektro", "Elektro", "Elektro", "Elektro", "Erde", "Erde", "Erde", "Erde", "Erde", "Eis", "Eis", "Eis", "Eis", "Eis", "Luft", "Luft", "Luft", "Luft", "Luft");
    private static final List<Integer> hpValues = List.of(12, 11, 14, 9, 13, 11, 10, 13, 12, 9, 10, 9, 12, 13, 11, 9, 8, 11, 10, 14, 15, 13, 10, 12, 11, 10, 12, 14, 11, 9, 9, 11, 13, 10, 12);
    private static final List<Integer> damageValues = List.of(12, 10, 8, 13, 14, 11, 9, 14, 12, 13, 10, 12, 9, 14, 13, 13, 14, 11, 10, 8, 9, 13, 8, 12, 14, 12, 10, 14, 13, 9, 14, 13, 10, 9, 12);
    private static final List<String> extras = List.of("Entfesselt ein feuriges Inferno!", "Brennt mit der Hitze von tausend Sonnen!", "Schmiedet Schwerter aus flüssiger Lava!", "Tanzt um Feuerstürme herum!", "Herrscht über Vulkan und Flamme!", "Beherrscht die Wellen und Meerestiere.", "Beruhigt und kontrolliert die Gezeiten.", "Tiefseeabenteuer und Ozeansymphonie!", "Ruft die Meeresgötter zu Hilfe!", "Wasserwirbel und Strudelmeisterschaft!", "Umschlingt Gegner mit Ranken!", "Meister der Photosynthese und des Waldwachstums.", "Blütenzauber und Fruchtbarkeitsernte!", "Wurzeln, die bis in die Tiefen reichen!", "Beherrscht den Wald in seiner ganzen Pracht!", "Schlägt mit der Kraft eines Gewitters zu.", "Elektrisiert das Schlachtfeld mit einem Funken.", "Blitze zucken durch die Luft!", "Ladung, die die Luft zum Zerreißen bringt!", "Donnerhall und elektrische Eruption!", "Fest wie ein Berg, unnachgiebig.", "Mutter der Erde und der Natur.", "Erdbeben und Steinschatten!", "Felsen, die wie Wasser fließen!", "Wurzeln, die sich tief in die Erde graben!", "Eisige Kältepeitsche!", "Frostiger Sturm!", "Schneesturm und gefrorene Stille!", "Glitzerndes Eis und kristalline Eleganz!", "Frostbeulen und gefrorene Schicksale!", "Ruft die Winde und Stürme herbei!", "Tanzt mit den Lüften!", "Flügel aus Wolken und federleichter Tanz!", "Sturmfänger und Wolkenwanderer!", "Sphärenklänge und Windgesang!");



    //===============================================REQUEST METHODS===============================================\\

    public List<Hero> showHero() {
        try {
            return heroRepository.getAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public ResponseEntity<Hero> createHero(Hero hero) {
        try {
            Hero newHero = heroRepository.add(hero);
            return ResponseEntity.ok(newHero);
        } catch (Exception e) {
            return ResponseEntity.ok(hero);
        }
    }

    public String createProfiles() {
        try {
            heroRepository.resetId();
            heroRepository.deleteAll();

            for (int i = 0; i < heroNames.size(); i++) {
                Hero hero = new Hero(
                        0,
                        heroNames.get(i),
                        hpValues.get(i),
                        damageValues.get(i),
                        heroTypes.get(i),
                        extras.get(i)
                );
                heroRepository.add(hero);
            }
            return "Profile wurden erfolgreich erstellt!";
        } catch (Exception e) {
            return "Fehler beim Erstellen der Profile: " + e.getMessage();
        }
    }

    public String delhero(Hero herodel, Integer id) {
        if (id == null) return "ID fehlt!";
        try {
            heroRepository.delete(id);
            return "Hero wurde gelöscht!";
        } catch (NoSuchElementException e) {
            return "Hero mit ID " + id + " nicht gefunden!";
        } catch (Exception e) {
            return "Fehler beim Löschen des Helden: " + e.getMessage();
        }
    }

    public String deleteAllHeroes() {
        try {
            heroRepository.deleteAll();
            heroRepository.resetId();
            return "Alle Helden wurden gelöscht!";
        } catch (Exception e) {
            return "Fehler beim Löschen aller Helden: " + e.getMessage();
        }
    }

    public String editHero(Hero heroe) {
        if (heroe.HP() < 0 || heroe.HP() > 100 || heroe.Damage() < 0 || heroe.Damage() > 100 || heroe.type() == null) {
            return "Ungültige Werte!";
        }

        try {
            heroRepository.update(heroe);
            return "Held wurde aktualisiert!";
        } catch (NoSuchElementException e) {
            return "Held mit ID " + heroe.id() + " nicht gefunden!";
        } catch (Exception e) {
            return "Fehler beim Aktualisieren: " + e.getMessage();
        }
    }
}
