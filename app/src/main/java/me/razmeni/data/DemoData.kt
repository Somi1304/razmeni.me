package me.razmeni.data

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
            lng = 21.8967
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
            lng = 21.8986
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
            lng = 21.8992
        )
    )
    demo.forEach { ExchangeRepo.add(it, points = 0) }
}
