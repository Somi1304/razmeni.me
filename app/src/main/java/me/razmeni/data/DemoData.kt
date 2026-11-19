package me.razmeni.data

import com.google.firebase.Timestamp

suspend fun seedDemoData() {
    val demo = listOf(
        Exchange(
            name = "Menjačnica Centar",
            type = ExchangeType.MENJACNICA.name,
            address = "Obrenovićeva 12, Niš",
            description = "Mali šalter u prolazu kod Kalče, menja bez provizije.",
            workingHours = "08-20, nedeljom 09-14",
            buyEur = 116.90,
            sellEur = 117.40,
            lat = 43.3196,
            lng = 21.8967,
            ratings = mapOf("u1" to 5L, "u2" to 5L, "u3" to 4L),
            comments = listOf(
                Comment("Milan", "Odličan kurs, uvek brza usluga!", Timestamp.now()),
                Comment("Jelena", "Povoljniji kurs nego u većini banaka.", Timestamp.now()),
                Comment("Stefan", "Ljubazno osoblje i bez provizije.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Banka Obrenovićeva",
            type = ExchangeType.BANKA.name,
            address = "Obrenovićeva 38, Niš",
            description = "Ekspozitura banke sa menjačkim šalterom i bankomatom ispred ulaza.",
            workingHours = "08-19, subotom 08-13",
            buyEur = 116.60,
            sellEur = 117.75,
            lat = 43.3181,
            lng = 21.8986,
            ratings = mapOf("u4" to 4L, "u5" to 3L, "u6" to 4L),
            comments = listOf(
                Comment("Marko", "Malo je veća gužva ujutru, ali usluga je u redu.", Timestamp.now()),
                Comment("Ana", "Bankomat radi 24/7 ispred ulaza.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Pošta Niš centar",
            type = ExchangeType.POSTA.name,
            address = "Vožda Karađorđa 13a, Niš",
            description = "Glavna pošta u centru, menjački poslovi se obavljaju na šalteru 5.",
            workingHours = "07-19, subotom 07-14",
            buyEur = 116.70,
            sellEur = 117.60,
            lat = 43.3221,
            lng = 21.8992,
            ratings = mapOf("u7" to 4L, "u8" to 4L, "u9" to 5L),
            comments = listOf(
                Comment("Petar", "Korektan kurs, brza obrada na šalteru.", Timestamp.now()),
                Comment("Marija", "Korisno jer odmah završim i ostale uplate.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Kalča",
            type = ExchangeType.MENJACNICA.name,
            address = "TC Kalča, Prizemlje, Niš",
            description = "Nalazi se na ulazu u TC Kalča iz pravca Obrenovićeve.",
            workingHours = "08-20, nedeljom zatvoreno",
            buyEur = 116.85,
            sellEur = 117.45,
            lat = 43.3190,
            lng = 21.8955,
            ratings = mapOf("u10" to 5L, "u11" to 4L, "u12" to 5L),
            comments = listOf(
                Comment("Nikola", "Super kurs za evro i franake.", Timestamp.now()),
                Comment("Milica", "Uvek imaju sitne novčanice.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Banka Poštanska Štedionica",
            type = ExchangeType.BANKA.name,
            address = "Kneza Miloša 4, Niš",
            description = "Povoljna zamena većih iznosa valuta uz brzu uslugu.",
            workingHours = "08-18, subotom 08-12",
            buyEur = 116.65,
            sellEur = 117.70,
            lat = 43.3165,
            lng = 21.8920,
            ratings = mapOf("u13" to 4L, "u14" to 4L),
            comments = listOf(
                Comment("Dušan", "Povoljno za veće iznose novca.", Timestamp.now()),
                Comment("Katarina", "Veoma ljubazna šalterska radnica.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Tvrđava",
            type = ExchangeType.MENJACNICA.name,
            address = "Đuke Dinić 2, Niš",
            description = "Preko puta ulaza u Nišku tvrđavu i glavnu pijacu.",
            workingHours = "07-20, nedeljom 08-16",
            buyEur = 116.95,
            sellEur = 117.35,
            lat = 43.3245,
            lng = 21.8950,
            ratings = mapOf("u15" to 5L, "u16" to 5L, "u17" to 5L),
            comments = listOf(
                Comment("Aleksandar", "Najbolji kurs u gradu, svaka čast!", Timestamp.now()),
                Comment("Ivana", "Radi i nedeljom pre podne, spas u zadnji čas.", Timestamp.now()),
                Comment("Bojan", "Brza usluga, bez ikakvog čekanja.", Timestamp.now())
            )
        ),
        Exchange(
            name = "NLB Komercijalna Banka",
            type = ExchangeType.BANKA.name,
            address = "Generala Milojka Lešjanina 10, Niš",
            description = "Centralna ekspozitura u Lešjaninovoj ulici.",
            workingHours = "08-17, subotom ne radi",
            buyEur = 116.60,
            sellEur = 117.80,
            lat = 43.3202,
            lng = 21.8915,
            ratings = mapOf("u18" to 3L, "u19" to 4L),
            comments = listOf(
                Comment("Nemanja", "Standardan kurs za banke.", Timestamp.now()),
                Comment("Sanja", "Lepo uređen prostor i klijentska zona.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Zlatnik",
            type = ExchangeType.MENJACNICA.name,
            address = "Pantelejska 18, Niš",
            description = "Menjačnica i zalagaonica u naselju Pantelej.",
            workingHours = "08-21 svaki dan",
            buyEur = 116.88,
            sellEur = 117.42,
            lat = 43.3270,
            lng = 21.9020,
            ratings = mapOf("u20" to 5L, "u21" to 4L, "u22" to 5L),
            comments = listOf(
                Comment("Filip", "Rade do kasno uveče što je odlično.", Timestamp.now()),
                Comment("Tanja", "Veoma ljubazni i profesionalni.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Pošta Durlan",
            type = ExchangeType.POSTA.name,
            address = "Knjaževačka 45, Niš",
            description = "Pošta u naselju Durlan sa menjačkim šalterom.",
            workingHours = "07-19, subotom 07-13",
            buyEur = 116.68,
            sellEur = 117.65,
            lat = 43.3295,
            lng = 21.9180,
            ratings = mapOf("u23" to 4L, "u24" to 3L),
            comments = listOf(
                Comment("Dejan", "Zgodno za stanovnike Durlana.", Timestamp.now()),
                Comment("Dragana", "Ponekad bude red, ali ide brzo.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Durlan Ekspres",
            type = ExchangeType.MENJACNICA.name,
            address = "Knjaževačka 102, Niš",
            description = "Brza zamena novca u blizini Slatke kuće u Durlanu.",
            workingHours = "08-20, nedeljom 08-14",
            buyEur = 116.92,
            sellEur = 117.38,
            lat = 43.3320,
            lng = 21.9250,
            ratings = mapOf("u25" to 5L, "u26" to 4L, "u27" to 5L),
            comments = listOf(
                Comment("Uroš", "Korektan kurs i povoljne cene.", Timestamp.now()),
                Comment("Maja", "Uvek sveže informacije o kursu.", Timestamp.now())
            )
        ),
        Exchange(
            name = "OTP Banka Niš",
            type = ExchangeType.BANKA.name,
            address = "Trg Kralja Milana 5, Niš",
            description = "Ekspozitura na samom centralnom gradskom trgu.",
            workingHours = "08-18, subotom 09-13",
            buyEur = 116.55,
            sellEur = 117.78,
            lat = 43.3212,
            lng = 21.8960,
            ratings = mapOf("u28" to 4L, "u29" to 4L),
            comments = listOf(
                Comment("Vojin", "Lokacija na samom trgu je super.", Timestamp.now()),
                Comment("Sonja", "Bankomat uvek napunjen i spreman.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Zona 3",
            type = ExchangeType.MENJACNICA.name,
            address = "Bulevar Nemanjića 25, Niš",
            description = "U sklopu tržnog centra Zona 3 na Bulevaru.",
            workingHours = "08-20, subotom 08-16",
            buyEur = 116.90,
            sellEur = 117.40,
            lat = 43.3175,
            lng = 21.9110,
            ratings = mapOf("u30" to 5L, "u31" to 5L),
            comments = listOf(
                Comment("Goran", "Uvek fer kurs i dobri uslovi.", Timestamp.now()),
                Comment("Milena", "Odlična menjačnica u komšiluku.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Pošta Bulevar",
            type = ExchangeType.POSTA.name,
            address = "Bulevar Nemanjića 85, Niš",
            description = "Veća pošta pored Parka Svetog Save.",
            workingHours = "07-20, subotom 07-14",
            buyEur = 116.72,
            sellEur = 117.58,
            lat = 43.3150,
            lng = 21.9210,
            ratings = mapOf("u32" to 4L, "u33" to 4L, "u34" to 5L),
            comments = listOf(
                Comment("Nenad", "Pored parka, super lokacija.", Timestamp.now()),
                Comment("Irena", "Šalter za menjačnicu je odvojen od pošte.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Park",
            type = ExchangeType.MENJACNICA.name,
            address = "Pariske Komune 1, Niš",
            description = "Odmah preko puta Parka Svetog Save.",
            workingHours = "08-21 svaki dan",
            buyEur = 116.89,
            sellEur = 117.41,
            lat = 43.3160,
            lng = 21.9165,
            ratings = mapOf("u35" to 5L, "u36" to 4L),
            comments = listOf(
                Comment("Slobodan", "Radi kasno, veoma praktično.", Timestamp.now()),
                Comment("Jovana", "Ljubazna devojka na šalteru.", Timestamp.now())
            )
        ),
        Exchange(
            name = "UniCredit Banka",
            type = ExchangeType.BANKA.name,
            address = "Bulevar Nemanjića 14a, Niš",
            description = "Moderna ekspozitura sa zonom za samousluživanje.",
            workingHours = "08-17, subotom ne radi",
            buyEur = 116.62,
            sellEur = 117.72,
            lat = 43.3188,
            lng = 21.9060,
            ratings = mapOf("u37" to 4L, "u38" to 4L),
            comments = listOf(
                Comment("Luka", "Brzo se završava obrada zamene novca.", Timestamp.now()),
                Comment("Tamara", "Dobar kurs za švajcarski franak.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Palilula",
            type = ExchangeType.MENJACNICA.name,
            address = "Episkopska 22, Niš",
            description = "Kod Palilulske rampe i pijace u naselju Palilula.",
            workingHours = "07-19, subotom 07-15",
            buyEur = 116.87,
            sellEur = 117.43,
            lat = 43.3130,
            lng = 21.8950,
            ratings = mapOf("u39" to 5L, "u40" to 5L),
            comments = listOf(
                Comment("Darko", "Odličan kurs rano ujutru.", Timestamp.now()),
                Comment("Olivera", "Stalno menjam ovde novac.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Železnička",
            type = ExchangeType.MENJACNICA.name,
            address = "Dimitrija Tucovića 4, Niš",
            description = "Blizu železničke i autobuske stanice.",
            workingHours = "06-22 svaki dan",
            buyEur = 116.84,
            sellEur = 117.46,
            lat = 43.3145,
            lng = 21.8860,
            ratings = mapOf("u41" to 4L, "u42" to 5L, "u43" to 4L),
            comments = listOf(
                Comment("Siniša", "Radno vreme od 6h ujutru je spas kad putuješ!", Timestamp.now()),
                Comment("Marina", "Menjaju i manje poznate valute.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Erste Banka Niš",
            type = ExchangeType.BANKA.name,
            address = "Vožda Karađorđa 5, Niš",
            description = "U strogom centru grada pored suda.",
            workingHours = "08-17, subotom ne radi",
            buyEur = 116.64,
            sellEur = 117.74,
            lat = 43.3218,
            lng = 21.8970,
            ratings = mapOf("u44" to 4L, "u45" to 3L),
            comments = listOf(
                Comment("Srđan", "Poslovni ambijent i brza usluga.", Timestamp.now()),
                Comment("Nevena", "Prihvataju i platne kartice za kupovinu.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Marger",
            type = ExchangeType.MENJACNICA.name,
            address = "Trg Pavla Stojkovića 8, Niš",
            description = "Kultna menjačnica na Margeru.",
            workingHours = "08-20, subotom 08-15",
            buyEur = 116.91,
            sellEur = 117.39,
            lat = 43.3170,
            lng = 21.8900,
            ratings = mapOf("u46" to 5L, "u47" to 5L, "u48" to 5L),
            comments = listOf(
                Comment("Dragan", "Uvek najpovoljniji euro u kraju.", Timestamp.now()),
                Comment("Zorica", "Ljubazni i ekspeditivni.", Timestamp.now()),
                Comment("Nenad", "Korektan odnos prema mušterijama.", Timestamp.now())
            )
        ),
        Exchange(
            name = "Menjačnica Delta Planet",
            type = ExchangeType.MENJACNICA.name,
            address = "Bulevar Nemanjića 11b, Niš",
            description = "Nalazi se na prvom spratu šoping mola Delta Planet.",
            workingHours = "10-22 svaki dan",
            buyEur = 116.86,
            sellEur = 117.44,
            lat = 43.3198,
            lng = 21.9190,
            ratings = mapOf("u49" to 5L, "u50" to 4L, "u51" to 5L),
            comments = listOf(
                Comment("Viktor", "Radno vreme do 22h je fenomenalno!", Timestamp.now()),
                Comment("Anja", "Super mesto dok si u kupovini.", Timestamp.now())
            )
        )
    )
    demo.forEach { ExchangeRepo.add(it, points = 0) }
}
