package br.com.plutomc.lobby.login.captcha;

import br.com.plutomc.core.common.utils.Callback;
import org.bukkit.entity.Player;

public interface Captcha {
   void verify(Player var1, Callback<Boolean> var2);
}
