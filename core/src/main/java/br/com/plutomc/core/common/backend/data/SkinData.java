package br.com.plutomc.core.common.backend.data;

import java.util.Optional;
import java.util.UUID;
import br.com.plutomc.core.common.utils.skin.Skin;

public interface SkinData {
   Optional<Skin> loadData(String var1);

   void save(Skin var1, int var2);

   String[] loadSkinById(UUID var1);
}
