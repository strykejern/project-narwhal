# Prosjektplan for spillprosjekt 2009 i TDT4100 #

## Gruppens medlemmer: ##

  * Anders Eie, Bachelor Informatikk 1.år
  * Johan Jansen, Bachelor Informatikk 1.år

Vi har snakket Hallvard om at vi bare var to personer. Han mente det var helt greit så lenge vi viste at vi virkelig var seriøs og i stand til å gjennomføre spillprosjektet med to personer.

## Deres motivasjon for å ta spillprosjektet ##

### Johan Jansen ###
Jeg mener at jeg lærer mye mer når jeg utvikler litt fritt i java. Men litt eksperimentering og med frie tøyler så kan jeg prøve ut konsepter i Java jeg har tenkt nøye om før. Jeg har allerede lært mye mer om hvordan bilder og grafikk fungerer i java, hva hash tables egentlig er og hvordan man dekoderer og spiller av lyder. Videre synes jeg at det er gøy å programmere, spesielt å utvikle spill. Jeg har hatt noe tidligere erfaringer i spillutvikling som kommer til å hjelpe mye på dette prosjektet. Ikke minst kommer dette til å hjelpe i hvordan vi kan koordinere fremtidlige programmeringsprosjekt ved hjelp av SVN, dokumentasjon og fordeling av arbeid samt planlegging.

### Anders Eie ###
Det er gøy å lage spill, og dette gir god erfaring i gruppearbeid innen programmering.

## Beskrivelse av spillet ##
Vi har ikke bestemt oss for et definitivt navn på spillet, derfor bruker vi kodenavnet Project Narwhal for øyeblikket. Vi ønsker å utvikle et 2D romskipspill der man skyter på andre romskip. Vi ønsker å ha det i en top-view format og ikke som en side-scroller. Videre så har vi planlagt at man kan kræsje med andre romskip, meteorer og planeter i en realistisk verdensrom fysikk. (Noe som betyr at vi implementere en fysikk motor). Vi har også planer om svarte hull, gravitasjonsfelt rundt alle objekter i verdensrommet og såkalte ormehull hvis tiden strekker til. Vi kommer til å ha lydeffekter i verdensrommet. I første utgangspunkt spiller man mot datamaskinen, men hvis det er tid for det, vil vi også prøve oss på nettverk. Styringen av romskipet skjer ved mus og keyboard, hvor keyboard brukes til å gi gass, bremse eller skyte og musa styrer retningen til romskipet. Vi skal prøve å utvikle våpen, skjold, osv. så modulært så mulig slik at man kanskje kan oppgradere visse elementer i romskipet. (oppgradere våpensystemer, radar, hastighet eller lignende)

Screenshots av spillet finnes i wikien til prosjektsiden på google code

## Utviklingen av spillet ##

Bruker SVN og Skype for å koordinere kodeutviklingen.

Først utvikle spillmotoren. Vi kommer til å trenge flere komponenter i spillmotoren:

  * Grafikk - Rotasjon, forstørrelse, delbilder, alpha, partikkel motor (effektiv generering og rendering av mange images samtidig på skjermen).
  * Ressurs Mananger - Lasting av data slik at alt fungerer "cross-platform" og som JAR fil.
  * Fysikk - Kollidering, flytting av spillobjekter med realistisk fysikk, osv...
  * Lyd - Dekoding og avspilling av lydformater som OGG, WAV og AU. Både for musikk og lydeffekter. Mulighet til å slå av lyd, justere volum, loops, osv.
  * Input - Enkel klasse for lesing av tilstanden til knapper som brukes til å styre spillet, både fra mus og keyboard.
  * Spillobjekt - Et spill objekt er et romskip, planet, rakett osv. Altså et hvilketsomhelst objekt som man kan interaktere med i spillet.
  * Når vi har bygget opp grunnsteinene for prosjektet så kan vi utvikle selve elementene i spillet selv.

  * Game - Hoved loop som holder orden på spillets gang.
  * Romskip - Arver fra Spillobjekt, leser Input, bruker grafikk og lyd
  * Planeter - Arver fra Spillobjekt
  * Våpen - Våpen enheter som romskip kan bruke. Bruker grafikk og lyd
  * Bakgrunn - Genererer stjerner og for bakgrunnsbilder. Bruker grafikk
  * UI - Tegner opp en in-game UI som viser hvor mye liv romskipet til spilleren har.
  * Menu - Spill-meny, forandre instillinger, starte nytt spill, avslutte, osv.
Alle nåværende klasser finnes i SVN

## Eventuelt ##

Vi bruker OGG vorbis API dekoder biblioteket som lar oss spille av andre lydtyper enn ukomprimerte råformat som WAV og AU (som java bare støtter til vanlig). Ellers ønsker vi å utvikle meste av spillmotoren selv. (Grafikk, fysikk og lyd, osv.) Vi kan muligvis ha et eget filformat til et romskip sine statistikker, men det går vi inn på nærmere når vi har kommet lengre ut i utviklingen.

## Linker ##

Prosjektside - http://code.google.com/p/project-narwhal/

Screenshots - http://code.google.com/p/project-narwhal/wiki/Screenshots