package com.example.dinoar.model

object DinosaurRepository {
    val dinosaurs = listOf(
        Dinosaur(DinosaurId.TREX, "Tiranosaurio Rex", "Cretácico tardío", "Carnívoro",
            "12–13 metros", "Gran dinosaurio terópodo conocido por sus poderosas mandíbulas y extremidades anteriores pequeñas.",
            "markers/trex_marker.jpg", "models/trex.glb", scientificName = "Tyrannosaurus rex", expectedAnimations = 5),
        Dinosaur(DinosaurId.TRICERATOPS, "Triceratops", "Cretácico tardío", "Herbívoro",
            "8–9 metros", "Dinosaurio caracterizado por sus tres cuernos y una gran estructura ósea alrededor del cuello.",
            "markers/triceratops_marker.jpg", "models/triceratops.glb", scientificName = "Triceratops sp.", expectedAnimations = 0, fallbackModelPath = "models/demo_pyramid.glb"),
        Dinosaur(DinosaurId.VELOCIRAPTOR, "Velociraptor", "Cretácico tardío", "Carnívoro",
            "Aproximadamente 2 metros", "Pequeño dinosaurio terópodo emplumado, ágil y bípedo, caracterizado por una gran garra curva en el segundo dedo de cada pie.",
            "markers/velociraptor_marker.jpg", "models/velociraptor.glb",
            // Bounds computed from original positions, joint weights and inverse bind matrices.
            ModelFraming(-0.029312f, 0.950084f, -0.446427f, 0.586652f, 0.959821f, 1.813569f), scientificName = "Velociraptor sp.", expectedAnimations = 26),
        Dinosaur(DinosaurId.PACHYCEPHALOSAURUS, "Pachycephalosaurus", "Cretácico tardío", "Principalmente herbívoro; dieta debatida",
            "Unos 3 metros", "Dinosaurio bípedo de América del Norte. Su cúpula craneal pudo tener funciones de exhibición o interacción entre individuos; su uso exacto sigue en estudio.", "markers/pachycephalosaurus_marker.png", "models/pachycephalosaurus.glb",
            ModelFraming(-0.28342486f, 1.05804239f, -0.32504889f, 0.78991056f, 1.05863773f, 1.70281415f),
            scientificName = "Pachycephalosaurus sp.", group = "Dinosaurio", features = "Cúpula ósea en el cráneo, protuberancias alrededor de la cabeza y postura bípeda.",
            sourceUrl = "https://www.nhm.ac.uk/discover/dino-directory/pachycephalosaurus.html", modelCredit = "PBR Pachycephalasaurus (Animated) — Ferocious Industries (https://sketchfab.com/ferociousindustries.matthias). CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/). https://sketchfab.com/3d-models/pbr-pachycephalasaurus-animated-6eea5cee4afa4730bf75c6329a43e56d. Render de tarjeta derivado del modelo; GLB sin modificar.", expectedAnimations = 21,
            presentation = ModelPresentation(-65f, 0f)),
        Dinosaur(DinosaurId.MOSASAURUS, "Mosasaurus", "Cretácico tardío", "Carnívoro",
            "8–12 metros según especie; estimaciones mayores debatidas", "Reptil marino que respiraba aire y cazaba en los mares del Cretácico. Sus extremidades se transformaron en aletas. No era un dinosaurio.", "markers/mosasaurus_marker.png", "models/mosasaurus.glb",
            ModelFraming(-0.00000002f, 0.00854163f, -0.14962452f, 0.07807941f, 0.04103131f, 0.25583256f),
            scientificName = "Mosasaurus sp.", group = "Reptil marino", features = "Cuerpo alargado, mandíbulas con dientes, aletas y cola propulsora.",
            sourceUrl = "https://www.nhm.ac.uk/discover/what-is-a-mosasaur.html", modelCredit = "Mosasaurus — Alexey Zaika (https://sketchfab.com/lexazaixa25). CC-BY-4.0 (http://creativecommons.org/licenses/by/4.0/). https://sketchfab.com/3d-models/mosasaurus-684754a2ad8c46aca0634326b53d1f04. Render de tarjeta derivado del modelo; GLB sin modificar.", expectedAnimations = 1,
            presentation = ModelPresentation(-65f, 10f)),
        Dinosaur(DinosaurId.DIMORPHODON, "Dimorphodon", "Jurásico temprano", "Carnívoro: pequeños vertebrados",
            "Unos 1 metro de longitud; hasta 2 metros de envergadura", "Pterosaurio del Jurásico temprano con dos tipos de dientes. Sus alas eran membranas sostenidas por un dedo alargado. No era un dinosaurio.", "markers/dimorphodon_marker.png", "models/dimorphodon.glb",
            ModelFraming(0.00181398f, 0.18502732f, -5.75940566f, 12.94278987f, 1.40765403f, 10.90749690f),
            scientificName = "Dimorphodon sp.", group = "Pterosaurio", features = "Cabeza grande, dentición diferenciada, alas membranosas y cola larga.",
            sourceUrl = "https://www.nhm.ac.uk/discover/mary-anning-fossils.html", modelCredit = "Modelo proporcionado por el usuario; autor y licencia no incluidos en el GLB. Tarjeta renderizada desde su malla.", expectedAnimations = 1,
            presentation = ModelPresentation(-10f, 35f)),
        Dinosaur(DinosaurId.SPINOSAURUS, "Spinosaurus", "Cretácico tardío", "Carnívoro, con consumo de peces",
            "Aproximadamente 14 metros", "Gran terópodo del norte de África. Su hocico y dientes ayudaban a capturar peces. Se investiga cuánto tiempo pasaba nadando o junto a las orillas.", "markers/spinosaurus_marker.png", "models/spinosaurus.glb",
            ModelFraming(0.00007394f, 0.92793309f, -0.29357999f, 0.40718778f, 0.93393171f, 2.15100752f),
            scientificName = "Spinosaurus sp.", group = "Dinosaurio", features = "Hocico largo, dientes cónicos y altas espinas dorsales que sostenían una vela.",
            sourceUrl = "https://www.nhm.ac.uk/discover/dino-directory/spinosaurus.html", modelCredit = "Modelo proporcionado por el usuario; autor y licencia no incluidos en el GLB. Tarjeta renderizada desde su malla.", expectedAnimations = 1,
            presentation = ModelPresentation(-65f, 0f)),
        Dinosaur(DinosaurId.ANKYLOSAURUS, "Ankylosaurus", "Cretácico tardío", "Herbívoro",
            "Hasta unos 8 metros", "Dinosaurio cuadrúpedo acorazado de América del Norte. Su cuerpo estaba protegido por placas óseas y su cola terminaba en una maza.", "markers/ankylosaurus_marker.png", "models/ankylosaurus.glb",
            ModelFraming(0.00000015f, 4.27472347f, -3.12023994f, 4.56390511f, 4.33316694f, 12.24428931f),
            scientificName = "Ankylosaurus sp.", group = "Dinosaurio", features = "Armadura ósea, cuerpo bajo y ancho, y maza caudal.",
            sourceUrl = "https://www.nhm.ac.uk/discover/dino-directory/ankylosaurus.html", modelCredit = "Modelo proporcionado por el usuario; autor y licencia no incluidos en el GLB. Tarjeta renderizada desde su malla.", expectedAnimations = 1,
            presentation = ModelPresentation(-65f, 0f))
    )
    fun get(id: DinosaurId) = dinosaurs.first { it.id == id }
    fun fromName(id: String?) = dinosaurs.firstOrNull { it.id.name == id }
}
