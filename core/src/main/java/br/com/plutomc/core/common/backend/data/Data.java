package br.com.plutomc.core.common.backend.data;

import com.google.gson.JsonElement;
import br.com.plutomc.core.common.backend.Query;

public interface Data<T extends Query<JsonElement>> {
   T getQuery();
}
