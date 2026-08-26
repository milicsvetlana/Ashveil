# Enemy AI and Navigation System

## Overview

Enemy sistem u Ashveil-u podržava tri različita tipa neprijatelja:

- **Shade** – ground melee enemy koji prilazi igraču i po potrebi razbija ograde.
- **Wisp** – flying enemy koji direktno prati igrača i napada dash napadom.
- **Wraith** – ground ranged enemy koji održava udaljenost od igrača i ispaljuje projektile.

Svi neprijatelji nasleđuju zajedničku apstraktnu klasu `Enemy`, dok svaki konkretan tip implementira sopstveno ponašanje kroz odvojene AI state-ove.

Ground neprijatelji koriste zajednički `DistanceField` za navigaciju, dok flying neprijatelji mogu koristiti direktno kretanje jer ignorišu prepreke na zemlji.

---

## Enemy base class

Klasa `Enemy` predstavlja zajedničku osnovu za sve neprijatelje.

Ona sadrži funkcionalnosti koje su zajedničke svim enemy tipovima:

- poziciju i kretanje nasleđeno iz `Entity`,
- referencu na Player-a,
- `EnemyType`,
- `CollisionSystem`,
- health i damage sistem,
- ALIVE/DYING lifecycle,
- hit flash,
- prikaz HP bara,
- AI decision timer,
- zajedničke movement metode.

Glavna `update(float delta)` metoda je označena kao `final`.

Na taj način konkretni enemy tipovi ne menjaju osnovni lifecycle neprijatelja, već implementiraju samo:

```java
protected abstract void updateAlive(float delta);
protected abstract void updateAiDecision();
```

`updateAiDecision()` služi za donošenje AI odluka, dok `updateAlive()` izvršava trenutno ponašanje svakog frame-a.

---

## AI decision throttling

AI odluke se ne računaju svakog frame-a.

Klasa `Enemy` koristi `aiDecisionTimer` i novu odluku donosi u intervalu definisanom kroz `Config.ENEMY_AI_DECISION_INTERVAL`.

Trenutna vrednost je približno `0.10s`.

Time se odvajaju:

- **AI decisions** – izbor stanja i ponašanja,
- **state execution** – kretanje, timeri i napadi koji se izvršavaju svakog frame-a.

Ovakva podela smanjuje broj nepotrebnih AI proračuna bez vidljivog usporavanja reakcije neprijatelja.

---

# Collision and Navigation

## MovementType

Collision sistem razlikuje dva tipa kretanja:

```text
GROUND
FLYING
```

Ground entiteti reaguju na prepreke u svetu, dok flying entiteti mogu da prelete preko ground prepreka.

Flying movement ipak poštuje granice mape.

---

## NavigationMode

Za ground navigaciju postoje dva režima:

```text
NORMAL
BREAK_FENCES
```

### NORMAL

Fence predstavlja normalnu prepreku i pathfinding pokušava da pronađe put oko njega.

### BREAK_FENCES

Fence se za potrebe pathfinding-a smatra prolaznim.

Ovaj režim koristi Shade samo kada normalan put do Player-a ne postoji.

Tree, Rock i Chest blokiraju navigaciju u oba režima.

---

## Physical collision vs navigation collision

Sistem razlikuje fizičku collision proveru i navigacionu collision proveru.

Fence fizički uvek blokira ground enemy-ja. Međutim, u `BREAK_FENCES` navigation režimu DistanceField može da planira put kroz Fence.

Kada Shade stigne do takvog Fence-a, ne prolazi fizički kroz njega, već prelazi u stanje napada i uništava ga.

Time pathfinding može da planira destruktivan put bez narušavanja fizičkog collision sistema.

---

# DistanceField

## Purpose

Shade i Wraith koriste zajednički `DistanceField`.

Umesto da svaki enemy zasebno računa celu putanju do Player-a, `World` održava jedno distance field polje koje svi odgovarajući neprijatelji mogu da koriste.

Vrednost svakog tile-a predstavlja udaljenost tog tile-a od Player-a. Player-ov tile ima vrednost `0`, a enemy koji želi da priđe Player-u bira susedni tile sa manjom vrednošću.

---

## BFS algorithm

Distance field se računa pomoću Breadth-First Search algoritma.

Koriste se stanja:

```text
WHITE – tile još nije posećen
GREY  – tile je pronađen i dodat u queue
BLACK – tile je potpuno obrađen
```

BFS koristi četiri osnovna suseda: gore, dole, levo i desno, i `Queue<int[]>` implementiran pomoću `ArrayDeque`.

Nedostupni tile-ovi imaju vrednost `DistanceField.UNREACHABLE`.

---

## Two distance matrices

DistanceField održava dve matrice:

```text
normalDistances
breakFenceDistances
```

Prva se računa koristeći `NavigationMode.NORMAL`, a druga koristeći `NavigationMode.BREAK_FENCES`.

Zahvaljujući tome Shade može prvo da proveri da li postoji normalan put do Player-a. Tek ako normalan put ne postoji koristi matricu koja dozvoljava planiranje kroz Fence.

---

## DistanceField rebuilding

Distance field se ne preračunava svakog frame-a.

Ponovni BFS se izvršava kada:

- Player pređe na drugi tile,
- promeni se collision struktura sveta.

`CollisionSystem` održava `revision` broj. Revision se povećava kada se registruje ili ukloni collidable objekat.

DistanceField pamti prethodnu revision vrednost i na osnovu toga zna kada postojeće matrice više nisu validne.

---

# Enemy Movement

## Direction movement

Metoda `moveInDirection(...)` koristi se kada enemy ima smer kretanja, ali nema konkretnu krajnju tačku.

Primer je Wisp koji direktno leti prema Player-u.

Postoji i overload koji prima posebnu brzinu, što omogućava da Wisp tokom dash-a koristi drugačiju brzinu od svoje normalne movement brzine.

---

## Waypoint movement

Ground enemy navigacija koristi `moveTowardPoint(...)`.

Enemy bira sledeći tile iz DistanceField-a i pomera se prema njegovoj world koordinati.

Metoda sprečava da enemy pređe preko waypoint-a kada je movement step veći od preostale distance.

Ovo je posebno važno za prolazak kroz uske tile-based prolaze, jer entity ostaje pravilno poravnat sa navigacionom mrežom.

---

## Diagonal movement

BFS koristi četiri pravca, ali Shade i Wraith pri izboru sledećeg waypoint-a mogu da analiziraju osam susednih tile-ova.

Time je omogućeno prirodnije dijagonalno kretanje.

Kod dijagonalnog kretanja proveravaju se oba ortogonalna tile-a pored dijagonale. Ako je neki od njih blokiran, dijagonalni korak se odbacuje.

Time se sprečava prolazak kroz uglove prepreka.

---

# Shade

Shade je ground melee enemy.

Njegova state machine sadrži:

```text
CHASE
ATTACK_PLAYER
ATTACK_FENCE
```

## CHASE

Shade koristi DistanceField za navigaciju prema Player-u.

Prvo pokušava `NORMAL` navigation. Ako je njegov trenutni položaj nedostupan u normalnoj matrici, prelazi na `BREAK_FENCES`.

## ATTACK_PLAYER

Kada Shade dođe u kontakt sa Player-om, prekida navigaciju i periodično nanosi melee damage.

Napadi koriste cooldown.

## ATTACK_FENCE

Ako je Shade primoran da koristi `BREAK_FENCES` put i njegov sledeći waypoint sadrži Fence, prelazi u `ATTACK_FENCE`.

Fence prima damage dok ne bude uništen.

Nakon njegovog uklanjanja:

- CollisionSystem revision se menja,
- DistanceField se ponovo računa,
- Shade nastavlja chase.

Shade neće rušiti Fence ako postoji normalan put oko njega.

---

# Wisp

Wisp koristi `MovementType.FLYING` i ne koristi DistanceField.

Kreće se direktno prema Player-u i ignoriše Fence, Tree, Rock, Chest i ground Tiled collision.

I dalje ne može da izađe van granica mape.

Wisp koristi state machine:

```text
APPROACH
CHARGING
DASHING
RECOVERING
```

## APPROACH

Wisp direktno leti prema Player-u. Kada dođe u definisani charge range prelazi u `CHARGING`.

## CHARGING

Wisp prestaje da se kreće i određeno vreme priprema napad.

Na kraju charge-a računa i normalizuje smer prema trenutnoj poziciji Player-a. Taj smer se zatim zaključava.

## DASHING

Wisp se velikom brzinom kreće u prethodno zaključanom smeru.

Tokom dash-a više ne prati Player-ovu trenutnu poziciju, pa igrač može da izbegne napad.

Jedan dash može da nanese damage Player-u najviše jednom.

Dash se završava nakon pogotka ili nakon isteka maksimalnog dash trajanja.

## RECOVERING

Nakon dash-a Wisp kratko ostaje nepomičan.

Po isteku recovery perioda vraća se u `APPROACH`.

---

# Wraith

Wraith je ground ranged enemy i koristi `DistanceField` u `NavigationMode.NORMAL`.

Njegova state machine sadrži:

```text
APPROACH
ATTACK
RETREAT
```

## APPROACH

Kada je Wraith predaleko od Player-a, bira susedne tile-ove sa manjim DistanceField vrednostima.

Time se približava Player-u.

## ATTACK

Kada je Player na odgovarajućoj ranged udaljenosti, Wraith prestaje da se kreće i periodično ispaljuje projektile.

Napad koristi cooldown kako bi se kontrolisala brzina pucanja.

## RETREAT

Ako Player priđe preblizu, Wraith pokušava da poveća rastojanje.

Za razliku od `APPROACH` stanja, pri izboru waypoint-a bira susedni tile sa većom DistanceField vrednošću.

Na taj način isti DistanceField omogućava i approach traženjem manjih vrednosti i retreat traženjem većih vrednosti.

Kada se AI stanje promeni, prethodni waypoint se poništava kako Wraith ne bi nastavio movement odluku iz starog stanja.

---

# Projectile System

Wraith ranged napad koristi odvojen `ProjectileSystem`.

## Projectile

`Projectile` čuva:

- trenutnu poziciju,
- velocity po X i Y osi,
- damage,
- collision bounds,
- lifetime,
- active status.

Pri kreiranju projektila direction vektor se normalizuje, a zatim se iz njega i zadate brzine izračunava velocity.

Tokom svakog update-a:

```text
x += velocityX * delta
y += velocityY * delta
```

Collision bounds prati novu poziciju.

Ako lifetime istekne, projektil postaje neaktivan.

---

## ProjectileSystem

`ProjectileSystem` poseduje listu aktivnih projektila.

Njegove odgovornosti su:

- kreiranje projektila,
- update svih projektila,
- collision detection,
- uklanjanje neaktivnih projektila.

Projectile objekat sam ne zna za Player-a, mapu ili destructible objekte.

Na taj način je razdvojeno:

```text
Projectile
→ kretanje i sopstveno stanje

ProjectileSystem
→ interakcija projektila sa svetom
```

---

## Projectile collision rules

### Player

Projectile nanosi damage i deaktivira se.

### Fence

Projectile nanosi damage Fence-u i deaktivira se.

### Tree, Rock i Chest

Projectile ne nanosi ranged damage, već se samo zaustavlja i deaktivira.

### Tiled collision i granica mape

Projectile se deaktivira.

### Enemy

Enemy objekti se ne proveravaju kao projectile mete, pa Wraith ne pogađa druge enemy-je.

### Crops and Saplings

Ne predstavljaju projectile prepreke i projektil ih ignoriše.

---

# Enemy Rendering

Enemy rendering je izdvojen iz gameplay logike u `EnemyRenderer`.

Enemy klase ne čuvaju `Texture` ili druge LibGDX rendering resurse.

Renderer bira teksturu na osnovu `EnemyType` i `Facing`.

Trenutni enemy spritesheet-ovi imaju četiri directional frame-a.

Sprite može vizuelno biti veći od njegovog fizičkog collision bounds-a.

Time collision ostaje tile-based i predvidljiv, dok enemy može vizuelno zauzimati veću površinu.

---

## Hit flash

Kada enemy primi damage, njegov `hitFlashTimer` se aktivira.

EnemyRenderer tokom tog perioda koristi drugačiji SpriteBatch tint kako bi se vizuelno prikazao pogodak.

---

## Wisp charge rendering

Wisp ima poseban charge spritesheet.

Gameplay klasa izlaže samo informaciju da li je trenutno u `CHARGING` stanju.

Renderer na osnovu toga bira normalnu ili charge teksturu.

Na taj način AI klasa ne zavisi od Texture objekata.

---

## Projectile rendering

Wraith projectile koristi directional spritesheet.

Pravac frame-a određuje se iz `velocityX` i `velocityY`.

Trenutno postoje četiri vizuelna pravca:

```text
DOWN
UP
LEFT
RIGHT
```

Fizičko kretanje projektila nije ograničeno na ova četiri pravca i može biti dijagonalno.

Renderer samo bira najbliži postojeći directional frame.

---

# World Integration

`World` poseduje:

- enemy listu,
- `CollisionSystem`,
- `DistanceField`,
- `ProjectileSystem`.

DistanceField se update-uje nakon Player movement-a.

Enemy update zatim koristi trenutno distance field stanje.

ProjectileSystem se update-uje nakon enemy-ja, tako da projektil ispaljen tokom enemy update-a može odmah da postane deo sveta.

Destroyed object cleanup se izvršava nakon toga, pa Fence koji uništi Shade ili Wraith projectile koristi isti postojeći cleanup sistem.

---

# Current Enemy Types

| Enemy | Movement | Attack | Navigation |
|---|---|---|---|
| Shade | Ground | Melee / Fence destruction | DistanceField NORMAL + BREAK_FENCES |
| Wisp | Flying | Charge + dash | Direct movement |
| Wraith | Ground | Ranged projectile | DistanceField NORMAL |

Ovakva struktura omogućava da svaki enemy ima različito ponašanje, dok zajedničke funkcionalnosti ostaju u `Enemy`, `CollisionSystem`, `DistanceField` i `ProjectileSystem`.

Vizuelni asseti i animacije mogu kasnije da budu prošireni bez promene osnovne AI i navigation arhitekture.
