package ro.hunys.tn2;

/**
 * Toate textele jocului, în română și în engleză. Liniile care încep cu '#' sunt titluri.
 * Limba se schimbă cu set(); de aceea câmpurile nu sunt finale.
 */
public final class Texts {
    private Texts() {
    }

    public static boolean EN;
    public static final String AUTHOR = "Hunys with Claude AI";
    public static String TITLE, TITLE2, SUBTITLE, PLACE, EPISODE;
    public static String M_CONTINUE, M_NEW, M_SETTINGS, M_EXIT, M_BACK, M_RESUME, M_SAVE, M_MAIN, M_CONTROLS;
    public static String S_DIFF, S_PIXEL, S_SOUND, S_VIBRA, S_LANG, S_SENS, S_FPS, S_HANDED;
    public static String[] DIFF_NAMES, PIXEL_NAMES, SENS_NAMES, HANDED_NAMES;
    public static String ON, OFF, LANG_NAME;
    public static String SAVED, SAVE_FAIL, LOADED, LOAD_FAIL;
    public static String TAP_CONTINUE, TAP_CLOSE, SCROLL_HINT;
    public static String GOT_AMMO, GOT_SHELLS, GOT_SMGAMMO, GOT_MED, GOT_SHOTGUN, GOT_SMG;
    public static String GOT_BLUE, GOT_RED, GOT_YELLOW, GOT_GREEN;
    public static String LOCK_BLUE, LOCK_RED, LOCK_YELLOW, LOCK_GREEN, USE_BLUE, USE_RED, USE_YELLOW, USE_GREEN;
    public static String STAIRS_NO_POWER, NEED_MARA, ENTRANCE_LOCKED, ELEVATOR_DEAD, ELEVATOR_BROKEN, GARAGE_LOCKED;
    public static String GEN_ON, GEN_DONE, NO_AMMO, RELOADING;
    public static String[] WEAPONS;
    public static String PAUSE_TITLE, MAP_TITLE, DEAD_TITLE, DEAD_LOAD, DEAD_RETRY, DEAD_MENU;
    public static String OBJ_LABEL, NOTES_LABEL, RADIO_NAME;
    public static String[] OBJ;
    public static String[] STORY;
    public static String[] NOTE_TITLES, NOTES;
    public static String[] RADIO;
    public static String SCENE_MARA, SCENE_IOANA;
    public static String[] WIN;
    public static String STATS_TITLE, STAT_DIFF, STAT_TIME, STAT_KILLS, STAT_SHOTS, STAT_NOTES;
    public static String[] CONTROLS;
    public static String[] BTN;
    private static String[] sceneIds, sceneTexts;

    static {
        set(false);
    }

    public static void set(boolean english) {
        EN = english;
        if (english) {
            english();
        } else {
            romanian();
        }
    }

    /** Scena de la prima intrare într-o cameră (sau null). */
    public static String scene(String id) {
        for (int i = 0; i < sceneIds.length; i++) {
            if (sceneIds[i].equals(id)) {
                return sceneTexts[i];
            }
        }
        return null;
    }

    private static void romanian() {
        TITLE = "TURA DE NOAPTE";
        TITLE2 = "2";
        SUBTITLE = "Ediție specială";
        PLACE = "ZORI TV · București";
        EPISODE = "Episodul 1: Parterul";
        M_CONTINUE = "Continuă";
        M_NEW = "Joc nou";
        M_SETTINGS = "Setări";
        M_EXIT = "Ieșire";
        M_BACK = "Înapoi";
        M_RESUME = "Continuă jocul";
        M_SAVE = "Salvează jocul";
        M_MAIN = "Meniu principal";
        M_CONTROLS = "Comenzi";
        S_DIFF = "Dificultate";
        S_PIXEL = "Pixeli";
        S_SOUND = "Sunet";
        S_VIBRA = "Vibrații";
        S_LANG = "Limba";
        S_SENS = "Sensibilitate";
        S_FPS = "Contor FPS";
        S_HANDED = "Butonul FOC";
        DIFF_NAMES = new String[] {"Ușor", "Normal", "Greu"};
        PIXEL_NAMES = new String[] {"Fini", "Medii", "Retro"};
        SENS_NAMES = new String[] {"Mică", "Medie", "Mare"};
        HANDED_NAMES = new String[] {"Dreapta", "Stânga"};
        ON = "Pornit";
        OFF = "Oprit";
        LANG_NAME = "Română";
        SAVED = "Joc salvat.";
        SAVE_FAIL = "Salvarea nu a reușit.";
        LOADED = "Joc încărcat.";
        LOAD_FAIL = "Salvarea nu poate fi citită.";
        TAP_CONTINUE = "Atinge ecranul pentru a continua";
        TAP_CLOSE = "Atinge ca să închizi";
        SCROLL_HINT = "Trage în sus / jos ca să derulezi";
        GOT_AMMO = "Muniție 9mm +";
        GOT_SHELLS = "Cartușe +";
        GOT_SMGAMMO = "Încărcătoare PM +";
        GOT_MED = "Trusă de prim ajutor +";
        GOT_SHOTGUN = "PUȘCĂ! Butonul cu arma schimbă arma.";
        GOT_SMG = "PISTOL-MITRALIERĂ! Ține FOC apăsat.";
        GOT_BLUE = "Card ALBASTRU (arhiva)";
        GOT_RED = "Card ROȘU (camera tehnică)";
        GOT_YELLOW = "Cheia GALBENĂ (rampa)";
        GOT_GREEN = "Card VERDE";
        LOCK_BLUE = "Încuiat. Trebuie cardul ALBASTRU.";
        LOCK_RED = "Încuiat. Trebuie cardul ROȘU.";
        LOCK_YELLOW = "Încuiat. Trebuie cheia GALBENĂ.";
        LOCK_GREEN = "Încuiat. Trebuie cardul VERDE.";
        USE_BLUE = "Cardul ALBASTRU a deschis ușa.";
        USE_RED = "Cardul ROȘU a deschis ușa.";
        USE_YELLOW = "Cheia GALBENĂ a deschis ușa.";
        USE_GREEN = "Cardul VERDE a deschis ușa.";
        STAIRS_NO_POWER = "Ușa scării e electronică. Nu are curent.";
        NEED_MARA = "Nu urc fără Mara. Ea știe drumul.";
        ENTRANCE_LOCKED = "Ușile de la intrare s-au blocat.";
        ELEVATOR_DEAD = "Liftul nu are curent.";
        ELEVATOR_BROKEN = "Liftul e blocat între etaje.";
        GARAGE_LOCKED = "Ușa de garaj e blocată din afară.";
        GEN_ON = "Generatorul pornește! Se aprind luminile.";
        GEN_DONE = "Generatorul merge.";
        NO_AMMO = "Fără muniție!";
        RELOADING = "REÎNCĂRCARE...";
        WEAPONS = new String[] {"Pistol", "Pușcă", "Pistol-mitralieră"};
        PAUSE_TITLE = "PAUZĂ";
        MAP_TITLE = "HARTA · PARTER";
        DEAD_TITLE = "AI MURIT";
        DEAD_LOAD = "Încarcă ultima salvare";
        DEAD_RETRY = "Încearcă din nou";
        DEAD_MENU = "Meniu principal";
        OBJ_LABEL = "Obiectiv";
        NOTES_LABEL = "Notițe";
        RADIO_NAME = "IOANA (stație)";
        OBJ = new String[] {
            "Găsește-o pe Mara",
            "Găsește cardul roșu al lui Sorin, în regie",
            "Pornește generatorul din camera tehnică",
            "Urcă pe scara de serviciu"
        };
        BTN = new String[] {"FOC", "FOLOSEȘTE", "ARMĂ", "REÎNC.", "HARTĂ", "PAUZĂ"};
        CONTROLS = new String[] {
            "Stânga jos: joystick - mers și pas lateral",
            "Trage pe partea dreaptă a ecranului - rotire",
            "FOC - trage (ține apăsat la pistolul-mitralieră)",
            "Poți roti și cu degetul pe butonul FOC",
            "Mâna - deschide uși, vorbește, folosește",
            "Arma - schimbă arma; săgețile - reîncarcă",
            "Sus dreapta: harta și pauza (salvare)"
        };
        STORY = new String[] {
            "#BUCUREȘTI, ORA 06:12\n\nAmbulanța intră în oraș pe străzi goale. Semafoarele clipesc galben. Pe bancheta din spate, cutia cu probe tace de o oră.\n\nIoana nu-și ia ochii de pe ea.",
            "Jurnalista se numește Mara Ilie, de la ZORI TV. A răspuns la al doilea apel și a zis un singur lucru:\n\n„Veniți direct la televiziune. La 7 fix intrăm în direct cu voi. Vă aștept la intrare.”",
            "#ZORI TV, ORA 06:31\n\nLa intrare nu așteaptă nimeni. Clădirea e întunecată, doar firma EMISIE de deasupra studioului clipește roșu.\n\nPe trotuar, o dubă albă cu ușile deschise. Pe ea scrie VECTRA BIO.",
            "Ușile glisante se deschid singure și se închid în urma lor. Se blochează.\n\nÎn hol, toate televizoarele arată același lucru: bare colorate și mesajul EMISIE ÎNTRERUPTĂ.",
            "Ioana se închide în biroul pazei, cu probele și cu monitoarele camerelor de supraveghere. Îi întinde lui Andrei o stație radio:\n\n„Găsește-o pe Mara. Eu te ghidez de aici.”\n\n#OBIECTIV: GĂSEȘTE-O PE MARA"
        };
        SCENE_MARA = "#MARA ILIE\n\nMara iese de după canapea, cu microfonul strâns ca pe un cuțit.\n„Voi sunteți? Am crezut că sunt ei.”\n\n„Vectra a tăiat curentul la 6 fix. Au adus «vaccinuri» pentru toată tura de noapte. Jumătate dintre colegi s-au întors... altfel.”\n\n„Intrăm în direct din regia centrală, de la etaj. Dar scara are încuietoare electronică și generatorul e oprit. Cardul roșu de la camera tehnică îl avea Sorin, tehnicianul din regia studioului 1.”\n\n#OBIECTIV: GĂSEȘTE CARDUL ROȘU";
        SCENE_IOANA = "#IOANA\n\nIoana nu se uită la tine, ci la monitoare. Pe unul dintre ele, o siluetă în combinezon alb trece încet prin hol.\n\n„Sunt bine. Probele sunt aici. Tu ai grijă de tine.”\n\nCutia cu probe stă pe birou. Din ea se aude din nou zgâriatul. Mai repede.";
        sceneIds = new String[] {"cantina", "invitati", "wc", "magazie", "arhiva", "montaj", "regie", "studio1", "tehnic",
            "machiaj", "garderoba", "rampa", "coridor_a"};
        sceneTexts = new String[] {
            "#CANTINA\n\nPe mese, cafele începute și micul dejun al turei de noapte. Pe o tavă, zeci de fiole goale cu eticheta VN-9.\n\nPe perete, un afiș: «Vaccinare gratuită pentru angajați. Oferită de Vectra Bio.»",
            "#SALA INVITAȚILOR\n\nAici așteptau invitații emisiunii de dimineață. Pe ecran rulează în buclă aceeași reclamă: un doctor zâmbitor ține o seringă.\n\n«VN-9. Siguranța ta, misiunea noastră.»",
            "#TOALETA\n\nOglinzile sunt sparte. Pe una dintre ele, cineva a scris cu ruj:\n\nNU VĂ LĂSAȚI ÎNȚEPAȚI.",
            "#MAGAZIA\n\nCutii cu decoruri vechi, cabluri și reflectoare stricate. Cineva a încercat să se ascundă aici. N-a reușit.",
            "#ARHIVA\n\nRafturi până în tavan, cu casete vechi de treizeci de ani. Pe o masă, o cutie deschisă: VECTRA BIO - REPORTAJ INTERZIS, 2019.\n\nCaseta lipsește.",
            "#CAMERA DE MONTAJ\n\nPe monitoare a rămas oprită aceeași imagine: o hală plină de lăzi frigorifice. Data din colț e de acum trei ani.",
            "#REGIA STUDIOULUI 1\n\nZeci de ecrane, toate pe bare colorate. Pe pupitru, o cană de cafea încă caldă și o dâră lungă de sânge care duce sub masă.",
            "#STUDIOUL 1\n\nPlatoul jurnalului de dimineață e pregătit: lumini, camere, scaunele prezentatorilor. Ceasul de pe perete s-a oprit la 06:00:00.\n\nPe prompter, prima știre: «Vaccinarea cu VN-9 începe azi în toată țara.»",
            "#CAMERA TEHNICĂ\n\nServerele bâzâie pe baterii. Maneta generatorului e trasă jos și sigilată cu bandă galbenă: VECTRA - NU ATINGEȚI.",
            "#MACHIAJ\n\nDoar oglinzile cu becuri mai luminează. Pe scaunul din mijloc, un halat cu numele prezentatoarei. Pe jos, o seringă goală.",
            "#GARDEROBA\n\nCostume pe umerașe, rânduri întregi. Unele se mișcă, deși nu e niciun curent de aer.",
            "#RAMPA DE ÎNCĂRCARE\n\nUșa de garaj e blocată. Afară se aude motorul dubei Vectra mergând în gol.\n\nLăzi albe, stivuite până sus: VN-9, LOT DE DISTRIBUȚIE - BUCUREȘTI.",
            "#SCARA DE SERVICIU\n\nLa capătul coridorului, ușa scării are un cititor de card stins. Fără curent nu se deschide."
        };
        NOTE_TITLES = new String[] {
            "REGISTRUL RECEPȚIEI", "JURNAL DE PAZĂ", "AFIȘ PE FRIGIDER", "PROGRAMUL INVITAȚILOR",
            "FIȘA CASETEI 2019-114", "BILET LIPIT PE MONITOR", "CARNETUL LUI SORIN", "SCENARIUL JURNALULUI",
            "NOTIȚELE MAREI", "ORDIN VECTRA - ECHIPA 2", "LISTA DE MACHIAJ", "AVIZ DE LIVRARE"
        };
        NOTES = new String[] {
            "05:40 - A sosit echipa Vectra Bio (6 persoane), «campanie de vaccinare pentru angajați». Aprobat de director.\n05:55 - Vectra cere acces la camera tehnică.\n06:00 - ",
            "05:58 - Cei de la Vectra au intrat în camera tehnică. Zic că «verifică generatorul».\n06:00 - S-a luat curentul. Ușa scării s-a blocat.\n06:04 - Vaccinații stau nemișcați pe holuri. Am încuiat arhiva cu cardul albastru. Îl las aici, în birou.",
            "Vaccinare GRATUITĂ cu VN-9!\nAzi, între 05:45 și 06:00, la cantină.\nCine se vaccinează primește o zi liberă.\n\nPe colț, cu pixul: «eu nu mă duc. - Sorin»",
            "Bine ați venit la «Știrile Zorii»! Vă rugăm să așteptați aici până vă cheamă regizorul.\n\nInvitatul de azi: dr. Valentin Marcu, Vectra Bio, despre «noul vaccin al României».\n\nNumele e tăiat cu markerul roșu.",
            "Reportaj: «Ce se ascunde în laboratorul de pe deal». Autor: Mara Ilie.\nStare: RETRAS DE LA DIFUZARE, la cererea conducerii.\n\nMotiv: «presiuni din partea unui sponsor».\nSponsor: Vectra Bio.",
            "Mara, am păstrat o copie a reportajului din 2019. E pe hard-diskul din regia centrală, la etaj. Dacă pățesc ceva, dă-l pe post.\n- Sorin",
            "Mi-au cerut cardul roșu de la camera tehnică. Am zis că l-am pierdut.\n\nDacă citești asta și eu nu mai sunt eu: cardul e în buzunarul de la piept. Ia-l.",
            "ȘTIREA 1: Vaccinarea cu VN-9 începe astăzi în toată țara. Ministerul Sănătății recomandă...\n\nTextul continuă pe trei pagini. Nimeni nu l-a mai citit.",
            "06:02 - s-a luat curentul.\n06:05 - Vali de la sport a mușcat-o pe machieuză.\n06:09 - m-am închis în studioul 2.\n\nDacă vin Ioana și fratele ei, trebuie să ajungem în regia centrală. De acolo putem da totul pe post.",
            "Obiectiv: nicio emisiune până la ora 9:00, când loturile VN-9 pleacă spre spitale.\nGeneratorul rămâne oprit.\n\nMartorii nu se recuperează.",
            "06:00 - Prezentatoarea (vaccinată)\n06:10 - Invitat Vectra (anulat)\n06:20 - Meteo\n\nDedesubt: «nu mai ține nici fondul de ten. are pielea rece.» Și mai jos: «cheia de la rampă e în garderobă, ca de obicei.»",
            "Expeditor: Vectra Bio, lotul VN-9\nDestinatar: 14 spitale din București\nLivrare: azi, ora 09:00\n\nSemnătura de primire: «dr. V. Marcu». Cerneala e încă umedă."
        };
        RADIO = new String[] {
            "Andrei, mă auzi? M-am închis în biroul pazei. Văd holul pe camere... Nu e nimeni. Adică nimeni viu.",
            "Pe camere văd studioul 2 încuiat pe dinăuntru. Poate Mara s-a ascuns acolo. E pe aripa de est.",
            "Cardul albastru deschide arhiva, în capătul de vest. Paznicul scria că a încuiat ceva înăuntru. Ai grijă.",
            "Mara zice că regia studioului 1 e în aripa de vest, lângă coridorul de serviciu.",
            "Camera tehnică e în capătul de est, după studioul 2. Ușa roșie.",
            "O pușcă? Paznicii țineau arma de serviciu în arhivă. Folosește-o.",
            "S-au aprins luminile! Și... toți de pe holuri s-au întors spre lumină. Fugi la scară, Andrei!"
        };
        WIN = new String[] {
            "Ușa scării se deschide cu un clic. Mara urcă prima, cu lanterna telefonului.\n\nDin stație, vocea Ioanei: „Pe camere văd etajul. Redacția e plină. Și ceva mare se mișcă în regia centrală.”",
            "#SFÂRȘITUL EPISODULUI 1\n\nCurentul a revenit. Emisiunea, încă nu.\n\n#URMEAZĂ: REDACȚIA"
        };
        STATS_TITLE = "#STATISTICI";
        STAT_DIFF = "Dificultate: ";
        STAT_TIME = "Timp: ";
        STAT_KILLS = "Infectați eliminați: ";
        STAT_SHOTS = "Focuri trase: ";
        STAT_NOTES = "Notițe găsite: ";
    }

    private static void english() {
        TITLE = "NIGHT SHIFT";
        TITLE2 = "2";
        SUBTITLE = "Special Report";
        PLACE = "ZORI TV · Bucharest";
        EPISODE = "Episode 1: The Ground Floor";
        M_CONTINUE = "Continue";
        M_NEW = "New game";
        M_SETTINGS = "Settings";
        M_EXIT = "Exit";
        M_BACK = "Back";
        M_RESUME = "Resume game";
        M_SAVE = "Save game";
        M_MAIN = "Main menu";
        M_CONTROLS = "Controls";
        S_DIFF = "Difficulty";
        S_PIXEL = "Pixels";
        S_SOUND = "Sound";
        S_VIBRA = "Vibration";
        S_LANG = "Language";
        S_SENS = "Sensitivity";
        S_FPS = "FPS counter";
        S_HANDED = "FIRE button";
        DIFF_NAMES = new String[] {"Easy", "Normal", "Hard"};
        PIXEL_NAMES = new String[] {"Fine", "Medium", "Retro"};
        SENS_NAMES = new String[] {"Low", "Medium", "High"};
        HANDED_NAMES = new String[] {"Right", "Left"};
        ON = "On";
        OFF = "Off";
        LANG_NAME = "English";
        SAVED = "Game saved.";
        SAVE_FAIL = "Saving failed.";
        LOADED = "Game loaded.";
        LOAD_FAIL = "The save can't be read.";
        TAP_CONTINUE = "Tap the screen to continue";
        TAP_CLOSE = "Tap to close";
        SCROLL_HINT = "Drag up / down to scroll";
        GOT_AMMO = "9mm ammo +";
        GOT_SHELLS = "Shells +";
        GOT_SMGAMMO = "SMG magazines +";
        GOT_MED = "First aid kit +";
        GOT_SHOTGUN = "SHOTGUN! The weapon button switches weapons.";
        GOT_SMG = "SUBMACHINE GUN! Hold FIRE down.";
        GOT_BLUE = "BLUE card (archive)";
        GOT_RED = "RED card (technical room)";
        GOT_YELLOW = "YELLOW key (loading dock)";
        GOT_GREEN = "GREEN card";
        LOCK_BLUE = "Locked. You need the BLUE card.";
        LOCK_RED = "Locked. You need the RED card.";
        LOCK_YELLOW = "Locked. You need the YELLOW key.";
        LOCK_GREEN = "Locked. You need the GREEN card.";
        USE_BLUE = "The BLUE card opened the door.";
        USE_RED = "The RED card opened the door.";
        USE_YELLOW = "The YELLOW key opened the door.";
        USE_GREEN = "The GREEN card opened the door.";
        STAIRS_NO_POWER = "The stairwell door is electronic. No power.";
        NEED_MARA = "I'm not going up without Mara. She knows the way.";
        ENTRANCE_LOCKED = "The entrance doors are locked.";
        ELEVATOR_DEAD = "The elevator has no power.";
        ELEVATOR_BROKEN = "The elevator is stuck between floors.";
        GARAGE_LOCKED = "The garage door is locked from outside.";
        GEN_ON = "The generator starts! The lights come on.";
        GEN_DONE = "The generator is running.";
        NO_AMMO = "Out of ammo!";
        RELOADING = "RELOADING...";
        WEAPONS = new String[] {"Pistol", "Shotgun", "Submachine gun"};
        PAUSE_TITLE = "PAUSED";
        MAP_TITLE = "MAP · GROUND FLOOR";
        DEAD_TITLE = "YOU DIED";
        DEAD_LOAD = "Load the last save";
        DEAD_RETRY = "Try again";
        DEAD_MENU = "Main menu";
        OBJ_LABEL = "Objective";
        NOTES_LABEL = "Notes";
        RADIO_NAME = "IOANA (radio)";
        OBJ = new String[] {
            "Find Mara",
            "Find Sorin's red card, in the control room",
            "Start the generator in the technical room",
            "Take the service stairs up"
        };
        BTN = new String[] {"FIRE", "USE", "WEAPON", "RELOAD", "MAP", "PAUSE"};
        CONTROLS = new String[] {
            "Bottom left: joystick - walk and strafe",
            "Drag on the right side of the screen - turn",
            "FIRE - shoot (hold it with the submachine gun)",
            "You can also turn with your finger on FIRE",
            "Hand - open doors, talk, use",
            "Gun - switch weapon; arrows - reload",
            "Top right: map and pause (save)"
        };
        STORY = new String[] {
            "#BUCHAREST, 6:12 AM\n\nThe ambulance enters the city through empty streets. The traffic lights blink yellow. On the back seat, the sample box has been silent for an hour.\n\nIoana can't take her eyes off it.",
            "The journalist's name is Mara Ilie, from ZORI TV. She picked up the second call and said only one thing:\n\n\"Come straight to the station. At seven sharp we go live with you. I'll wait for you at the entrance.\"",
            "#ZORI TV, 6:31 AM\n\nNobody is waiting at the entrance. The building is dark; only the ON AIR sign above the studio blinks red.\n\nOn the sidewalk, a white van with its doors open. On its side: VECTRA BIO.",
            "The sliding doors open by themselves and close behind them. They lock.\n\nIn the lobby, every TV shows the same thing: color bars and the message BROADCAST INTERRUPTED.",
            "Ioana locks herself in the security office, with the samples and the camera monitors. She hands Andrei a radio:\n\n\"Find Mara. I'll guide you from here.\"\n\n#OBJECTIVE: FIND MARA"
        };
        SCENE_MARA = "#MARA ILIE\n\nMara steps out from behind the sofa, gripping her microphone like a knife.\n\"Is that you? I thought it was them.\"\n\n\"Vectra cut the power at six sharp. They brought «vaccines» for the whole night shift. Half of my colleagues came back... different.\"\n\n\"We go live from the master control room, upstairs. But the stairwell has an electronic lock and the generator is off. Sorin, the technician in the Studio 1 control room, had the red card to the technical room.\"\n\n#OBJECTIVE: FIND THE RED CARD";
        SCENE_IOANA = "#IOANA\n\nIoana isn't looking at you but at the monitors. On one of them, a figure in a white suit slowly crosses the lobby.\n\n\"I'm fine. The samples are here. You take care of yourself.\"\n\nThe sample box sits on the desk. The scratching inside has started again. Faster.";
        sceneIds = new String[] {"cantina", "invitati", "wc", "magazie", "arhiva", "montaj", "regie", "studio1", "tehnic",
            "machiaj", "garderoba", "rampa", "coridor_a"};
        sceneTexts = new String[] {
            "#THE CAFETERIA\n\nOn the tables, half-finished coffees and the night shift's breakfast. On a tray, dozens of empty vials labeled VN-9.\n\nOn the wall, a poster: «Free vaccination for employees. Courtesy of Vectra Bio.»",
            "#GREEN ROOM\n\nThe morning show's guests waited here. The screen loops the same commercial: a smiling doctor holding a syringe.\n\n«VN-9. Your safety, our mission.»",
            "#RESTROOM\n\nThe mirrors are broken. On one of them, someone wrote in lipstick:\n\nDON'T LET THEM STICK YOU.",
            "#STOREROOM\n\nBoxes of old sets, cables and broken spotlights. Someone tried to hide in here. They didn't make it.",
            "#THE ARCHIVE\n\nShelves up to the ceiling, with tapes thirty years old. On a table, an open box: VECTRA BIO - BANNED REPORT, 2019.\n\nThe tape is missing.",
            "#EDITING ROOM\n\nThe monitors are frozen on the same image: a hall full of refrigerated crates. The date in the corner is three years old.",
            "#STUDIO 1 CONTROL ROOM\n\nDozens of screens, all on color bars. On the desk, a coffee mug still warm and a long smear of blood leading under the table.",
            "#STUDIO 1\n\nThe morning news set is ready: lights, cameras, the anchors' chairs. The wall clock stopped at 06:00:00.\n\nOn the teleprompter, the first story: «VN-9 vaccination starts today across the country.»",
            "#TECHNICAL ROOM\n\nThe servers hum on battery power. The generator lever is pulled down and sealed with yellow tape: VECTRA - DO NOT TOUCH.",
            "#MAKEUP ROOM\n\nOnly the bulb-framed mirrors still glow. On the middle chair, a robe with the anchor's name. On the floor, an empty syringe.",
            "#WARDROBE\n\nCostumes on hangers, row after row. Some of them are moving, though there's no draft at all.",
            "#LOADING DOCK\n\nThe garage door is locked. Outside, the Vectra van's engine is idling.\n\nWhite crates stacked to the top: VN-9, DISTRIBUTION BATCH - BUCHAREST.",
            "#SERVICE STAIRS\n\nAt the end of the corridor, the stairwell door has a dead card reader. Without power, it won't open."
        };
        NOTE_TITLES = new String[] {
            "RECEPTION LOG", "SECURITY LOG", "POSTER ON THE FRIDGE", "GUEST SCHEDULE",
            "TAPE FILE 2019-114", "NOTE STUCK ON A MONITOR", "SORIN'S NOTEBOOK", "NEWS SCRIPT",
            "MARA'S NOTES", "VECTRA ORDER - TEAM 2", "MAKEUP LIST", "DELIVERY NOTICE"
        };
        NOTES = new String[] {
            "5:40 AM - Vectra Bio team arrived (6 people), «employee vaccination campaign». Approved by the director.\n5:55 AM - Vectra asks for access to the technical room.\n6:00 AM - ",
            "5:58 AM - The Vectra people went into the technical room. They say they're «checking the generator».\n6:00 AM - The power went out. The stairwell door locked.\n6:04 AM - The vaccinated just stand still in the corridors. I locked the archive with the blue card. I'm leaving it here, in the office.",
            "FREE vaccination with VN-9!\nToday, between 5:45 and 6:00 AM, in the cafeteria.\nEveryone who gets vaccinated gets a day off.\n\nIn the corner, in pen: «I'm not going. - Sorin»",
            "Welcome to «The Dawn News»! Please wait here until the director calls you.\n\nToday's guest: Dr. Valentin Marcu, Vectra Bio, on «Romania's new vaccine».\n\nThe name is crossed out in red marker.",
            "Report: «What's hidden in the lab on the hill». Author: Mara Ilie.\nStatus: PULLED FROM BROADCAST at management's request.\n\nReason: «pressure from a sponsor».\nSponsor: Vectra Bio.",
            "Mara, I kept a copy of the 2019 report. It's on the hard drive in the master control room, upstairs. If anything happens to me, put it on air.\n- Sorin",
            "They asked me for the red card to the technical room. I said I lost it.\n\nIf you're reading this and I'm not myself anymore: the card is in my breast pocket. Take it.",
            "STORY 1: VN-9 vaccination starts today across the country. The Ministry of Health recommends...\n\nThe text goes on for three pages. Nobody read it after that.",
            "6:02 AM - the power went out.\n6:05 AM - Vali from sports bit the makeup artist.\n6:09 AM - I locked myself in Studio 2.\n\nIf Ioana and her brother come, we have to reach the master control room. From there we can put it all on air.",
            "Objective: no broadcast until 9:00 AM, when the VN-9 batches leave for the hospitals.\nThe generator stays off.\n\nThe witnesses are not to be recovered.",
            "6:00 AM - The anchor (vaccinated)\n6:10 AM - Vectra guest (cancelled)\n6:20 AM - Weather\n\nBelow: «even foundation won't stay on. her skin is cold.» And further down: «the loading dock key is in the wardrobe, as usual.»",
            "Sender: Vectra Bio, batch VN-9\nRecipient: 14 hospitals in Bucharest\nDelivery: today, 9:00 AM\n\nReceived by: «Dr. V. Marcu». The ink is still wet."
        };
        RADIO = new String[] {
            "Andrei, can you hear me? I locked myself in the security office. I can see the lobby on the cameras... There's nobody. I mean, nobody alive.",
            "On the cameras I can see Studio 2 locked from the inside. Maybe Mara is hiding there. It's in the east wing.",
            "The blue card opens the archive, at the west end. The guard wrote that he locked something in there. Be careful.",
            "Mara says the Studio 1 control room is in the west wing, next to the service corridor.",
            "The technical room is at the east end, past Studio 2. The red door.",
            "A shotgun? The guards kept their service weapon in the archive. Use it.",
            "The lights are on! And... everyone in the corridors turned toward the light. Run to the stairs, Andrei!"
        };
        WIN = new String[] {
            "The stairwell door opens with a click. Mara goes up first, with her phone's flashlight.\n\nFrom the radio, Ioana's voice: \"On the cameras I can see the first floor. The newsroom is full. And something big is moving in the master control room.\"",
            "#END OF EPISODE 1\n\nThe power is back. The broadcast, not yet.\n\n#NEXT: THE NEWSROOM"
        };
        STATS_TITLE = "#STATISTICS";
        STAT_DIFF = "Difficulty: ";
        STAT_TIME = "Time: ";
        STAT_KILLS = "Infected killed: ";
        STAT_SHOTS = "Shots fired: ";
        STAT_NOTES = "Notes found: ";
    }
}
