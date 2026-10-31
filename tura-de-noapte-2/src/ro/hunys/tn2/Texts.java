package ro.hunys.tn2;

/**
 * Toate textele jocului, în română și în engleză. Liniile care încep cu '#' sunt titluri.
 * Tablourile cu două dimensiuni sunt pe episoade: [0] parterul, [1] etajul 2, [2] acoperișul.
 * Limba se schimbă cu set(); de aceea câmpurile nu sunt finale.
 */
public final class Texts {
    private Texts() {
    }

    public static boolean EN;
    public static final String AUTHOR = "Hunys with Claude AI";
    public static String TITLE, TITLE2, SUBTITLE, PLACE;
    public static String[] EP_NAMES, MAP_TITLES;
    public static String M_CONTINUE, M_NEW, M_SETTINGS, M_EXIT, M_BACK, M_RESUME, M_SAVE, M_MAIN, M_CONTROLS;
    public static String M_EPISODES, M_NEXT, LOCKED_EP;
    public static String S_DIFF, S_PIXEL, S_SOUND, S_VIBRA, S_LANG, S_SENS, S_FPS, S_BRIGHT;
    public static String[] DIFF_NAMES, PIXEL_NAMES, SENS_NAMES, BRIGHT_NAMES;
    public static String ON, OFF, LANG_NAME;
    public static String SAVED, SAVE_FAIL, LOADED, LOAD_FAIL;
    public static String TAP_CONTINUE, TAP_CLOSE, SCROLL_HINT;
    public static String GOT_AMMO, GOT_SHELLS, GOT_SMGAMMO, GOT_MED, GOT_SHOTGUN, GOT_SMG;
    public static String GOT_BLUE, GOT_RED, GOT_YELLOW, GOT_GREEN;
    public static String LOCK_BLUE, LOCK_YELLOW, LOCK_GREEN, USE_BLUE, USE_RED, USE_YELLOW, USE_GREEN;
    public static String[] LOCK_RED;
    public static String STAIRS_NO_POWER, NEED_ELENA, NO_WAY_BACK, ENTRANCE_LOCKED, ELEVATOR_DEAD, GARAGE_LOCKED;
    public static String VECTRA_SEALED, VECTRA_OPEN, LIFT_ON, LIFT_DONE, LIFT_ERROR, HELI_NO_PILOT, HELI_WAIT;
    public static String GEN_ON, GEN_DONE, NO_AMMO, RELOADING;
    public static String PAUSE_TITLE, DEAD_TITLE, DEAD_LOAD, DEAD_RETRY, DEAD_MENU;
    public static String OBJ_LABEL, NOTES_LABEL, RADIO_NAME;
    public static String[][] OBJ, STORY, WIN, NOTE_TITLES, NOTES, RADIO;
    public static String SCENE_IOANA, SCENE_ELENA, SCENE_CRISTI, SCENE_CREATURE;
    public static String STATS_TITLE, STAT_DIFF, STAT_TIME, STAT_KILLS, STAT_SHOTS, STAT_NOTES;
    public static String[] CONTROLS;
    public static String[] BTN;
    private static String[] sceneIds, sceneTexts, roomIds, roomNames;

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

    /** Numele camerei pe hartă (sau null). */
    public static String roomName(String id) {
        for (int i = 0; i < roomIds.length; i++) {
            if (roomIds[i].equals(id)) {
                return roomNames[i];
            }
        }
        return null;
    }

    /** Mesajul radio pe care îl declanșează prima intrare în cameră (sau -1). */
    public static int roomRadio(String id) {
        if (id.equals("urgente") || id.equals("pediatrie")) {
            return World.R_ROOM;
        }
        if (id.equals("acoperis")) {
            return World.R_OUT;
        }
        return -1;
    }

    /** Textul mesajului radio din episodul dat (sau null dacă episodul nu îl are). */
    public static String radio(int ep, int id) {
        if (ep < 0 || ep >= RADIO.length || id < 0 || id >= RADIO[ep].length) {
            return null;
        }
        return RADIO[ep][id];
    }

    static final String[] ROOM_IDS = {"rampa", "paza", "urgente", "resuscitare", "radiologie", "ct", "camera_comanda",
        "capela", "tehnic", "coridor", "vestiare", "wc", "hol", "internari", "farmacie", "cafenea", "cabinet",
        "scara", "garda", "oficiu", "ati", "bloc", "sala1", "spalator", "sala2", "depozit", "pediatrie", "cabinet2",
        "wc2", "asistente", "anticamera", "laborator", "izolare", "scara_serviciu", "masini", "rezervoare",
        "ventilatie", "electric", "acoperis", "dispecerat", "heliport"};

    // ================================================================== română
    private static void romanian() {
        TITLE = "TURA DE NOAPTE";
        TITLE2 = "2";
        SUBTITLE = "Spitalul Universitar";
        PLACE = "Spitalul Universitar · București";
        EP_NAMES = new String[] {"Episodul 1: Parterul", "Episodul 2: Etajele", "Episodul 3: Acoperișul"};
        MAP_TITLES = new String[] {"HARTA · PARTER", "HARTA · ETAJUL 2", "HARTA · ACOPERIȘ"};
        M_CONTINUE = "Continuă";
        M_NEW = "Joc nou";
        M_SETTINGS = "Setări";
        M_EXIT = "Ieșire";
        M_BACK = "Înapoi";
        M_RESUME = "Continuă jocul";
        M_SAVE = "Salvează jocul";
        M_MAIN = "Meniu principal";
        M_CONTROLS = "Comenzi";
        M_EPISODES = "Episoade";
        M_NEXT = "Episodul următor";
        LOCKED_EP = "Termină episodul dinainte";
        S_DIFF = "Dificultate";
        S_PIXEL = "Pixeli";
        S_SOUND = "Sunet";
        S_VIBRA = "Vibrații";
        S_LANG = "Limba";
        S_SENS = "Sensibilitate";
        S_FPS = "Contor FPS";
        S_BRIGHT = "Luminozitate";
        DIFF_NAMES = new String[] {"Ușor", "Normal", "Greu"};
        PIXEL_NAMES = new String[] {"Fini", "Medii", "Retro"};
        SENS_NAMES = new String[] {"Mică", "Medie", "Mare"};
        BRIGHT_NAMES = new String[] {"Foarte întunecat", "Întunecat", "Normal", "Luminos", "Foarte luminos"};
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
        GOT_BLUE = "Card ALBASTRU";
        GOT_RED = "Card ROȘU";
        GOT_YELLOW = "Card GALBEN";
        GOT_GREEN = "Card VERDE";
        LOCK_BLUE = "Încuiat. Trebuie cardul ALBASTRU.";
        LOCK_RED = new String[] {"Încuiat. Trebuie cardul ROȘU.", "Încuiat. Trebuie cardul ROȘU.",
            "Camera mașinilor. Cheia o are pilotul."};
        LOCK_YELLOW = "Încuiat. Trebuie cardul GALBEN.";
        LOCK_GREEN = "Încuiat. Trebuie cardul VERDE.";
        USE_BLUE = "Cardul ALBASTRU a deschis ușa.";
        USE_RED = "Ușa s-a deschis.";
        USE_YELLOW = "Cardul GALBEN a deschis ușa.";
        USE_GREEN = "Cardul VERDE a deschis ușa.";
        STAIRS_NO_POWER = "Ușa scării e blocată electromagnetic. Nu e curent.";
        NEED_ELENA = "Nu urci fără Elena. Ea știe drumul.";
        NO_WAY_BACK = "Scara de dedesubt e plină de ei. Nu te întorci.";
        ENTRANCE_LOCKED = "Intrarea e blocată din afară. Carantină.";
        ELEVATOR_DEAD = "Liftul nu merge.";
        GARAGE_LOCKED = "Ușa de garaj s-a blocat în urma voastră.";
        VECTRA_SEALED = "Ușă sigilată de Vectra. Nu se deschide.";
        VECTRA_OPEN = "Alarma tace. Ușa sigilată se deschide!";
        LIFT_ON = "Maneta cedează. Liftul de serviciu pornește!";
        LIFT_DONE = "Liftul urcă.";
        LIFT_ERROR = "Panoul clipește: EROARE FRÂNĂ.";
        HELI_NO_PILOT = "Elicopterul e încuiat. Unde e pilotul?";
        HELI_WAIT = "Cristi: „Nu plecăm fără ele!”";
        GEN_ON = "Generatorul pornește! Se aprind luminile.";
        GEN_DONE = "Generatorul merge.";
        NO_AMMO = "Fără muniție!";
        RELOADING = "REÎNCĂRCARE...";
        PAUSE_TITLE = "PAUZĂ";
        DEAD_TITLE = "AI MURIT";
        DEAD_LOAD = "Încarcă ultima salvare";
        DEAD_RETRY = "Încearcă din nou";
        DEAD_MENU = "Meniu principal";
        OBJ_LABEL = "Obiectiv";
        NOTES_LABEL = "Notițe";
        RADIO_NAME = "IOANA (stație)";
        OBJ = new String[][] {
            {"Găsește-o pe dr. Elena Radu", "Găsește cardul roșu pentru camera tehnică",
                "Pornește generatorul din camera tehnică", "Urcă pe scara de incendiu"},
            {"Caută cardul galben în ATI", "Ia cardul verde din blocul operator", "Intră în laboratorul Vectra",
                "Urcă pe scara de serviciu"},
            {"Găsește-l pe pilotul Cristi", "Repornește liftul din camera mașinilor", "Oprește creatura",
                "Urcă în elicopter"}
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
        STORY = new String[][] {
            {
                "#BUCUREȘTI, ORA 06:05\n\nAmbulanța intră în oraș odată cu primele lumini. În urmă, fabrica Vectra încă arde la orizont.\n\nIoana ține cutia cu probe pe genunchi. Azi-noapte, ceva zgâria capacul pe dinăuntru. Acum e liniște.",
                "La radio, aceeași știre, din jumătate în jumătate de oră:\n\n„Campania națională de vaccinare cu noul ser VN-9 începe astăzi, la ora 6:00, la Spitalul Universitar din București.”\n\nCamionul de la rampa fabricii apucase să plece.",
                "#ORA 06:12\n\nIoana o sună pe Elena Radu, prietena ei, medic la Urgențe la Universitar.\n\n„Elena, nu faceți vaccinul! Mă auzi? Opriți tot...”\n\nDincolo se sparge ceva. Apoi un țipăt. Apoi nimic.",
                "#SPITALUL UNIVERSITAR, ORA 06:24\n\nPoliția a închis bulevardul. Pe toate posturile, același anunț: armata închide ieșirile din oraș.\n\nPe acoperișul spitalului stă un elicopter SMURD, cu elicea nemișcată. E singurul drum afară.",
                "Andrei bagă ambulanța pe rampa de la Urgențe. Ușa de garaj coboară singură în urma lor și se blochează.\n\nIoana se închide în camera pazei, cu probele și cu monitoarele camerelor. Îi întinde o stație radio:\n\n„Găsește-o pe Elena. Eu te ghidez de aici.”\n\n#OBIECTIV: GĂSEȘTE-O PE DR. ELENA RADU"
            },
            {
                "#ETAJUL 2, ORA 07:10\n\nScara de incendiu urcă până la etajul 2. Mai sus, cineva a baricadat treptele cu paturi și dulapuri. Pe ele scrie cu spray: CARANTINĂ VECTRA.",
                "Ioana, în stație: „Elena a găsit planurile clădirii. Laboratorul Vectra de la etajul 2 are o scară de serviciu doar a lui. Duce direct pe acoperiș.”",
                "„Ușile laboratorului merg cu cardul verde. Pe camere l-am văzut pe coordonatorul Vectra intrând în blocul operator. N-a mai ieșit. Iar blocul se deschide cu cardul galben, de la ATI.”\n\n#OBIECTIV: CAUTĂ CARDUL GALBEN ÎN ATI"
            },
            {
                "#ETAJUL TEHNIC, ORA 07:52\n\nScara de serviciu se termină într-un hol de beton plin de țevi. De sus se aude vântul. Și, din puțul liftului, un zgâriat lung, ca de unghii pe metal.",
                "„Elicopterul e tot acolo”, zice Ioana în stație. „Pilotul, Cristi Moraru, s-a închis în dispeceratul heliportului. Nu răspunde la telefon, dar l-am văzut mișcându-se pe camere.”",
                "„Și Andrei... noi nu putem urca pe scară, e plină de ei. Liftul de serviciu e blocat. Dacă îl repornești din camera mașinilor, venim sus cu el.”\n\n#OBIECTIV: GĂSEȘTE-L PE PILOTUL CRISTI"
            }
        };
        WIN = new String[][] {
            {
                "Ușa scării de incendiu se deblochează cu un clic. Elena se uită înapoi, spre holul plin de scaune goale.\n\n„Eram patruzeci în tura de noapte. Ne-am vaccinat primii. Ca să dăm exemplu.”",
                "Elena coboară la Ioana, în camera pazei: „Cineva trebuie să vă țină ușa deschisă. Și să stea cu ochii pe cutia aia.”\n\nCutia cu probe zgârie iar. Mai tare. Ioana o leagă cu bandă adezivă.",
                "#SFÂRȘITUL EPISODULUI 1\n\nCurentul s-a întors la parter. Sus, la etajul 2, Vectra are un laborator despre care nu știa nimeni.\n\n#URMEAZĂ: ETAJELE"
            },
            {
                "Subiectul 9 cade și nu se mai ridică. La încheietură are o brățară de pacient: VN-9 / LOT 0 / VALEA NEAGRĂ.",
                "Scara de serviciu e îngustă și urcă în spirală. Din stație, vocea Ioanei, gâfâind:\n\n„Andrei... cutia. Capacul e spart. Era goală când m-am uitat. Ceva urcă prin puțul liftului.”",
                "#SFÂRȘITUL EPISODULUI 2\n\nLaboratorul Vectra e mort. Dar ce era în cutia cu probe a scăpat.\n\n#URMEAZĂ: ACOPERIȘUL"
            },
            {
                "Creatura se zbate o ultimă dată pe betonul ud și rămâne nemișcată. Ușile liftului se deschid: Ioana și Elena, palide, cu cutia goală între ele.",
                "Cristi pornește motoarele. Elicea taie aerul rece al dimineții. Spitalul se micșorează sub ei, apoi tot orașul, cu fumul lui și cu girofarele de pe bulevarde.",
                "Ioana ține telefonul în mână. Are semnal. Primul apel: celula de criză de la Otopeni. Al doilea: toate televiziunile care mai emit.\n\n„Avem dovezi. Și avem un martor.” Se uită la Elena. „Doi.”",
                "Elena se uită pe geam, spre oraș.\n\n„Loturile VN-9 au plecat azi-dimineață spre 14 spitale”, șoptește. „Universitarul a fost doar primul.”",
                "#SFÂRȘITUL EPISODULUI 3\n\nAndrei, Ioana, Elena și Cristi au scăpat din Spitalul Universitar. VN-9 încă nu.\n\n#SFÂRȘIT... DEOCAMDATĂ"
            }
        };
        SCENE_IOANA = "#IOANA\n\nIoana nu se uită la tine, ci la monitoare.\n„Elena e pe undeva pe aici. Știu. Ea nu se vaccinează niciodată prima.”\n\nCutia cu probe stă pe birou, legată cu bandă. Din ea se aude din nou zgâriatul. Mai repede decât azi-noapte.";
        SCENE_ELENA = "#DR. ELENA RADU\n\nElena iese de după pupitrul tomografului, cu un bisturiu în mână.\n„Ioana te-a trimis? Bine. Atunci știi.”\n\n„Vectra a oprit generatorul la 6 fix. Ușile de incendiu sunt electromagnetice: fără curent, nu iese nimeni. Lifturile sunt moarte.”\n\n„Cardul roșu de la camera tehnică îl avea farmacista-șefă. Ea a semnat pentru lot. Dacă mai e la farmacie... nu mai e ea.”";
        SCENE_CRISTI = "#PILOTUL CRISTI\n\nUșa se deschide cu o lanternă bătându-ți în ochi.\n„Ești viu? Viu de-a binelea?”\n\n„Elicopterul are plinul făcut. Doar că n-am plecat singur. Mai așteptam pe cineva. Oricine.”\n\n„Liftul de serviciu e blocat. Uite cheia de la camera mașinilor. Trage maneta galbenă și cabina urcă singură.”\n\nCristi îți dă o cheie cu breloc roșu.";
        SCENE_CREATURE = "#CEVA IESE DIN PUȚ\n\nCablurile scârțâie, cabina pornește. Apoi, de afară, de pe acoperiș, un urlet acoperă vântul. Ușile casei liftului se îndoaie și cedează.\n\nCe iese de acolo a încăput azi-noapte într-o cutie de probe.\n\nAcum e cât o mașină mică.";
        sceneIds = new String[] {"urgente", "resuscitare", "radiologie", "ct", "capela", "tehnic", "vestiare", "hol",
            "internari", "farmacie", "cafenea", "cabinet", "wc", "garda", "ati", "bloc", "sala1", "spalator", "sala2",
            "depozit", "pediatrie", "cabinet2", "asistente", "anticamera", "laborator", "izolare", "masini",
            "rezervoare", "ventilatie", "electric", "acoperis"};
        sceneTexts = new String[] {
            "#URGENȚELE\n\nTărgi răsturnate, perdele smulse, un monitor care piuie singur într-o boxă goală.\n\nPe tabla de la triaj, cu markerul: VACCINAȚI - BOXELE 1-4. Toate boxele sunt goale.",
            "#SALA DE RESUSCITARE\n\nDefibrilatorul e încă pornit. Pe masă, un halat cu ecusonul ELENA RADU, plin de sânge.\n\nNu e sângele ei. Speri.",
            "#RADIOLOGIA\n\nPereți plumbuiți, uși grele. În sala de așteptare, scaune răsturnate. Din camera de comandă se aude o voce de femeie numărând încet: „...șapte, opt, nouă...”",
            "#SALA CT\n\nTomograful încă bâzâie. Pe ecran, ultima imagine: un craniu și, înăuntru, ceva ce n-ar trebui să fie acolo.",
            "#CAPELA\n\nLumânări aprinse de curând. Pe o targă, un cearșaf.\n\nSub cearșaf, ceva respiră.",
            "#CAMERA TEHNICĂ\n\nGeneratorul e oprit. Maneta e sigilată cu bandă galbenă: VECTRA - NU ATINGEȚI.\n\nBanda se rupe ușor.",
            "#VESTIARELE\n\nDulapuri deschise, halate pe jos. Dulapul 13 are lacătul rupt. Înăuntru miroase a ulei de armă.",
            "#HOLUL CENTRAL\n\nSute de scaune aliniate pentru vaccinare. Pe ecrane rulează în buclă aceeași reclamă: un doctor zâmbitor cu o seringă.\n\n„VN-9. Siguranța ta, misiunea noastră.”",
            "#INTERNĂRI\n\nImprimanta încă scoate foi: aceeași fișă, iar și iar.\n\n„Reacție post-vaccinală. Reacție post-vaccinală. Reacție post-vaccinală.”",
            "#FARMACIA\n\nRafturile cu sedative sunt goale. Pe tejghea, un registru deschis: „ridicat de Vectra, 05:40”.\n\nÎn fund, farmacista-șefă stă cu spatele, nemișcată. Are un card roșu la gât.",
            "#CAFENEAUA\n\nCafele începute, covrigi neatinși. Pe o masă, zeci de fiole goale VN-9 și un afiș: „Vaccinații primesc cafeaua gratis!”",
            "#CABINETUL DE GARDĂ\n\nPatul e făcut. Pe birou, telefonul fix clipește: 1 mesaj nou.",
            "#TOALETA\n\nOglinda e spartă. Pe faianță, scris cu ruj:\n\nNU VĂ LĂSAȚI ÎNȚEPAȚI.",
            "#CAMERA DE GARDĂ\n\nUn pat nefăcut, o cană de cafea rece. Pe ușă, pe dinăuntru, urme de pumni. Cineva a încercat să iasă. Sau să intre.",
            "#ATI\n\nMonitoarele piuie liniștit, toate în același ritm. Paturile sunt ocupate.\n\nPacienții stau în capul oaselor și se întorc spre lanternă. Toți odată.",
            "#BLOCUL OPERATOR\n\nLumina rece a circuitului de urgență. Pe jos, urme de roți de targă și de picioare goale. Toate duc spre sala 2.",
            "#SALA DE OPERAȚIE 1\n\nInstrumentele sunt așezate frumos pe măsuță, numărate. Pe masa de operație nu e nimeni.\n\nCurelele sunt rupte.",
            "#SPĂLĂTORUL\n\nRobinetele curg de ore întregi. Apa e roz.",
            "#SALA DE OPERAȚIE 2\n\nSub lampa scialitică zace coordonatorul Vectra. Are cardul verde prins de halat.",
            "#DEPOZITUL\n\nCutii cu VN-9 stivuite până în tavan, gata de plecare. Pe etichete: Colentina, Floreasca, Colțea, Elias...",
            "#PEDIATRIA\n\nPaturi mici, făcute frumos. Desene pe pereți, jucării pe jos. Nicio urmă de sânge. Doar liniște.\n\nPe tablă, cu creta: „Plecăm la 5. Pa, pa, spital!”",
            "#CABINETUL MEDICAL\n\nTrusa de urgență e deschisă și bandajele lipsesc. Cineva și-a oblojit singur o mușcătură aici.",
            "#CAMERA ASISTENTELOR\n\nPe canapea, o pătură și o carte deschisă. Pe frigider, un magnet: „Cea mai bună asistentă din lume”.",
            "#ANTICAMERA LABORATORULUI\n\nDușul de decontaminare merge singur. Pe jos, un paznic Vectra, cu arma încă în mână. N-a apucat să tragă.",
            "#LABORATORUL VECTRA\n\nCușca din fund e îndoită în afară, ca o cutie de conserve. Pe toate ecranele, un singur rând:\n\nSUBIECTUL 9 - ÎN AFARA CELULEI.",
            "#CAMERA DE IZOLARE\n\nUn pat sudat de podea, curele groase cât palma. Toate rupte.",
            "#CAMERA MAȘINILOR\n\nMotoarele lifturilor stau. Panoul liftului 2 clipește roșu: EROARE FRÂNĂ.\n\nDin puț se aude ceva care urcă.",
            "#REZERVOARELE\n\nRezervoarele de apă ale spitalului. Pe unul, un sigiliu Vectra proaspăt și o etichetă: TRATAT.",
            "#CENTRALA DE VENTILAȚIE\n\nVentilatoarele urlă. Pe filtre, o pastă verde, lipicioasă, care pulsează încet.",
            "#CAMERA ELECTRICĂ\n\nTablourile bâzâie. De aici pleacă circuitul de urgență: ATI, blocul operator, laboratorul și liftul 2.",
            "#ACOPERIȘUL\n\nAerul rece al dimineții. Dincolo de balustradă, Bucureștiul se trezește în fum și sirene, iar soarele răsare roșu peste blocuri.\n\nPe heliport, elicopterul așteaptă, alb și roșu, cu elicea nemișcată."
        };
        NOTE_TITLES = new String[][] {
            {"REGISTRUL PAZEI", "FIȘA DE TRIAJ", "BILET PE DEFIBRILATOR", "RAPORT CT", "FOAIE PE MASA DIN CAPELĂ",
                "ORDIN DE LUCRU VECTRA", "BILET ÎN DULAPUL 13", "AFIȘ ÎN HOL", "LISTA INTERNĂRILOR",
                "MESAJ PE TELEFONUL DE GARDĂ"},
            {"PROGRAMUL DE GARDĂ", "FOAIA DE OBSERVAȚIE, PATUL 4", "PROTOCOL OPERATOR", "CARNETUL COORDONATORULUI",
                "AVIZ DE LIVRARE", "BILET PE UȘA PEDIATRIEI", "SCRISOARE DE LA ANA", "REȚETĂ NETERMINATĂ",
                "INSTRUCȚIUNI DE SIGURANȚĂ", "RAPORT VN-9, CONFIDENȚIAL", "ZGÂRIETURI PE PERETE"},
            {"BILET PE BALUSTRADĂ", "INSTRUCȚIUNI LIFT 2", "JURNALUL INSTALATORULUI", "SEMN PE CENTRALĂ",
                "TABLOUL ELECTRIC", "AFIȘ PE CASA LIFTULUI", "JURNALUL DE ZBOR"}
        };
        NOTES = new String[][] {
            {
                "05:30 - A sosit camionul Vectra cu lotul VN-9. Șase însoțitori, ecusoane albastre.\n05:45 - Directorul a ordonat: tura de noapte se vaccinează prima, „ca exemplu”.\n06:00 - Au început vaccinările.\n06:07 - ",
                "Pacient: Moraru V., 34 de ani. Vaccinat VN-9 la 06:01.\nTemperatura: 34,1 °C. Pupile fixe. Nu răspunde la nume.\nCod: ROȘU.\n\nDedesubt, alt scris: „s-a ridicat de pe targă. a mușcat-o pe Ana.”",
                "Nu-i resuscitați! Nu sunt morți. Sau sunt, dar nu contează.\n\nDacă inima li se oprește, se ridică după trei minute. Am numărat.\n- dr. E. Radu",
                "Creier: activitate anormală în trunchiul cerebral. Restul: fără semne vitale.\n\nObservație: pacientul s-a ridicat în timpul scanării.\nRecomandare: NU MAI SCANAȚI.",
                "Am adus trei corpuri aici la 6:20. La 6:30 erau doar două sub cearșafuri.\n\nAl treilea stă în colț și se uită la lumânări. Nu mă mai întorc.\n- Gelu, brancardier",
                "Echipa tehnică 2:\n- generatorul spitalului rămâne OPRIT după 06:00;\n- ușile de incendiu: blocate electromagnetic;\n- nu iese nimeni până la sosirea echipei de recuperare.\n\nCardul roșu: la farmacista-șefă, care „va colabora”.",
                "Mi-am lăsat pușca aici, să nu sperii lumea la vaccinare. Mă întorc după tură.\n- Sg. maj. Dinu, Jandarmeria\n\nNu s-a mai întors.",
                "CAMPANIA NAȚIONALĂ DE VACCINARE VN-9\nPrimii vaccinați: personalul spitalului.\nAzi, de la ora 6:00.\n\nSponsor: Vectra Bio. „Siguranța ta, misiunea noastră.”",
                "05:50 - 06:15: 46 de internări, toate cu același diagnostic: „reacție post-vaccinală”.\n\nUltima rubrică e completată tremurat: „toți. toți cei vaccinați.”",
                "De la: dr. E. Radu\n\nNu faceți vaccinul. Am văzut testele de stabilitate pe biroul omului de la Vectra. Lotul 9 n-a trecut NICIUN test.\nRepet: NU-L FACEȚI.\n\nTrimis: 05:58. Citit: de nimeni."
            },
            {
                "ATI, noaptea 14/15: dr. Popa, as. Neagu, as. Ilie.\n\nCineva a tăiat toate numele cu pixul și a scris dedesubt: „vaccinați - 06:05”.",
                "Pacient ventilat, nevaccinat.\n06:20 - colegii vaccinați intră în salon. Nu se uită la monitoare. Se uită la pacienți.\n06:22 - am încuiat cardul galben în postul asistentelor. Nu-l dau nimănui.\n- as. Neagu",
                "Intervenție: „recoltare probe”, Subiectul 7.\nEchipa: Vectra Bio.\nAnestezie: NU A FOST NEVOIE.\n\nPe margine, de mână: „a deschis ochii pe masă”.",
                "dr. Paul Iliescu, coordonator regional Vectra.\n\nSubiectul 9 trebuia sedat la fiecare 4 ore. Ultima doză: 03:00. Am întârziat.\n\nDacă citești asta, cardul verde e la mine. Nu deschide laboratorul. Te rog.",
                "Vectra Bio - lotul VN-9\nDestinatari: 14 spitale din București\nPlecare: 07:00\n\nSemnătura de primire lipsește. Cineva a desenat peste ea un craniu.",
                "Toți copiii au fost mutați la Spitalul de Copii la ora 5:00, cu ambulanțele. Părinții au fost anunțați.\n\nNu i-am lăsat să-i vaccineze.\n- as. șefă Marin",
                "Doamna asistentă,\nvă mulțumesc că ați avut grijă de pisica mea de pluș. Când o să fiu mare, o să mă fac și eu doctoriță.\n\nAna, 6 ani",
                "Pacient: Iliescu P.\nDiagnostic: mușcătură, antebrațul stâng.\nTratament: ...\n\nRețeta se termină aici, cu o dâră lungă de cerneală.",
                "Camera de izolare: Subiectul 9.\nGreutate: 140 kg (78 kg la internare).\nNu răspunde la sedative peste 300 mg.\n\nÎn caz de breșă: sigilați laboratorul. Ușa se deschide numai când pulsul subiectului ajunge la ZERO.",
                "Transformare: 100%, în 6 minute.\nTemperatura corpului: 34 °C.\nRăspuns la durere: absent.\n\nRecomandare: oprirea programului.\nDecizia conducerii: „lansare conform planului”.\n- V. Marcu",
                "Pe peretele celulei, zgâriate cu unghiile, aceleași două cuvinte, de zeci de ori:\n\nVALEA NEAGRĂ VALEA NEAGRĂ VALEA NEAGRĂ VALEA NEAGRĂ"
            },
            {
                "Dacă ajungeți aici: pilotul e în dispecerat, lângă heliport. Bateți de trei ori, rar. Altfel nu deschide.\n- un brancardier care a urcat primul",
                "La blocare:\n1. Verificați frâna.\n2. Ridicați maneta galbenă RESET.\n3. Cabina revine automat la parter și urcă la ultimul etaj apelat.\n\nATENȚIE: nu stați pe acoperișul cabinei!",
                "05:30 - cei de la Vectra au pus ceva în rezervorul de apă. Zic că e „clor”.\n05:40 - am oprit pompa. N-am încredere.\n06:10 - m-au văzut.",
                "ATENȚIE: filtrele laboratorului Vectra se evacuează AICI.\nNu deschideți fără mască.\n\nCineva a deschis.",
                "Circuit de urgență: ATI, bloc operator, laborator Vectra, LIFT 2.\nLiftul 2: BLOCAT (eroare frână).\nResetare: camera mașinilor, maneta galbenă.",
                "ZONĂ DE ATERIZARE - ACCES INTERZIS\nNu vă apropiați de elicopter cu elicea în mișcare.\n\nDedesubt, cu markerul: „ce a ieșit din lift avea opt picioare”.",
                "06:10 - Misiune anulată. Ordin: elicopterul rămâne la sol.\n06:40 - Armata închide spațiul aerian peste București.\n07:30 - Nu mai răspunde nimeni de la sol.\n\nPlinul e făcut. Aștept."
            }
        };
        RADIO = new String[][] {
            {
                "Andrei, mă auzi? Sunt în camera pazei. Văd tot parterul pe camere. Elena nu e la Urgențe... dar ușa Radiologiei e încuiată pe dinăuntru.",
                "Asistenta de la triaj avea cardul albastru la gât. Cu el se deschide Radiologia. Nu te uita la fața ei. Doar ia-l.",
                "Ai cardul albastru! Radiologia e pe coridor, la dreapta Urgențelor. Pereții de acolo sunt plumbuiți. Dacă Elena s-a ascuns undeva, acolo s-a ascuns.",
                "Cardul roșu! Camera tehnică e la capătul de est al coridorului. Generatorul e acolo.",
                null, null,
                "Pușca jandarmului? Bine. Ține-o aproape. Cu pistolul nu-i mai oprești pe cei mari.",
                null,
                "S-a aprins tot! Pe camere, becul de la ușa scării e verde. Luați-o pe scară și urcați! Eu rămân aici, cu Elena și cu probele.",
                "Elena e vie? Doamne... Spune-i că sunt aici. Și că eu i-am zis să nu facă vaccinul.",
                null, null,
                "Ce a fost asta?! A scuipat ceva verde, care topește linoleumul! Nu sta în fața lui!",
                null, null,
                "Buteliile de oxigen sar în aer dacă le nimerești. Ai grijă unde tragi. Sau nu. Folosește-le."
            },
            {
                "Te aud. Sunt cu Elena în camera pazei. Zice că ATI-ul, blocul operator și laboratorul au curent pe circuitul de urgență. Restul etajului e pe întuneric.",
                "Pediatria... Elena zice că pe copii i-au mutat la 5 dimineața, înainte de vaccinare. Măcar ei au scăpat.",
                null, null,
                "Cardul galben! Blocul operator e la mijlocul coridorului. Coordonatorul Vectra a intrat în sala 2 și n-a mai ieșit.",
                "Cardul verde! Laboratorul e pe culoarul din sud. Andrei... pe camerele de acolo nu se vede nimic. Doar o umbră mare care se plimbă încolo și încoace.",
                null,
                "Pistol-mitralieră? De la paznicii Vectra? Ține FOC apăsat. Și nu te opri.",
                null, null,
                "Ăla e... Elena zice că e Subiectul 9, primul vaccinat. L-au adus aseară de la Valea Neagră. Trage în el până nu se mai mișcă!",
                "S-a oprit alarma laboratorului. Ușa sigilată s-a deschis! Scara de serviciu e în spatele ei.",
                "Încă unul care scuipă! Mișcă-te în lateral!",
                null, null,
                "Atenție la butelii, sunt peste tot pe etajul ăsta. Un glonț și sar în aer."
            },
            {
                "Ești pe etajul tehnic. Camera mașinilor e la nord, cu ușă roșie. Ieșirea pe acoperiș e la capătul holului. Cristi e în dispecerat, în colțul de sud-est.",
                null, null, null, null, null, null, null, null,
                "L-ai găsit pe Cristi! Spune-i că avem dovezi. Și că venim cu liftul, dacă îl pornești.",
                null,
                "A murit? Spune-mi că a murit. ...Liftul a ajuns! Venim! Ține elicopterul pornit!",
                "Scuipătorii au urcat și pe acoperiș. Ferește-te!",
                "Liftul urcă... Andrei, aia de pe acoperiș e din cutia noastră! Nu-l lăsa să ajungă la elicopter!",
                "Te văd pe camera de pe heliport! Elicopterul e întreg. Dispeceratul e în colțul de sud-est.",
                "Buteliile de pe acoperiș sunt de rezervă, pentru elicopter. Explodează la fel de bine."
            }
        };
        STATS_TITLE = "#STATISTICI";
        STAT_DIFF = "Dificultate: ";
        STAT_TIME = "Timp: ";
        STAT_KILLS = "Infectați eliminați: ";
        STAT_SHOTS = "Focuri trase: ";
        STAT_NOTES = "Notițe găsite: ";
        roomIds = ROOM_IDS;
        roomNames = new String[] {"RAMPĂ", "PAZĂ", "URGENȚE", "RESUSCITARE", "RADIOLOGIE", "CT", "COMANDĂ", "CAPELĂ",
            "TEHNIC", null, "VESTIARE", "WC", "HOL", "INTERNĂRI", "FARMACIE", "CAFENEA", "CABINET", "SCARĂ", "GARDĂ",
            "OFICIU", "ATI", "BLOC OPERATOR", "SALA 1", "SPĂLĂTOR", "SALA 2", "DEPOZIT", "PEDIATRIE", "CABINET", "WC",
            "ASISTENTE", "ANTICAMERĂ", "LABORATOR", "IZOLARE", "SCARĂ", "MAȘINI LIFT", "REZERVOARE", "VENTILAȚIE",
            "ELECTRIC", "ACOPERIȘ", "DISPECERAT", "HELIPORT"};
    }

    // ================================================================== engleză
    private static void english() {
        TITLE = "NIGHT SHIFT";
        TITLE2 = "2";
        SUBTITLE = "University Hospital";
        PLACE = "University Hospital · Bucharest";
        EP_NAMES = new String[] {"Episode 1: Ground Floor", "Episode 2: The Wards", "Episode 3: The Roof"};
        MAP_TITLES = new String[] {"MAP · GROUND FLOOR", "MAP · 2ND FLOOR", "MAP · ROOF"};
        M_CONTINUE = "Continue";
        M_NEW = "New game";
        M_SETTINGS = "Settings";
        M_EXIT = "Exit";
        M_BACK = "Back";
        M_RESUME = "Resume";
        M_SAVE = "Save game";
        M_MAIN = "Main menu";
        M_CONTROLS = "Controls";
        M_EPISODES = "Episodes";
        M_NEXT = "Next episode";
        LOCKED_EP = "Finish the previous episode";
        S_DIFF = "Difficulty";
        S_PIXEL = "Pixels";
        S_SOUND = "Sound";
        S_VIBRA = "Vibration";
        S_LANG = "Language";
        S_SENS = "Sensitivity";
        S_FPS = "FPS counter";
        S_BRIGHT = "Brightness";
        DIFF_NAMES = new String[] {"Easy", "Normal", "Hard"};
        PIXEL_NAMES = new String[] {"Fine", "Medium", "Retro"};
        SENS_NAMES = new String[] {"Low", "Medium", "High"};
        BRIGHT_NAMES = new String[] {"Very dark", "Dark", "Normal", "Bright", "Very bright"};
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
        GOT_SHOTGUN = "SHOTGUN! The gun button switches weapons.";
        GOT_SMG = "SUBMACHINE GUN! Hold FIRE.";
        GOT_BLUE = "BLUE card";
        GOT_RED = "RED card";
        GOT_YELLOW = "YELLOW card";
        GOT_GREEN = "GREEN card";
        LOCK_BLUE = "Locked. You need the BLUE card.";
        LOCK_RED = new String[] {"Locked. You need the RED card.", "Locked. You need the RED card.",
            "Machine room. The pilot has the key."};
        LOCK_YELLOW = "Locked. You need the YELLOW card.";
        LOCK_GREEN = "Locked. You need the GREEN card.";
        USE_BLUE = "The BLUE card opened the door.";
        USE_RED = "The door opened.";
        USE_YELLOW = "The YELLOW card opened the door.";
        USE_GREEN = "The GREEN card opened the door.";
        STAIRS_NO_POWER = "The stairwell door has a magnetic lock. No power.";
        NEED_ELENA = "Not without Elena. She knows the way.";
        NO_WAY_BACK = "The stairs below are full of them. No going back.";
        ENTRANCE_LOCKED = "The entrance is locked from outside. Quarantine.";
        ELEVATOR_DEAD = "The elevator is dead.";
        GARAGE_LOCKED = "The garage door locked behind you.";
        VECTRA_SEALED = "Sealed by Vectra. It won't open.";
        VECTRA_OPEN = "The alarm stops. The sealed door opens!";
        LIFT_ON = "The lever gives. The service lift starts!";
        LIFT_DONE = "The lift is coming up.";
        LIFT_ERROR = "The panel blinks: BRAKE ERROR.";
        HELI_NO_PILOT = "The helicopter is locked. Where is the pilot?";
        HELI_WAIT = "Cristi: \"We're not leaving without them!\"";
        GEN_ON = "The generator starts! The lights come on.";
        GEN_DONE = "The generator is running.";
        NO_AMMO = "Out of ammo!";
        RELOADING = "RELOADING...";
        PAUSE_TITLE = "PAUSED";
        DEAD_TITLE = "YOU DIED";
        DEAD_LOAD = "Load last save";
        DEAD_RETRY = "Try again";
        DEAD_MENU = "Main menu";
        OBJ_LABEL = "Objective";
        NOTES_LABEL = "Notes";
        RADIO_NAME = "IOANA (radio)";
        OBJ = new String[][] {
            {"Find Dr. Elena Radu", "Find the red card for the technical room", "Start the generator in the technical room",
                "Take the fire stairs"},
            {"Look for the yellow card in the ICU", "Take the green card from the operating theatre",
                "Get into the Vectra lab", "Take the service stairs"},
            {"Find Cristi, the pilot", "Restart the lift from the machine room", "Stop the creature", "Get in the helicopter"}
        };
        BTN = new String[] {"FIRE", "USE", "GUN", "RELOAD", "MAP", "PAUSE"};
        CONTROLS = new String[] {
            "Bottom left: joystick - walk and strafe",
            "Drag on the right side of the screen - turn",
            "FIRE - shoot (hold it with the submachine gun)",
            "You can also turn with your finger on FIRE",
            "Hand - open doors, talk, use",
            "Gun - switch weapon; arrows - reload",
            "Top right: map and pause (save)"
        };
        STORY = new String[][] {
            {
                "#BUCHAREST, 06:05\n\nThe ambulance enters the city with the first light. Behind them, the Vectra factory is still burning on the horizon.\n\nIoana holds the sample box on her knees. Last night something was scratching the lid from the inside. Now it's quiet.",
                "On the radio, the same news every half hour:\n\n\"The national vaccination campaign with the new VN-9 serum starts today at 6:00, at the University Hospital in Bucharest.\"\n\nThe truck from the factory dock had already left.",
                "#06:12\n\nIoana calls Elena Radu, her friend, an ER doctor at the University Hospital.\n\n\"Elena, don't give the vaccine! Can you hear me? Stop everything...\"\n\nSomething breaks on the other end. Then a scream. Then nothing.",
                "#UNIVERSITY HOSPITAL, 06:24\n\nThe police have closed the boulevard. On every channel, the same announcement: the army is sealing the city.\n\nOn the hospital roof stands an air ambulance helicopter, rotor still. It's the only way out.",
                "Andrei drives the ambulance into the ER bay. The garage door comes down by itself behind them and locks.\n\nIoana locks herself in the security room, with the samples and the camera monitors. She hands him a radio:\n\n\"Find Elena. I'll guide you from here.\"\n\n#OBJECTIVE: FIND DR. ELENA RADU"
            },
            {
                "#2ND FLOOR, 07:10\n\nThe fire stairs go up to the 2nd floor. Above that, someone barricaded the steps with beds and cabinets. Spray-painted on them: VECTRA QUARANTINE.",
                "Ioana, on the radio: \"Elena found the building plans. The Vectra lab on the 2nd floor has its own service stairs. They go straight to the roof.\"",
                "\"The lab doors take the green card. On the cameras I saw the Vectra coordinator walk into the operating theatre. He never came out. And the theatre opens with the yellow card, from the ICU.\"\n\n#OBJECTIVE: LOOK FOR THE YELLOW CARD IN THE ICU"
            },
            {
                "#SERVICE FLOOR, 07:52\n\nThe service stairs end in a concrete hallway full of pipes. You can hear the wind above. And, from the lift shaft, a long scratching, like nails on metal.",
                "\"The helicopter is still there,\" Ioana says on the radio. \"The pilot, Cristi Moraru, locked himself in the helipad dispatch room. He doesn't answer the phone, but I saw him move on the cameras.\"",
                "\"And Andrei... we can't take the stairs, they're full of them. The service lift is stuck. If you restart it from the machine room, we'll come up with it.\"\n\n#OBJECTIVE: FIND CRISTI, THE PILOT"
            }
        };
        WIN = new String[][] {
            {
                "The fire door unlocks with a click. Elena looks back at the hall full of empty chairs.\n\n\"There were forty of us on the night shift. We got the shot first. To set an example.\"",
                "Elena goes down to Ioana, in the security room: \"Someone has to keep the door open for you. And keep an eye on that box.\"\n\nThe sample box scratches again. Harder. Ioana tapes it shut.",
                "#END OF EPISODE 1\n\nThe power is back on the ground floor. Upstairs, on the 2nd floor, Vectra has a lab nobody knew about.\n\n#NEXT: THE WARDS"
            },
            {
                "Subject 9 falls and doesn't get up. On his wrist, a patient bracelet: VN-9 / BATCH 0 / VALEA NEAGRĂ.",
                "The service stairs are narrow and spiral upward. On the radio, Ioana's voice, out of breath:\n\n\"Andrei... the box. The lid is broken. It was empty when I looked. Something is climbing up the lift shaft.\"",
                "#END OF EPISODE 2\n\nThe Vectra lab is dead. But whatever was in the sample box got out.\n\n#NEXT: THE ROOF"
            },
            {
                "The creature twitches one last time on the wet concrete and goes still. The lift doors open: Ioana and Elena, pale, the empty box between them.",
                "Cristi starts the engines. The rotor cuts the cold morning air. The hospital shrinks beneath them, then the whole city, with its smoke and the flashing lights on the boulevards.",
                "Ioana holds her phone. She has signal. First call: the crisis unit at Otopeni. Second: every TV station still on the air.\n\n\"We have proof. And we have a witness.\" She looks at Elena. \"Two.\"",
                "Elena looks out the window at the city.\n\n\"The VN-9 batches left this morning for 14 hospitals,\" she whispers. \"The University Hospital was only the first.\"",
                "#END OF EPISODE 3\n\nAndrei, Ioana, Elena and Cristi got out of the University Hospital. VN-9 hasn't. Not yet.\n\n#THE END... FOR NOW"
            }
        };
        SCENE_IOANA = "#IOANA\n\nIoana isn't looking at you, but at the monitors.\n\"Elena is somewhere around here. I know it. She never goes first for a shot.\"\n\nThe sample box sits on the desk, taped shut. The scratching is back. Faster than last night.";
        SCENE_ELENA = "#DR. ELENA RADU\n\nElena comes out from behind the CT console, a scalpel in her hand.\n\"Ioana sent you? Good. Then you know.\"\n\n\"Vectra shut down the generator at six sharp. The fire doors have magnetic locks: no power, nobody gets out. The lifts are dead.\"\n\n\"The head pharmacist had the red card for the technical room. She signed for the batch. If she's still in the pharmacy... she isn't herself anymore.\"";
        SCENE_CRISTI = "#CRISTI, THE PILOT\n\nThe door opens with a flashlight shining in your eyes.\n\"You're alive? Really alive?\"\n\n\"The helicopter is fuelled. I just didn't want to leave alone. I was waiting for someone. Anyone.\"\n\n\"The service lift is stuck. Here's the key to the machine room. Pull the yellow lever and the car comes up by itself.\"\n\nCristi hands you a key on a red keychain.";
        SCENE_CREATURE = "#SOMETHING COMES OUT OF THE SHAFT\n\nThe cables creak and the car starts moving. Then, from outside, on the roof, a howl drowns out the wind. The lift house doors bend and give way.\n\nWhat comes out fit in a sample box last night.\n\nNow it's the size of a small car.";
        sceneIds = new String[] {"urgente", "resuscitare", "radiologie", "ct", "capela", "tehnic", "vestiare", "hol",
            "internari", "farmacie", "cafenea", "cabinet", "wc", "garda", "ati", "bloc", "sala1", "spalator", "sala2",
            "depozit", "pediatrie", "cabinet2", "asistente", "anticamera", "laborator", "izolare", "masini",
            "rezervoare", "ventilatie", "electric", "acoperis"};
        sceneTexts = new String[] {
            "#EMERGENCY ROOM\n\nOverturned gurneys, torn curtains, a monitor beeping alone in an empty bay.\n\nOn the triage board, in marker: VACCINATED - BAYS 1-4. Every bay is empty.",
            "#RESUSCITATION ROOM\n\nThe defibrillator is still on. On the table, a coat with the badge ELENA RADU, covered in blood.\n\nNot her blood. You hope.",
            "#RADIOLOGY\n\nLead-lined walls, heavy doors. Chairs knocked over in the waiting area. From the control room, a woman's voice counting slowly: \"...seven, eight, nine...\"",
            "#CT ROOM\n\nThe scanner is still humming. On the screen, the last image: a skull and, inside it, something that shouldn't be there.",
            "#CHAPEL\n\nCandles, lit not long ago. On a gurney, a sheet.\n\nUnder the sheet, something is breathing.",
            "#TECHNICAL ROOM\n\nThe generator is off. The lever is sealed with yellow tape: VECTRA - DO NOT TOUCH.\n\nThe tape tears easily.",
            "#LOCKER ROOM\n\nOpen lockers, coats on the floor. Locker 13 has a broken padlock. It smells of gun oil inside.",
            "#MAIN HALL\n\nHundreds of chairs lined up for the vaccination. The screens loop the same ad: a smiling doctor with a syringe.\n\n\"VN-9. Your safety, our mission.\"",
            "#ADMISSIONS\n\nThe printer is still spitting out pages: the same form, over and over.\n\n\"Post-vaccination reaction. Post-vaccination reaction. Post-vaccination reaction.\"",
            "#PHARMACY\n\nThe sedative shelves are empty. On the counter, an open register: \"collected by Vectra, 05:40\".\n\nIn the back, the head pharmacist stands with her back turned, motionless. A red card hangs from her neck.",
            "#CAFE\n\nHalf-finished coffees, untouched pretzels. On a table, dozens of empty VN-9 vials and a poster: \"Free coffee for the vaccinated!\"",
            "#ON-CALL ROOM\n\nThe bed is made. On the desk, the landline blinks: 1 new message.",
            "#RESTROOM\n\nThe mirror is broken. On the tiles, in lipstick:\n\nDON'T LET THEM JAB YOU.",
            "#ON-CALL ROOM\n\nAn unmade bed, a cold cup of coffee. On the inside of the door, fist marks. Someone tried to get out. Or in.",
            "#ICU\n\nThe monitors beep calmly, all in the same rhythm. The beds are taken.\n\nThe patients sit up and turn toward your flashlight. All at once.",
            "#OPERATING THEATRE\n\nThe cold light of the emergency circuit. On the floor, gurney wheel tracks and bare footprints. They all lead to room 2.",
            "#OPERATING ROOM 1\n\nThe instruments are neatly laid out on the tray, counted. Nobody is on the operating table.\n\nThe straps are torn.",
            "#SCRUB ROOM\n\nThe taps have been running for hours. The water is pink.",
            "#OPERATING ROOM 2\n\nUnder the surgical lamp lies the Vectra coordinator. The green card is clipped to his coat.",
            "#STOREROOM\n\nBoxes of VN-9 stacked to the ceiling, ready to go. On the labels: Colentina, Floreasca, Colțea, Elias...",
            "#PAEDIATRICS\n\nSmall beds, neatly made. Drawings on the walls, toys on the floor. Not a drop of blood. Just silence.\n\nOn the board, in chalk: \"Leaving at 5. Bye-bye, hospital!\"",
            "#EXAMINATION ROOM\n\nThe emergency kit is open and the bandages are gone. Someone patched up a bite here by himself.",
            "#NURSES' ROOM\n\nOn the couch, a blanket and an open book. On the fridge, a magnet: \"World's best nurse\".",
            "#LAB ANTEROOM\n\nThe decontamination shower runs by itself. On the floor, a Vectra guard, gun still in hand. He never got a shot off.",
            "#VECTRA LAB\n\nThe cage in the back is bent outward, like a tin can. Every screen shows a single line:\n\nSUBJECT 9 - OUTSIDE CELL.",
            "#ISOLATION ROOM\n\nA bed welded to the floor, straps as thick as a hand. All torn.",
            "#MACHINE ROOM\n\nThe lift motors are still. The panel for lift 2 blinks red: BRAKE ERROR.\n\nFrom the shaft you hear something climbing.",
            "#WATER TANKS\n\nThe hospital's water tanks. On one, a fresh Vectra seal and a label: TREATED.",
            "#VENTILATION PLANT\n\nThe fans roar. On the filters, a sticky green paste, slowly pulsing.",
            "#ELECTRICAL ROOM\n\nThe panels hum. The emergency circuit starts here: ICU, operating theatre, the lab and lift 2.",
            "#THE ROOF\n\nCold morning air. Beyond the railing, Bucharest wakes up in smoke and sirens, and the sun rises red over the apartment blocks.\n\nOn the helipad, the helicopter waits, white and red, rotor still."
        };
        NOTE_TITLES = new String[][] {
            {"SECURITY LOG", "TRIAGE SHEET", "NOTE ON THE DEFIBRILLATOR", "CT REPORT", "PAGE ON THE CHAPEL TABLE",
                "VECTRA WORK ORDER", "NOTE IN LOCKER 13", "POSTER IN THE HALL", "ADMISSIONS LIST",
                "MESSAGE ON THE ON-CALL PHONE"},
            {"ON-CALL ROTA", "OBSERVATION SHEET, BED 4", "SURGICAL PROTOCOL", "THE COORDINATOR'S NOTEBOOK",
                "DELIVERY NOTE", "NOTE ON THE PAEDIATRICS DOOR", "LETTER FROM ANA", "UNFINISHED PRESCRIPTION",
                "SAFETY INSTRUCTIONS", "VN-9 REPORT, CONFIDENTIAL", "SCRATCHES ON THE WALL"},
            {"NOTE ON THE RAILING", "LIFT 2 INSTRUCTIONS", "PLUMBER'S LOG", "SIGN ON THE PLANT", "ELECTRICAL PANEL",
                "SIGN ON THE LIFT HOUSE", "FLIGHT LOG"}
        };
        NOTES = new String[][] {
            {
                "05:30 - The Vectra truck arrived with batch VN-9. Six escorts, blue badges.\n05:45 - The director's orders: the night shift gets vaccinated first, \"as an example\".\n06:00 - Vaccinations started.\n06:07 - ",
                "Patient: Moraru V., 34. VN-9 vaccine at 06:01.\nTemperature: 34.1 °C. Fixed pupils. Doesn't respond to his name.\nCode: RED.\n\nBelow, in other handwriting: \"got up from the gurney. bit Ana.\"",
                "Don't resuscitate them! They're not dead. Or they are, but it doesn't matter.\n\nWhen the heart stops, they get up three minutes later. I counted.\n- Dr. E. Radu",
                "Brain: abnormal activity in the brainstem. Otherwise: no vital signs.\n\nNote: the patient sat up during the scan.\nRecommendation: STOP SCANNING.",
                "I brought three bodies here at 6:20. At 6:30 there were only two under the sheets.\n\nThe third one sits in the corner, staring at the candles. I'm not coming back.\n- Gelu, orderly",
                "Technical team 2:\n- the hospital generator stays OFF after 06:00;\n- fire doors: magnetically locked;\n- nobody leaves until the recovery team arrives.\n\nRed card: with the head pharmacist, who \"will cooperate\".",
                "Left my shotgun here so I don't scare people at the vaccination. Back after my shift.\n- Sgt. Maj. Dinu, Gendarmerie\n\nHe never came back.",
                "NATIONAL VN-9 VACCINATION CAMPAIGN\nFirst in line: the hospital staff.\nToday, from 6:00.\n\nSponsored by Vectra Bio. \"Your safety, our mission.\"",
                "05:50 - 06:15: 46 admissions, all with the same diagnosis: \"post-vaccination reaction\".\n\nThe last entry is written with a shaking hand: \"all of them. everyone who got the shot.\"",
                "From: Dr. E. Radu\n\nDon't give the vaccine. I saw the stability tests on the Vectra man's desk. Batch 9 failed EVERY test.\nAgain: DON'T GIVE IT.\n\nSent: 05:58. Read: by nobody."
            },
            {
                "ICU, night of the 14th: Dr. Popa, Nurse Neagu, Nurse Ilie.\n\nSomeone crossed out every name in pen and wrote underneath: \"vaccinated - 06:05\".",
                "Ventilated patient, not vaccinated.\n06:20 - the vaccinated staff walk into the ward. They don't look at the monitors. They look at the patients.\n06:22 - I locked the yellow card in the nurses' station. I'm not giving it to anyone.\n- Nurse Neagu",
                "Procedure: \"sample collection\", Subject 7.\nTeam: Vectra Bio.\nAnaesthesia: NOT NEEDED.\n\nIn the margin, by hand: \"opened his eyes on the table\".",
                "Dr. Paul Iliescu, Vectra regional coordinator.\n\nSubject 9 had to be sedated every 4 hours. Last dose: 03:00. I was late.\n\nIf you're reading this, I have the green card. Don't open the lab. Please.",
                "Vectra Bio - batch VN-9\nRecipients: 14 hospitals in Bucharest\nDeparture: 07:00\n\nThe receiving signature is missing. Someone drew a skull over it.",
                "All the children were moved to the Children's Hospital at 5:00, by ambulance. The parents have been told.\n\nWe didn't let them vaccinate the kids.\n- Head Nurse Marin",
                "Dear nurse,\nthank you for taking care of my plush cat. When I grow up, I'm going to be a doctor too.\n\nAna, age 6",
                "Patient: Iliescu P.\nDiagnosis: bite, left forearm.\nTreatment: ...\n\nThe prescription ends here, in a long streak of ink.",
                "Isolation room: Subject 9.\nWeight: 140 kg (78 kg on admission).\nNo response to sedatives above 300 mg.\n\nIn case of breach: seal the lab. The door only opens when the subject's pulse reaches ZERO.",
                "Transformation: 100%, within 6 minutes.\nBody temperature: 34 °C.\nResponse to pain: none.\n\nRecommendation: stop the programme.\nManagement decision: \"launch as planned\".\n- V. Marcu",
                "On the cell wall, scratched with fingernails, the same two words, dozens of times:\n\nVALEA NEAGRĂ VALEA NEAGRĂ VALEA NEAGRĂ VALEA NEAGRĂ"
            },
            {
                "If you made it here: the pilot is in the dispatch room by the helipad. Knock three times, slowly. Otherwise he won't open.\n- an orderly who came up first",
                "If stuck:\n1. Check the brake.\n2. Raise the yellow RESET lever.\n3. The car returns to the ground floor and comes up to the last floor called.\n\nWARNING: do not stand on top of the car!",
                "05:30 - the Vectra people put something in the water tank. They said it's \"chlorine\".\n05:40 - I shut off the pump. I don't trust them.\n06:10 - they saw me.",
                "WARNING: the Vectra lab filters exhaust HERE.\nDo not open without a mask.\n\nSomeone opened it.",
                "Emergency circuit: ICU, operating theatre, Vectra lab, LIFT 2.\nLift 2: STUCK (brake error).\nReset: machine room, yellow lever.",
                "LANDING AREA - NO ENTRY\nDo not approach the helicopter while the rotor is turning.\n\nBelow, in marker: \"what came out of the lift had eight legs\".",
                "06:10 - Mission cancelled. Orders: the helicopter stays on the ground.\n06:40 - The army closes the airspace over Bucharest.\n07:30 - Nobody answers from the ground anymore.\n\nThe tank is full. I'm waiting."
            }
        };
        RADIO = new String[][] {
            {
                "Andrei, can you hear me? I'm in the security room. I can see the whole ground floor on the cameras. Elena isn't in the ER... but the Radiology door is locked from the inside.",
                "The triage nurse had the blue card around her neck. It opens Radiology. Don't look at her face. Just take it.",
                "You have the blue card! Radiology is down the corridor, right of the ER. The walls there are lead-lined. If Elena is hiding anywhere, it's there.",
                "The red card! The technical room is at the east end of the corridor. The generator is in there.",
                null, null,
                "The gendarme's shotgun? Good. Keep it close. The pistol won't stop the big ones anymore.",
                null,
                "Everything's lit up! On the cameras, the light on the stairwell door is green. Take the stairs and go up! I'm staying here, with Elena and the samples.",
                "Elena's alive? Oh God... Tell her I'm here. And that I told her not to take the shot.",
                null, null,
                "What was that?! It spat something green that melts the floor! Don't stand in front of it!",
                null, null,
                "Oxygen cylinders blow up if you hit them. Watch where you shoot. Or don't. Use them."
            },
            {
                "I hear you. I'm with Elena in the security room. She says the ICU, the operating theatre and the lab run on the emergency circuit. The rest of the floor is dark.",
                "Paediatrics... Elena says they moved the kids out at 5 in the morning, before the vaccination. At least they got away.",
                null, null,
                "The yellow card! The operating theatre is halfway down the corridor. The Vectra coordinator went into room 2 and never came out.",
                "The green card! The lab is off the south passage. Andrei... the cameras there show nothing. Just a big shadow pacing back and forth.",
                null,
                "A submachine gun? From the Vectra guards? Hold FIRE. And don't stop.",
                null, null,
                "That's... Elena says it's Subject 9, the first one vaccinated. They brought him from Valea Neagră last night. Shoot until it stops moving!",
                "The lab alarm stopped. The sealed door is open! The service stairs are behind it.",
                "Another spitter! Move sideways!",
                null, null,
                "Careful with the cylinders, they're all over this floor. One bullet and they go up."
            },
            {
                "You're on the service floor. The machine room is to the north, red door. The roof exit is at the end of the hallway. Cristi is in the dispatch room, in the south-east corner.",
                null, null, null, null, null, null, null, null,
                "You found Cristi! Tell him we have proof. And that we're coming up in the lift, if you start it.",
                null,
                "Is it dead? Tell me it's dead. ...The lift is here! We're coming! Keep the helicopter running!",
                "The spitters made it to the roof too. Watch out!",
                "The lift is coming up... Andrei, that thing on the roof came out of our box! Don't let it reach the helicopter!",
                "I can see you on the helipad camera! The helicopter is in one piece. The dispatch room is in the south-east corner.",
                "The cylinders on the roof are spares for the helicopter. They blow up just as well."
            }
        };
        STATS_TITLE = "#STATISTICS";
        STAT_DIFF = "Difficulty: ";
        STAT_TIME = "Time: ";
        STAT_KILLS = "Infected killed: ";
        STAT_SHOTS = "Shots fired: ";
        STAT_NOTES = "Notes found: ";
        roomIds = ROOM_IDS;
        roomNames = new String[] {"AMBULANCE BAY", "SECURITY", "ER", "RESUS", "RADIOLOGY", "CT", "CONTROL", "CHAPEL",
            "TECHNICAL", null, "LOCKERS", "WC", "HALL", "ADMISSIONS", "PHARMACY", "CAFE", "ON-CALL", "STAIRS", "ON-CALL",
            "PANTRY", "ICU", "THEATRE", "ROOM 1", "SCRUB", "ROOM 2", "STOREROOM", "PAEDIATRICS", "EXAM ROOM", "WC",
            "NURSES", "ANTEROOM", "LAB", "ISOLATION", "STAIRS", "LIFT MOTORS", "WATER TANKS", "VENTILATION",
            "ELECTRICAL", "ROOF", "DISPATCH", "HELIPAD"};
    }
}
