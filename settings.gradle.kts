rootProject.name = "pluto-minecraft"

include("viaversion-api")
include("compat")
include("compat:snakeyaml-compat-common")
findProject(":compat:snakeyaml-compat-common")?.name = "snakeyaml-compat-common"
include("compat:snakeyaml1-compat")
findProject(":compat:snakeyaml1-compat")?.name = "snakeyaml1-compat"
include("compat:snakeyaml2-compat")
findProject(":compat:snakeyaml2-compat")?.name = "snakeyaml2-compat"
include("viarewind")
include("viaversion-bukkit")
include("protocollib")
include("core")
include("core-bukkit")
include("proxy")
include("lobby-core")
include("lobby-core:lobby-main")
findProject(":lobby-core:lobby-main")?.name = "lobby-main"
include("lobby-core:lobby-bedwars")
findProject(":lobby-core:lobby-bedwars")?.name = "lobby-bedwars"
include("lobby-core:lobby-login")
findProject(":lobby-core:lobby-login")?.name = "lobby-login"
