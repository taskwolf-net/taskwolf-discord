package net.taskwolf.discord.access;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Key;

@CrossOrigin
@RestController
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GuildController {
  private final Key secretKey;
  private final GuildDatabaseTable guildDatabaseTable;

  @RequestMapping(path = "/discord/guild/add/", method = RequestMethod.GET)
  public void test(
    HttpServletResponse response, @RequestParam("api_key") String apiKey,
    @RequestParam("guild_id") String guildId
  ) {
    if (!validateApiKey(apiKey)) {
      return;
    }
    response.setHeader("Location", "https://google.com");
    response.setStatus(302);
  }

  private boolean validateApiKey(String apiKey) {
    try {
      Jwts.parser()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(apiKey);
      return true;
    } catch (Exception exception) {
      return false;
    }
  }
}
