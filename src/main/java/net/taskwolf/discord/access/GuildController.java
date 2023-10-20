package net.taskwolf.discord.access;

import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.access.TaskwolfRestController;
import net.taskwolf.core.user.UserDatabaseTable;
import org.springframework.web.bind.annotation.*;

import java.security.Key;

@CrossOrigin
@RestController
public class GuildController extends TaskwolfRestController {
  private final GuildDatabaseTable guildDatabaseTable;

  private GuildController(
    Key secretKey, UserDatabaseTable userDatabaseTable,
    GuildDatabaseTable guildDatabaseTable
  ) {
    super(secretKey, userDatabaseTable);
    this.guildDatabaseTable = guildDatabaseTable;
  }

  @RequestMapping(path = "/discord/guild/add/", method = RequestMethod.GET)
  public void test(
    HttpServletResponse response, @RequestParam("api_key") String apiKey,
    @RequestParam("guild_id") String guildId
  ) {
    if (!isValidApiKey(apiKey)) {
      return;
    }
    response.setHeader("Location", "https://google.com");
    response.setStatus(302);
  }
}
