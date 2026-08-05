# Ashveil Inventory System

## 1. Svrha sistema

Inventory sistem čuva igračeve predmete i omogućava rad sa njima preko miša i tastature.

Podržane su dve vrste interakcije:

- Scene2D drag-and-drop preko miša;
- navigacija preko WASD ili strelica i potvrda preko Enter tastera.

Sistem je podeljen na backend i UI sloj. Backend poseduje stvarne podatke i izvršava operacije nad predmetima, dok UI samo prikazuje trenutno stanje i traži od backenda da izvrši odgovarajuću akciju.

Ovakva podela sprečava da vizuelne komponente direktno menjaju item podatke mimo pravila inventory sistema.

---

## 2. Organizacija slotova

Inventory sadrži ukupno 20 slotova.

- Slotovi `0–4` pripadaju hotbaru.
- Slotovi `5–19` pripadaju glavnom inventory delu.

Vizuelni raspored je:

```text
 5   6   7   8   9
10  11  12  13  14
15  16  17  18  19

 0   1   2   3   4
```

Hotbar se vizuelno prikazuje ispod glavnog inventory dela, ali u backendu i dalje zauzima prvih pet indeksa. Time gameplay logika može jednostavno da koristi indekse `0–4` za direktan izbor hotbar slota.

---

## 3. Backend klase

### 3.1 `Inventory`

`Inventory` poseduje kolekciju slotova. Svaki slot sadrži:

- jedan `ItemStack`;
- ili `null` ako je slot prazan.

UI ne čuva sopstvenu kopiju inventory podataka. Uvek čita trenutno stanje iz backend objekta, na primer:

```java
inventory.getSlot(slotIndex)
```

Na taj način gameplay, crafting, pickup, drop i inventory UI koriste isti izvor podataka.

### 3.2 `ItemStack`

`ItemStack` predstavlja sadržaj jednog slota.

Sadrži:

- `ItemType type`;
- `int quantity`;
- durability vrednost za iteme koji koriste durability.

Stackable item može imati quantity veći od jedan. Nestackable item, kao što je alat, obično zauzima jedan slot i koristi durability umesto količine.

### 3.3 `ItemType`

`ItemType` definiše osobine zajedničke za sve iteme tog tipa, uključujući:

- display name;
- description;
- maksimalnu veličinu stacka;
- maksimalni durability;
- informaciju da li je item stackable;
- damage profile.

Details panel čita statične osobine iz `ItemType`, dok trenutni quantity i durability dolaze iz `ItemStack` objekta.

---

## 4. Osnovne inventory operacije

### 4.1 `moveSlot(sourceIndex, destinationIndex)`

Ovo je glavna operacija koju koriste i mouse drag-and-drop i Enter premeštanje preko tastature.

Rezultat zavisi od sadržaja source i destination slota.

#### Prazan destination

Ceo source stack se premešta u destination slot.

```text
WOOD x5 | EMPTY
    ↓
EMPTY   | WOOD x5
```

#### Različiti item tipovi

Source i destination stack menjaju mesta.

```text
WOOD x5 | STONE x3
    ↓
STONE x3 | WOOD x5
```

#### Isti stackable item tip

Stackovi se spajaju do maksimalne veličine stacka.

Ako ceo source stack ne može da stane u destination, višak ostaje u source slotu.

#### Nestackable itemi

Nestackable itemi se ne spajaju. Oni se swapuju kao dva različita item tipa.

Metoda vraća `boolean` da bi UI znao da li je inventory stvarno promenjen.

### 4.2 `splitStack(sourceIndex, destinationIndex, amount)`

Ova operacija premešta samo deo stacka.

Trenutna implementacija dozvoljava split kada:

- source sadrži stackable item;
- source quantity je veći od količine koja se odvaja;
- destination slot je prazan.

Kod Shift + drag operacije količina koja se vuče računa se ovako:

```java
(sourceQuantity + 1) / 2
```

Kada je quantity neparan, veći deo se vuče.

Primeri:

```text
6 → vuče se 3, ostaje 3
5 → vuče se 3, ostaje 2
2 → vuče se 1, ostaje 1
```

---

## 5. UI arhitektura

### 5.1 `GameMenuUi`

`GameMenuUi` je glavni Scene2D kontejner menija.

Poseduje:

- `Stage`;
- root tabelu;
- tab dugmad;
- content tabelu;
- `InventoryPanel`;
- `CraftingPanel`;
- `ShopPanel`.

Ne sadrži inventory pravila. Njegov posao je da menja aktivni panel i prosleđuje menu komande trenutno aktivnom panelu.

### 5.2 `MenuPanel`

`MenuPanel` je zajednička bazna klasa za sve glavne panele.

Definiše lifecycle metode:

```java
public void refresh()
public void onShow()
public void onHide()
```

Takođe sadrži prazne metode za navigaciju:

```java
public void moveSelectionLeft()
public void moveSelectionRight()
public void moveSelectionUp()
public void moveSelectionDown()
public void confirmSelection()
```

Panel override-uje samo akcije koje podržava.

Zbog toga `GameScreen` i `GameMenuUi` ne moraju da poznaju detalje inventory rasporeda.

### 5.3 `InventoryPanel`

`InventoryPanel` koordinira ceo inventory UI.

Njegove odgovornosti su:

- kreiranje svih 20 slot view objekata;
- raspoređivanje glavnih slotova i hotbara;
- osvežavanje slotova iz backend inventory objekta;
- čuvanje trenutno selektovanog slota;
- osvežavanje details panela;
- registracija drag-and-drop source i target objekata;
- tastaturna navigacija;
- Enter pickup i placement logika;
- čišćenje privremenih UI stanja pri izlasku iz panela.

### 5.4 `InventorySlotUi`

`InventorySlotUi` predstavlja jedan vizuelni slot.

Sastoji se od više Scene2D slojeva:

- background slike;
- item slike;
- quantity labela;
- durability bara.

Slot čuva dva nezavisna UI stanja:

- `selected` — trenutna selekcija mišem ili tastaturom;
- `keyboardPickedUp` — slot čiji item je izabran prvim Enterom.

Ova dva stanja moraju biti odvojena. Nakon prvog Entera, selekcija može da se pomera ka destination slotu, dok source slot ostaje posebno označen.

### 5.5 `DurabilityBarUi`

`DurabilityBarUi` je izdvojena Scene2D komponenta.

Sadrži:

- pozadinu;
- fill sliku čija širina predstavlja trenutni durability odnos.

Odnos se računa kao:

```text
current durability / maximum durability
```

Za iteme koji ne koriste durability komponenta se čisti ili sakriva.

### 5.6 `UiSkinFactory`

`UiSkinFactory` centralizuje trenutni privremeni izgled UI-ja.

Pravi solid-color resurse za:

- pozadinu menija;
- pozadinu inventory panela;
- običan inventory slot;
- hotbar slot;
- selected slot;
- keyboard picked-up slot;
- item placeholder;
- durability bar.

Ove boje su samo privremeni vizuelni placeholderi. Layout, Scene2D struktura, lifecycle i inventory logika ostaju upotrebljivi kada se kasnije uvedu prave teksture i fontovi.

---

## 6. Lifecycle panela

Kada se meni otvori, aktivni panel dobija:

```java
onShow()
```

Podrazumevani `onShow()` poziva `refresh()`, pa panel prikazuje najnovije backend stanje.

Kada korisnik promeni tab ili zatvori meni, prethodni panel dobija:

```java
onHide()
```

`InventoryPanel.onHide()` čisti:

- trenutno selektovani slot;
- keyboard pickup stanje;
- pripadajuće vizuelne indikatore.

Time se sprečava da privremeno stanje ostane aktivno nakon napuštanja inventory panela.

---

## 7. Selekcija mišem

Svaki `InventorySlotUi` ima `InputListener`.

Kada miš uđe u slot, `InventoryPanel` poziva:

```java
selectSlot(slotIndex)
```

`selectSlot()`:

1. proverava da li je indeks validan;
2. skida selected izgled sa prethodnog slota;
3. čuva novi `selectedSlotIndex`;
4. postavlja selected izgled na novi slot;
5. osvežava details panel.

Hover mišem i keyboard navigacija koriste isti `selectedSlotIndex` i istu `selectSlot()` metodu. Zato ne postoje odvojena mouse i keyboard selection stanja.

---

## 8. Scene2D drag-and-drop

Mouse premeštanje itema koristi LibGDX Scene2D `DragAndDrop` sistem.

Svaki inventory slot registruje se kao:

- `DragAndDrop.Source`;
- `DragAndDrop.Target`.

### 8.1 `Source`

`Source` predstavlja actor iz kog drag može da počne.

Za inventory je svaki slot potencijalni source.

Drag počinje u metodi:

```java
dragStart(...)
```

Metoda:

1. čita source slot indeks;
2. uzima stvarni `ItemStack` iz `Inventory` objekta;
3. vraća `null` ako je slot prazan;
4. proverava da li je Shift pritisnut;
5. odlučuje da li je običan drag ili split drag;
6. računa dragged quantity;
7. pravi `Payload`;
8. pravi privremeni drag actor;
9. vraća payload.

### 8.2 `Payload`

`Payload` prenosi podatke od source-a do target-a.

Inventory koristi `InventoryDragData`, koji sadrži:

- source slot indeks;
- dragged quantity;
- informaciju da li je u pitanju split drag.

Payload ne postaje novi autoritativni item stack. On sadrži samo podatke potrebne da backend kasnije izvrši odgovarajuću operaciju.

### 8.3 Zašto stvarni `ItemStack` ostaje u source slotu

Stvarni item se ne uklanja u `dragStart()`.

Tokom prevlačenja:

- pravi `ItemStack` ostaje u source slotu;
- drag actor je samo vizuelna kopija;
- inventory se menja tek kada validan target prihvati drop.

Ovo je važno jer korisnik može da pusti miš van svih slotova. U tom slučaju se ne poziva backend operacija i inventory ostaje nepromenjen.

Kada bi se item uklanjao već u `dragStart()`, svaki neuspešan drag bi zahtevao dodatnu rollback logiku.

### 8.4 Drag actor

Drag actor je privremeni Scene2D `Stack` koji prikazuje:

- item placeholder;
- dragged quantity.

Kod split draga label prikazuje količinu koja se stvarno odvaja, a ne ceo source quantity.

Drag actor koristi:

```java
dragActor.setTouchable(Touchable.disabled);
```

Time privremeni actor ne presreće input koji mora da stigne do destination slota ispod njega.

### 8.5 `setDragTime(0)`

Drag-and-drop kontroler koristi:

```java
dragAndDrop.setDragTime(0);
```

Bez ove postavke, veoma brz drag može da se završi pre nego što Scene2D proglasi drag aktivnim. Tada deluje kao da brzo prevlačenje ne radi.

Vrednost nula omogućava da drag počne odmah.

### 8.6 `Target`

`Target` predstavlja actor koji može da primi payload.

Metoda `drag(...)` proverava da li je trenutni destination validan.

Običan drag je validan kada:

- source i destination nisu isti slot.

Split drag je validan kada:

- source i destination nisu isti;
- destination slot je prazan.

Ovo prati trenutni ugovor metode `Inventory.splitStack()`.

### 8.7 `drop(...)`

Kada se payload pusti na validan target, `drop(...)` čita `InventoryDragData`.

Zatim bira backend operaciju:

```text
normal drag → inventory.moveSlot(...)
split drag  → inventory.splitStack(...)
```

Tek nakon uspešne backend promene panel:

- osvežava slotove;
- selektuje destination slot;
- osvežava details panel.

### 8.8 Podržane mouse operacije

Drag-and-drop sistem podržava:

- move celog stacka u prazan slot;
- swap različitih itema;
- merge kompatibilnih stackova;
- pomeranje alata;
- Shift + drag split u prazan slot;
- odustajanje puštanjem van validnog targeta.

---

## 9. Keyboard input arhitektura

### 9.1 Input kontekst

`GameScreen` odlučuje da li je aktivan gameplay ili menu input.

Kada je meni zatvoren:

- WASD pomera igrača;
- Q baca item;
- E aktivira interact.

Kada je meni otvoren:

- world update se ne izvršava;
- Q i E menjaju tabove;
- WASD i strelice pomeraju menu selekciju;
- Enter potvrđuje akciju.

Tako isti fizički taster može imati različito značenje u različitom kontekstu bez konflikta.

### 9.2 `KeyBindings`

`KeyBindings` čuva semantičke key bindinge.

Menu akcije imaju odvojena polja čak i kada trenutno koriste isti fizički taster kao neka gameplay akcija.

Na primer:

- previous menu tab koristi Q;
- next menu tab koristi E;
- menu navigation koristi strelice;
- WASD se dodatno prihvata kao alternativa;
- menu confirm koristi Enter.

Ovakva struktura kasnije olakšava remapping kontrola.

### 9.3 Tok prosleđivanja inputa

Tok menu inputa je:

```text
GameScreen
    ↓
GameMenuUi
    ↓
trenutno aktivni MenuPanel
    ↓
InventoryPanel implementacija
```

`GameScreen` detektuje fizički taster.

`GameMenuUi` prosleđuje semantičku komandu aktivnom panelu.

`InventoryPanel` odlučuje kako ta komanda utiče na selekciju ili inventory operaciju.

Na taj način `GameScreen` ne mora da zna raspored inventory slotova.

---

## 10. Keyboard navigacija kroz slotove

WASD i strelice rade isto.

Horizontalno kretanje kruži unutar istog vizuelnog reda.

Primeri:

```text
Left sa 5  → 9
Left sa 10 → 14
Left sa 15 → 19
Left sa 0  → 4

Right sa 9  → 5
Right sa 14 → 10
Right sa 19 → 15
Right sa 4  → 0
```

Vertikalno kretanje kruži kroz redove iste kolone.

Primer jedne kolone:

```text
7
↓
12
↓
17
↓
2
↓
7
```

Kada nijedan slot nije selektovan, prvi navigation input bira slot `5`, jer je to prvi vizuelni slot glavnog inventory dela.

Sve promene moraju da koriste:

```java
selectSlot(newIndex)
```

Direktna izmena `selectedSlotIndex` nije dovoljna, jer tada highlight i details panel ne bi bili osveženi.

---

## 11. Enter pickup i placement

Keyboard premeštanje itema koristi Enter u dva koraka.

### Prvi Enter: pickup

Kada se Enter pritisne na nepraznom selektovanom slotu:

- indeks slota se čuva u `keyboardMoveSourceIndex`;
- source slot dobija `keyboardPickedUp` izgled;
- stvarni item ostaje u backend source slotu.

### Navigacija

Korisnik pomera običnu selekciju preko WASD ili strelica.

Source slot ostaje posebno označen, dok se selected stanje pomera ka destination slotu.

### Drugi Enter: placement

Kada se Enter pritisne na drugom slotu, panel poziva:

```java
inventory.moveSlot(keyboardMoveSourceIndex, selectedSlotIndex)
```

Zato keyboard placement podržava iste backend operacije kao običan mouse drag:

- move;
- merge;
- swap.

Nakon uspešne operacije:

- keyboard pickup stanje se briše;
- picked-up izgled se skida;
- slotovi se osvežavaju.

### Cancel

Ako se Enter ponovo pritisne dok je source slot i dalje selektovan, operacija se otkazuje.

Backend podaci se ne menjaju.

### Prazan source

Enter na praznom slotu ne pokreće pickup.

### Napuštanje panela

Promena taba ili zatvaranje menija poziva `onHide()`, koji otkazuje privremeni keyboard pickup.

---

## 12. Prioritet vizuelnih stanja slota

Jedan slot može imati više logičkih UI stanja.

Background se bira po sledećem prioritetu:

```text
keyboard picked up
selected
default
```

Picked-up stanje ima najveći prioritet zato što označava nezavršenu inventory operaciju.

Selected stanje označava trenutni destination kandidat.

Default stanje se koristi kada nijedno od prethodnih nije aktivno.

---

## 13. Details panel

Details panel prikazuje podatke trenutno selektovanog slota.

Za selektovan prazan slot prikazuje:

```text
Empty slot.
```

Kada nijedan slot nije selektovan prikazuje:

```text
No item selected.
```

Za item prikazuje odgovarajuće podatke:

- name;
- description;
- durability;
- quantity;
- entity damage.

Quantity se prikazuje samo za stackable iteme.

Durability se prikazuje samo za tipove koji koriste durability.

---

## 14. Ključne dizajnerske odluke

### Backend je autoritativan

UI ne tretira drag actor niti vizuelno stanje kao pravi inventory podatak.

Sve trajne izmene prolaze kroz `Inventory`.

### Postoji jedan selection model

Mouse hover, WASD i strelice koriste isti `selectedSlotIndex`.

### Privremeni podaci su odvojeni

`InventoryDragData` postoji samo tokom mouse draga.

`keyboardMoveSourceIndex` postoji samo tokom nezavršene Enter operacije.

Nijedan od njih ne zamenjuje stvarni `ItemStack`.

### Nevalidna operacija ne menja backend

Drop van targeta, split na zauzet slot, Enter na praznom source slotu ili cancel na istom source slotu ne menjaju inventory.

### Lifecycle briše privremeno stanje

`onHide()` čisti selekciju i keyboard pickup.

Time se sprečava da staro UI stanje ostane aktivno nakon promene taba ili zatvaranja menija.

---

## 15. Test checklist

### Prikaz

- Prikazano je svih 20 slotova.
- Slotovi `0–4` nalaze se u hotbar redu.
- Slotovi `5–19` nalaze se u glavnom inventory gridu.
- Quantity vrednosti su tačne.
- Durability bar postoji samo za odgovarajuće iteme.
- Details panel prati selekciju.

### Mouse selection

- Hover selektuje slot.
- Prethodni selected izgled se skida.
- Prazan slot prikazuje `Empty slot.`.
- Item prikazuje odgovarajuće podatke.

### Mouse drag-and-drop

- Ceo stack se pomera u prazan slot.
- Različiti itemi se swapuju.
- Kompatibilni stackovi se merguju.
- Pun destination stack ne gubi iteme.
- Alati se pomeraju ili swapuju bez merge-a.
- Brzo prevlačenje radi.
- Drop van slotova ne menja ništa.
- Drag na originalni source ne menja ništa.

### Split drag

- Shift + drag parnog stacka pravi jednake polovine.
- Shift + drag neparnog stacka daje veći deo destination slotu.
- Split radi samo za stackable quantity veći od jedan.
- Destination mora biti prazan.
- Nevalidan split ne menja inventory.

### Tab navigacija

- E prelazi na sledeći tab.
- Q prelazi na prethodni tab.
- Navigacija kruži između Inventory, Crafting i Shop tabova.
- Aktivni tab dobija odgovarajući izgled.
- Prethodni panel dobija `onHide()`.
- Novi panel dobija `onShow()`.

### Keyboard navigacija

- WASD i strelice rade isto.
- Horizontalno kretanje kruži unutar istog reda.
- Vertikalno kretanje kruži unutar iste kolone.
- Prvi navigation input bira slot `5`.
- Highlight i details se osvežavaju posle svakog pomeraja.

### Enter movement

- Enter na itemu pokreće keyboard pickup.
- Source slot dobija picked-up izgled.
- Selekcija može da se pomera dok source ostaje označen.
- Drugi Enter izvršava move, merge ili swap.
- Enter ponovo na source slotu otkazuje operaciju.
- Enter na praznom source slotu ne radi ništa.
- Promena taba ili zatvaranje menija briše pickup stanje.

---

## 16. Moguća buduća proširenja

Postojeća arhitektura dozvoljava kasnije dodavanje:

- pravih item ikonica;
- teksturisanih slot border-a;
- controller podrške;
- potpuno podesivih key bindinga;
- keyboard stack splitting-a;
- context menija;
- tooltipova;
- zvučnih efekata;
- drag target feedback-a;
- animacija premeštanja;
- čuvanja i učitavanja inventory stanja.

Sva buduća proširenja treba i dalje da koriste `Inventory` kao autoritativni backend i ne treba da premeštaju trajnu item logiku u Scene2D actor klase.
