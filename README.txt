Juraj Holas - Delaunayova triangulacia + Voronoiov diagram

Program bol vypracovany v jazyku Java (IDE Eclipse). V hlavnom priecinku je
holasdgs.jar subor obsahujuci aplikaciu, v bin/delaunayVoronoi/ su vsetky
skompilovane triedy, a v src/ su kompletne zdrojove subory. Hlavna trieda
aplikacie je ProgramApplet.class .

OVLADANIE:
Vsetky ovladacie prvky su vytvorene na zaklade pozadaviek projektu:
 -> v lavom stlpci si uzivatel vyberie ci chce zobrazit Delaunayovu
    triangulaciu, Voronoiov diagram, ci opisane kruznice trojuholnikov
 -> lave kliknutie prida vrchol do Delaunayovej triangulacie na pozadovanu
    poziciu
 -> prave kliknutie zvyrazni zvoleny bod, bunku Voronoiovho diagramu do ktorej
    patri, a urcujuci vrchol z Delaunayovej triangulacie. Bod pretrvava zvoleny
    aj po pridani dalsich vrcholov triangulacie, pricom jeho bunka sa
    automaticky aktualizuje. Pokial chcete zvoleny bod zrusit, kliknite na
    tlacitko 'Clear point in cell'.
 -> tlacitkom 'Add 10 random sites' sa do Delaunayovej triangulacie prida 10
    bodov na nahodnych poziciach
 -> tlacitkom 'Clear all' sa vymazu vsetky doteraz pridane body triangulacie,
    ako aj vybrany bod na zvyraznenie